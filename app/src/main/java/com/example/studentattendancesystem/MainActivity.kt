package com.example.studentattendancesystem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var userTypeText: TextView
    private lateinit var addStudentBtn: Button
    private lateinit var manageStudentsBtn: Button
    private lateinit var markAttendanceBtn: Button
    private lateinit var viewAttendanceBtn: Button
    private lateinit var viewReportsBtn: Button
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get user info from SharedPreferences
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User")
        val userType = prefs.getString("userType", "teacher")

        welcomeText = findViewById(R.id.welcomeText)
        userTypeText = findViewById(R.id.userTypeText)
        addStudentBtn = findViewById(R.id.addStudentBtn)
        manageStudentsBtn = findViewById(R.id.manageStudentsBtn)
        markAttendanceBtn = findViewById(R.id.markAttendanceBtn)
        viewAttendanceBtn = findViewById(R.id.viewAttendanceBtn)
        viewReportsBtn = findViewById(R.id.viewReportsBtn)
        logoutBtn = findViewById(R.id.logoutBtn)

        // Set welcome text
        welcomeText.text = "Welcome, $fullName!"
        userTypeText.text = "Role: ${userType?.uppercase()}"

        // Button click listeners
        addStudentBtn.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        manageStudentsBtn.setOnClickListener {
            startActivity(Intent(this, ManageStudentsActivity::class.java))
        }

        markAttendanceBtn.setOnClickListener {
            startActivity(Intent(this, MarkAttendanceActivity::class.java))
        }

        viewAttendanceBtn.setOnClickListener {
            startActivity(Intent(this, ViewAttendanceActivity::class.java))
        }

        viewReportsBtn.setOnClickListener {
            startActivity(Intent(this, AttendanceReportsActivity::class.java))
        }

        logoutBtn.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear login state
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Navigate to login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Show exit dialog instead of going back
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Do you want to exit the app?")
            .setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}