package but.info.projet.data

import but.info.projet.utils.JsonParser

class ClubRepository {
    private val api = ClubAPI()
    private val parser = JsonParser()

    fun getClubs(): List<Club> {
        val json = api.getAllClubs()
        return parser.parseClubs(json)
    }

    fun deactivateClub(id: Long) {
        api.deactivateClub(id)
    }


}