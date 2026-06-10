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

    @Delete
    suspend fun delete(payment: Payment)
}
