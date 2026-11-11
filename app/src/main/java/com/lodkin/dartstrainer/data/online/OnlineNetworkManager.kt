package com.lodkin.dartstrainer.data.online

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

data class OnlineInvite(
    val id: String,
    val fromUser: String,
    val matchId: String
)

data class GameStateRecord(
    val id: String,
    val matchId: String,
    val gameType: String,
    val stateJson: String,
    val player1Id: String,
    val player2Id: String
)

data class ChatMessageRecord(
    val id: String,
    val fromUser: String,
    val text: String,
    val created: String
)

object OnlineNetworkManager {
    private const val BASE_URL = "http://179.236.147.116:8090"
    private const val PLAYERS_URL = "$BASE_URL/api/collections/players/records"
    private const val INVITES_URL = "$BASE_URL/api/collections/invites/records"
    private const val GAME_STATES_URL = "$BASE_URL/api/collections/game_states/records"
    private const val MESSAGES_URL = "$BASE_URL/api/collections/messages/records"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    // УМНАЯ РЕГИСТРАЦИЯ С ПАРОЛЕМ И ЗАЩИТОЙ ОТ "adm"
    suspend fun registerPlayer(nickname: String, level: Int, password: String): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Защита от запрещенных ников (adm / адм)
            val lower = nickname.lowercase().replace("_", "")
            if (lower.contains("adm") || lower.contains("адм")) {
                return@withContext "Никнейм содержит запрещенные буквы (adm/адм)"
            }

            // 2. Ищем игрока по нику
            val filter = "(nickname='$nickname')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val checkUrl = "$PLAYERS_URL?filter=$encodedFilter"
            
            val checkRequest = Request.Builder().url(checkUrl).get().build()
            var existingPlayer: JSONObject? = null
            var existingId: String? = null

