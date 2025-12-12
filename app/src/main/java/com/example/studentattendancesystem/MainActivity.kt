package com.example.studentattendancesystem

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var userTypeText: TextView
    private lateinit var addStudentBtn: Button
    private lateinit var manageStudentsBtn: Button
    private lateinit var markAttendanceBtn: Button
    private lateinit var viewAttendanceBtn: Button
    private lateinit var viewReportsBtn: Button
    private lateinit var btnScanQR: Button
    private lateinit var btnShowQR: Button
    private lateinit var logoutBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User")
        val userType = prefs.getString("userType", "teacher")
        val username = prefs.getString("username", "") // This is RegNo for Student

        welcomeText = findViewById(R.id.welcomeText)
        userTypeText = findViewById(R.id.userTypeText)
        addStudentBtn = findViewById(R.id.addStudentBtn)
        manageStudentsBtn = findViewById(R.id.manageStudentsBtn)
        markAttendanceBtn = findViewById(R.id.markAttendanceBtn)
        viewAttendanceBtn = findViewById(R.id.viewAttendanceBtn)
        viewReportsBtn = findViewById(R.id.viewReportsBtn)
        btnScanQR = findViewById(R.id.btnScanQR)
        btnShowQR = findViewById(R.id.btnShowQR)
        logoutBtn = findViewById(R.id.logoutBtn)

        welcomeText.text = "Welcome, $fullName!"
        userTypeText.text = "Role: ${userType?.uppercase()}"

        // Handle User Roles
        if (userType == "student") {
            // Student: Hide Admin buttons, Show QR Button
            addStudentBtn.visibility = View.GONE
            manageStudentsBtn.visibility = View.GONE
            markAttendanceBtn.visibility = View.GONE
            viewAttendanceBtn.visibility = View.GONE
            btnScanQR.visibility = View.GONE // Student can't scan

            viewReportsBtn.text = "📊 My Attendance"
            btnShowQR.visibility = View.VISIBLE
        } else {
            // Teacher: Show Scan Button, Hide Student QR Button
            btnScanQR.visibility = View.VISIBLE
            btnShowQR.visibility = View.GONE
        }

        // Listeners
        addStudentBtn.setOnClickListener { startActivity(Intent(this, AddStudentActivity::class.java)) }
        manageStudentsBtn.setOnClickListener { startActivity(Intent(this, ManageStudentsActivity::class.java)) }
        markAttendanceBtn.setOnClickListener { startActivity(Intent(this, MarkAttendanceActivity::class.java)) }
        viewAttendanceBtn.setOnClickListener { startActivity(Intent(this, ViewAttendanceActivity::class.java)) }

        // Teacher: Scan QR
        btnScanQR.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        // Student: Show QR
        btnShowQR.setOnClickListener {
            if (!username.isNullOrEmpty()) {
                showQRCodeDialog(username)
            }
        }

        viewReportsBtn.setOnClickListener {
            val intent = Intent(this, AttendanceReportsActivity::class.java)
            if (userType == "student") {
                intent.putExtra("STUDENT_REG", username)
            }
            startActivity(intent)
        }

        logoutBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    val prefsEdit = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit()
                    prefsEdit.clear().apply()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showQRCodeDialog(regNo: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_qr_code, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.qrImageView)
        val textReg = dialogView.findViewById<TextView>(R.id.qrRegText)

        textReg.text = regNo

        // Generate QR Logic
        try {
            val bitMatrix = MultiFormatWriter().encode(regNo, BarcodeFormat.QR_CODE, 500, 500)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AlertDialog.Builder(this)
            .setTitle("My Student ID")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
}