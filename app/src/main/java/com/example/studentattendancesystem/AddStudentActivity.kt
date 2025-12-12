package com.example.studentattendancesystem

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddStudentActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper
    private lateinit var nameInput: EditText
    private lateinit var regInput: EditText
    private lateinit var departmentSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addBtn: Button
    private lateinit var clearBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        db = SQLiteHelper(this)

        nameInput = findViewById(R.id.nameInput)
        regInput = findViewById(R.id.regInput)
        departmentSpinner = findViewById(R.id.departmentSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        addBtn = findViewById(R.id.addBtn)
        clearBtn = findViewById(R.id.clearBtn)

        setupSpinners()

        addBtn.setOnClickListener {
            addStudent()
        }

        clearBtn.setOnClickListener {
            clearFields()
        }
    }

    private fun setupSpinners() {
        // Department Spinner
        val departments = arrayOf(
            "Select Department",
            "Computer Science",
            "Information Technology",
            "Software Engineering",
            "Business Management",
            "Engineering",
            "Arts"
        )

        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, departments)
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        departmentSpinner.adapter = deptAdapter

        // Year Spinner
        val years = arrayOf("Select Year", "1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
    }

    private fun addStudent() {
        val name = nameInput.text.toString().trim()
        val reg = regInput.text.toString().trim()
        val department = departmentSpinner.selectedItem.toString()
        val year = yearSpinner.selectedItem.toString()
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            nameInput.requestFocus()
            return
        }

        if (reg.isEmpty()) {
            regInput.error = "Registration number is required"
            regInput.requestFocus()
            return
        }

        if (department == "Select Department") {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show()
            return
        }

        if (year == "Select Year") {
            Toast.makeText(this, "Please select a year", Toast.LENGTH_SHORT).show()
            return
        }

        // Optional email validation
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Invalid email format"
            emailInput.requestFocus()
            return
        }

        // Optional phone validation
        if (phone.isNotEmpty() && phone.length < 10) {
            phoneInput.error = "Phone number must be at least 10 digits"
            phoneInput.requestFocus()
            return
        }

        // Check if registration number already exists
        if (db.studentRegExists(reg)) {
            regInput.error = "This registration number already exists"
            regInput.requestFocus()
            return
        }

        // Insert student
        val id = db.addStudent(
            studentId = reg,
            name = name,
            department = department,
            year = year,
            email = if (email.isEmpty()) null else email,
            phone = if (phone.isEmpty()) null else phone
        )

        if (id > 0) {
            Toast.makeText(this, "Student added successfully!", Toast.LENGTH_SHORT).show()
            clearFields()
        } else {
            Toast.makeText(this, "Error adding student", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        nameInput.text.clear()
        regInput.text.clear()
        emailInput.text.clear()
        phoneInput.text.clear()
        departmentSpinner.setSelection(0)
        yearSpinner.setSelection(0)
        nameInput.requestFocus()
    }
}