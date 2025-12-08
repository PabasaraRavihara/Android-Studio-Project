package com.example.studentattendancesystem


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // use activity_main.xml provided earlier

        val addStudentBtn = findViewById<Button>(R.id.addStudentBtn)
        val markAttendanceBtn = findViewById<Button>(R.id.markAttendanceBtn)
        val viewAttendanceBtn = findViewById<Button>(R.id.viewAttendanceBtn)

        addStudentBtn.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        markAttendanceBtn.setOnClickListener {
            startActivity(Intent(this, MarkAttendanceActivity::class.java))
        }

        viewAttendanceBtn.setOnClickListener {
            startActivity(Intent(this, ViewAttendanceActivity::class.java))
        }
    }
}
