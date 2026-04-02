package but.info.projet.data

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class ClubAPI {

    private val baseUrl = "https://sae3g5.skopee.fr/api/clubs" //https://g5-devc3.unicaen.fr/api/clubs

    fun getAllClubs(): String {
        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = "GET"
            if (connection.responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                """{ "erreur" : "Erreur serveur (HTTP ${connection.responseCode})" }"""
            }
        } catch (e: Exception) {
            val message = e.message?.replace("\"", "\\\"") ?: "Erreur inconnue"
            """{ "erreur": "$message" }"""
        } finally {
            connection.disconnect()
        }
    }

    fun deactivateClub(id: Long): Boolean {
        val url = URL(baseUrl + "clubs/$id")
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
                Log.e("API","Erreur API : $responseCode")
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            connection.disconnect()
        }
    }
}