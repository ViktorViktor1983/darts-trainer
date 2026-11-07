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

    // Настраиваем клиент (даем ему 10 секунд на ответ)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Регистрирует игрока в базе данных (или обновляет его данные)
     * Возвращает true, если всё прошло успешно.
     */
    suspend fun registerPlayer(nickname: String, level: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            // Формируем данные для отправки
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
}
