package com.example.studentattendancesystem

data class Student(
    val id: Int = 0,
    val studentCode: String?,
    val name: String?,
    val department: String?,
    val year: String?,
    val email: String? = null,
    val phone: String? = null
)