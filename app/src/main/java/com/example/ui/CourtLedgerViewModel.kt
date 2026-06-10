package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val currentUser: Member? = null
)

class CourtLedgerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = Repository(
        database.groupDao(),
        database.memberDao(),
        database.sessionDao(),
        database.expenseItemDao(),
        database.attendanceDao(),
        database.paymentDao()
    )

    // All available groups
    val allGroups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently selected group ID
    private val _selectedGroupId = MutableStateFlow<Int?>(null)
    val selectedGroupId: StateFlow<Int?> = _selectedGroupId.asStateFlow()

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

                val groupDetailsFlow = combine(
                    groupFlow,
                    membersFlow,
                    sessionsFlow
                ) { group, members, sessions ->
                    Triple(group, members, sessions)
                }

                combine(
                    groupDetailsFlow,
                    expensesFlow,
                    attendanceFlow,
                    paymentsFlow
                ) { details, expenses, attendances, payments ->
                    val group = details.first
                    val members = details.second
                    val sessions = details.third

                    if (group == null) return@combine GroupDashboardState()

                    val currentUser = members.find { it.isCurrentUser } ?: members.firstOrNull()

                    // 1. Calculate session-specific details and shares
                    val sessionDetails = sessions.map { session ->
                        val sessionExpenses = expenses.filter { it.sessionId == session.id }
                        val totalSessionCost = sessionExpenses.sumOf { it.amount }
                        val sessionAttendance = attendances.filter { it.sessionId == session.id }
                        val presentAttendance = sessionAttendance.filter { it.isPresent }

                        val splitMap = mutableMapOf<Int, Double>()
                        if (presentAttendance.isNotEmpty()) {
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

                    // 2. Accumulate member overall balances
                    val balances = members.map { member ->
                        // Calculate total split cost accrued from sessions
                        var costShare = 0.0
                        for (sDetail in sessionDetails) {
                            costShare += sDetail.splits[member.id] ?: 0.0
                        }

                        // Calculate total payments made
                        val paid = payments.filter { it.memberId == member.id }.sumOf { it.amount }

                        MemberBalance(
                            member = member,
                            totalCostShare = costShare,
                            totalPaid = paid,
                            outstandingBalance = costShare - paid
                        )
                    }

                    GroupDashboardState(
                        group = group,
                        members = members,
                        sessions = sessions,
                        payments = payments,
                        balances = balances,
                        sessionDetails = sessionDetails,
                        currentUser = currentUser
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

        val aliceId = repository.insertMember(alice).toInt()
        val bobId = repository.insertMember(bob).toInt()
        val charlieId = repository.insertMember(charlie).toInt()
        val dianaId = repository.insertMember(diana).toInt()

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
                Attendance(sessionId = 0, memberId = dianaId, isPresent = false)
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
                Attendance(sessionId = 0, memberId = dianaId, isPresent = true)
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

    fun addMember(name: String, role: String, email: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertMember(
                Member(groupId = groupId, name = name, role = role, email = email)
            )
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
        attendanceList: List<Attendance>
    ) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertSessionWithDetails(
                Session(groupId = groupId, dateMillis = dateMillis, notes = notes, totalCost = 0.0),
                expenses,
                attendanceList
            )
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun addPayment(memberId: Int, amount: Double, notes: String) {
        val groupId = _selectedGroupId.value ?: return
        viewModelScope.launch {
            repository.insertPayment(
                Payment(groupId = groupId, memberId = memberId, amount = amount, dateMillis = System.currentTimeMillis(), notes = notes)
            )
        }
    }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch {
            repository.deletePayment(payment)
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
}
