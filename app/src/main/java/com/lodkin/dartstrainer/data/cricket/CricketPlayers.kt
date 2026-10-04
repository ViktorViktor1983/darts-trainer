package com.lodkin.dartstrainer.data.cricket

import android.content.Context

// 16 ботов с уровнями.
//
// averageMin/Max — для КРИКЕТА: среднее количество МЕТОК за подход
//                  в десятых долях (8 = 0.8, 12 = 1.2).
// pprMin/Max     — для x01: средний набор очков (PPR).
// dblMin/Max     — для x01: точность удвоений в процентах.
data class CricketBot(
    val id: Int,
    val name: String,
    val description: String,
    val averageMin: Int,      // десятые доли меток (например 8 = 0.8)
    val averageMax: Int,      // десятые доли меток (например 12 = 1.2)
    val pprMin: Int = 0,      // PPR x01, минимум
    val pprMax: Int = 0,      // PPR x01, максимум
    val dblMin: Double = 0.0, // точность удвоений x01, %
    val dblMax: Double = 0.0  // точность удвоений x01, %
) {
    val averageMinLabel: String get() = "${averageMin / 10}.${averageMin % 10}"
    val averageMaxLabel: String get() = "${averageMax / 10}.${averageMax % 10}"

    // Подпись для выбора бота в x01
    val pprLabel: String
        get() = "ср. $pprMin–$pprMax · удв. ${dblMin.toInt()}–${dblMax.toInt()}%"
}

val CRICKET_BOTS: List<CricketBot> = listOf(
    CricketBot(1,  "Новичок",       "Начинающий",          8,  12,  22, 26,   5.0,  8.0),
    CricketBot(2,  "Ученик",        "Осваивается",         12, 16,  28, 32,   8.0, 10.0),
    CricketBot(3,  "Любитель",      "Играет для себя",     16, 20,  33, 37,   9.0, 12.0),
    CricketBot(4,  "Уверенный",     "Стабильный любитель", 20, 24,  38, 42,  11.0, 14.0),
    CricketBot(5,  "Опытный",       "Опытный любитель",    24, 28,  43, 47,  14.0, 17.0),
    CricketBot(6,  "Практик",       "Много тренируется",   28, 31,  49, 53,  16.0, 19.0),
    CricketBot(7,  "Разрядник",     "III разряд",          31, 34,  52, 55,  17.0, 20.0),
    CricketBot(8,  "Турнирный",     "II разряд",           34, 37,  55, 59,  18.0, 21.0),
    CricketBot(9,  "Сильный",       "I разряд",            37, 40,  61, 66,  21.0, 24.0),
    CricketBot(10, "Крепкий",       "Уверенный I разряд",  40, 43,  66, 71,  24.0, 27.0),
    CricketBot(11, "КМС",           "Кандидат в мастера",  43, 46,  71, 77,  26.0, 29.0),
    CricketBot(12, "Почти мастер",  "На подступах к МС",   46, 48,  80, 86,  29.0, 33.0),
    CricketBot(13, "Мастер",        "Мастер спорта",       48, 50,  87, 93,  32.0, 35.0),
    CricketBot(14, "Чемпион",       "Победитель турниров", 50, 53,  93, 98,  36.0, 40.0),
    CricketBot(15, "Профи",         "Высокий уровень",     53, 57,  99, 103, 40.0, 44.0),
    CricketBot(16, "Легенда",       "Мировой уровень",     57, 70, 104, 110, 44.0, 48.0)
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
