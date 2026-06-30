package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MemberBalance(
    val member: Member,
    val totalCostShare: Double,
    val totalPaid: Double,
    val outstandingBalance: Double
)

data class SessionSplitDetail(
    val session: Session,
    val expenseItems: List<ExpenseItem>,
    val splits: Map<Int, Double>, // Member ID -> Share amount
    val attendees: List<Member>
)

data class GroupDashboardState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val sessionDetails: List<SessionSplitDetail> = emptyList(),
    val currentUser: Member? = null,
    val bulkExpenses: List<BulkExpense> = emptyList(),
    val bankTransactions: List<BankTransaction> = emptyList(),
    val bankBalance: Double = 0.0
)

data class GroupStateInfo(
    val group: Group?,
    val members: List<Member>,
    val sessions: List<Session>,
    val bulkExpenses: List<BulkExpense>
)

class CourtLedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = Repository(
        database.groupDao(),
        database.memberDao(),
        database.sessionDao(),
        database.expenseItemDao(),
        database.attendanceDao(),
        database.paymentDao(),
        database.bulkExpenseDao(),
        database.bankTransactionDao()
    )

    // All available groups
    val allGroups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected group ID
    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

    // Theme Mode Preference persistence
    private val sharedPrefs = application.getSharedPreferences("courtledger_prefs", android.content.Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        sharedPrefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    // Email logs simulation list
    private val _emailLogs = MutableStateFlow<List<String>>(emptyList())
    val emailLogs: StateFlow<List<String>> = _emailLogs.asStateFlow()

    // Combined live dashboard state
    val dashboardState: StateFlow<GroupDashboardState> = _selectedGroupId
        .flatMapLatest { groupId ->
            if (groupId == null) {
                flowOf(GroupDashboardState())
            } else {
                val groupFlow = flow { emit(repository.getGroupById(groupId)) }
                val membersFlow = repository.getMembersForGroup(groupId)
                val sessionsFlow = repository.getSessionsForGroup(groupId)
                val expensesFlow = repository.getExpensesForGroup(groupId)
                val attendanceFlow = repository.getAttendanceForGroup(groupId)
                val paymentsFlow = repository.getPaymentsForGroup(groupId)
                val bulkExpensesFlow = repository.getBulkExpensesForGroup(groupId)
                val bankFlow = repository.getBankTransactionsForGroup(groupId)

                val groupDetailsFlow = combine(
                    groupFlow,
                    membersFlow,
                    sessionsFlow,
                    bulkExpensesFlow
                ) { group, members, sessions, bulk ->
                    GroupStateInfo(group, members, sessions, bulk)
                }

                combine(
                    groupDetailsFlow,
                    expensesFlow,
                    attendanceFlow,
                    paymentsFlow,
                    bankFlow
                ) { details, expenses, attendances, payments, bankTrans ->
                    val group = details.group
                    val members = details.members
                    val sessions = details.sessions
                    val bulkExpenses = details.bulkExpenses

                    if (group == null) return@combine GroupDashboardState()

                    val currentUser = members.find { it.isCurrentUser } ?: members.firstOrNull()

                    // 1. Calculate session-specific details and shares
                    val sessionDetails = sessions.map { session ->
                        val sessionExpenses = expenses.filter { it.sessionId == session.id }
                        val totalSessionCost = sessionExpenses.sumOf { it.amount }
                        val sessionAttendance = attendances.filter { it.sessionId == session.id }
                        val presentAttendance = sessionAttendance.filter { it.isPresent }

                        val splitMap = mutableMapOf<Int, Double>()
                        if (!session.isPaidFromBank && presentAttendance.isNotEmpty()) {
                            val presentWithOverride = presentAttendance.filter { it.costOverride != null }
                            val totalOverrides = presentWithOverride.sumOf { it.costOverride ?: 0.0 }
                            val noOverrideCount = presentAttendance.size - presentWithOverride.size

                            val remainingCost = totalSessionCost - totalOverrides
                            val defaultShare = if (noOverrideCount > 0) {
                                (remainingCost / noOverrideCount).coerceAtLeast(0.0)
                            } else {
                                0.0
                            }

                            for (att in presentAttendance) {
                                val share = att.costOverride ?: defaultShare
                                splitMap[att.memberId] = share
                            }
                        }

                        SessionSplitDetail(
                            session = session,
                            expenseItems = sessionExpenses,
                            splits = splitMap,
                            attendees = members.filter { m -> sessionAttendance.any { it.memberId == m.id && it.isPresent } }
                        )
                    }

                    // Identify regular members (to split bulk expenses)
                    val regularMembers = members.filter { it.role == "Admin" || it.role == "Member" }
                    val regularCount = regularMembers.size.coerceAtLeast(1)

                    // 2. Accumulate member overall balances
                    val primaryAdmin = members.find { it.role == "Admin" } ?: members.find { it.isCurrentUser } ?: members.firstOrNull()
                    val totalNonBankSessionsCost = sessionDetails.sumOf { if (!it.session.isPaidFromBank) it.expenseItems.sumOf { e -> e.amount } else 0.0 }
                    val totalOtherPayments = if (primaryAdmin != null) {
                        payments.filter { it.memberId != primaryAdmin.id }.sumOf { it.amount }
                    } else {
                        0.0
                    }

                    val balances = members.map { member ->
                        // Calculate total split cost accrued from sessions (excluding bank paid sessions!)
                        var costShare = 0.0
                        for (sDetail in sessionDetails) {
                            if (!sDetail.session.isPaidFromBank) {
                                costShare += sDetail.splits[member.id] ?: 0.0
                            }
                        }

                        // Add block/bulk expenses if member is a regular user (Admin or Member, and not group-funded)
                        val isRegular = member.role == "Admin" || member.role == "Member"
                        if (isRegular) {
                            for (be in bulkExpenses) {
                                if (!be.isPaidFromBank) {
                                    costShare += be.amount / regularCount
                                }
                            }
                        }

                        // Calculate total payments made (including any bulk purchases they bought/funded!)
                        var paid = payments.filter { it.memberId == member.id }.sumOf { it.amount }
                        // Only count bulk purchases paid by member if not funded directly from bank resource. Ignore negative bulk expenses (reductions).
                        val paidBulk = bulkExpenses.filter { it.paidByMemberId == member.id && !it.isPaidFromBank && it.amount > 0.0 }.sumOf { it.amount }
                        paid += paidBulk

                        if (primaryAdmin != null && member.id == primaryAdmin.id) {
                            // Admin is credited with paying all non-bank session costs, and debited with payments made by others to them
                            paid += totalNonBankSessionsCost
                            paid -= totalOtherPayments
                        }

                        MemberBalance(
                            member = member,
                            totalCostShare = costShare,
                            totalPaid = paid,
                            outstandingBalance = costShare - paid
                        )
                    }

                    val calculatedBankBalance = bankTrans.sumOf { it.amount }

                    GroupDashboardState(
                        group = group,
                        members = members,
                        sessions = sessions,
                        payments = payments,
                        balances = balances,
                        sessionDetails = sessionDetails,
                        currentUser = currentUser,
                        bulkExpenses = bulkExpenses,
                        bankTransactions = bankTrans,
                        bankBalance = calculatedBankBalance
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupDashboardState())

    init {
        // Pre-populate if database is empty
        viewModelScope.launch {
            repository.allGroups.first().let { currentGroups ->
                if (currentGroups.isEmpty()) {
                    setupDemoData()
                } else {
                    _selectedGroupId.value = currentGroups.first().id
                }
            }
        }
    }

    private suspend fun setupDemoData() {
        // Group 1: Wednesday Badminton Club
        val g1Id = repository.insertGroup(
            Group(name = "Wednesday Badminton Club", sportType = "Badminton", currency = "$", themeColor = "Teal")
        ).toInt()

        val alice = Member(groupId = g1Id, name = "Alice Tan", role = "Admin", email = "alice@courtledger.com", isCurrentUser = true)
        val bob = Member(groupId = g1Id, name = "Bob Lim", role = "Member", email = "bob@example.com")
        val charlie = Member(groupId = g1Id, name = "Charlie Ng", role = "Member", email = "charlie@example.com")
        val diana = Member(groupId = g1Id, name = "Diana Wong", role = "Member", email = "diana@example.com")
        val ethan = Member(groupId = g1Id, name = "Ethan Guest", role = "One-time Player", email = "ethan@example.com") // Guest player

        val aliceId = repository.insertMember(alice).toInt()
        val bobId = repository.insertMember(bob).toInt()
        val charlieId = repository.insertMember(charlie).toInt()
        val dianaId = repository.insertMember(diana).toInt()
        val ethanId = repository.insertMember(ethan).toInt()

        // Insert Bulk Expense for Group 1: $120.00 split among 4 regular members (Alice, Bob, Charlie, Diana) only (excluding Ethan)
        repository.insertBulkExpense(
            BulkExpense(
                groupId = g1Id,
                title = "10x Aerosonic Shuttlecock Tubes",
                amount = 120.0,
                paidByMemberId = aliceId,
                dateMillis = System.currentTimeMillis() - 5 * 24 * 3600 * 1000L,
                notes = "Bulk bought for regular members"
            )
        )

        // Session 1: Early June Court Playing
        // Court rentals: $40, shuttlecocks: $16. Total = $56
        val s1 = Session(groupId = g1Id, dateMillis = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L, notes = "Competitive play 4-6 PM")
        repository.insertSessionWithDetails(
            session = s1,
            expenses = listOf(
                ExpenseItem(sessionId = 0, name = "Court Hire (Court 3)", amount = 40.0),
                ExpenseItem(sessionId = 0, name = "Aeroplane Shuttlecocks", amount = 16.0)
            ),
            attendanceList = listOf(
                Attendance(sessionId = 0, memberId = aliceId, isPresent = true),
                Attendance(sessionId = 0, memberId = bobId, isPresent = true),
                Attendance(sessionId = 0, memberId = charlieId, isPresent = true),
                Attendance(sessionId = 0, memberId = dianaId, isPresent = false),
                Attendance(sessionId = 0, memberId = ethanId, isPresent = false)
            )
        )

        // Session 2: Mid June Court Playing
        // Court rentals: $30, snacks: $15. Total = $45
        // Charlie Ng has an override of paying exactly $10 for snacks/courts since he only joined half session!
        val s2 = Session(groupId = g1Id, dateMillis = System.currentTimeMillis() - 3 * 24 * 3600 * 1000L, notes = "Friendly play 2-4 PM")
        repository.insertSessionWithDetails(
            session = s2,
            expenses = listOf(
                ExpenseItem(sessionId = 0, name = "Court Hire (Court 1)", amount = 30.0),
                ExpenseItem(sessionId = 0, name = "Isotonic Drinks", amount = 15.0)
            ),
            attendanceList = listOf(
                Attendance(sessionId = 0, memberId = aliceId, isPresent = true),
                Attendance(sessionId = 0, memberId = bobId, isPresent = true),
                Attendance(sessionId = 0, memberId = charlieId, isPresent = true, costOverride = 10.0),
                Attendance(sessionId = 0, memberId = dianaId, isPresent = true),
                Attendance(sessionId = 0, memberId = ethanId, isPresent = true) // Ethan attended this session
            )
        )

        // Pre-recorded payment
        repository.insertPayment(
            Payment(groupId = g1Id, memberId = bobId, amount = 15.0, dateMillis = System.currentTimeMillis() - 1 * 24 * 3600 * 1000L, notes = "Hand-delivered cash")
        )

        // Group 2: Sunday Soccer Yard
        val g2Id = repository.insertGroup(
            Group(name = "Sunday Soccer Yard", sportType = "Football", currency = "£", themeColor = "Emerald")
        ).toInt()

        val dave = Member(groupId = g2Id, name = "Dave Admin", role = "Admin", email = "dave@example.com", isCurrentUser = true)
        val eva = Member(groupId = g2Id, name = "Eva Green", role = "Member", email = "eva@example.com")
        val frank = Member(groupId = g2Id, name = "Frank Lampard", role = "Member", email = "frank@example.com")

        val daveId = repository.insertMember(dave).toInt()
        val evaId = repository.insertMember(eva).toInt()
        val frankId = repository.insertMember(frank).toInt()

        val sFootball = Session(groupId = g2Id, dateMillis = System.currentTimeMillis() - 2 * 24 * 3600 * 1000L, notes = "5-a-side match")
        repository.insertSessionWithDetails(
            session = sFootball,
            expenses = listOf(
                ExpenseItem(sessionId = 0, name = "Pitch rental", amount = 60.0),
                ExpenseItem(sessionId = 0, name = "Match Ball", amount = 15.0)
            ),
            attendanceList = listOf(
                Attendance(sessionId = 0, memberId = daveId, isPresent = true),
                Attendance(sessionId = 0, memberId = evaId, isPresent = true),
                Attendance(sessionId = 0, memberId = frankId, isPresent = true)
            )
        )

        // Set Default selection
        _selectedGroupId.value = g1Id
    }

    fun selectGroup(groupId: Int) {
        _selectedGroupId.value = groupId
    }

    fun addGroup(name: String, sportType: String, currency: String, themeColor: String) {
        viewModelScope.launch {
            val grId = repository.insertGroup(
                Group(name = name, sportType = sportType, currency = currency, themeColor = themeColor)
            ).toInt()

            // Automatically create the admin creating this group as first member
            val creator = Member(
                groupId = grId,
                name = "Group Admin",
                role = "Admin",
                email = "admin@example.com",
                isCurrentUser = true
            )
            repository.insertMember(creator)
            _selectedGroupId.value = grId
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            repository.insertGroup(group)
        }
    }

    fun addMember(name: String, role: String, email: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertMember(
                Member(groupId = groupId, name = name, role = role, email = email)
            )
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun switchUser(memberId: Int) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.switchCurrentUser(groupId, memberId)
        }
    }

    fun addSession(
        notes: String,
        dateMillis: Long,
        expenses: List<ExpenseItem>,
        attendanceList: List<Attendance>,
        isPaidFromBank: Boolean = false
    ) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            val totalCost = expenses.sumOf { it.amount }
            val sessionId = repository.insertSessionWithDetails(
                Session(groupId = groupId, dateMillis = dateMillis, notes = notes, totalCost = 0.0, isPaidFromBank = isPaidFromBank),
                expenses,
                attendanceList
            )
            if (isPaidFromBank && totalCost > 0.0) {
                repository.insertBankTransaction(
                    BankTransaction(
                        groupId = groupId,
                        dateMillis = dateMillis,
                        description = "Session Fee: $notes",
                        amount = -totalCost
                    )
                )
            }
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
            if (session.isPaidFromBank) {
                val groupTransactions = repository.getBankTransactionsForGroup(session.groupId).firstOrNull() ?: emptyList()
                val targetStr = "Session Fee: ${session.notes}"
                val matched = groupTransactions.find {
                    it.amount < 0.0 && (it.description == targetStr || Math.abs(it.amount - (-session.totalCost)) < 0.01)
                }
                if (matched != null) {
                    repository.deleteBankTransaction(matched)
                }
            }
        }
    }

    fun addPayment(memberId: Int, amount: Double, notes: String) {
        addGuestPaymentWithReduction(memberId, amount, notes, isDeduction = false)
    }

    fun addGuestPaymentWithReduction(memberId: Int, amount: Double, notes: String, isDeduction: Boolean) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            // Check current outstanding balance to see if they are overpaying
            val currentState = dashboardState.value
            val bal = currentState.balances.find { it.member.id == memberId }
            val currentOwed = bal?.outstandingBalance ?: 0.0
            val memberName = bal?.member?.name ?: "Guest Player"

            repository.insertPayment(
                Payment(
                    groupId = groupId,
                    memberId = memberId,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    notes = notes + if (isDeduction) " (Minus Bulk Cost applied)" else ""
                )
            )

            if (isDeduction) {
                 repository.insertBulkExpense(
                     BulkExpense(
                         groupId = groupId,
                         title = "Guest reduction ($memberName)",
                         amount = -amount,
                         paidByMemberId = memberId,
                         dateMillis = System.currentTimeMillis(),
                         notes = "Minus Bulk reduction funded by guest contribution"
                     )
                 )
            } else if (amount > currentOwed) {
                val overpaidExcess = amount - currentOwed.coerceAtLeast(0.0)
                if (overpaidExcess > 0.0) {
                    repository.insertBankTransaction(
                        BankTransaction(
                            groupId = groupId,
                            dateMillis = System.currentTimeMillis(),
                            description = "Overpayment by $memberName",
                            amount = overpaidExcess,
                            memberId = memberId,
                            isSystemOverpayment = true
                        )
                    )
                }
            }
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            // Reverse latest system overpayment if one exists around the same time
            val groupTransactions = repository.getBankTransactionsForGroup(payment.groupId).firstOrNull() ?: emptyList()
            val matched = groupTransactions.find {
                it.isSystemOverpayment && it.memberId == payment.memberId && Math.abs(it.dateMillis - System.currentTimeMillis()) < 60000L
            } ?: groupTransactions.find {
                it.isSystemOverpayment && it.memberId == payment.memberId
            }
            if (matched != null) {
                repository.deleteBankTransaction(matched)
            }
        }
    }

    fun addBulkExpense(title: String, amount: Double, paidByMemberId: Int, isPaidFromBank: Boolean = false) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertBulkExpense(
                BulkExpense(
                    groupId = groupId,
                    title = title,
                    amount = amount,
                    paidByMemberId = paidByMemberId,
                    dateMillis = System.currentTimeMillis(),
                    isPaidFromBank = isPaidFromBank
                )
            )
            if (isPaidFromBank && amount > 0.0) {
                repository.insertBankTransaction(
                    BankTransaction(
                        groupId = groupId,
                        dateMillis = System.currentTimeMillis(),
                        description = "Bulk Expense: $title",
                        amount = -amount
                    )
                )
            }
        }
    }

    fun deleteBulkExpense(bulkExpense: BulkExpense) {
        viewModelScope.launch {
            repository.deleteBulkExpense(bulkExpense)
            if (bulkExpense.isPaidFromBank) {
                val groupTransactions = repository.getBankTransactionsForGroup(bulkExpense.groupId).firstOrNull() ?: emptyList()
                val targetStr = "Bulk Expense: ${bulkExpense.title}"
                val matched = groupTransactions.find {
                    it.amount < 0.0 && (it.description == targetStr || Math.abs(it.amount - (-bulkExpense.amount)) < 0.01)
                }
                if (matched != null) {
                    repository.deleteBankTransaction(matched)
                }
            }
        }
    }

    fun addManualBankTransaction(description: String, amount: Double) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertBankTransaction(
                BankTransaction(
                    groupId = groupId,
                    dateMillis = System.currentTimeMillis(),
                    description = description,
                    amount = amount
                )
            )
        }
    }

    fun updateBulkExpense(bulkExpense: BulkExpense) {
        viewModelScope.launch {
            repository.insertBulkExpense(bulkExpense)
        }
    }

    // High fidelity email notification simulator
    fun simulateWeeklyEmailSummary() {
        val state = dashboardState.value
        val groupName = state.group?.name ?: "Group"
        val currency = state.group?.currency ?: "$"

        val summaryContent = buildString {
            appendLine("===== [CourtLedger Email Server] =====")
            appendLine("Subject: Weekly Session Summary - $groupName")
            appendLine("To: All Group Members")
            appendLine("Sent on: ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date())}")
            appendLine("--------------------------------------------------")
            appendLine("Here is this week's ledger snapshot:")
            appendLine()
            appendLine("Active Sessions:")
            state.sessions.take(3).forEach { session ->
                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(session.dateMillis))
                appendLine("- Session ($dateStr): Cost $currency${String.format("%.2f", session.totalCost)} (${session.notes})")
            }
            if (state.sessions.size > 3) {
                appendLine("- And ${state.sessions.size - 3} more dynamic sessions...")
            }
            appendLine()
            appendLine("Current Participant Balances:")
            state.balances.forEach { balance ->
                val status = if (balance.outstandingBalance >= 0) {
                    "owes $currency${String.format("%.2f", balance.outstandingBalance)}"
                } else {
                    "is owed $currency${String.format("%.2f", -balance.outstandingBalance)}"
                }
                appendLine("- ${balance.member.name} (${balance.member.email}): $status")
            }
            appendLine()
            appendLine("Regards,")
            appendLine("CourtLedger Automatic Split engine")
            appendLine("======================================")
        }

        _emailLogs.update { current ->
            listOf(summaryContent) + current
        }
    }

    fun clearEmailLogs() {
        _emailLogs.value = emptyList()
    }

    private fun escapeCsv(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current = java.lang.StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    suspend fun exportAllToCsvString(): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val sb = java.lang.StringBuilder()
            sb.appendLine("# CourtLedger Full Backup CSV (Universally compatible format)")
            sb.appendLine("# Generated on: " + java.util.Date().toString())
            sb.appendLine("# Format: Prefix,Fields...")

            // Groups
            val groups = database.groupDao().getAllGroupsSync()
            groups.forEach { g ->
                sb.appendLine("G,${g.id},${escapeCsv(g.name)},${escapeCsv(g.sportType)},${escapeCsv(g.currency)},${escapeCsv(g.themeColor)}")
            }

            // Members
            val members = database.memberDao().getAllMembers()
            members.forEach { m ->
                sb.appendLine("M,${m.id},${m.groupId},${escapeCsv(m.name)},${escapeCsv(m.role)},${escapeCsv(m.email)},${m.isCurrentUser}")
            }

            // Sessions
            val sessions = database.sessionDao().getAllSessions()
            sessions.forEach { s ->
                sb.appendLine("S,${s.id},${s.groupId},${s.dateMillis},${escapeCsv(s.notes)},${s.totalCost}")
            }

            // ExpenseItems
            val expenses = database.expenseItemDao().getAllExpenseItems()
            expenses.forEach { e ->
                sb.appendLine("E,${e.id},${e.sessionId},${escapeCsv(e.name)},${e.amount}")
            }

            // Attendance
            val attendances = database.attendanceDao().getAllAttendance()
            attendances.forEach { a ->
                sb.appendLine("A,${a.id},${a.sessionId},${a.memberId},${a.isPresent},${a.costOverride ?: ""}")
            }

            // Payments
            val payments = database.paymentDao().getAllPayments()
            payments.forEach { p ->
                sb.appendLine("P,${p.id},${p.groupId},${p.memberId},${p.amount},${p.dateMillis},${escapeCsv(p.notes)}")
            }

            // BulkExpenses
            val bulkExpenses = database.bulkExpenseDao().getAllBulkExpenses()
            bulkExpenses.forEach { b ->
                sb.appendLine("B,${b.id},${b.groupId},${escapeCsv(b.title)},${b.amount},${b.paidByMemberId},${b.dateMillis},${escapeCsv(b.notes)}")
            }

            // BankTransactions
            val bankTransactions = database.bankTransactionDao().getAllBankTransactions()
            bankTransactions.forEach { bt ->
                sb.appendLine("BT,${bt.id},${bt.groupId},${bt.dateMillis},${escapeCsv(bt.description)},${bt.amount},${bt.memberId ?: ""},${bt.isSystemOverpayment}")
            }

            sb.toString()
        }
    }

    suspend fun importAllFromCsvString(csvContent: String): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val lines = csvContent.lineSequence()

                val groupsToInsert = mutableListOf<Group>()
                val membersToInsert = mutableListOf<Member>()
                val sessionsToInsert = mutableListOf<Session>()
                val expensesToInsert = mutableListOf<ExpenseItem>()
                val attendancesToInsert = mutableListOf<Attendance>()
                val paymentsToInsert = mutableListOf<Payment>()
                val bulkExpensesToInsert = mutableListOf<BulkExpense>()
                val bankTransactionsToInsert = mutableListOf<BankTransaction>()

                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
                    val parts = parseCsvLine(trimmed)
                    if (parts.isEmpty()) continue

                    val prefix = parts[0]
                    when (prefix) {
                        "G" -> {
                            if (parts.size >= 6) {
                                groupsToInsert.add(
                                    Group(
                                        id = parts[1].toInt(),
                                        name = parts[2],
                                        sportType = parts[3],
                                        currency = parts[4],
                                        themeColor = parts[5]
                                    )
                                )
                            }
                        }
                        "M" -> {
                            if (parts.size >= 7) {
                                membersToInsert.add(
                                    Member(
                                        id = parts[1].toInt(),
                                        groupId = parts[2].toInt(),
                                        name = parts[3],
                                        role = parts[4],
                                        email = parts[5],
                                        isCurrentUser = parts[6].toBoolean()
                                    )
                                )
                            }
                        }
                        "S" -> {
                            if (parts.size >= 6) {
                                sessionsToInsert.add(
                                    Session(
                                        id = parts[1].toInt(),
                                        groupId = parts[2].toInt(),
                                        dateMillis = parts[3].toLong(),
                                        notes = parts[4],
                                        totalCost = parts[5].toDouble()
                                    )
                                )
                            }
                        }
                        "E" -> {
                            if (parts.size >= 5) {
                                expensesToInsert.add(
                                    ExpenseItem(
                                        id = parts[1].toInt(),
                                        sessionId = parts[2].toInt(),
                                        name = parts[3],
                                        amount = parts[4].toDouble()
                                    )
                                )
                            }
                        }
                        "A" -> {
                            if (parts.size >= 5) {
                                val costOverrideStr = parts.getOrNull(5)
                                val costOverride = if (costOverrideStr.isNullOrEmpty()) null else costOverrideStr.toDoubleOrNull()
                                attendancesToInsert.add(
                                    Attendance(
                                        id = parts[1].toInt(),
                                        sessionId = parts[2].toInt(),
                                        memberId = parts[3].toInt(),
                                        isPresent = parts[4].toBoolean(),
                                        costOverride = costOverride
                                    )
                                )
                            }
                        }
                        "P" -> {
                            if (parts.size >= 7) {
                                paymentsToInsert.add(
                                    Payment(
                                        id = parts[1].toInt(),
                                        groupId = parts[2].toInt(),
                                        memberId = parts[3].toInt(),
                                        amount = parts[4].toDouble(),
                                        dateMillis = parts[5].toLong(),
                                        notes = parts[6]
                                    )
                                )
                            }
                        }
                        "B" -> {
                            if (parts.size >= 8) {
                                bulkExpensesToInsert.add(
                                    BulkExpense(
                                        id = parts[1].toInt(),
                                        groupId = parts[2].toInt(),
                                        title = parts[3],
                                        amount = parts[4].toDouble(),
                                        paidByMemberId = parts[5].toInt(),
                                        dateMillis = parts[6].toLong(),
                                        notes = parts[7]
                                    )
                                )
                            }
                        }
                        "BT" -> {
                            if (parts.size >= 6) {
                                val memIdStr = parts.getOrNull(6) ?: ""
                                val memId = if (memIdStr.isEmpty()) null else memIdStr.toIntOrNull()
                                val isOver = parts.getOrNull(7)?.toBoolean() ?: false
                                bankTransactionsToInsert.add(
                                    BankTransaction(
                                        id = parts[1].toInt(),
                                        groupId = parts[2].toInt(),
                                        dateMillis = parts[3].toLong(),
                                        description = parts[4],
                                        amount = parts[5].toDouble(),
                                        memberId = memId,
                                        isSystemOverpayment = isOver
                                    )
                                )
                            }
                        }
                    }
                }

                database.withTransaction {
                    // We must delete from children/foreign key constrained tables first to avoid SQLite constraints!
                    database.expenseItemDao().deleteAllExpenseItems()
                    database.attendanceDao().deleteAllAttendance()
                    database.paymentDao().deleteAllPayments()
                    database.bulkExpenseDao().deleteAllBulkExpenses()
                    database.bankTransactionDao().deleteAllBankTransactions()
                    database.sessionDao().deleteAllSessions()
                    database.memberDao().deleteAllMembers()
                    database.groupDao().deleteAllGroups()

                    // Insert parent records first, then child records!
                    database.groupDao().insertAll(groupsToInsert)
                    database.memberDao().insertAll(membersToInsert)
                    database.sessionDao().insertAll(sessionsToInsert)
                    database.expenseItemDao().insertAll(expensesToInsert)
                    database.attendanceDao().insertAll(attendancesToInsert)
                    database.paymentDao().insertAll(paymentsToInsert)
                    database.bulkExpenseDao().insertAll(bulkExpensesToInsert)
                    database.bankTransactionDao().insertAll(bankTransactionsToInsert)
                }

                val firstGroup = groupsToInsert.firstOrNull()
                if (firstGroup != null) {
                    _selectedGroupId.value = firstGroup.id
                } else {
                    _selectedGroupId.value = null
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun deleteGroupCascaded(groupId: Int): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                database.withTransaction {
                    // 1. Find sessions for group
                    val sessions = database.sessionDao().getAllSessions().filter { it.groupId == groupId }
                    sessions.forEach { session ->
                        // 2. Delete attendance for each session
                        database.attendanceDao().deleteAttendanceForSession(session.id)
                        // 3. Delete expense items for each session
                        database.expenseItemDao().deleteExpensesForSession(session.id)
                        // 4. Delete the session
                        database.sessionDao().delete(session)
                    }

                    // 5. Delete bulk expenses for group
                    val bulkExpenses = database.bulkExpenseDao().getAllBulkExpenses().filter { it.groupId == groupId }
                    bulkExpenses.forEach { database.bulkExpenseDao().delete(it) }

                    // 6. Delete payments for group
                    val payments = database.paymentDao().getAllPayments().filter { it.groupId == groupId }
                    payments.forEach { database.paymentDao().delete(it) }

                    // 7. Delete members for group
                    val members = database.memberDao().getAllMembers().filter { it.groupId == groupId }
                    members.forEach { database.memberDao().delete(it) }

                    // 8. Delete group itself
                    val group = database.groupDao().getGroupById(groupId)
                    if (group != null) {
                        database.groupDao().delete(group)
                    }
                }

                // Pick another group if available
                val allGroups = database.groupDao().getAllGroupsSync()
                val nextGroup = allGroups.firstOrNull()
                _selectedGroupId.value = nextGroup?.id

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
