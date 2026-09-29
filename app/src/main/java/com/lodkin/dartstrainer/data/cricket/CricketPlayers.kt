package com.lodkin.dartstrainer.data.cricket

import android.content.Context

// 16 ботов с именами и уровнями
data class CricketBot(
    val id: Int,
    val name: String,
    val description: String,
    val averageMin: Int,
    val averageMax: Int
)

val CRICKET_BOTS: List<CricketBot> = listOf(
    CricketBot(1, "Новичок", "Начинающий игрок", 15, 20),
    CricketBot(2, "Ученик", "Осваивается", 18, 22),
    CricketBot(3, "Любитель", "Играет для себя", 20, 25),
    CricketBot(4, "Уверенный", "Стабильный любитель", 22, 28),
    CricketBot(5, "Опытный", "Опытный любитель", 25, 30),
    CricketBot(6, "Практик", "Много тренируется", 27, 33),
    CricketBot(7, "Разрядник", "III разряд", 30, 36),
    CricketBot(8, "Турнирный", "II разряд", 33, 40),
    CricketBot(9, "Сильный", "I разряд", 36, 44),
    CricketBot(10, "Крепкий", "Уверенный I разряд", 40, 48),
    CricketBot(11, "КМС", "Кандидат в мастера", 45, 55),
    CricketBot(12, "Почти мастер", "На подступах к МС", 50, 60),
    CricketBot(13, "Мастер", "Мастер спорта", 55, 70),
    CricketBot(14, "Чемпион", "Победитель турниров", 60, 80),
    CricketBot(15, "Профи", "Высокий уровень", 70, 90),
    CricketBot(16, "Легенда", "Мировой уровень", 85, 110)
)

// Хранилище имён игроков
object PlayerNamesStorage {
    private const val PREFS = "player_names"
    private const val KEY_NAMES = "names_list"
    private const val KEY_LAST_PLAYER1 = "last_player1"
    private const val KEY_LAST_PLAYER2 = "last_player2"
    private const val SEPARATOR = "|||"

    fun getSavedNames(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NAMES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    fun saveName(context: Context, name: String) {
        if (name.isBlank()) return
        val trimmed = name.trim()
        val current = getSavedNames(context).toMutableList()
        // Убираем дубликат
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        // Добавляем в начало
        current.add(0, trimmed)
        // Ограничиваем 20 именами
        val limited = current.take(20)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_NAMES, limited.joinToString(SEPARATOR)).apply()
    }

    fun getLastPlayer1(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_PLAYER1, "") ?: ""

    fun setLastPlayer1(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LAST_PLAYER1, name).apply()
    }

    fun getLastPlayer2(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LAST_PLAYER2, "") ?: ""

    fun setLastPlayer2(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_LAST_PLAYER2, name).apply()
    }
}
