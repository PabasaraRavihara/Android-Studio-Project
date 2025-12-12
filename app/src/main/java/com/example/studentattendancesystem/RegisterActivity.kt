package com.example.studentattendancesystem

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var fullNameInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var userTypeGroup: RadioGroup
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = SQLiteHelper(this)

        fullNameInput = findViewById(R.id.fullNameInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        userTypeGroup = findViewById(R.id.userTypeGroup)
        registerBtn = findViewById(R.id.registerBtn)

        registerBtn.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val fullName = fullNameInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        // Validation
        if (fullName.isEmpty()) {
            fullNameInput.error = "Full name is required"
            fullNameInput.requestFocus()
            return
        }

        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            usernameInput.requestFocus()
            return
        }

        if (username.length < 4) {
            usernameInput.error = "Username must be at least 4 characters"
            usernameInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            passwordInput.requestFocus()
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            confirmPasswordInput.requestFocus()
            return
        }

        // Check if username already exists
        if (db.usernameExists(username)) {
            usernameInput.error = "Username already exists"
            usernameInput.requestFocus()
            return
        }

        // Get selected user type
        val selectedId = userTypeGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadio = findViewById<RadioButton>(selectedId)
        val userType = when (selectedRadio.id) {
            R.id.radioAdmin -> "admin"
            R.id.radioTeacher -> "teacher"
            R.id.radioStudent -> "student" // Handle student logic
            else -> "teacher"
        }

        // SECURITY CHECK: If student, Username MUST be their Reg No
        if (userType == "student") {
            if (!db.studentRegExists(username)) {
                usernameInput.error = "Reg No not found in system!"
                Toast.makeText(this, "Student Registration Failed: Your Reg No was not found. Ask your teacher to add you first.", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Insert user
        val result = db.addUser(username, password, userType, fullName)

        if (result > 0) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}