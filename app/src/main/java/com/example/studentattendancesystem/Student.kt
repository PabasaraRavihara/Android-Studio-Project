package com.example.studentattendancesystem

data class Student(
    var id: String = "", // Firebase Document ID
    var studentName: String? = null,
    var studentRegNo: String? = null,
    var department: String? = null,
    var academicYear: String? = null,
    var email: String? = null,
    var phone: String? = null
)