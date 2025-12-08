package com.example.studentattendancesystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: StudentAttendanceAdapter
    private var students = ArrayList<Student>()
    private lateinit var dateTxt: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnSave: Button
    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_attendance)

        db = SQLiteHelper(this)
        recycler = findViewById(R.id.studentRecyclerView)
        dateTxt = findViewById(R.id.dateText)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnSave = findViewById(R.id.saveAttendanceBtn)

        dateTxt.text = "Date: $selectedDate"
        recycler.layoutManager = LinearLayoutManager(this)
        loadStudents()

        btnPickDate.setOnClickListener { pickDate() }
        btnSave.setOnClickListener { saveAttendance() }
    }

    private fun loadStudents() {
        students = db.getAllStudents()
        adapter = StudentAttendanceAdapter(students)
        recycler.adapter = adapter
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, year, month, day ->
            val mm = month + 1
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, mm, day)
            dateTxt.text = "Date: $selectedDate"
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    private fun saveAttendance() {
        val attendanceList = adapter.getAttendanceStates()
        var anySaved = false
        for ((student, present) in attendanceList) {
            val status = if (present) "Present" else "Absent"
            val res = db.markAttendance(student.id, selectedDate, status, null)
            if (res > 0) anySaved = true
        }
        if (anySaved) Toast.makeText(this, "Attendance saved", Toast.LENGTH_SHORT).show()
        else Toast.makeText(this, "No students to save", Toast.LENGTH_SHORT).show()
    }
}