package but.info.projet.utils

import but.info.projet.data.Club
import org.json.JSONArray
import org.json.JSONObject

class JsonParser {
    fun parseClubs(json: String): List<Club> {
        val payload = json.trim()
        if (payload.isEmpty()) {
            return emptyList()
        }

        // API may return either a raw array or an object containing an error/data payload.
        val array = when {
            payload.startsWith("{") -> {
                val obj = JSONObject(payload)
                if (obj.has("erreur")) {
                    throw IllegalStateException(obj.getString("erreur"))
                }
                obj.optJSONArray("data")
                    ?: if (obj.has("club_id") || obj.has("id")) JSONArray().put(obj) else JSONArray()
            }
            else -> JSONArray(payload)
        }

        val clubs = mutableListOf<Club>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            clubs.add(
                Club(
                    id = obj.getLongOrFallback("club_id", "id"),
                    name = obj.getStringOrFallback("club_name", "name"),
                    address = obj.getStringOrFallback("club_address", "address"),
                    active = obj.getIntOrFallback("club_active", "active"),
                    dirty = 0
                )
            )
        }

        return clubs
    }

    fun toUpdatePayload(club: Club): String {
        return JSONObject()
            .put("club_name", club.name)
            .put("club_address", club.address)
            .put("club_active", club.active)
            .toString()
    }

    private fun JSONObject.getLongOrFallback(primary: String, fallback: String): Long {
        return when {
            has(primary) -> getLong(primary)
            has(fallback) -> getLong(fallback)
            else -> throw IllegalStateException("Missing id field ($primary/$fallback)")
        }
    }

    private fun JSONObject.getStringOrFallback(primary: String, fallback: String): String {
        return when {
            has(primary) -> getString(primary)
            has(fallback) -> getString(fallback)
            else -> throw IllegalStateException("Missing name/address field ($primary/$fallback)")
        }
    }

    private fun JSONObject.getIntOrFallback(primary: String, fallback: String): Int {
        return when {
            has(primary) -> getInt(primary)
            has(fallback) -> getInt(fallback)
            else -> 1
        }
    }
}
