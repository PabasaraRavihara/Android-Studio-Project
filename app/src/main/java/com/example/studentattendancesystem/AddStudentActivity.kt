package com.example.studentattendancesystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AddStudentActivity : AppCompatActivity() {

    private lateinit var db: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_student)

        db = SQLiteHelper(this)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val regInput = findViewById<EditText>(R.id.regInput)
        val addBtn = findViewById<Button>(R.id.addBtn)

        addBtn.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val reg = regInput.text.toString().trim()

            if (name.isEmpty() || reg.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = db.addStudent(reg, name, "Dept", "1") // Defaulting Dept/Year for simplicity
            if (id > 0) {
                Toast.makeText(this, "Student added", Toast.LENGTH_SHORT).show()
                nameInput.text.clear()
                regInput.text.clear()
            } else {
                Toast.makeText(this, "Error adding student", Toast.LENGTH_SHORT).show()
            }
        }
    }
}