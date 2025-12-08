package com.example.studentattendancesystem


data class AttendanceRecord(
    val id: Int = 0,
    val studentName: String? = null,
    val studentCode: String? = null,
    val date: String? = null,
    val status: String? = null,
    val subject: String? = null
)
