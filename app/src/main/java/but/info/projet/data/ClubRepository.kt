package but.info.projet.data

import android.content.Context
import but.info.projet.utils.DatabaseHelper

class ClubRepository(private val clubDao: ClubDao) {
    constructor(context: Context) : this(
        ClubDao(DatabaseHelper(context.applicationContext))
    )

    fun getAllLocal(): List<Club> {
        return clubDao.getAll()
    }

    fun getAllActiveLocal(): List<Club> {
        return clubDao.getAllActive()
    }

    fun getLocalById(id: Long): Club? {
        return clubDao.getById(id)
    }

    fun saveLocalChanges(club: Club) {
        clubDao.insertOrUpdateLocal(club)
    }

    fun updateLocal(club: Club) {
        clubDao.update(club)
    }

    fun deactivateLocal(id: Long) {
        clubDao.deactivateById(id)
    }

    fun getPendingSync(): List<Club> {
        return clubDao.getDirty()
    }

    fun markSynced(id: Long) {
        clubDao.markSynced(id)
    }

    fun markSynced(ids: Collection<Long>) {
        ids.forEach { markSynced(it) }
    }

    fun mergeFromServer(clubs: List<Club>) {
        clubs.forEach { remoteClub ->
            // Keep local unsynced edits as source of truth until they are pushed.
            clubDao.insertOrUpdateFromServer(remoteClub)
        }
    }
}
