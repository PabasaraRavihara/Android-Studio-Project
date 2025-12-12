package com.example.studentattendancesystem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var registerLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user already logged in
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_login)

        db = SQLiteHelper(this)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginBtn = findViewById(R.id.loginBtn)
        registerLink = findViewById(R.id.registerLink)

        loginBtn.setOnClickListener {
            performLogin()
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Validation
        if (username.isEmpty()) {
            usernameInput.error = "Username is required"
            usernameInput.requestFocus()
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            passwordInput.requestFocus()
            return
        }

        // Check credentials
        val user = db.loginUser(username, password)

        if (user != null) {
            // Save login state
            val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("isLoggedIn", true)
                putInt("userId", user.id)
                putString("username", user.username)
                putString("userType", user.userType)
                putString("fullName", user.fullName)
                apply()
            }

            Toast.makeText(this, "Welcome ${user.fullName}!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            passwordInput.text.clear()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back
        finishAffinity()
    }
}