package com.example.studentattendancesystem

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "attendance.db"
        private const val DATABASE_VERSION = 2

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COL_USER_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"
        private const val COL_USER_TYPE = "user_type"
        private const val COL_FULL_NAME = "full_name"

        // Students Table
        private const val TABLE_STUDENTS = "students"
        private const val COL_STUDENT_ID = "id"
        private const val COL_STUDENT_REG = "student_id"
        private const val COL_NAME = "name"
        private const val COL_DEPARTMENT = "department"
        private const val COL_YEAR = "year"
        private const val COL_EMAIL = "email"
        private const val COL_PHONE = "phone"

        // Attendance Table
        private const val TABLE_ATTENDANCE = "attendance"
        private const val COL_ATT_ID = "id"
        private const val COL_ATT_STUDENT_FK = "student_id"
        private const val COL_DATE = "date"
        private const val COL_STATUS = "status"
        private const val COL_SUBJECT = "subject"
        private const val COL_MARKED_BY = "marked_by"
        private const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create Users Table
        val createUsers = """
            CREATE TABLE $TABLE_USERS (
              $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_USERNAME TEXT UNIQUE NOT NULL,
              $COL_PASSWORD TEXT NOT NULL,
              $COL_USER_TYPE TEXT NOT NULL,
              $COL_FULL_NAME TEXT
            );
        """.trimIndent()

        // Create Students Table
        val createStudents = """
            CREATE TABLE $TABLE_STUDENTS (
              $COL_STUDENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_STUDENT_REG TEXT UNIQUE NOT NULL,
              $COL_NAME TEXT NOT NULL,
              $COL_DEPARTMENT TEXT,
              $COL_YEAR TEXT,
              $COL_EMAIL TEXT,
              $COL_PHONE TEXT
            );
        """.trimIndent()

        // Create Attendance Table
        val createAttendance = """
            CREATE TABLE $TABLE_ATTENDANCE (
              $COL_ATT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_ATT_STUDENT_FK INTEGER NOT NULL,
              $COL_DATE TEXT NOT NULL,
              $COL_STATUS TEXT NOT NULL,
              $COL_SUBJECT TEXT,
              $COL_MARKED_BY TEXT,
              $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY($COL_ATT_STUDENT_FK) REFERENCES $TABLE_STUDENTS($COL_STUDENT_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createUsers)
        db.execSQL(createStudents)
        db.execSQL(createAttendance)

        // Insert default admin user
        val cv = ContentValues().apply {
            put(COL_USERNAME, "admin")
            put(COL_PASSWORD, "admin123")
            put(COL_USER_TYPE, "admin")
            put(COL_FULL_NAME, "System Administrator")
        }
        db.insert(TABLE_USERS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ATTENDANCE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STUDENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ==================== USER FUNCTIONS ====================

    fun loginUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USERNAME=? AND $COL_PASSWORD=?",
            arrayOf(username, password)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USERNAME)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)),
                userType = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_TYPE)),
                fullName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FULL_NAME))
            )
        }
        cursor.close()
        return user
    }

    fun addUser(username: String, password: String, userType: String, fullName: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD, password)
            put(COL_USER_TYPE, userType)
            put(COL_FULL_NAME, fullName)
        }
        return db.insert(TABLE_USERS, null, cv)
    }

    fun usernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_USERS WHERE $COL_USERNAME=?",
            arrayOf(username)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // ==================== STUDENT FUNCTIONS ====================

    fun addStudent(
        studentId: String,
        name: String,
        department: String,
        year: String,
        email: String? = null,
        phone: String? = null
    ): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_STUDENT_REG, studentId)
            put(COL_NAME, name)
            put(COL_DEPARTMENT, department)
            put(COL_YEAR, year)
            put(COL_EMAIL, email)
            put(COL_PHONE, phone)
        }
        return db.insert(TABLE_STUDENTS, null, cv)
    }

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
                    year = cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                )
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getStudentsByDepartmentYear(department: String?, year: String?): ArrayList<Student> {
        val list = ArrayList<Student>()
        val db = readableDatabase

        val query = buildString {
            append("SELECT * FROM $TABLE_STUDENTS WHERE 1=1")
            if (!department.isNullOrEmpty()) append(" AND $COL_DEPARTMENT='$department'")
            if (!year.isNullOrEmpty()) append(" AND $COL_YEAR='$year'")
            append(" ORDER BY $COL_NAME")
        }

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val s = Student(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_STUDENT_ID)),
                    studentCode = cursor.getString(cursor.getColumnIndexOrThrow(COL_STUDENT_REG)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    department = cursor.getString(cursor.getColumnIndexOrThrow(COL_DEPARTMENT)),
                    year = cursor.getString(cursor.getColumnIndexOrThrow(COL_YEAR)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE))
                )
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateStudent(
        id: Int,
        studentCode: String,
        name: String,
        department: String,
        year: String,
        email: String? = null,
        phone: String? = null
    ): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_STUDENT_REG, studentCode)
            put(COL_NAME, name)
            put(COL_DEPARTMENT, department)
            put(COL_YEAR, year)
            put(COL_EMAIL, email)
            put(COL_PHONE, phone)
        }
        return db.update(TABLE_STUDENTS, cv, "$COL_STUDENT_ID=?", arrayOf(id.toString()))
    }

    fun deleteStudent(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_STUDENTS, "$COL_STUDENT_ID=?", arrayOf(id.toString()))
    }

    fun studentRegExists(regNo: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM $TABLE_STUDENTS WHERE $COL_STUDENT_REG=?",
            arrayOf(regNo)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllDepartments(): List<String> {
        val list = ArrayList<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT $COL_DEPARTMENT FROM $TABLE_STUDENTS WHERE $COL_DEPARTMENT IS NOT NULL ORDER BY $COL_DEPARTMENT",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // ==================== ATTENDANCE FUNCTIONS ====================

    fun markAttendance(
        studentId: Int,
        date: String,
        status: String,
        subject: String?,
        markedBy: String? = null
    ): Long {
        val db = writableDatabase

        // Delete existing record
        if (subject != null) {
            db.delete(
                TABLE_ATTENDANCE,
                "$COL_ATT_STUDENT_FK=? AND $COL_DATE=? AND $COL_SUBJECT=?",
                arrayOf(studentId.toString(), date, subject)
            )
        } else {
            db.delete(
                TABLE_ATTENDANCE,
                "$COL_ATT_STUDENT_FK=? AND $COL_DATE=? AND ($COL_SUBJECT IS NULL OR $COL_SUBJECT='')",
                arrayOf(studentId.toString(), date)
            )
        }

        val cv = ContentValues().apply {
            put(COL_ATT_STUDENT_FK, studentId)
            put(COL_DATE, date)
            put(COL_STATUS, status)
            put(COL_SUBJECT, subject)
            put(COL_MARKED_BY, markedBy)
        }
        return db.insert(TABLE_ATTENDANCE, null, cv)
    }

    fun getAttendanceByDate(date: String, subject: String? = null): ArrayList<AttendanceRecord> {
        val list = ArrayList<AttendanceRecord>()
        val db = readableDatabase

        val query = if (subject != null) {
            """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, s.$COL_DEPARTMENT, s.$COL_YEAR,
                   a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT, a.$COL_MARKED_BY
            FROM $TABLE_ATTENDANCE a
            JOIN $TABLE_STUDENTS s ON a.$COL_ATT_STUDENT_FK = s.$COL_STUDENT_ID
            WHERE a.$COL_DATE = ? AND a.$COL_SUBJECT = ?
            ORDER BY s.$COL_NAME
            """.trimIndent()
        } else {
            """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, s.$COL_DEPARTMENT, s.$COL_YEAR,
                   a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT, a.$COL_MARKED_BY
            FROM $TABLE_ATTENDANCE a
            JOIN $TABLE_STUDENTS s ON a.$COL_ATT_STUDENT_FK = s.$COL_STUDENT_ID
            WHERE a.$COL_DATE = ?
            ORDER BY s.$COL_NAME
            """.trimIndent()
        }

        val cursor = if (subject != null) {
            db.rawQuery(query, arrayOf(date, subject))
        } else {
            db.rawQuery(query, arrayOf(date))
        }

        if (cursor.moveToFirst()) {
            do {
                val record = AttendanceRecord(
                    id = cursor.getInt(0),
                    studentName = cursor.getString(1),
                    studentCode = cursor.getString(2),
                    department = cursor.getString(3),
                    year = cursor.getString(4),
                    date = cursor.getString(5),
                    status = cursor.getString(6),
                    subject = cursor.getString(7),
                    markedBy = cursor.getString(8)
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAttendanceForStudent(studentId: Int): ArrayList<AttendanceRecord> {
        val list = ArrayList<AttendanceRecord>()
        val db = readableDatabase

        val query = """
            SELECT a.$COL_ATT_ID, s.$COL_NAME, s.$COL_STUDENT_REG, s.$COL_DEPARTMENT, s.$COL_YEAR,
                   a.$COL_DATE, a.$COL_STATUS, a.$COL_SUBJECT, a.$COL_MARKED_BY
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
                    department = cursor.getString(3),
                    year = cursor.getString(4),
                    date = cursor.getString(5),
                    status = cursor.getString(6),
                    subject = cursor.getString(7),
                    markedBy = cursor.getString(8)
                )
                list.add(record)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getAttendanceStats(studentId: Int): AttendanceStats {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN $COL_STATUS='Present' THEN 1 ELSE 0 END) as present,
                SUM(CASE WHEN $COL_STATUS='Absent' THEN 1 ELSE 0 END) as absent
            FROM $TABLE_ATTENDANCE
            WHERE $COL_ATT_STUDENT_FK = ?
            """.trimIndent(),
            arrayOf(studentId.toString())
        )

        var stats = AttendanceStats(0, 0, 0, 0.0)
        if (cursor.moveToFirst()) {
            val total = cursor.getInt(0)
            val present = cursor.getInt(1)
            val absent = cursor.getInt(2)
            val percentage = if (total > 0) (present.toDouble() / total * 100) else 0.0
            stats = AttendanceStats(total, present, absent, percentage)
        }
        cursor.close()
        return stats
    }
}

