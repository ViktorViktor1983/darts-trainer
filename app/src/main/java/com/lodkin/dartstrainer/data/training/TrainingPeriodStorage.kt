package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище текущего тренировочного периода.
 *
 * Период — это отрезок времени между сдачами экзамена (нормативов).
 * Когда игрок сдаёт экзамен, мы просто увеличиваем номер периода на 1.
 * Все старые игры остаются в базе с прошлым номером периода.
 */
object TrainingPeriodStorage {
    private const val PREFS_NAME = "training_period"
    private const val KEY_CURRENT_PERIOD = "current_period"

    /**
     * Получить номер текущего периода (по умолчанию 1).
     */
    fun getCurrentPeriod(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CURRENT_PERIOD, 1)
    }

    /**
     * Перейти к новому периоду (вызывается после сдачи экзамена).
     */
    fun startNewPeriod(context: Context) {
        val current = getCurrentPeriod(context)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CURRENT_PERIOD, current + 1)
            .apply()
    }

    /**
     * Сбросить период к первому (вызывается при полном сбросе).
     */
    fun reset(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CURRENT_PERIOD, 1)
            .apply()
    }
}
