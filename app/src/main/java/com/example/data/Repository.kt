package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val groupDao: GroupDao,
    private val memberDao: MemberDao,
    private val sessionDao: SessionDao,
    private val expenseItemDao: ExpenseItemDao,
    private val attendanceDao: AttendanceDao,
    private val paymentDao: PaymentDao,
    private val bulkExpenseDao: BulkExpenseDao,
    private val bankTransactionDao: BankTransactionDao
) {
    // Groups
    val allGroups: Flow<List<Group>> = groupDao.getAllGroups()

    suspend fun getGroupById(groupId: Int): Group? = groupDao.getGroupById(groupId)

    suspend fun insertGroup(group: Group): Long = groupDao.insert(group)

    suspend fun updateGroup(group: Group) = groupDao.update(group)

    suspend fun deleteGroup(group: Group) = groupDao.delete(group)

    // Members
    fun getMembersForGroup(groupId: Int): Flow<List<Member>> = memberDao.getMembersForGroup(groupId)

    suspend fun insertMember(member: Member): Long = memberDao.insert(member)

    suspend fun updateMember(member: Member) = memberDao.update(member)

    suspend fun deleteMember(member: Member) = memberDao.delete(member)

    suspend fun switchCurrentUser(groupId: Int, memberId: Int) {
        memberDao.clearCurrentUserStatus(groupId)
        memberDao.setCurrentUser(memberId)
    }

    // Sessions & Expenses & Attendance
    fun getSessionsForGroup(groupId: Int): Flow<List<Session>> = sessionDao.getSessionsForGroup(groupId)

    suspend fun getSessionById(sessionId: Int): Session? = sessionDao.getSessionById(sessionId)

    fun getExpensesForSession(sessionId: Int): Flow<List<ExpenseItem>> = expenseItemDao.getExpensesForSession(sessionId)

    fun getExpensesForGroup(groupId: Int): Flow<List<ExpenseItem>> = expenseItemDao.getExpensesForGroup(groupId)

    fun getAttendanceForSession(sessionId: Int): Flow<List<Attendance>> = attendanceDao.getAttendanceForSession(sessionId)

    fun getAttendanceForGroup(groupId: Int): Flow<List<Attendance>> = attendanceDao.getAttendanceForGroup(groupId)

    suspend fun insertSessionWithDetails(
        session: Session,
        expenses: List<ExpenseItem>,
        attendanceList: List<Attendance>
    ): Long {
        val totalCost = expenses.sumOf { it.amount }
        val finalSession = session.copy(totalCost = totalCost)
        val sessionId = sessionDao.insert(finalSession).toInt()

        // Insert expenses with reference to sessionId
        val updatedExpenses = expenses.map { it.copy(sessionId = sessionId) }
        expenseItemDao.insertAll(updatedExpenses)

        // Insert attendance with reference to sessionId
        val updatedAttendance = attendanceList.map { it.copy(sessionId = sessionId) }
        attendanceDao.insertAll(updatedAttendance)

        return sessionId.toLong()
    }

    suspend fun updateSessionWithDetails(
        session: Session,
        expenses: List<ExpenseItem>,
        attendanceList: List<Attendance>
    ) {
        val totalCost = expenses.sumOf { it.amount }
        val finalSession = session.copy(totalCost = totalCost)
        sessionDao.update(finalSession)

        // Reset and insert new details
        expenseItemDao.deleteExpensesForSession(session.id)
        val updatedExpenses = expenses.map { it.copy(sessionId = session.id) }
        expenseItemDao.insertAll(updatedExpenses)

        attendanceDao.deleteAttendanceForSession(session.id)
        val updatedAttendance = attendanceList.map { it.copy(sessionId = session.id) }
        attendanceDao.insertAll(updatedAttendance)
    }

    suspend fun deleteSession(session: Session) {
        expenseItemDao.deleteExpensesForSession(session.id)
        attendanceDao.deleteAttendanceForSession(session.id)
        sessionDao.delete(session)
    }

    // Payments
    fun getPaymentsForGroup(groupId: Int): Flow<List<Payment>> = paymentDao.getPaymentsForGroup(groupId)

    suspend fun insertPayment(payment: Payment): Long = paymentDao.insert(payment)

    suspend fun deletePayment(payment: Payment) = paymentDao.delete(payment)

    // Bulk Expenses
    fun getBulkExpensesForGroup(groupId: Int): Flow<List<BulkExpense>> = bulkExpenseDao.getBulkExpensesForGroup(groupId)

    suspend fun insertBulkExpense(bulkExpense: BulkExpense): Long = bulkExpenseDao.insert(bulkExpense)

    suspend fun deleteBulkExpense(bulkExpense: BulkExpense) = bulkExpenseDao.delete(bulkExpense)

    // Bank Transactions
    fun getBankTransactionsForGroup(groupId: Int): Flow<List<BankTransaction>> = bankTransactionDao.getBankTransactionsForGroup(groupId)

    suspend fun insertBankTransaction(transaction: BankTransaction): Long = bankTransactionDao.insert(transaction)

    suspend fun deleteBankTransaction(transaction: BankTransaction) = bankTransactionDao.delete(transaction)
}
