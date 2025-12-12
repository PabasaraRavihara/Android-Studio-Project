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

    private val stateMap = HashMap<Int, Boolean>()

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
        holder.name.text = s.name
        holder.reg.text = s.studentCode
        holder.dept.text = "${s.department} - ${s.year}"

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