package com.example.studentattendancesystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AttendanceRecordAdapter
    private lateinit var dateTxt: TextView
    private lateinit var btnPickDate: Button
    private lateinit var btnLoad: Button
    private lateinit var subjectSpinner: Spinner
    private lateinit var emptyView: TextView

    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_attendance)

        db = SQLiteHelper(this)

        recyclerView = findViewById(R.id.attendanceRecycler)
        dateTxt = findViewById(R.id.dateTextView)
        btnPickDate = findViewById(R.id.btnPickDateView)
        btnLoad = findViewById(R.id.btnLoadAttendance)
        subjectSpinner = findViewById(R.id.subjectSpinner)
        emptyView = findViewById(R.id.emptyView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSubjectSpinner()

        dateTxt.text = selectedDate

        btnPickDate.setOnClickListener { pickDate() }
        btnLoad.setOnClickListener { loadAttendance() }

        // Load initial data
        loadAttendance()
    }

    private fun setupSubjectSpinner() {
        val subjects = arrayOf("All Subjects", "Mathematics", "Science", "English", "History", "Programming")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter
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
        val selectedSubject = subjectSpinner.selectedItem.toString()
        val subject = if (selectedSubject == "All Subjects") null else selectedSubject

        val records = db.getAttendanceByDate(selectedDate, subject)

        if (records.isEmpty()) {
            recyclerView.visibility = android.view.View.GONE
            emptyView.visibility = android.view.View.VISIBLE
            emptyView.text = "No attendance records found for $selectedDate"
        } else {
            recyclerView.visibility = android.view.View.VISIBLE
            emptyView.visibility = android.view.View.GONE

            adapter = AttendanceRecordAdapter(records)
            recyclerView.adapter = adapter

            // Show summary
            val presentCount = records.count { it.status == "Present" }
            val absentCount = records.count { it.status == "Absent" }

            Toast.makeText(
                this,
                "Total: ${records.size} | Present: $presentCount | Absent: $absentCount",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

// Adapter for displaying attendance records
class AttendanceRecordAdapter(
    private val records: List<AttendanceRecord>
) : RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder>() {

    inner class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.recordName)
        val regText: TextView = view.findViewById(R.id.recordReg)
        val deptText: TextView = view.findViewById(R.id.recordDept)
        val statusText: TextView = view.findViewById(R.id.recordStatus)
        val subjectText: TextView = view.findViewById(R.id.recordSubject)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        holder.nameText.text = record.studentName
        holder.regText.text = record.studentCode
        holder.deptText.text = "${record.department ?: ""} - ${record.year ?: ""}"
        holder.statusText.text = record.status

        // Color code status
        if (record.status == "Present") {
            holder.statusText.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_green_dark))
        } else {
            holder.statusText.setTextColor(holder.itemView.resources.getColor(android.R.color.holo_red_dark))
        }

        holder.subjectText.text = record.subject ?: "General"
    }

    override fun getItemCount(): Int = records.size
}