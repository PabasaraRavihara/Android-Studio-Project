package com.example.studentattendancesystem

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AddStudentActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        db = FirebaseFirestore.getInstance()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val regInput = findViewById<EditText>(R.id.regInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)

        val deptSpinner = findViewById<Spinner>(R.id.departmentSpinner)
        val yearSpinner = findViewById<Spinner>(R.id.yearSpinner)

        val addBtn = findViewById<Button>(R.id.addBtn)
        val clearBtn = findViewById<Button>(R.id.clearBtn)

        // Spinners Setup
        val deptList = arrayOf("Select Department", "ICT", "Engineering", "Bio Systems", "Management")
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, deptList)
        deptSpinner.adapter = deptAdapter

        val yearList = arrayOf("Select Year", "1st Year", "2nd Year", "3rd Year", "4th Year")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearList)
        yearSpinner.adapter = yearAdapter

        addBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val reg = regInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()

            val selectedDept = deptSpinner.selectedItem.toString()
            val selectedYear = yearSpinner.selectedItem.toString()

            if (name.isEmpty() || reg.isEmpty()) {
                Toast.makeText(this, "Name and Reg No are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDept == "Select Department" || selectedYear == "Select Year") {
                Toast.makeText(this, "Please select Department and Year", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // === අලුත් කොටස: කලින් මේ Reg No එක තියෙනවද කියලා බලනවා ===
            checkAndSaveStudent(name, reg, selectedDept, selectedYear, email, phone)
        }

        clearBtn.setOnClickListener {
            nameInput.text.clear()
            regInput.text.clear()
            emailInput.text.clear()
            phoneInput.text.clear()
            deptSpinner.setSelection(0)
            yearSpinner.setSelection(0)
        }
    }

    private fun checkAndSaveStudent(name: String, reg: String, dept: String, year: String, email: String, phone: String) {
        // Firebase එකෙන් අහනවා මේ Reg No එක තියෙන අය ඉන්නවද කියලා
        db.collection("students")
            .whereEqualTo("studentRegNo", reg)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // දැනටමත් කෙනෙක් ඉන්නවා නම් Error එකක් පෙන්වනවා
                    Toast.makeText(this, "⚠️ Student with Reg No $reg already exists!", Toast.LENGTH_LONG).show()
                } else {
                    // කවුරුත් නෑ, ඒ කියන්නේ Save කරන්න පුළුවන්
                    saveToFirebase(name, reg, dept, year, email, phone)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking database", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirebase(name: String, reg: String, dept: String, year: String, email: String, phone: String) {
        val studentMap = hashMapOf(
            "studentName" to name,
            "studentRegNo" to reg,
            "department" to dept,
            "academicYear" to year,
            "email" to email,
            "phone" to phone,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("students")
            .add(studentMap)
            .addOnSuccessListener {
                Toast.makeText(this, "✅ Student Added Successfully!", Toast.LENGTH_SHORT).show()
                finish() // Save වුණාට පස්සේ Form එක වහලා දානවා
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}