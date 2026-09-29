package com.lodkin.dartstrainer.data.cricket

import android.content.Context

// 16 ботов с уровнями для крикета
// averageMin/Max теперь = среднее количество МЕТОК за подход (0..9)
data class CricketBot(
    val id: Int,
    val name: String,
    val description: String,
    val averageMin: Int,      // десятые доли меток (например 8 = 0.8)
    val averageMax: Int       // десятые доли меток (например 12 = 1.2)
) {
    val averageMinLabel: String get() = "${averageMin / 10}.${averageMin % 10}"
    val averageMaxLabel: String get() = "${averageMax / 10}.${averageMax % 10}"
}

val CRICKET_BOTS: List<CricketBot> = listOf(
    CricketBot(1, "Новичок", "Начинающий", 8, 12),
    CricketBot(2, "Ученик", "Осваивается", 12, 16),
    CricketBot(3, "Любитель", "Играет для себя", 16, 20),
    CricketBot(4, "Уверенный", "Стабильный любитель", 20, 24),
    CricketBot(5, "Опытный", "Опытный любитель", 24, 28),
    CricketBot(6, "Практик", "Много тренируется", 28, 31),
    CricketBot(7, "Разрядник", "III разряд", 31, 34),
    CricketBot(8, "Турнирный", "II разряд", 34, 37),
    CricketBot(9, "Сильный", "I разряд", 37, 40),
    CricketBot(10, "Крепкий", "Уверенный I разряд", 40, 43),
    CricketBot(11, "КМС", "Кандидат в мастера", 43, 46),
    CricketBot(12, "Почти мастер", "На подступах к МС", 46, 48),
    CricketBot(13, "Мастер", "Мастер спорта", 48, 50),
    CricketBot(14, "Чемпион", "Победитель турниров", 50, 53),
    CricketBot(15, "Профи", "Высокий уровень", 53, 57),
    CricketBot(16, "Легенда", "Мировой уровень", 57, 70)
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
        current.removeAll { it.equals(trimmed, ignoreCase = true) }
        current.add(0, trimmed)
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
