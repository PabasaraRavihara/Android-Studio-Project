package com.example.studentattendancesystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var listView: ListView
    private lateinit var dateTxt: TextView
    private lateinit var btnPick: Button
    private lateinit var btnLoad: Button
    private var selectedDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        db = SQLiteHelper(this)
        listView = findViewById(R.id.attendanceList)
        dateTxt = findViewById(R.id.dateTextView)
        btnPick = findViewById(R.id.btnPickDateView)
        btnLoad = findViewById(R.id.btnLoadAttendance)

        dateTxt.text = selectedDate

        btnPick.setOnClickListener { pickDate() }
        btnLoad.setOnClickListener { loadAttendance() }

        loadAttendance()
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        val dp = DatePickerDialog(this, { _, y, m, d ->
            val mm = m + 1
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, mm, d)
            dateTxt.text = selectedDate
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dp.show()
    }

    private fun loadAttendance() {
        val records = db.getAttendanceByDate(selectedDate)
        val display = records.map { "${it.studentName} (${it.studentCode})\nStatus: ${it.status}" }
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
        listView.adapter = adapter
    }
}