package com.example.studentattendancesystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class MarkAttendanceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: StudentAttendanceAdapter
    private lateinit var dateTxt: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnSave: Button

    // Students ලිස්ට් එක තියාගන්න
    private var students = ArrayList<Student>()

    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_attendance)

        // 1. Firebase පණගන්වා ගැනීම
        db = FirebaseFirestore.getInstance()

        recycler = findViewById(R.id.studentRecyclerView)
        dateTxt = findViewById(R.id.dateText)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnSave = findViewById(R.id.saveAttendanceBtn)

        dateTxt.text = "Date: $selectedDate"
        recycler.layoutManager = LinearLayoutManager(this)

        // 2. Firebase වලින් Students ලා Load කිරීම
        loadStudentsFromFirebase()

        btnPickDate.setOnClickListener { pickDate() }
        btnSave.setOnClickListener { saveAttendanceToFirebase() }
    }

    private fun loadStudentsFromFirebase() {
        Toast.makeText(this, "Loading students...", Toast.LENGTH_SHORT).show()

        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                students.clear()
                for (document in result) {
                    val student = Student(
                        id = document.id,
                        studentName = document.getString("studentName"),
                        studentRegNo = document.getString("studentRegNo"),
                        department = document.getString("department"),
                        academicYear = document.getString("academicYear"),
                        email = document.getString("email"),
                        phone = document.getString("phone")
                    )
                    students.add(student)
                }

                // Adapter එකට Data දානවා
                adapter = StudentAttendanceAdapter(students)
                recycler.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show()
            }
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

    private fun saveAttendanceToFirebase() {
        // Adapter එකෙන් Present/Absent තෝරාගත් අය ගන්නවා
        val attendanceList = adapter.getAttendanceStates()

        var saveCount = 0
        val totalToSave = attendanceList.filter { it.second }.size // Present අය විතරක් ගණන් කරනවා

        if (totalToSave == 0) {
            Toast.makeText(this, "Please mark at least one student present", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Saving Attendance...", Toast.LENGTH_SHORT).show()

        for ((student, isPresent) in attendanceList) {
            // Present අයට විතරක් Attendance Record එකක් දාමු (නැත්නම් Absent කියලා දාන්නත් පුළුවන්)
            if (isPresent) {
                val attendanceMap = hashMapOf(
                    "studentName" to student.studentName,
                    "studentRegNo" to student.studentRegNo,
                    "date" to selectedDate,
                    "status" to "Present",
                    "subject" to "Manual Mark", // ටීචර් අතින් මාක් කරන නිසා
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("attendance")
                    .add(attendanceMap)
                    .addOnSuccessListener {
                        saveCount++
                        if (saveCount == totalToSave) {
                            Toast.makeText(this, "Attendance Saved Successfully!", Toast.LENGTH_SHORT).show()
                            finish() // වැඩේ ඉවරයි, එළියට යනවා
                        }
                    }
            }
        }
    }
}