package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище настроек блоков тренировки.
 *
 * Игрок может отключать блоки, которые ему не нужны.
 * Основная часть — обязательный блок, её отключить нельзя.
 */
object TrainingBlocksStorage {

    private const val PREFS = "training_blocks"
    private const val KEY_WARMUP = "block_warmup"
    private const val KEY_MAIN = "block_main"
    private const val KEY_PSYCHOLOGY = "block_psychology"
    private const val KEY_STRENGTH = "block_strength"

    // ── Разминка ──
    fun isWarmupEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WARMUP, true)

    fun setWarmupEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_WARMUP, value).apply()
    }

    // ── Основная часть (всегда включена) ──
    fun isMainEnabled(context: Context): Boolean = true

    // ── Психология ──
    fun isPsychologyEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PSYCHOLOGY, true)

    fun setPsychologyEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_PSYCHOLOGY, value).apply()
    }

    // ── Силовая ──
    fun isStrengthEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_STRENGTH, true)

    fun setStrengthEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_STRENGTH, value).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
