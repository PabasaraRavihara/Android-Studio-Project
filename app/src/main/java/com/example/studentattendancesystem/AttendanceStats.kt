package com.example.studentattendancesystem

data class AttendanceStats(
    val total: Int,
    val present: Int,
    val absent: Int,
    val percentage: Double
)