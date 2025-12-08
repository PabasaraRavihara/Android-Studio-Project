package com.example.studentattendancesystem

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ManageStudentsActivity : AppCompatActivity(), StudentAdapter.Listener {

    private lateinit var db: SQLiteHelper
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: StudentAdapter
    private var list = ArrayList<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_students)

        db = SQLiteHelper(this)
        recycler = findViewById(R.id.studentRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        loadStudents()
    }

    private fun loadStudents() {
        list = db.getAllStudents()
        adapter = StudentAdapter(list, this)
        recycler.adapter = adapter
    }

    override fun onEdit(student: Student) {
        // Layout Inflater එක හරියට ගන්න
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_edit_student, null)

        val etName = dialogView.findViewById<EditText>(R.id.editName)
        val etReg = dialogView.findViewById<EditText>(R.id.editReg)

        etName.setText(student.name)
        etReg.setText(student.studentCode)

        // androidx AlertDialog එක පාවිච්චි කරන්න
        AlertDialog.Builder(this)
            .setTitle("Edit Student")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newReg = etReg.text.toString().trim()

                // Department/Year වලට හිස් අගයන් දාමු (Update function එකේ ඉල්ලන නිසා)
                db.updateStudent(student.id, newReg, newName, student.department ?: "", student.year ?: "")

                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                loadStudents()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDelete(student: Student) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Delete ${student.name}?")
            .setPositiveButton("Delete") { _, _ ->
                db.deleteStudent(student.id)
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                loadStudents()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onItemClick(student: Student) {
        // Optional: Do something on click
    }
}