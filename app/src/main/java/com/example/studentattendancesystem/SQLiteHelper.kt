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

        // Users Table (Login/Register සඳහා මෙය තබා ගනිමු)
        private const val TABLE_USERS = "users"
        private const val COL_USER_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD = "password"
        private const val COL_USER_TYPE = "user_type"
        private const val COL_FULL_NAME = "full_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users Table එක පමණක් සෑදීම
        val createUsers = """
            CREATE TABLE $TABLE_USERS (
              $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
              $COL_USERNAME TEXT UNIQUE NOT NULL,
              $COL_PASSWORD TEXT NOT NULL,
              $COL_USER_TYPE TEXT NOT NULL,
              $COL_FULL_NAME TEXT
            );
        """.trimIndent()

        db.execSQL(createUsers)

        // Default Admin කෙනෙක්ව ඇතුළත් කිරීම
        val cv = ContentValues().apply {
            put(COL_USERNAME, "admin")
            put(COL_PASSWORD, "admin123")
            put(COL_USER_TYPE, "admin")
            put(COL_FULL_NAME, "System Administrator")
        }
        db.insert(TABLE_USERS, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ==================== USER FUNCTIONS (LOGIN/REGISTER) ====================

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

    // Student Validation for Registration (Firebase Check Logic එක Register Activity එකේ වෙනම හදන්න වෙයි,
    // නැත්නම් දැනට ඕනම කෙනෙක්ට Register වෙන්න පුළුවන් විදියට තියමු).
    fun studentRegExists(regNo: String): Boolean {
        // දැන් අපි Students ලව Firebase එකේ manage කරන නිසා,
        // මෙතනින් TRUE යවමු ලේසියට. (පස්සේ ඕන නම් Firebase check එකක් දාන්න පුළුවන්)
        return true
    }
}