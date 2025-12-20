package com.example.studentattendancesystem

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.google.firebase.firestore.FirebaseFirestore

class ScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        db = FirebaseFirestore.getInstance()

        // කැමරා අවසරය ඉල්ලීම
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 123)
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        // ලොග් වෙලා ඉන්න Student ගේ විස්තර ගැනීම
        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val studentName = prefs.getString("fullName", "Unknown")
        val studentReg = prefs.getString("username", "Unknown") // Student ගේ Username එක තමයි RegNo එක

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val qrText = it.text // QR එකෙන් එන Text එක (උදා: ICT_2025-12-20)
                val parts = qrText.split("_")

                if (parts.size >= 2) {
                    val subject = parts[0]
                    val date = parts[1]

                    markAttendanceInFirebase(studentName, studentReg, subject, date)
                } else {
                    Toast.makeText(this, "Invalid Class QR!", Toast.LENGTH_SHORT).show()
                    codeScanner.startPreview()
                }
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun markAttendanceInFirebase(name: String?, reg: String?, subject: String, date: String) {
        val attendanceData = hashMapOf(
            "studentName" to name,
            "studentRegNo" to reg,
            "subject" to subject,
            "date" to date,
            "status" to "Present",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("attendance")
            .add(attendanceData)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Attendance Marked for $subject!", Toast.LENGTH_LONG).show()
                finish() // වැඩේ ඉවරයි, එළියට යනවා
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save attendance!", Toast.LENGTH_SHORT).show()
                codeScanner.startPreview()
            }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) codeScanner.startPreview()
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) codeScanner.releaseResources()
        super.onPause()
    }
}