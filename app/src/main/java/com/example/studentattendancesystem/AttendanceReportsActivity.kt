package com.example.studentattendancesystem

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AttendanceReportsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var studentSpinner: Spinner
    private lateinit var generateBtn: Button
    private lateinit var btnSavePdf: Button
    private lateinit var reportLayout: CardView
    private lateinit var studentNameTxt: TextView
    private lateinit var regNoTxt: TextView
    private lateinit var deptYearTxt: TextView
    private lateinit var totalClassesTxt: TextView
    private lateinit var presentTxt: TextView
    private lateinit var absentTxt: TextView
    private lateinit var percentageTxt: TextView
    private lateinit var statusTxt: TextView
    private lateinit var lblSelectStudent: TextView

    private var students = ArrayList<Student>()
    private var currentStudent: Student? = null

    // Helper class to store calculated stats
    data class AttendanceStats(val total: Int, val present: Int, val absent: Int, val percentage: Double)
    private var currentStats: AttendanceStats? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_reports)

        db = FirebaseFirestore.getInstance()

        studentSpinner = findViewById(R.id.studentSpinner)
        generateBtn = findViewById(R.id.generateReportBtn)
        btnSavePdf = findViewById(R.id.btnSavePdf)
        reportLayout = findViewById(R.id.reportLayout)
        studentNameTxt = findViewById(R.id.studentNameTxt)
        regNoTxt = findViewById(R.id.regNoTxt)
        deptYearTxt = findViewById(R.id.deptYearTxt)
        totalClassesTxt = findViewById(R.id.totalClassesTxt)
        presentTxt = findViewById(R.id.presentTxt)
        absentTxt = findViewById(R.id.absentTxt)
        percentageTxt = findViewById(R.id.percentageTxt)
        statusTxt = findViewById(R.id.statusTxt)
        lblSelectStudent = findViewById(R.id.lblSelectStudent)

        // Check if student logged in (passed from MainActivity)
        val specificStudentReg = intent.getStringExtra("STUDENT_REG")

        if (specificStudentReg != null) {
            // STUDENT MODE: Auto-load based on RegNo
            studentSpinner.visibility = View.GONE
            generateBtn.visibility = View.GONE
            lblSelectStudent.visibility = View.GONE
            loadStudentByRegNo(specificStudentReg)
        } else {
            // TEACHER MODE: Load list
            loadStudentsList()

            generateBtn.setOnClickListener {
                val selectedIndex = studentSpinner.selectedItemPosition
                if (selectedIndex > 0 && students.isNotEmpty()) {
                    val student = students[selectedIndex - 1]
                    currentStudent = student
                    calculateAttendanceStats(student)
                } else {
                    Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnSavePdf.setOnClickListener {
            if (currentStudent != null && currentStats != null) {
                if (checkPermission()) {
                    generatePDF(currentStudent!!, currentStats!!)
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun loadStudentsList() {
        db.collection("students").get()
            .addOnSuccessListener { result ->
                students.clear()
                val studentNames = mutableListOf("Select Student")

                for (document in result) {
                    val s = Student(
                        id = document.id,
                        studentName = document.getString("studentName"),
                        studentRegNo = document.getString("studentRegNo"),
                        department = document.getString("department"),
                        academicYear = document.getString("academicYear"),
                        email = document.getString("email"),
                        phone = document.getString("phone")
                    )
                    students.add(s)
                    studentNames.add("${s.studentName} (${s.studentRegNo})")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studentNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                studentSpinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadStudentByRegNo(regNo: String) {
        db.collection("students")
            .whereEqualTo("studentRegNo", regNo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val s = Student(
                        id = document.id,
                        studentName = document.getString("studentName"),
                        studentRegNo = document.getString("studentRegNo"),
                        department = document.getString("department"),
                        academicYear = document.getString("academicYear"),
                        email = document.getString("email"),
                        phone = document.getString("phone")
                    )
                    currentStudent = s
                    calculateAttendanceStats(s)
                } else {
                    Toast.makeText(this, "Student not found!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun calculateAttendanceStats(student: Student) {
        // Query Attendance Collection in Firebase
        db.collection("attendance")
            .whereEqualTo("studentRegNo", student.studentRegNo)
            .get()
            .addOnSuccessListener { documents ->
                var present = 0
                val total = documents.size()

                for (doc in documents) {
                    if (doc.getString("status") == "Present") {
                        present++
                    }
                }

                val absent = total - present
                val percentage = if (total > 0) (present.toDouble() / total * 100) else 0.0

                val stats = AttendanceStats(total, present, absent, percentage)
                currentStats = stats

                displayReportUI(student, stats)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch attendance records", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayReportUI(student: Student, stats: AttendanceStats) {
        reportLayout.visibility = View.VISIBLE
        btnSavePdf.visibility = View.VISIBLE

        studentNameTxt.text = student.studentName
        regNoTxt.text = "Reg No: ${student.studentRegNo}"
        deptYearTxt.text = "${student.department} - ${student.academicYear}"

        totalClassesTxt.text = stats.total.toString()
        presentTxt.text = stats.present.toString()
        absentTxt.text = stats.absent.toString()

        val percentageStr = "%.2f%%".format(stats.percentage)
        percentageTxt.text = percentageStr

        val status = when {
            stats.percentage >= 75 -> {
                statusTxt.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                "✅ Good Standing"
            }
            stats.percentage >= 60 -> {
                statusTxt.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                "⚠️ Warning"
            }
            else -> {
                statusTxt.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                "❌ Critical"
            }
        }
        statusTxt.text = status
    }

    // ==================== PDF GENERATION ====================
    private fun generatePDF(student: Student, stats: AttendanceStats) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Title
        paint.color = Color.parseColor("#3B82F6")
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Student Attendance Report", 160f, 60f, paint)

        // Details
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.isFakeBoldText = false

        var y = 120f
        canvas.drawText("Name: ${student.studentName}", 50f, y, paint)
        y += 30f
        canvas.drawText("Reg No: ${student.studentRegNo}", 50f, y, paint)
        y += 30f
        canvas.drawText("Department: ${student.department}", 50f, y, paint)
        y += 30f
        canvas.drawText("Year: ${student.academicYear}", 50f, y, paint)

        y += 20f
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)

        // Stats
        y += 50f
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Attendance Summary", 50f, y, paint)

        y += 40f
        paint.textSize = 16f
        paint.isFakeBoldText = false

        canvas.drawText("Total Classes: ${stats.total}", 50f, y, paint)
        y += 30f
        canvas.drawText("Present: ${stats.present}", 50f, y, paint)
        y += 30f
        canvas.drawText("Absent: ${stats.absent}", 50f, y, paint)

        y += 40f
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Overall Percentage: %.2f%%".format(stats.percentage), 50f, y, paint)

        pdfDocument.finishPage(page)

        val fileName = "Attendance_${student.studentRegNo?.replace("/", "_")}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Saved: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return true
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (currentStudent != null && currentStats != null) generatePDF(currentStudent!!, currentStats!!)
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }
}