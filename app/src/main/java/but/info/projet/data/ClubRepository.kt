package but.info.projet.data

import but.info.projet.utils.JsonParser

class ClubRepository {
    private val api = ClubAPI()

    fun getClubs(): List<Club> = api.getAllClubs()

    fun deactivateClub(id: Long) = api.deactivateClub(id)

    fun getActives(): List<Club> = api.getActives()

}