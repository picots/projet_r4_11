package but.info.projet.utils

import androidx.collection.emptyObjectList
import but.info.projet.data.Club
import org.json.JSONArray
import org.json.JSONObject

class JsonParser {
    fun parseClubs(json: String): List<Club> {
        if(json.contains("erreur", true))
            throw Exception(JSONObject(json).getString("erreur"))
        val array = JSONArray(json)
        var obj: JSONObject
        var club: Club
        val clubs: MutableList<Club> = mutableListOf()
        for (i in 0..array.length()) {
            obj = array.getJSONObject(i)
            club = Club(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                address = obj.getString("address"),
                active = obj.getInt("active")
            )
            clubs.plus(club)
        }
        return clubs
    }
}