            client.newCall(checkRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val items = JSONObject(body).optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            existingPlayer = items.getJSONObject(0)
                            existingId = existingPlayer?.optString("id")
                        }
                    }
                } else {
                    return@withContext "Ошибка проверки: ${response.code}"
                }
            }

            val jsonBody = JSONObject().apply {
                put("nickname", nickname)
                put("level", level)
                if (password.isNotBlank()) {
                    put("password", password)
                }
            }.toString()

            if (existingId != null) {
                // Игрок существует. Проверяем пароль.
                val dbPassword = existingPlayer?.optString("password") ?: ""
                
                if (dbPassword.isNotBlank()) {
                    // Если в базе пароль есть, он должен совпадать
                    if (dbPassword != password) {
                        return@withContext "Неверный пароль"
                    }
                } else {
                    // Если в базе пароля нет (старый игрок), а мы ввели новый — он запишется через PATCH ниже.
                }

                val updateRequest = Request.Builder().url("$PLAYERS_URL/$existingId").patch(jsonBody.toRequestBody(JSON)).build()
                client.newCall(updateRequest).execute().use { response ->
                    if (response.isSuccessful) return@withContext null
                    else {
                        val err = response.body?.string() ?: ""
                        return@withContext "Ошибка обновления: ${response.code} $err"
                    }
                }
            } else {
                // Игрока нет. Создаем нового.
                val createRequest = Request.Builder().url(PLAYERS_URL).post(jsonBody.toRequestBody(JSON)).build()
                client.newCall(createRequest).execute().use { response ->
                    if (response.isSuccessful) return@withContext null
                    else {
                        val err = response.body?.string() ?: ""
                        return@withContext "Ошибка создания: ${response.code} $err"
                    }
                }
            }
        } catch (e: Exception) {
            return@withContext "Исключение: ${e.message}"
        }
    }

    suspend fun findMatchingOpponents(myNickname: String, levels: List<Int>): List<String> = withContext(Dispatchers.IO) {
        try {
            val levelsFilter = levels.joinToString(" || ") { "level=$it" }
            val filter = "(nickname!='$myNickname' && ($levelsFilter))"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$PLAYERS_URL?filter=$encodedFilter"
            
            val request = Request.Builder().url(url).get().build()
            val result = mutableListOf<String>()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            result.add(items.getJSONObject(i).optString("nickname"))
                        }
                    }
                }
            }
            result
        } catch (e: Exception) { emptyList() }
    }

    suspend fun sendBroadcastInvites(fromUser: String, toUsers: List<String>, matchId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            var allSuccess = true
            for (toUser in toUsers) {
                val jsonBody = JSONObject().apply {
                    put("from_user", fromUser)
                    put("to_user", toUser)
                    put("status", "pending")
                    put("match_id", matchId)
                }.toString()

                val request = Request.Builder().url(INVITES_URL).post(jsonBody.toRequestBody(JSON)).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) allSuccess = false
                }
            }
            allSuccess
        } catch (e: Exception) { false }
    }

    suspend fun sendSingleInvite(fromUser: String, toUser: String): String? = withContext(Dispatchers.IO) {
        try {
            val matchId = UUID.randomUUID().toString()
            val jsonBody = JSONObject().apply {
                put("from_user", fromUser)
                put("to_user", toUser)
                put("status", "pending")
                put("match_id", matchId)
            }.toString()

            val request = Request.Builder().url(INVITES_URL).post(jsonBody.toRequestBody(JSON)).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) return@withContext matchId
                null
            }
        } catch (e: Exception) { null }
    }

    suspend fun checkIncomingInvite(myNickname: String): OnlineInvite? = withContext(Dispatchers.IO) {
        try {
            val filter = "(to_user='$myNickname'%26%26status='pending')"
            val url = "$INVITES_URL?filter=$filter" 
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val inv = items.getJSONObject(0)
                        return@withContext OnlineInvite(inv.getString("id"), inv.getString("from_user"), inv.getString("match_id"))
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun acceptInvite(inviteId: String, matchId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply { put("status", "accepted") }.toString()
            val patchReq = Request.Builder().url("$INVITES_URL/$inviteId").patch(jsonBody.toRequestBody(JSON)).build()
            
            var success = false
            client.newCall(patchReq).execute().use { response -> success = response.isSuccessful }

            if (success) {
                val filter = "(match_id='$matchId'%26%26id!='$inviteId')"
                val url = "$INVITES_URL?filter=$filter"
                val getReq = Request.Builder().url(url).get().build()
                
                client.newCall(getReq).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val items = JSONObject(body).optJSONArray("items")
                            if (items != null) {
                                for (i in 0 until items.length()) {
                                    val otherId = items.getJSONObject(i).optString("id")
                                    val deleteReq = Request.Builder().url("$INVITES_URL/$otherId").delete().build()
                                    client.newCall(deleteReq).execute().close()
                                }
                            }
                        }
                    }
                }
            }
            success
        } catch (e: Exception) { false }
    }

    suspend fun checkIfMatchAccepted(matchId: String): String? = withContext(Dispatchers.IO) {
        try {
            val filter = "(match_id='$matchId'%26%26status='accepted')"
            val url = "$INVITES_URL?filter=$filter"
            val request = Request.Builder().url(url).get().build()
            
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val items = JSONObject(body).optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            return@withContext items.getJSONObject(0).optString("to_user")
                        }
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun cancelMatchSearch(matchId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = "(match_id='$matchId')"
            val url = "$INVITES_URL?filter=$filter"
            val request = Request.Builder().url(url).get().build()
            var deletedAny = false

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val items = JSONObject(body).optJSONArray("items")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val id = items.getJSONObject(i).optString("id")
                                val deleteReq = Request.Builder().url("$INVITES_URL/$id").delete().build()
                                client.newCall(deleteReq).execute().close()
                                deletedAny = true
                            }
                        }
                    }
                }
            }
            deletedAny
        } catch (e: Exception) { false }
    }

    suspend fun deleteInvite(inviteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$INVITES_URL/$inviteId").delete().build()
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun upsertGameState(matchId: String, gameType: String, newJson: String, p1Name: String, p2Name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val filter = "(match_id='$matchId')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$GAME_STATES_URL?filter=$encodedFilter"
            
            var existingId: String? = null
            val getRequest = Request.Builder().url(url).get().build()
            client.newCall(getRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val items = JSONObject(body ?: "").optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        existingId = items.getJSONObject(0).optString("id")
                    }
                }
            }

            if (existingId != null) {
                val jsonBody = JSONObject().apply { put("state_json", newJson) }.toString()
                val patchRequest = Request.Builder().url("$GAME_STATES_URL/$existingId").patch(jsonBody.toRequestBody(JSON)).build()
                client.newCall(patchRequest).execute().use { response -> return@withContext response.isSuccessful }
            } else {
                val jsonBody = JSONObject().apply {
                    put("match_id", matchId)
                    put("game_type", gameType)
                    put("state_json", newJson)
                    put("player1_id", p1Name)
                    put("player2_id", p2Name)
                }.toString()
                val postRequest = Request.Builder().url(GAME_STATES_URL).post(jsonBody.toRequestBody(JSON)).build()
                client.newCall(postRequest).execute().use { response -> return@withContext response.isSuccessful }
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getGameState(matchId: String): GameStateRecord? = withContext(Dispatchers.IO) {
        try {
            val filter = "(match_id='$matchId')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$GAME_STATES_URL?filter=$encodedFilter"
            
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val obj = items.getJSONObject(0)
                        return@withContext GameStateRecord(
                            id = obj.getString("id"),
                            matchId = obj.getString("match_id"),
                            gameType = obj.getString("game_type"),
                            stateJson = obj.getString("state_json"),
                            player1Id = obj.optString("player1_id"),
                            player2Id = obj.optString("player2_id")
                        )
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun deleteGameState(recordId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$GAME_STATES_URL/$recordId").delete().build()
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (e: Exception) { false }
    }

    suspend fun cleanupOldGames(): Boolean = withContext(Dispatchers.IO) {
        try {
            val threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val dateString = dateFormat.format(Date(threeHoursAgo))

            val filter = "(created < '$dateString')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$GAME_STATES_URL?filter=$encodedFilter"
            
            val request = Request.Builder().url(url).get().build()
            var deletedAny = false

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val items = JSONObject(body).optJSONArray("items")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val id = items.getJSONObject(i).optString("id")
                                val deleteReq = Request.Builder().url("$GAME_STATES_URL/$id").delete().build()
                                client.newCall(deleteReq).execute().close()
                                deletedAny = true
                            }
                        }
                    }
                }
            }
            deletedAny
        } catch (e: Exception) { false }
    }

    suspend fun sendChatMessage(matchId: String, fromUser: String, text: String): String? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("match_id", matchId)
                put("from_user", fromUser)
                put("text", text)
            }.toString()

            val request = Request.Builder().url(MESSAGES_URL).post(jsonBody.toRequestBody(JSON)).build()
            client.newCall(request).execute().use { response -> 
                if (response.isSuccessful) null
                else {
                    val err = response.body?.string() ?: ""
                    "Ошибка ${response.code}: $err"
                }
            }
        } catch (e: Exception) { 
            "Исключение: ${e.message}" 
        }
    }

    suspend fun getChatMessages(matchId: String): List<ChatMessageRecord> = withContext(Dispatchers.IO) {
        try {
            val filter = "(match_id='$matchId')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$MESSAGES_URL?filter=$encodedFilter&sort=created"
            
            val request = Request.Builder().url(url).get().build()
            val result = mutableListOf<ChatMessageRecord>()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext emptyList()
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val obj = items.getJSONObject(i)
                            result.add(
                                ChatMessageRecord(
                                    id = obj.getString("id"),
                                    fromUser = obj.getString("from_user"),
                                    text = obj.getString("text"),
                                    created = obj.getString("created")
                                )
                            )
                        }
                    }
                }
            }
            result
        } catch (e: Exception) { emptyList() }
    }
}
