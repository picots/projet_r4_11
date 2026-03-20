package but.info.projet.data

import java.net.HttpURLConnection
import java.net.URL

class ClubAPI {

    private val baseUrl = "https://localhost:8000/api"

    fun getAllClubs(): String {
        val url = URL("$baseUrl/clubs")
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
}