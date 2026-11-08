package com.lodkin.dartstrainer.data.online

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

// Модель для входящего приглашения
data class OnlineInvite(
    val id: String,
    val fromUser: String
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

    // Возвращает null при успехе, или String с текстом ошибки
    suspend fun registerPlayer(nickname: String, level: Int): String? = withContext(Dispatchers.IO) {
        try {
            // 1. Проверяем, есть ли игрок (с кодированием URL)
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
                // 2. Игрок уже есть -> Обновляем данные
                val updateRequest = Request.Builder()
                    .url("$PLAYERS_URL/$existingId")
                    .patch(jsonBody.toRequestBody(JSON))
                    .build()
                client.newCall(updateRequest).execute().use { response ->
                    if (response.isSuccessful) return@withContext null
                    else {
                        val err = response.body?.string() ?: ""
                        return@withContext "Ошибка обновления: ${response.code} $err"
                    }
                }
            } else {
                // 3. Игрока нет -> Создаем нового
                val createRequest = Request.Builder()
                    .url(PLAYERS_URL)
                    .post(jsonBody.toRequestBody(JSON))
                    .build()
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

    suspend fun sendInvite(fromUser: String, toUser: String): String? = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("from_user", fromUser)
                put("to_user", toUser)
                put("status", "pending")
            }.toString()

            val request = Request.Builder().url(INVITES_URL).post(jsonBody.toRequestBody(JSON)).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) return@withContext JSONObject(body).optString("id")
                }
                null
            }
        } catch (e: Exception) { null }
    }

    suspend fun findOpponent(myNickname: String): String? = withContext(Dispatchers.IO) {
        try {
            val filter = "(nickname!='$myNickname')"
            val encodedFilter = URLEncoder.encode(filter, "UTF-8")
            val url = "$PLAYERS_URL?filter=$encodedFilter"
            
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        return@withContext items.getJSONObject(0).optString("nickname")
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun checkIncomingInvite(myNickname: String): OnlineInvite? = withContext(Dispatchers.IO) {
        try {
            val filter = "(to_user='$myNickname'%26%26status='pending')"
            // Уже содержит %26 (это &), но на всякий случай кодируем целиком, кроме спецсимволов
            val url = "$INVITES_URL?filter=$filter" 
            val request = Request.Builder().url(url).get().build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val items = JSONObject(body).optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val inv = items.getJSONObject(0)
                        return@withContext OnlineInvite(inv.getString("id"), inv.getString("from_user"))
                    }
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun acceptInvite(inviteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply { put("status", "accepted") }.toString()
            val request = Request.Builder().url("$INVITES_URL/$inviteId").patch(jsonBody.toRequestBody(JSON)).build()
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (e: Exception) { false }
    }

    suspend fun checkMyInviteStatus(inviteId: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$INVITES_URL/$inviteId").get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) return@withContext JSONObject(body).optString("status")
                }
            }
            null
        } catch (e: Exception) { null }
    }

    suspend fun deleteInvite(inviteId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$INVITES_URL/$inviteId").delete().build()
            client.newCall(request).execute().use { response -> response.isSuccessful }
        } catch (e: Exception) { false }
    }
}
