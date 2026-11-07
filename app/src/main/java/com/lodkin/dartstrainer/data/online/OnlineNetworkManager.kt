package com.lodkin.dartstrainer.data.online

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object OnlineNetworkManager {
    // Адрес нашего сервера PocketBase
    private const val BASE_URL = "http://179.236.147.116:8090"
    private const val PLAYERS_URL = "$BASE_URL/api/collections/players/records"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Регистрирует игрока в базе данных (отправляет ник и уровень).
     */
    suspend fun registerPlayer(nickname: String, level: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("nickname", nickname)
                put("level", level)
            }.toString()

            val requestBody = jsonBody.toRequestBody(JSON)
            val request = Request.Builder()
                .url(PLAYERS_URL)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Ищет случайного соперника в базе данных (кроме самого себя).
     * Возвращает никнейм найденного игрока или null, если никого нет.
     */
    suspend fun findOpponent(myNickname: String): String? = withContext(Dispatchers.IO) {
        try {
            // Формируем запрос: ищем всех, чей ник НЕ равен моему
            val url = "$PLAYERS_URL?filter=(nickname!='$myNickname')"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@withContext null
                    val jsonObject = JSONObject(responseBody)
                    val items = jsonObject.optJSONArray("items")
                    
                    if (items != null && items.length() > 0) {
                        // Если кто-то найден, берем первого из списка и возвращаем его ник
                        val opponent = items.getJSONObject(0)
                        return@withContext opponent.optString("nickname")
                    }
                }
            }
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
