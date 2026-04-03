package but.info.projet.data

import android.util.Log
import but.info.projet.utils.JsonParser
import java.net.HttpURLConnection
import java.net.URL

class ClubAPI {

    private val baseUrl =
        "https://sae3g5.skopee.fr/api/clubs" //https://g5-devc3.unicaen.fr/api/clubs

    private val parser = JsonParser()

    private var clubs : List<Club> = emptyList()


    fun getAllClubs(): List<Club> {
        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use {
                    clubs = parser.parseClubs(it.readText() )
                }
            } else {
                Log.e("API", "code de réponse : ${connection.responseCode}")
            }
        } catch (e: Exception) {
           Log.e("API", e.message!!)
        } finally {
            connection.disconnect()
        }
        return clubs
    }

    fun deactivateClub(id: Long): Boolean {
        val url = URL("$baseUrl/$id")
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "PUT"

            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonBody = """{"club_active": 0}"""

            connection.outputStream.use { output ->
                val input = jsonBody.toByteArray(Charsets.UTF_8)
                output.write(input)
                output.flush()
            }

            val responseCode = connection.responseCode

            if (responseCode in 200..299) {
                true
            } else {
                Log.e("API", "Erreur API : $responseCode")
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.disconnect()
        }
    }

    fun getAllActives() = clubs.filter { club -> club.active == 1 }
}