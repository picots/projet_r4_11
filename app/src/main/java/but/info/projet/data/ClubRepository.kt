package but.info.projet.data

import android.content.Context
import but.info.projet.utils.DatabaseHelper

class ClubRepository(
    private val clubDao: ClubDao,
    private val clubApi: ClubAPI = ClubAPI()
) {
    constructor(context: Context) : this(
        ClubDao(DatabaseHelper(context.applicationContext)),
        ClubAPI()
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

    fun hasAuthentication(): Boolean {
        return clubApi.hasAuthentication()
    }

    fun authenticate(identifier: String, password: String): Boolean {
        return clubApi.authenticate(identifier, password)
    }

    fun getAllRemote(): List<Club> {
        return clubApi.getAllClubs()
    }

    fun mergeFromServer(clubs: List<Club>) {
        clubs.forEach { remoteClub ->
            // Keep local unsynced edits as source of truth until they are pushed.
            clubDao.insertOrUpdateFromServer(remoteClub)
        }
    }

    fun pushPendingChanges(): List<Long> {
        val syncedIds = mutableListOf<Long>()
        getPendingSync().forEach { localClub ->
            if (clubApi.updateClub(localClub)) {
                markSynced(localClub.id)
                syncedIds.add(localClub.id)
            }
        }
        return syncedIds
    }

    fun refreshFromRemote(): List<Club> {
        val clubs = getAllRemote()
        mergeFromServer(clubs)
        return getAllLocal()
    }

    fun synchronize(): List<Club> {
        pushPendingChanges()
        return refreshFromRemote()
    }

    // Compatibility with the previous API-only repository contract.
    fun getClubs(): List<Club> = synchronize()

    fun deactivateClub(id: Long): Boolean {
        deactivateLocal(id)
        val success = clubApi.deactivateClub(id)
        if (success) {
            markSynced(id)
        }
        return success
    }

    fun getActives(): List<Club> = getAllActiveLocal()
}
