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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AttendanceReportsActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_reports)

        db = SQLiteHelper(this)

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
            // STUDENT MODE: Hide admin controls
            studentSpinner.visibility = View.GONE
            generateBtn.visibility = View.GONE
            lblSelectStudent.visibility = View.GONE

            val student = db.getStudentByReg(specificStudentReg)
            if (student != null) {
                currentStudent = student
                displayReportForStudent(student)
            } else {
                Toast.makeText(this, "Student profile not found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // ADMIN/TEACHER MODE: Show spinner
            loadStudents()
            generateBtn.setOnClickListener {
                val selectedIndex = studentSpinner.selectedItemPosition
                if (selectedIndex > 0) {
                    val student = students[selectedIndex - 1]
                    currentStudent = student
                    displayReportForStudent(student)
                } else {
                    Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // PDF Button Click Listener
        btnSavePdf.setOnClickListener {
            if (currentStudent != null) {
                if (checkPermission()) {
                    generatePDF(currentStudent!!)
                } else {
                    requestPermission()
                }
            }
        }
    }

    private fun loadStudents() {
        students = db.getAllStudents()
        val studentNames = mutableListOf("Select Student")
        studentNames.addAll(students.map { "${it.name} (${it.studentCode})" })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        studentSpinner.adapter = adapter
    }

    private fun displayReportForStudent(student: Student) {
        val stats = db.getAttendanceStats(student.id)

        reportLayout.visibility = View.VISIBLE
        btnSavePdf.visibility = View.VISIBLE

        studentNameTxt.text = student.name
        regNoTxt.text = "Reg No: ${student.studentCode}"
        deptYearTxt.text = "${student.department} - ${student.year}"

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

    // ==================== PDF GENERATION LOGIC ====================
    private fun generatePDF(student: Student) {
        val stats = db.getAttendanceStats(student.id)

        // Create Document (A4 Size)
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()

        // 1. Title
        paint.color = Color.parseColor("#3B82F6") // Blue
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Student Attendance Report", 160f, 60f, paint)

        // 2. Student Details
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.isFakeBoldText = false

        var y = 120f
        canvas.drawText("Name: ${student.name}", 50f, y, paint)
        y += 30f
        canvas.drawText("Reg No: ${student.studentCode}", 50f, y, paint)
        y += 30f
        canvas.drawText("Department: ${student.department}", 50f, y, paint)
        y += 30f
        canvas.drawText("Year: ${student.year}", 50f, y, paint)

        // Line Separator
        y += 20f
        paint.color = Color.LTGRAY
        paint.strokeWidth = 2f
        canvas.drawLine(50f, y, 545f, y, paint)

        // 3. Statistics Section
        y += 50f
        paint.color = Color.BLACK
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Attendance Summary", 50f, y, paint)

        y += 40f
        paint.textSize = 16f
        paint.isFakeBoldText = false

        canvas.drawText("Total Classes:", 50f, y, paint)
        canvas.drawText(stats.total.toString(), 300f, y, paint)

        y += 30f
        canvas.drawText("Present:", 50f, y, paint)
        paint.color = Color.parseColor("#10B981") // Green
        canvas.drawText(stats.present.toString(), 300f, y, paint)

        y += 30f
        paint.color = Color.BLACK
        canvas.drawText("Absent:", 50f, y, paint)
        paint.color = Color.parseColor("#EF4444") // Red
        canvas.drawText(stats.absent.toString(), 300f, y, paint)

        y += 40f
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Overall Percentage:", 50f, y, paint)

        val percentStr = "%.2f%%".format(stats.percentage)
        canvas.drawText(percentStr, 300f, y, paint)

        // Footer (Date Generated)
        y += 100f
        paint.textSize = 12f
        paint.color = Color.GRAY
        paint.isFakeBoldText = false
        canvas.drawText("Generated by Student Attendance System", 50f, y, paint)

        pdfDocument.finishPage(page)

        // Save to Downloads folder
        val fileName = "Attendance_${student.studentCode?.replace("/", "_")}.pdf"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true // Android 10+ doesn't need permission for Downloads
        }
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (currentStudent != null) generatePDF(currentStudent!!)
            } else {
                Toast.makeText(this, "Permission Denied! Cannot save PDF.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}