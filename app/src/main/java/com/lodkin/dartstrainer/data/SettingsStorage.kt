package com.lodkin.dartstrainer.data

import android.content.Context

object SettingsStorage {
    private const val PREFS = "darts_trainer_settings"

    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_PLAYER_NAME = "player_name"
    private const val KEY_TRAINING_MINUTES = "training_minutes"
    private const val KEY_TRAININGS_PER_WEEK = "trainings_per_week"
    private const val KEY_TRAINING_MODE = "training_mode"
    private const val KEY_START_MODE = "start_mode"

    // Анкета пройдена или нет
    fun isOnboardingDone(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
    }

    // Имя игрока
    fun getPlayerName(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PLAYER_NAME, "") ?: ""

    fun setPlayerName(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PLAYER_NAME, name).apply()
    }

    // Время на тренировку (в минутах)
    fun getTrainingMinutes(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_TRAINING_MINUTES, 60)

    fun setTrainingMinutes(context: Context, minutes: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_TRAINING_MINUTES, minutes).apply()
    }

    // Тренировок в неделю
    fun getTrainingsPerWeek(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_TRAININGS_PER_WEEK, 3)

    fun setTrainingsPerWeek(context: Context, count: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_TRAININGS_PER_WEEK, count).apply()
    }

    // Режим тренировок: "schedule", "flexible", "free"
    fun getTrainingMode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TRAINING_MODE, "flexible") ?: "flexible"

    fun setTrainingMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_TRAINING_MODE, mode).apply()
    }

    // Способ определения уровня: "test", "start", "self"
    fun getStartMode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_START_MODE, "start") ?: "start"

    fun setStartMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_START_MODE, mode).apply()
    }
}
