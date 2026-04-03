package but.info.projet.utils

import android.util.Log
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
        for (i in 0..<array.length()) {
            obj = array.getJSONObject(i)
            club = Club(
                id = obj.getLong("club_id"),
                name = obj.getString("club_name"),
                address = obj.getString("club_address"),
                active = obj.getInt("club_active")
            )
            clubs.add(club)
        }
        return clubs
    }
}