package com.example.studentattendancesystem

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.text.SimpleDateFormat
import java.util.*

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

        // User විස්තර ලබාගැනීම
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "User")
        val userType = prefs.getString("userType", "teacher")
        val username = prefs.getString("username", "") // Student නම් මේ RegNo එක

        // UI Components හඳුනාගැනීම
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

        // ==========================================
        // USER ROLE MANAGEMENT (වැදගත්ම කොටස)
        // ==========================================
        if (userType == "student") {
            // --- STUDENT VIEW ---
            // Student ට Admin වැඩ කරන්න බෑ
            addStudentBtn.visibility = View.GONE
            manageStudentsBtn.visibility = View.GONE
            markAttendanceBtn.visibility = View.GONE
            viewAttendanceBtn.visibility = View.GONE

            viewReportsBtn.text = "📊 My Attendance"

            // Student කරන්නේ SCAN කරන එක (Generate නෙවෙයි)
            btnScanQR.visibility = View.VISIBLE
            btnShowQR.visibility = View.GONE
        } else {
            // --- TEACHER / ADMIN VIEW ---
            // Teacher ට ඔක්කොම පේනවා
            addStudentBtn.visibility = View.VISIBLE
            manageStudentsBtn.visibility = View.VISIBLE
            markAttendanceBtn.visibility = View.VISIBLE
            viewAttendanceBtn.visibility = View.VISIBLE
            viewReportsBtn.text = "📊 Attendance Reports"

            // Teacher කරන්නේ QR GENERATE කරන එක (Scan නෙවෙයි)
            btnScanQR.visibility = View.GONE
            btnShowQR.visibility = View.VISIBLE
            btnShowQR.text = "Generate Class QR"
        }

        // ==========================================
        // BUTTON LISTENERS
        // ==========================================

        addStudentBtn.setOnClickListener { startActivity(Intent(this, AddStudentActivity::class.java)) }
        manageStudentsBtn.setOnClickListener { startActivity(Intent(this, ManageStudentsActivity::class.java)) }
        markAttendanceBtn.setOnClickListener { startActivity(Intent(this, MarkAttendanceActivity::class.java)) }
        viewAttendanceBtn.setOnClickListener { startActivity(Intent(this, ViewAttendanceActivity::class.java)) }

        // Student: Teacher ගේ QR එක Scan කරන්න
        btnScanQR.setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }

        // Teacher: අලුත් QR එකක් Generate කරන්න
        btnShowQR.setOnClickListener {
            showQRCodeDialog()
        }

        // Reports Button
        viewReportsBtn.setOnClickListener {
            val intent = Intent(this, AttendanceReportsActivity::class.java)
            if (userType == "student") {
                intent.putExtra("STUDENT_REG", username)
            }
            startActivity(intent)
        }

        // Logout Button
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

    // ==========================================
    // TEACHER QR GENERATION DIALOG
    // ==========================================
    private fun showQRCodeDialog() {
        // Dialog Layout එක සම්බන්ධ කරගැනීම
        val dialogView = layoutInflater.inflate(R.layout.dialog_qr_code, null)
        val spinner = dialogView.findViewById<Spinner>(R.id.qrSubjectSpinner)
        val btnGen = dialogView.findViewById<Button>(R.id.btnGenerateQR)
        val imageView = dialogView.findViewById<ImageView>(R.id.qrImageView)

        // Spinner එකට Subjects ලිස්ට් එක දැමීම
        val subjects = arrayOf("ICT", "Engineering", "Mathematics", "Science", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, subjects)
        spinner.adapter = adapter

        // Dialog එක සෑදීම
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .create()

        // "Generate Now" Button එක එබුවම
        btnGen.setOnClickListener {
            val subject = spinner.selectedItem.toString()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // QR Code එකේ අන්තර්ගතය: Subject_Date
            val qrContent = "${subject}_${date}"

            try {
                // QR Code Image එක සෑදීම (BitMatrix -> Bitmap)
                val bitMatrix = MultiFormatWriter().encode(
                    qrContent, BarcodeFormat.QR_CODE, 500, 500
                )
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                // Image View එකට QR එක දැමීම
                imageView.setImageBitmap(bitmap)
                imageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error generating QR", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}