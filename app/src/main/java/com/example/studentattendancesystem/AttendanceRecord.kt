package com.example.studentattendancesystem

data class AttendanceRecord(
    val id: Int = 0,
    val studentName: String?,
    val studentCode: String?,
    val department: String? = null,
    val year: String? = null,
    val date: String?,
    val status: String?,
    val subject: String?,
    val markedBy: String? = null
)