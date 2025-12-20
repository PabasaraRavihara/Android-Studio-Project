package com.example.studentattendancesystem

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewAttendanceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
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

        // 1. Firebase පණගන්වා ගැනීම
        db = FirebaseFirestore.getInstance()

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
        btnLoad.setOnClickListener { loadAttendanceFromFirebase() }

        // Load initial data
        loadAttendanceFromFirebase()
    }

    private fun setupSubjectSpinner() {
        val subjects = arrayOf("All Subjects", "Mathematics", "Science", "English", "History", "Programming", "Manual Mark")
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

    private fun loadAttendanceFromFirebase() {
        val selectedSubject = subjectSpinner.selectedItem.toString()

        // Query එක හදනවා
        var query = db.collection("attendance")
            .whereEqualTo("date", selectedDate)

        // Subject එකක් තෝරලා නම් ඒකෙනුත් Filter කරනවා
        if (selectedSubject != "All Subjects") {
            query = query.whereEqualTo("subject", selectedSubject)
        }

        Toast.makeText(this, "Loading records...", Toast.LENGTH_SHORT).show()

        query.get()
            .addOnSuccessListener { result ->
                val records = ArrayList<AttendanceRecord>()

                for (document in result) {
                    // Firebase Data -> AttendanceRecord Object
                    val record = AttendanceRecord(
                        id = 0, // Firebase වලට ID එක String නිසා මෙතන 0 දාමු (Display එකට අවුලක් නෑ)
                        studentName = document.getString("studentName"),
                        studentCode = document.getString("studentRegNo"),
                        date = document.getString("date"),
                        status = document.getString("status"),
                        subject = document.getString("subject"),
                        // මේවා Attendance Table එකේ නැති නිසා හිස්ව තියමු හෝ Default දාමු
                        department = "N/A",
                        year = "",
                        markedBy = "Teacher"
                    )
                    records.add(record)
                }

                if (records.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    emptyView.text = "No attendance records found for $selectedDate"
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE

                    adapter = AttendanceRecordAdapter(records)
                    recyclerView.adapter = adapter

                    // Summary පෙන්වීම
                    val presentCount = records.count { it.status == "Present" }
                    val absentCount = records.count { it.status == "Absent" }
                    Toast.makeText(this, "Present: $presentCount | Absent: $absentCount", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading data!", Toast.LENGTH_SHORT).show()
            }
    }
}

// Adapter for displaying attendance records
class AttendanceRecordAdapter(
    private val records: List<AttendanceRecord>
) : RecyclerView.Adapter<AttendanceRecordAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.recordName)
        val regText: TextView = view.findViewById(R.id.recordReg)
        val deptText: TextView = view.findViewById(R.id.recordDept)
        val statusText: TextView = view.findViewById(R.id.recordStatus)
        val subjectText: TextView = view.findViewById(R.id.recordSubject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]

        holder.nameText.text = record.studentName
        holder.regText.text = record.studentCode

        // Department එක නැති නිසා නිකන් ඉරක් ගහමු
        holder.deptText.text = "${record.date}"

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