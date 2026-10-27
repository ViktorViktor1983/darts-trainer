package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище для пометок о пройденных проблемных секторах.
 * Использует SharedPreferences.
 */
object ProblemSectorsStorage {

    private const val PREFS_NAME = "problem_sectors_prefs"
    private const val KEY_COMPLETED = "completed_sectors"

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

    /** Очистить все пометки (вызывается при сдаче экзамена) */
    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_COMPLETED).apply()
    }
}
