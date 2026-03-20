package but.info.projet.data

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
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active"))
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
                    active = cursor.getInt(cursor.getColumnIndexOrThrow("active"))
                )
                clubs.add(club)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return clubs
    }

    fun insertAll(clubs: List<Club>) {

    }

    fun update(club: Club) {

    }
}