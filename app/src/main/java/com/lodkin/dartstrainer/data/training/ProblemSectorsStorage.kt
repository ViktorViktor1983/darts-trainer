package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище для пометок о пройденных проблемных секторах и их последних результатах.
 * Использует SharedPreferences.
 */
object ProblemSectorsStorage {

    private const val PREFS_NAME = "problem_sectors_prefs"
    private const val KEY_COMPLETED = "completed_sectors"
    private const val KEY_RESULTS = "session_results" // Формат: "D1:hits/total|D15:hits/total"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Получить список пройденных целей (например, ["D5", "T19"]) */
    fun getCompleted(context: Context): Set<String> {
        val raw = prefs(context).getStringSet(KEY_COMPLETED, emptySet()) ?: emptySet()
        return raw.toSet()
    }

    /** Добавить цель в пройденные */
    fun markCompleted(context: Context, target: String) {
        val current = getCompleted(context).toMutableSet()
        current.add(target)
        prefs(context).edit().putStringSet(KEY_COMPLETED, current).apply()
    }

    /** Сохранить результат отработки (например, "D1", 12, 15) */
    fun saveSessionResult(context: Context, target: String, hits: Int, total: Int) {
        val current = getSessionResults(context).toMutableMap()
        current[target] = "$hits/$total"
        val serialized = current.entries.joinToString("|") { "${it.key}:${it.value}" }
        prefs(context).edit().putString(KEY_RESULTS, serialized).apply()
    }

    /** Получить результаты всех отработок (Map: "D1" -> "12/15") */
    fun getSessionResults(context: Context): Map<String, String> {
        val raw = prefs(context).getString(KEY_RESULTS, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split("|").mapNotNull { entry ->
            val parts = entry.split(":", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    /** Очистить все пометки и результаты (вызывается при сдаче экзамена) */
    fun clear(context: Context) {
        prefs(context).edit()
            .remove(KEY_COMPLETED)
            .remove(KEY_RESULTS)
            .apply()
    }
}
