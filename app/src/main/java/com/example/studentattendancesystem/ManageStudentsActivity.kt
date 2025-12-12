package com.example.studentattendancesystem

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ManageStudentsActivity : AppCompatActivity(), StudentAdapter.Listener {

    private lateinit var db: SQLiteHelper
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: StudentAdapter
    private lateinit var searchInput: EditText
    private lateinit var departmentFilter: Spinner
    private lateinit var yearFilter: Spinner
    private lateinit var filterBtn: Button
    private lateinit var resetBtn: Button
    private var allStudents = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)

        db = SQLiteHelper(this)

        recycler = findViewById(R.id.studentRecycler)
        searchInput = findViewById(R.id.searchInput)
        departmentFilter = findViewById(R.id.departmentFilter)
        yearFilter = findViewById(R.id.yearFilter)
        filterBtn = findViewById(R.id.filterBtn)
        resetBtn = findViewById(R.id.resetBtn)

        recycler.layoutManager = LinearLayoutManager(this)

        setupFilters()
        loadAllStudents()

        filterBtn.setOnClickListener {
            applyFilters()
        }

        resetBtn.setOnClickListener {
            resetFilters()
        }
    }

    private fun setupFilters() {
        // Department filter
        val departments = mutableListOf("All Departments")
        departments.addAll(db.getAllDepartments())

        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentFilter.adapter = deptAdapter

        // Year filter
        val years = arrayOf("All Years", "1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearFilter.adapter = yearAdapter
    }

    private fun loadAllStudents() {
        allStudents = db.getAllStudents()
        updateRecyclerView(allStudents)
    }

    private fun applyFilters() {
        val searchQuery = searchInput.text.toString().trim().toLowerCase()
        val selectedDept = departmentFilter.selectedItem.toString()
        val selectedYear = yearFilter.selectedItem.toString()

        var filteredList = allStudents

        // Filter by department
        if (selectedDept != "All Departments") {
            filteredList = ArrayList(filteredList.filter { it.department == selectedDept })
        }

        // Filter by year
        if (selectedYear != "All Years") {
            filteredList = ArrayList(filteredList.filter { it.year == selectedYear })
        }

        // Filter by search query (name or reg)
        if (searchQuery.isNotEmpty()) {
            filteredList = ArrayList(filteredList.filter {
                it.name?.toLowerCase()?.contains(searchQuery) == true ||
                        it.studentCode?.toLowerCase()?.contains(searchQuery) == true
            })
        }

        updateRecyclerView(filteredList)

        Toast.makeText(this, "${filteredList.size} students found", Toast.LENGTH_SHORT).show()
    }

    private fun resetFilters() {
        searchInput.text.clear()
        departmentFilter.setSelection(0)
        yearFilter.setSelection(0)
        loadAllStudents()
    }

    private fun updateRecyclerView(list: ArrayList<Student>) {
        adapter = StudentAdapter(list, this)
        recycler.adapter = adapter
    }

    override fun onEdit(student: Student) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_edit_student, null)

        val etName = dialogView.findViewById<EditText>(R.id.editName)
        val etReg = dialogView.findViewById<EditText>(R.id.editReg)
        val etEmail = dialogView.findViewById<EditText>(R.id.editEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.editPhone)
        val spinnerDept = dialogView.findViewById<Spinner>(R.id.editDepartment)
        val spinnerYear = dialogView.findViewById<Spinner>(R.id.editYear)

        // Set current values
        etName.setText(student.name)
        etReg.setText(student.studentCode)
        etEmail.setText(student.email ?: "")
        etPhone.setText(student.phone ?: "")

        // Setup spinners
        val departments = arrayOf(
            "Computer Science", "Information Technology", "Software Engineering",
            "Business Management", "Engineering", "Arts"
        )
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept.adapter = deptAdapter

        val deptIndex = departments.indexOf(student.department)
        if (deptIndex >= 0) spinnerDept.setSelection(deptIndex)

        val years = arrayOf("1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        val yearIndex = years.indexOf(student.year)
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex)

        AlertDialog.Builder(this)
            .setTitle("Edit Student")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newReg = etReg.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()
                val newDept = spinnerDept.selectedItem.toString()
                val newYear = spinnerYear.selectedItem.toString()

                if (newName.isEmpty() || newReg.isEmpty()) {
                    Toast.makeText(this, "Name and Reg No are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                db.updateStudent(
                    student.id,
                    newReg,
                    newName,
                    newDept,
                    newYear,
                    if (newEmail.isEmpty()) null else newEmail,
                    if (newPhone.isEmpty()) null else newPhone
                )

                Toast.makeText(this, "Student updated", Toast.LENGTH_SHORT).show()
                loadAllStudents()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete ${student.name}?\n\nAll attendance records will also be deleted.")
            .setPositiveButton("Delete") { _, _ ->
                db.deleteStudent(student.id)
                Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show()
                loadAllStudents()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onItemClick(student: Student) {
        // Show student details
        val message = buildString {
            append("Name: ${student.name}\n")
            append("Reg No: ${student.studentCode}\n")
            append("Department: ${student.department}\n")
            append("Year: ${student.year}\n")
            if (!student.email.isNullOrEmpty()) append("Email: ${student.email}\n")
            if (!student.phone.isNullOrEmpty()) append("Phone: ${student.phone}\n")

            val stats = db.getAttendanceStats(student.id)
            append("\n--- Attendance Summary ---\n")
            append("Total Classes: ${stats.total}\n")
            append("Present: ${stats.present}\n")
            append("Absent: ${stats.absent}\n")
            append("Percentage: ${"%.2f".format(stats.percentage)}%")
        }

        AlertDialog.Builder(this)
            .setTitle("Student Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}