package com.example.studentattendancesystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAttendanceAdapter(
    private val items: List<Student>
) : RecyclerView.Adapter<StudentAttendanceAdapter.VH>() {

    // Changed Int to String because Firebase IDs are Strings
    private val stateMap = HashMap<String, Boolean>()

    init {
        for (s in items) stateMap[s.id] = false
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemName)
        val reg: TextView = view.findViewById(R.id.itemReg)
        val dept: TextView = view.findViewById(R.id.itemDept)
        val check: CheckBox = view.findViewById(R.id.itemPresent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_attendance, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]

        // Updated property names
        holder.name.text = s.studentName
        holder.reg.text = s.studentRegNo
        holder.dept.text = "${s.department} - ${s.academicYear}"

        // Fix recycling issue
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = stateMap[s.id] ?: false

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            stateMap[s.id] = isChecked
        }
    }

    override fun getItemCount(): Int = items.size

    fun getAttendanceStates(): List<Pair<Student, Boolean>> {
        val out = ArrayList<Pair<Student, Boolean>>()
        for (s in items) {
            out.add(Pair(s, stateMap[s.id] ?: false))
        }
        return out
    }
}