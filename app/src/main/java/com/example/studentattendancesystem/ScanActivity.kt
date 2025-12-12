package com.example.studentattendancesystem

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import java.text.SimpleDateFormat
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var db: SQLiteHelper
    private lateinit var subjectSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        db = SQLiteHelper(this)
        subjectSpinner = findViewById(R.id.scanSubjectSpinner)

        // Setup Subject Spinner
        val subjects = arrayOf("Mathematics", "Science", "English", "History", "Programming")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter

        // Check Permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            startScanning()
        }
    }

    private fun startScanning() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val regNo = it.text // QR Code text (Student RegNo)
                val subject = subjectSpinner.selectedItem.toString()
                markStudentAttendance(regNo, subject)
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun markStudentAttendance(regNo: String, subject: String) {
        val student = db.getStudentByReg(regNo)

        if (student != null) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Mark as Present
            val result = db.markAttendance(student.id, date, "Present", subject, "Teacher(QR)")

            if (result > 0) {
                Toast.makeText(this, "✅ Success: ${student.name} Marked Present!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error marking attendance", Toast.LENGTH_SHORT).show()
            }

            // Resume scanning after 2 seconds
            android.os.Handler().postDelayed({
                codeScanner.startPreview()
            }, 2000)

        } else {
            Toast.makeText(this, "❌ Invalid QR Code: Student not found", Toast.LENGTH_LONG).show()
            codeScanner.startPreview() // Resume immediately
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            Toast.makeText(this, "Camera permission required!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}