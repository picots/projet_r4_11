package but.info.projet.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import but.info.projet.utils.DatabaseHelper


class ClubDao(private val dbHelper: DatabaseHelper) {
    fun getAll(): List<Club> {
        val db = dbHelper.readableDatabase
        val clubs = mutableListOf<Club>()

        val cursor = db.rawQuery("SELECT * FROM club", null)

        if (cursor.moveToFirst()) {
            do {
                val club = Club(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active")),
                    dirty = cursor.getInt(cursor.getColumnIndexOrThrow("dirty"))
                )
                clubs.add(club)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return clubs
    }

    fun getAllActive(): List<Club> {
        val db = dbHelper.readableDatabase
        val clubs = mutableListOf<Club>()

        val cursor = db.rawQuery("SELECT * FROM club WHERE active=1", null)

        if (cursor.moveToFirst()) {
            do {
                val club = Club(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active")),
                    dirty = cursor.getInt(cursor.getColumnIndexOrThrow("dirty"))
                )
                clubs.add(club)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return clubs
    }

    fun insertAll(clubs: List<Club>) {
        val db = dbHelper.writableDatabase

        for (club in clubs) {
            val values = ContentValues().apply {
                put("id", club.id)
                put("name", club.name)
                put("address", club.address)
                put("active", club.active)
                put("dirty", club.dirty)
            }

            db.insertWithOnConflict(
                "club",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
        }
    }

    fun update(club: Club) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("name", club.name)
            put("address", club.address)
            put("active", club.active)
            put("dirty", 1)
        }

        db.update(
            "club",
            values,
            "id = ?",
            arrayOf(club.id.toString())
        )
    }
}