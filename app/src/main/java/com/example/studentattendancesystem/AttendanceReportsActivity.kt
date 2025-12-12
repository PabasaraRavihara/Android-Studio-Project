package com.example.studentattendancesystem

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AttendanceReportsActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var studentSpinner: Spinner
    private lateinit var generateBtn: Button
    private lateinit var reportLayout: LinearLayout
    private lateinit var studentNameTxt: TextView
    private lateinit var regNoTxt: TextView
    private lateinit var deptYearTxt: TextView
    private lateinit var totalClassesTxt: TextView
    private lateinit var presentTxt: TextView
    private lateinit var absentTxt: TextView
    private lateinit var percentageTxt: TextView
    private lateinit var statusTxt: TextView

    private var students = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_reports)

        db = SQLiteHelper(this)

        studentSpinner = findViewById(R.id.studentSpinner)
        generateBtn = findViewById(R.id.generateReportBtn)
        reportLayout = findViewById(R.id.reportLayout)
        studentNameTxt = findViewById(R.id.studentNameTxt)
        regNoTxt = findViewById(R.id.regNoTxt)
        deptYearTxt = findViewById(R.id.deptYearTxt)
        totalClassesTxt = findViewById(R.id.totalClassesTxt)
        presentTxt = findViewById(R.id.presentTxt)
        absentTxt = findViewById(R.id.absentTxt)
        percentageTxt = findViewById(R.id.percentageTxt)
        statusTxt = findViewById(R.id.statusTxt)

        loadStudents()

        generateBtn.setOnClickListener {
            generateReport()
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

    private fun generateReport() {
        val selectedIndex = studentSpinner.selectedItemPosition

        if (selectedIndex == 0) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
            return
        }

        val student = students[selectedIndex - 1]
        val stats = db.getAttendanceStats(student.id)

        // Show report
        reportLayout.visibility = android.view.View.VISIBLE

        studentNameTxt.text = student.name
        regNoTxt.text = "Reg No: ${student.studentCode}"
        deptYearTxt.text = "${student.department} - ${student.year}"

        totalClassesTxt.text = stats.total.toString()
        presentTxt.text = stats.present.toString()
        absentTxt.text = stats.absent.toString()

        val percentageStr = "%.2f%%".format(stats.percentage)
        percentageTxt.text = percentageStr

        // Determine status
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
}