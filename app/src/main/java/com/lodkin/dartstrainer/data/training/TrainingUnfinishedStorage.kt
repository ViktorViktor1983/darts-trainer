package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище ID незавершённых тренировочных партий.
 * Позволяет отделить тренировочные партии от свободных.
 */
object TrainingUnfinishedStorage {

    private const val PREFS_NAME = "training_unfinished_prefs"
    private const val KEY_501_IDS = "unfinished_501_ids"
    private const val KEY_CRICKET_IDS = "unfinished_cricket_ids"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── 501 ──

    fun addUnfinished501(context: Context, id: Long) {
        val current = getUnfinished501(context).toMutableSet()
        current.add(id)
        prefs(context).edit().putStringSet(KEY_501_IDS, current.map { it.toString() }.toSet()).apply()
    }

    fun removeUnfinished501(context: Context, id: Long) {
        val current = getUnfinished501(context).toMutableSet()
        current.remove(id)
        prefs(context).edit().putStringSet(KEY_501_IDS, current.map { it.toString() }.toSet()).apply()
    }

    fun getUnfinished501(context: Context): Set<Long> {
        val raw = prefs(context).getStringSet(KEY_501_IDS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun clear501(context: Context) {
        prefs(context).edit().remove(KEY_501_IDS).apply()
    }

    // ── Крикет ──

    fun addUnfinishedCricket(context: Context, id: Long) {
        val current = getUnfinishedCricket(context).toMutableSet()
        current.add(id)
        prefs(context).edit().putStringSet(KEY_CRICKET_IDS, current.map { it.toString() }.toSet()).apply()
    }

    fun removeUnfinishedCricket(context: Context, id: Long) {
        val current = getUnfinishedCricket(context).toMutableSet()
        current.remove(id)
        prefs(context).edit().putStringSet(KEY_CRICKET_IDS, current.map { it.toString() }.toSet()).apply()
    }

    fun getUnfinishedCricket(context: Context): Set<Long> {
        val raw = prefs(context).getStringSet(KEY_CRICKET_IDS, emptySet()) ?: emptySet()
        return raw.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun clearCricket(context: Context) {
        prefs(context).edit().remove(KEY_CRICKET_IDS).apply()
    }
}
