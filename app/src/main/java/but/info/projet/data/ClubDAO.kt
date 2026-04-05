package but.info.projet.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import but.info.projet.utils.DatabaseHelper


class ClubDao(private val dbHelper: DatabaseHelper) {
    fun getAll(): List<Club> {
        return queryClubs("SELECT * FROM club ORDER BY id")
    }

    fun getAllActive(): List<Club> {
        return queryClubs("SELECT * FROM club WHERE active=1 ORDER BY id")
    }

    fun getById(id: Long): Club? {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT * FROM club WHERE id=? LIMIT 1", arrayOf(id.toString())).use { cursor ->
            return if (cursor.moveToFirst()) {
                mapCursorToClub(cursor)
            } else {
                null
            }
        }
    }

    fun getDirty(): List<Club> {
        return queryClubs("SELECT * FROM club WHERE dirty=1 ORDER BY id")
    }

    fun insertAll(clubs: List<Club>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (club in clubs) {
                upsert(db, club, forcedDirty = club.dirty)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertOrUpdateLocal(club: Club) {
        val db = dbHelper.writableDatabase
        upsert(db, club, forcedDirty = 1)
    }

    fun insertOrUpdateFromServer(club: Club) {
        val existing = getById(club.id)
        if (existing?.dirty == 1) {
            return
        }

        val db = dbHelper.writableDatabase
        upsert(db, club, forcedDirty = 0)
    }

    fun markSynced(id: Long): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("dirty", 0)
        }
        return db.update("club", values, "id=?", arrayOf(id.toString()))
    }

    fun markDirty(id: Long): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("dirty", 1)
        }
        return db.update("club", values, "id=?", arrayOf(id.toString()))
    }

    fun update(club: Club) {
        insertOrUpdateLocal(club)
    }

    fun deactivateClub(club: Club) {
        deactivateById(club.id)
    }

    fun reactivateClub(club: Club) {
        reactivateById(club.id)
    }

    fun deactivateById(id: Long): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("active", 0)
            put("dirty", 1)
        }

        return db.update("club", values, "id = ?", arrayOf(id.toString()))
    }

    fun reactivateById(id: Long): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("active", 1)
            put("dirty", 1)
        }

        return db.update("club", values, "id = ?", arrayOf(id.toString()))
    }

    private fun queryClubs(sql: String, args: Array<String>? = null): List<Club> {
        val db = dbHelper.readableDatabase
        val clubs = mutableListOf<Club>()

        db.rawQuery(sql, args).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    clubs.add(mapCursorToClub(cursor))
                } while (cursor.moveToNext())
            }
        }

        return clubs
    }

    private fun mapCursorToClub(cursor: Cursor): Club {
        return Club(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
            active = cursor.getInt(cursor.getColumnIndexOrThrow("active")),
            dirty = cursor.getInt(cursor.getColumnIndexOrThrow("dirty"))
        )
    }

    private fun upsert(db: SQLiteDatabase, club: Club, forcedDirty: Int) {
        val values = ContentValues().apply {
            put("id", club.id)
            put("name", club.name)
            put("address", club.address)
            put("active", club.active)
            put("dirty", forcedDirty)
        }

        db.insertWithOnConflict(
            "club", null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}
