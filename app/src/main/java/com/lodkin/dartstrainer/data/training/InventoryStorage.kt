package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище инвентаря игрока (гантели, резинка).
 * Используется для подбора силовых упражнений.
 */
object InventoryStorage {
    private const val PREFS_NAME = "inventory_prefs"
    private const val KEY_HAS_DUMBBELLS = "has_dumbbells"
    private const val KEY_HAS_RESISTANCE_BAND = "has_resistance_band"
    private const val KEY_STRENGTH_INDEX = "strength_index"
    private const val KEY_PSYCHOLOGY_INDEX = "psychology_index"

    fun hasDumbbells(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_DUMBBELLS, false)
    }

    fun setHasDumbbells(context: Context, has: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HAS_DUMBBELLS, has).apply()
    }

    fun hasResistanceBand(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_RESISTANCE_BAND, false)
    }

    fun setHasResistanceBand(context: Context, has: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_HAS_RESISTANCE_BAND, has).apply()
    }

    /**
     * Индекс для силовых упражнений (чтобы брать по 3 и идти по кругу).
     */
    fun getStrengthIndex(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_STRENGTH_INDEX, 0)
    }

    fun incrementStrengthIndex(context: Context) {
        val current = getStrengthIndex(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_STRENGTH_INDEX, current + 3).apply()
    }

    /**
     * Индекс для психологических упражнений (по аналогии).
     */
    fun getPsychologyIndex(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PSYCHOLOGY_INDEX, 0)
    }

    fun incrementPsychologyIndex(context: Context) {
        val current = getPsychologyIndex(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_PSYCHOLOGY_INDEX, current + 3).apply()
    }

    fun resetIndexes(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_STRENGTH_INDEX, 0)
            .putInt(KEY_PSYCHOLOGY_INDEX, 0)
            .apply()
    }
}
