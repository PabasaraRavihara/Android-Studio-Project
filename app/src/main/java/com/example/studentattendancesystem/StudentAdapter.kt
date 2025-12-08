package com.example.studentattendancesystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(
    private val items: List<Student>,
    private val listener: Listener
) : RecyclerView.Adapter<StudentAdapter.VH>() {

    interface Listener {
        fun onEdit(student: Student)
        fun onDelete(student: Student)
        fun onItemClick(student: Student)
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        // වැරදි ලයින් එක මැකුවා (val itemView: Any)
        val name: TextView = view.findViewById(R.id.tvName)
        val reg: TextView = view.findViewById(R.id.tvReg)
        val editBtn: ImageButton = view.findViewById(R.id.btnEdit)
        val delBtn: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // item_student_row.xml එක මෙතනට call වෙනවා
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_student_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = items[position]
        holder.name.text = s.name
        holder.reg.text = s.studentCode

        // holder.itemView කියන එක parent class එකෙන් ඉබේම එනවා
        holder.itemView.setOnClickListener { listener.onItemClick(s) }
        holder.editBtn.setOnClickListener { listener.onEdit(s) }
        holder.delBtn.setOnClickListener { listener.onDelete(s) }
    }

    override fun getItemCount(): Int = items.size
}