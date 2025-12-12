package com.example.studentattendancesystem

data class User(
    val id: Int = 0,
    val username: String,
    val password: String,
    val userType: String,
    val fullName: String?
)