package but.info.projet.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "app.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE club (
                id INTEGER PRIMARY KEY,
                name TEXT,
                address TEXT,
                active INTEGER DEFAULT 1,
                dirty INTEGER DEFAULT 0
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS club")
        onCreate(db)
    }
}