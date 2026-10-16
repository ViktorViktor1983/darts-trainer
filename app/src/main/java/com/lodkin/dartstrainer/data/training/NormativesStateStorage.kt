package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище прогресса сдачи нормативов.
 *
 * Правила:
 *   • Прогресс сохраняется после каждого шага/лега.
 *   • Если прошло больше 24 часов — сбрасывается.
 *   • При возврате в тренировку предлагается продолжить.
 *
 * Прогресс включает:
 *   • completedSteps — id пройденных шагов (game501, cricket, sector20, biground, scoreset).
 *   • game501Levels / cricketLevels — уровни ботов на каждый лег (по 5).
 *   • game501Results / cricketResults — результаты сыгранных легов (true = победа).
 */
object NormativesStateStorage {

    private const val PREFS = "normatives_state"

    private const val KEY_ACTIVE = "active"
    private const val KEY_COMPLETED = "completed_steps"
    private const val KEY_GAME501_LEVELS = "g501_levels"
    private const val KEY_GAME501_RESULTS = "g501_results"
    private const val KEY_CRICKET_LEVELS = "cricket_levels"
    private const val KEY_CRICKET_RESULTS = "cricket_results"
    private const val KEY_LAST_UPDATE = "last_update"

    private const val MAX_IDLE_MILLIS = 24L * 60 * 60 * 1000  // 24 часа
    private const val SEP = ","

    data class Snapshot(
        val completedSteps: Set<String>,
        val game501Levels: List<Int>,
        val game501Results: List<Boolean>,
        val cricketLevels: List<Int>,
        val cricketResults: List<Boolean>,
        val lastUpdateMillis: Long
    )

    fun save(
        context: Context,
        completedSteps: Set<String>,
        game501Levels: List<Int>,
        game501Results: List<Boolean>,
        cricketLevels: List<Int>,
        cricketResults: List<Boolean>
    ) {
        prefs(context).edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_COMPLETED, completedSteps.joinToString(SEP))
            .putString(KEY_GAME501_LEVELS, game501Levels.joinToString(SEP))
            .putString(KEY_GAME501_RESULTS, game501Results.joinToString(SEP) { if (it) "1" else "0" })
            .putString(KEY_CRICKET_LEVELS, cricketLevels.joinToString(SEP))
            .putString(KEY_CRICKET_RESULTS, cricketResults.joinToString(SEP) { if (it) "1" else "0" })
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): Snapshot? {
        val p = prefs(context)
        if (!p.getBoolean(KEY_ACTIVE, false)) return null

        val lastUpdate = p.getLong(KEY_LAST_UPDATE, 0L)
        if (lastUpdate <= 0L) return null

        val completed = parseStringSet(p.getString(KEY_COMPLETED, "") ?: "")
        val g501Levels = parseIntList(p.getString(KEY_GAME501_LEVELS, "") ?: "")
        val g501Results = parseBoolList(p.getString(KEY_GAME501_RESULTS, "") ?: "")
        val cricketLevels = parseIntList(p.getString(KEY_CRICKET_LEVELS, "") ?: "")
        val cricketResults = parseBoolList(p.getString(KEY_CRICKET_RESULTS, "") ?: "")

        return Snapshot(
            completedSteps = completed,
            game501Levels = g501Levels,
            game501Results = g501Results,
            cricketLevels = cricketLevels,
            cricketResults = cricketResults,
            lastUpdateMillis = lastUpdate
        )
    }

    fun isExpired(snapshot: Snapshot): Boolean {
        val now = System.currentTimeMillis()
        return (now - snapshot.lastUpdateMillis) > MAX_IDLE_MILLIS
    }

    fun minutesSinceLastUpdate(context: Context): Int {
        val last = prefs(context).getLong(KEY_LAST_UPDATE, 0L)
        if (last <= 0L) return 0
        val diff = System.currentTimeMillis() - last
        return (diff / 60000L).toInt()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // ─────────────────────────────────────────────
    // Парсеры
    // ─────────────────────────────────────────────

    private fun parseStringSet(raw: String): Set<String> {
        if (raw.isBlank()) return emptySet()
        return raw.split(SEP).map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    private fun parseIntList(raw: String): List<Int> {
        if (raw.isBlank()) return emptyList()
        return raw.split(SEP).mapNotNull { it.trim().toIntOrNull() }
    }

    private fun parseBoolList(raw: String): List<Boolean> {
        if (raw.isBlank()) return emptyList()
        return raw.split(SEP).mapNotNull {
            when (it.trim()) {
                "1" -> true
                "0" -> false
                else -> null
            }
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
