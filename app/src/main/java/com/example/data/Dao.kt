package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY id DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getGroupById(id: Int): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: Group): Long

    @Query("SELECT * FROM groups")
    suspend fun getAllGroupsSync(): List<Group>

    @Query("DELETE FROM groups")
    suspend fun deleteAllGroups()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(groups: List<Group>)

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE groupId = :groupId ORDER BY name ASC")
    fun getMembersForGroup(groupId: Int): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: Member): Long

    @Query("SELECT * FROM members")
    suspend fun getAllMembers(): List<Member>

    @Query("DELETE FROM members")
    suspend fun deleteAllMembers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<Member>)

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)

    @Query("UPDATE members SET isCurrentUser = 0 WHERE groupId = :groupId")
    suspend fun clearCurrentUserStatus(groupId: Int)

    @Query("UPDATE members SET isCurrentUser = 1 WHERE id = :memberId")
    suspend fun setCurrentUser(memberId: Int)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun getSessionsForGroup(groupId: Int): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Int): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: Session): Long

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<Session>

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<Session>)

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)
}

@Dao
interface ExpenseItemDao {
    @Query("SELECT * FROM expense_items WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getExpensesForSession(sessionId: Int): Flow<List<ExpenseItem>>

    @Query("SELECT * FROM expense_items WHERE sessionId IN (SELECT id FROM sessions WHERE groupId = :groupId)")
    fun getExpensesForGroup(groupId: Int): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expenseItem: ExpenseItem): Long

    @Query("SELECT * FROM expense_items")
    suspend fun getAllExpenseItems(): List<ExpenseItem>

    @Query("DELETE FROM expense_items")
    suspend fun deleteAllExpenseItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ExpenseItem>)

    @Delete
    suspend fun delete(expenseItem: ExpenseItem)

    @Query("DELETE FROM expense_items WHERE sessionId = :sessionId")
    suspend fun deleteExpensesForSession(sessionId: Int)
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE sessionId = :sessionId")
    fun getAttendanceForSession(sessionId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE sessionId IN (SELECT id FROM sessions WHERE groupId = :groupId)")
    fun getAttendanceForGroup(groupId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance): Long

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendance(): List<Attendance>

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<Attendance>)

    @Update
    suspend fun update(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE sessionId = :sessionId")
    suspend fun deleteAttendanceForSession(sessionId: Int)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun getPaymentsForGroup(groupId: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Query("SELECT * FROM payments")
    suspend fun getAllPayments(): List<Payment>

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<Payment>)

    @Delete
    suspend fun delete(payment: Payment)
}

@Dao
interface BulkExpenseDao {
    @Query("SELECT * FROM bulk_expenses WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun getBulkExpensesForGroup(groupId: Int): Flow<List<BulkExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bulkExpense: BulkExpense): Long

    @Query("SELECT * FROM bulk_expenses")
    suspend fun getAllBulkExpenses(): List<BulkExpense>

    @Query("DELETE FROM bulk_expenses")
    suspend fun deleteAllBulkExpenses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bulkExpenses: List<BulkExpense>)

    @Delete
    suspend fun delete(bulkExpense: BulkExpense)
}

@Dao
interface BankTransactionDao {
    @Query("SELECT * FROM bank_transactions WHERE groupId = :groupId ORDER BY dateMillis DESC")
    fun getBankTransactionsForGroup(groupId: Int): Flow<List<BankTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: BankTransaction): Long

    @Query("SELECT * FROM bank_transactions")
    suspend fun getAllBankTransactions(): List<BankTransaction>

    @Query("DELETE FROM bank_transactions")
    suspend fun deleteAllBankTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<BankTransaction>)

    @Delete
    suspend fun delete(transaction: BankTransaction)
}
