package com.example.studentattendancesystem


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "attendance.db"
        private const val DATABASE_VERSION = 1

        // Students
        private const val TABLE_STUDENTS = "students"
        private const val COL_STUDENT_ID = "id"
        private const val COL_STUDENT_REG = "student_id"
        private const val COL_NAME = "name"
        private const val COL_DEPARTMENT = "department"
        private const val COL_YEAR = "year"

        // Attendance
        private const val TABLE_ATTENDANCE = "attendance"
        private const val COL_ATT_ID = "id"
        private const val COL_ATT_STUDENT_FK = "student_id"
        private const val COL_DATE = "date"
        private const val COL_STATUS = "status"
        private const val COL_SUBJECT = "subject"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createStudents = """
            CREATE TABLE $TABLE_STUDENTS (
              $COL_STUDENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_STUDENT_REG TEXT,
              $COL_NAME TEXT,
              $COL_DEPARTMENT TEXT,
              $COL_YEAR TEXT
            );
        """.trimIndent()

        val createAttendance = """
            CREATE TABLE $TABLE_ATTENDANCE (
              $COL_ATT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_ATT_STUDENT_FK INTEGER,
              $COL_DATE TEXT,
              $COL_STATUS TEXT,
              $COL_SUBJECT TEXT,
              FOREIGN KEY($COL_ATT_STUDENT_FK) REFERENCES $TABLE_STUDENTS($COL_STUDENT_ID)
            );
        """.trimIndent()

        db.execSQL(createStudents)
        db.execSQL(createAttendance)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        onCreate(db)
    }

    // Add student
    fun addStudent(studentId: String, name: String, department: String, year: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_STUDENT_REG, studentId)
            put(COL_NAME, name)
            put(COL_DEPARTMENT, department)
            put(COL_YEAR, year)
        }
        return db.insert(TABLE_STUDENTS, null, cv)
    }

    // Get all students
    fun getAllStudents(): ArrayList<Student> {
        val list = ArrayList<Student>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_STUDENTS ORDER BY $COL_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val s = Student(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID)),
                    studentCode = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_REG)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    department = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEPARTMENT)),
                    year = cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR))
                )
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Update student
    fun updateStudent(id: Int, studentCode: String, name: String, department: String, year: String): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_STUDENT_REG, studentCode)
            put(COL_NAME, name)
            put(COL_DEPARTMENT, department)
            put(COL_YEAR, year)
        }
        return db.update(TABLE_STUDENTS, cv, "$COL_STUDENT_ID=?", arrayOf(id.toString()))
    }

    // Delete student (also delete attendance records for that student)
    fun deleteStudent(id: Int): Int {
        val db = writableDatabase
        db.delete(TABLE_ATTENDANCE, "$COL_ATT_STUDENT_FK=?", arrayOf(id.toString()))
        return db.delete(TABLE_STUDENTS, "$COL_STUDENT_ID=?", arrayOf(id.toString()))
    }

    // Mark attendance (UPSERT-like: remove existing record for same student+date+subject, then insert)
    fun markAttendance(studentId: Int, date: String, status: String, subject: String? = null): Long {
        val db = writableDatabase
        // Delete any existing for same student+date+subject
        val selectionArgs = if (subject != null) arrayOf(studentId.toString(), date, subject) else arrayOf(studentId.toString(), date)
        if (subject != null) {
            db.delete(TABLE_ATTENDANCE, "$COL_ATT_STUDENT_FK=? AND $COL_DATE=? AND $COL_SUBJECT=?", selectionArgs)
        } else {
            db.delete(TABLE_ATTENDANCE, "$COL_ATT_STUDENT_FK=? AND $COL_DATE=? AND ($COL_SUBJECT IS NULL OR $COL_SUBJECT='')", selectionArgs)
        }

        val cv = ContentValues().apply {
            put(COL_ATT_STUDENT_FK, studentId)
            put(COL_DATE, date)
            put(COL_STATUS, status)
            put(COL_SUBJECT, subject)
        }
        return db.insert(TABLE_ATTENDANCE, null, cv)
    }

    // Get attendance by date (returns list of AttendanceRecord)
    fun getAttendanceByDate(date: String, subject: String? = null): ArrayList<AttendanceRecord> {
        val list = ArrayList<AttendanceRecord>()
        val db = readableDatabase
        val query = if (subject != null) {
            """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT
            FROM $TABLE_ATTENDANCE a
            JOIN $TABLE_STUDENTS s ON a.$COL_ATT_STUDENT_FK = s.$COL_STUDENT_ID
            WHERE a.$COL_DATE = ? AND a.$COL_SUBJECT = ?
            ORDER BY s.$COL_NAME
            """.trimIndent()
        } else {
            """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT
            FROM $TABLE_ATTENDANCE a
            JOIN $TABLE_STUDENTS s ON a.$COL_ATT_STUDENT_FK = s.$COL_STUDENT_ID
            WHERE a.$COL_DATE = ?
            ORDER BY s.$COL_NAME
            """.trimIndent()
        }

        val cursor = if (subject != null) db.rawQuery(query, arrayOf(date, subject)) else db.rawQuery(query, arrayOf(date))
        if (cursor.moveToFirst()) {
            do {
                val record = AttendanceRecord(
                    id = cursor.getInt(0),
                    studentName = cursor.getString(1),
                    studentCode = cursor.getString(2),
                    date = cursor.getString(3),
                    status = cursor.getString(4),
                    subject = cursor.getString(5)
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Get attendance summary for one student
    fun getAttendanceForStudent(studentId: Int): ArrayList<AttendanceRecord> {
        val list = ArrayList<AttendanceRecord>()
        val db = readableDatabase
        val query = """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT
            FROM $TABLE_ATTENDANCE a
            JOIN $TABLE_STUDENTS s ON a.$COL_ATT_STUDENT_FK = s.$COL_STUDENT_ID
            WHERE s.$COL_STUDENT_ID = ?
            ORDER BY a.$COL_DATE DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(studentId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val record = AttendanceRecord(
                    id = cursor.getInt(0),
                    studentName = cursor.getString(1),
                    studentCode = cursor.getString(2),
                    date = cursor.getString(3),
                    status = cursor.getString(4),
                    subject = cursor.getString(5)
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
