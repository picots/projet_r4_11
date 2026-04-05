package but.info.projet.data

import android.util.Log
import but.info.projet.utils.JsonParser
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class ClubAPI {
    private val hostUrl = "https://sae3g5.skopee.fr"
    private val baseUrl = "$hostUrl/api/clubs"
    private val parser = JsonParser()

    private var authorizationHeader: String? = null

    fun hasAuthentication(): Boolean {
        return !authorizationHeader.isNullOrBlank()
    }

    fun authenticate(identifier: String, password: String): Boolean {
        if (identifier.isBlank() || password.isBlank()) {
            return false
        }
        clearAuthentication()

        val connection = createConnection("$hostUrl/api/login")
        return try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")

            val payload = JSONObject()
                .put("username", identifier)
                .put("password", password)
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(StandardCharsets.UTF_8))
                output.flush()
            }

            val body = readResponseBody(connection)
            if (connection.responseCode !in 200..299) {
                Log.e(TAG, "POST /api/login failed code=${connection.responseCode} body=$body")
                return false
            }

            val json = JSONObject(body)
            val accessToken = json.optString("access_token")
            val tokenType = json.optString("token_type", "Bearer")
            if (accessToken.isBlank()) {
                Log.e(TAG, "POST /api/login missing token")
                return false
            }

            authorizationHeader = "$tokenType $accessToken"
            true
        } catch (e: Exception) {
            Log.e(TAG, "POST /api/login failed", e)
            false
        } finally {
            connection.disconnect()
        }
    }

    fun clearAuthentication() {
        authorizationHeader = null
    }

    fun getAllClubs(): List<Club> {
        val connection = createConnection(baseUrl)
        return try {
            connection.requestMethod = "GET"
            if (connection.responseCode !in 200..299) {
                Log.e(TAG, "GET /clubs failed with code ${connection.responseCode}")
                return emptyList()
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parser.parseClubs(response)
        } catch (e: Exception) {
            Log.e(TAG, "GET /clubs failed", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    fun updateClub(club: Club): Boolean {
        return putClub(club.id, parser.toUpdatePayload(club))
    }

    fun deactivateClub(id: Long): Boolean {
        return putClub(id, """{"club_active":0}""")
    }

    fun getActives(): List<Club> {
        return getAllClubs().filter { it.active == 1 }
    }

    private fun putClub(id: Long, payload: String): Boolean {
        val connection = createConnection("$baseUrl/$id")
        return try {
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            applyAuthenticationHeaders(connection)
            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
                output.flush()
            }

            val code = connection.responseCode
            if (code in 200..299) {
                true
            } else {
                if (code == 401) {
                    clearAuthentication()
                }
                Log.e(TAG, "PUT /clubs/$id failed with code $code body=${readResponseBody(connection)}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "PUT /clubs/$id failed", e)
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun createConnection(url: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
        }
    }

    private fun applyAuthenticationHeaders(connection: HttpURLConnection) {
        authorizationHeader?.let { connection.setRequestProperty("Authorization", it) }
    }

    private fun readResponseBody(connection: HttpURLConnection): String {
        return try {
            val stream = connection.errorStream ?: connection.inputStream ?: return ""
            stream.bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        private const val TAG = "ClubAPI"
    }
}
