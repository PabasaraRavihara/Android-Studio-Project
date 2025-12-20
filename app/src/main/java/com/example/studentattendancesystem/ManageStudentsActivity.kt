package com.example.studentattendancesystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
class ManageStudentsActivity : AppCompatActivity(), StudentAdapter.Listener {

    private lateinit var db: FirebaseFirestore
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: StudentAdapter
    private lateinit var searchInput: EditText
    private lateinit var departmentFilter: Spinner
    private lateinit var yearFilter: Spinner
    private lateinit var filterBtn: Button
    private lateinit var resetBtn: Button

    // List to hold students from Firebase
    private var allStudents = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        recycler = findViewById(R.id.studentRecycler)
        searchInput = findViewById(R.id.searchInput)
        departmentFilter = findViewById(R.id.departmentFilter)
        yearFilter = findViewById(R.id.yearFilter)
        filterBtn = findViewById(R.id.filterBtn)
        resetBtn = findViewById(R.id.resetBtn)

        recycler.layoutManager = LinearLayoutManager(this)

        setupFilters()
        loadAllStudentsFromFirebase() // Load data from Cloud

        filterBtn.setOnClickListener {
            applyFilters()
        }

        resetBtn.setOnClickListener {
            resetFilters()
        }
    }

    private fun setupFilters() {
        // Department filter
        val departments = arrayOf("All Departments", "ICT", "Engineering", "Bio Systems", "Management")
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentFilter.adapter = deptAdapter

        // Year filter
        val years = arrayOf("All Years", "1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearFilter.adapter = yearAdapter
    }

    private fun loadAllStudentsFromFirebase() {
        Toast.makeText(this, "Loading data...", Toast.LENGTH_SHORT).show()

        db.collection("students")
            .get()
            .addOnSuccessListener { result ->
                allStudents.clear()
                for (document in result) {
                    // Convert Firebase document to Student object manually
                    // We store the Document ID inside the student object to edit/delete later
                    val student = Student(
                        id = document.id, // Using String ID for Firebase
                        studentName = document.getString("studentName"),
                        studentRegNo = document.getString("studentRegNo"),
                        department = document.getString("department"),
                        academicYear = document.getString("academicYear"),
                        email = document.getString("email"),
                        phone = document.getString("phone")
                    )
                    allStudents.add(student)
                }
                updateRecyclerView(allStudents)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun applyFilters() {
        val searchQuery = searchInput.text.toString().trim().lowercase()
        val selectedDept = departmentFilter.selectedItem.toString()
        val selectedYear = yearFilter.selectedItem.toString()

        var filteredList = allStudents.toList()

        // Filter by department
        if (selectedDept != "All Departments") {
            filteredList = filteredList.filter { it.department == selectedDept }
        }

        // Filter by year
        if (selectedYear != "All Years") {
            filteredList = filteredList.filter { it.academicYear == selectedYear }
        }

        // Filter by search query (name or reg)
        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                (it.studentName?.lowercase()?.contains(searchQuery) == true) ||
                        (it.studentRegNo?.lowercase()?.contains(searchQuery) == true)
            }
        }

        updateRecyclerView(ArrayList(filteredList))
        Toast.makeText(this, "${filteredList.size} students found", Toast.LENGTH_SHORT).show()
    }

    private fun resetFilters() {
        searchInput.text.clear()
        departmentFilter.setSelection(0)
        yearFilter.setSelection(0)
        updateRecyclerView(allStudents)
    }

    private fun updateRecyclerView(list: ArrayList<Student>) {
        adapter = StudentAdapter(list, this)
        recycler.adapter = adapter
    }

    // --- Edit Functionality (Updates Firebase) ---
    override fun onEdit(student: Student) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_edit_student, null)

        val etName = dialogView.findViewById<EditText>(R.id.editName)
        val etReg = dialogView.findViewById<EditText>(R.id.editReg)
        val spinnerDept = dialogView.findViewById<Spinner>(R.id.editDepartment)
        val spinnerYear = dialogView.findViewById<Spinner>(R.id.editYear)

        // Set current values
        etName.setText(student.studentName)
        etReg.setText(student.studentRegNo)

        // Setup spinners
        val departments = arrayOf("ICT", "Engineering", "Bio Systems", "Management")
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDept.adapter = deptAdapter

        val deptIndex = departments.indexOf(student.department)
        if (deptIndex >= 0) spinnerDept.setSelection(deptIndex)

        val years = arrayOf("1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter

        val yearIndex = years.indexOf(student.academicYear)
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex)

        AlertDialog.Builder(this)
            .setTitle("Edit Student")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newName = etName.text.toString().trim()
                val newReg = etReg.text.toString().trim()
                val newDept = spinnerDept.selectedItem.toString()
                val newYear = spinnerYear.selectedItem.toString()

                val updateMap = mapOf(
                    "studentName" to newName,
                    "studentRegNo" to newReg,
                    "department" to newDept,
                    "academicYear" to newYear
                )

                // Update in Firebase using ID
                db.collection("students").document(student.id)
                    .update(updateMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Student Updated!", Toast.LENGTH_SHORT).show()
                        loadAllStudentsFromFirebase() // Refresh list
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- Delete Functionality (Removes from Firebase) ---
    override fun onDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete ${student.studentName}?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("students").document(student.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Student Deleted", Toast.LENGTH_SHORT).show()
                        loadAllStudentsFromFirebase() // Refresh list
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onItemClick(student: Student) {
        // Optional: Show full details
    }
}