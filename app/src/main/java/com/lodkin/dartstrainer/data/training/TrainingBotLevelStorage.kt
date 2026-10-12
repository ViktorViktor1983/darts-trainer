package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище уровня бота для тренировочных партий 501 и Крикета.
 *
 * Логика адаптации:
 *   • Первая тренировка — бот на 1 уровень слабее игрока (или 1, если уровня нет).
 *   • Выиграл партию — на следующей тренировке бот на +1 уровень.
 *   • Проиграл — на −1 уровень.
 *   • Уровень бота хранится между тренировками.
 *
 * Уровни: 1..16 (как у игрока).
 */
object TrainingBotLevelStorage {

    private const val PREFS = "training_bot_levels"
    private const val KEY_501 = "bot_level_501"
    private const val KEY_CRICKET = "bot_level_cricket"
    private const val MIN_LEVEL = 1
    private const val MAX_LEVEL = 16

    /**
     * Получить уровень бота для 501.
     * @param playerLevel  текущий уровень игрока (1..16). 0 — уровень неизвестен.
     */
    fun getLevel501(context: Context, playerLevel: Int): Int {
        val saved = prefs(context).getInt(KEY_501, 0)
        if (saved in MIN_LEVEL..MAX_LEVEL) return saved
        // Первый раз: на 1 уровень слабее игрока
        val default = if (playerLevel > 0) (playerLevel - 1).coerceAtLeast(MIN_LEVEL) else MIN_LEVEL
        prefs(context).edit().putInt(KEY_501, default).apply()
        return default
    }

    /**
     * Получить уровень бота для Крикета.
     */
    fun getLevelCricket(context: Context, playerLevel: Int): Int {
        val saved = prefs(context).getInt(KEY_CRICKET, 0)
        if (saved in MIN_LEVEL..MAX_LEVEL) return saved
        val default = if (playerLevel > 0) (playerLevel - 1).coerceAtLeast(MIN_LEVEL) else MIN_LEVEL
        prefs(context).edit().putInt(KEY_CRICKET, default).apply()
        return default
    }

    /**
     * Обновить уровень бота 501 после партии.
     * @param playerWon  true — игрок выиграл, бот идёт +1; false — бот идёт −1.
     */
    fun updateLevel501(context: Context, currentLevel: Int, playerWon: Boolean) {
        val newLevel = if (playerWon) {
            (currentLevel + 1).coerceAtMost(MAX_LEVEL)
        } else {
            (currentLevel - 1).coerceAtLeast(MIN_LEVEL)
        }
        prefs(context).edit().putInt(KEY_501, newLevel).apply()
    }

    fun updateLevelCricket(context: Context, currentLevel: Int, playerWon: Boolean) {
        val newLevel = if (playerWon) {
            (currentLevel + 1).coerceAtMost(MAX_LEVEL)
        } else {
            (currentLevel - 1).coerceAtLeast(MIN_LEVEL)
        }
        prefs(context).edit().putInt(KEY_CRICKET, newLevel).apply()
    }

    /**
     * Сбросить всё (для полного сброса в настройках тренировки).
     */
    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
