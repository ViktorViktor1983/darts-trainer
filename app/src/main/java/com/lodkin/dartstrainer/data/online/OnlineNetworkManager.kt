package com.lodkin.dartstrainer.data.online

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.TimeUnit

// Модель для входящего приглашения
data class OnlineInvite(
    val id: String,
    val fromUser: String,
    val matchId: String
)

object OnlineNetworkManager {
    private const val BASE_URL = "http://179.236.147.116:8090"
    private const val PLAYERS_URL = "$BASE_URL/api/collections/players/records"
    private const val INVITES_URL = "$BASE_URL/api/collections/invites/records"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    suspend fun registerPlayer(nickname: String, level: Int): String? = withContext(Dispatchers.IO) {
        try {
            val filter = "(nickname='$nickname')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val checkUrl = "$PLAYERS_URL?filter=$encodedFilter"
            
            val checkRequest = Request.Builder().url(checkUrl).get().build()
            var existingId: String? = null

            client.newCall(checkRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val items = JSONObject(body).optJSONArray("items")
                        if (items != null && items.length() > 0) {
                            existingId = items.getJSONObject(0).optString("id")
                        }
                    }
                } else {
                    return@withContext "Ошибка проверки: ${response.code}"
                }
            }

            val jsonBody = JSONObject().apply {
                put("nickname", nickname)
                put("level", level)
            }.toString()

            if (existingId != null) {
                val updateRequest = Request.Builder().url("$PLAYERS_URL/$existingId").patch(jsonBody.toRequestBody(JSON)).build()
                client.newCall(updateRequest).execute().use { response ->
                    if (response.isSuccessful) return@withContext null
                    else {
                        val err = response.body?.string() ?: ""
                        return@withContext "Ошибка обновления: ${response.code} $err"
                    }
                }
            } else {
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

    // Получаем список ВСЕХ подходящих соперников
    suspend fun findMatchingOpponents(myNickname: String, levels: List<Int>): List<String> = withContext(Dispatchers.IO) {
        try {
            // Формируем фильтр по уровням (например, level=1 || level=2)
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

    // Отправка массового приглашения всем найденным
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

    // Отправка одиночного приглашения (для друга)
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

    // Проверка входящего приглашения (возвращает первое попавшееся)
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

    // Принять приглашение и удалить остальные из этой же рассылки
    suspend fun acceptInvite(inviteId: String, matchId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Меняем статус текущего приглашения на accepted
            val jsonBody = JSONObject().apply { put("status", "accepted") }.toString()
            val patchReq = Request.Builder().url("$INVITES_URL/$inviteId").patch(jsonBody.toRequestBody(JSON)).build()
            
            var success = false
            client.newCall(patchReq).execute().use { response -> success = response.isSuccessful }

            if (success) {
                // 2. Ищем все остальные приглашения с таким же match_id и удаляем их
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

    // Проверить, не принял ли кто-то из рассылки наше приглашение
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

    // Удалить все приглашения по matchId (при отмене)
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
}
