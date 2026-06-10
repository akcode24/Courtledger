package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sportType: String, // "Badminton", "Football", "Tennis", "Squash"
    val currency: String = "$",
    val themeColor: String = "Teal" // "Teal", "Emerald", "Amber", "Indigo", "Crimson"
)

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val name: String,
    val role: String = "Member", // "Admin", "Member"
    val email: String,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val dateMillis: Long,
    val notes: String = "",
    val totalCost: Double = 0.0
)

@Entity(tableName = "expense_items")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val name: String, // e.g., "Court Fee", "Shuttlecocks", "Snacks"
    val amount: Double
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val memberId: Int,
    val isPresent: Boolean,
    val costOverride: Double? = null // Specific amount this user pays, overrides normal sharing
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val memberId: Int,
    val amount: Double,
    val dateMillis: Long,
    val notes: String = ""
)
