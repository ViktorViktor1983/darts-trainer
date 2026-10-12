package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище состояния активной тренировки.
 *
 * Нужно, чтобы игрок мог случайно выйти из приложения (или свернуть)
 * и вернуться к тренировке с того же места.
 *
 * Правила:
 *   • Если с последнего обновления прошло больше 3 часов — состояние считается устаревшим.
 *   • При завершении тренировки состояние очищается.
 *   • Сохраняется только позиция в цепочке (блок + номер упражнения + статусы),
 *     а не середина отдельного упражнения.
 */
object TrainingStateStorage {

    private const val PREFS = "training_state"

    private const val KEY_ACTIVE = "is_active"
    private const val KEY_SESSION_ID = "session_id"
    private const val KEY_MINUTES = "minutes"
    private const val KEY_ELAPSED = "elapsed_seconds"
    private const val KEY_PHASE = "phase"
    private const val KEY_EX_INDEX = "exercise_index"
    private const val KEY_LAST_UPDATE = "last_update"
    private const val KEY_STATUSES = "statuses"

    private const val MAX_IDLE_MILLIS = 3L * 60 * 60 * 1000

    data class Snapshot(
        val sessionId: Long,
        val minutes: Int,
        val elapsedSeconds: Int,
        val phaseName: String,
        val exerciseIndex: Int,
        val statuses: Map<Int, String>,
        val lastUpdateMillis: Long
    )

    fun save(
        context: Context,
        sessionId: Long,
        minutes: Int,
        elapsedSeconds: Int,
        phaseName: String,
        exerciseIndex: Int,
        statuses: Map<Int, String>
    ) {
        val statusString = statuses.entries.joinToString(",") { (idx, status) -> "$idx:$status" }
        prefs(context).edit()
            .putBoolean(KEY_ACTIVE, true)
            .putLong(KEY_SESSION_ID, sessionId)
            .putInt(KEY_MINUTES, minutes)
            .putInt(KEY_ELAPSED, elapsedSeconds)
            .putString(KEY_PHASE, phaseName)
            .putInt(KEY_EX_INDEX, exerciseIndex)
            .putString(KEY_STATUSES, statusString)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): Snapshot? {
        val p = prefs(context)
        val active = p.getBoolean(KEY_ACTIVE, false)
        if (!active) return null

        val lastUpdate = p.getLong(KEY_LAST_UPDATE, 0L)
        val now = System.currentTimeMillis()
        if (lastUpdate <= 0L || (now - lastUpdate) > MAX_IDLE_MILLIS) {
            clear(context)
            return null
        }

        val statusRaw = p.getString(KEY_STATUSES, "") ?: ""
        val statuses = if (statusRaw.isBlank()) emptyMap() else {
            statusRaw.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val idx = parts[0].toIntOrNull() ?: return@mapNotNull null
                    idx to parts[1]
                } else null
            }.toMap()
        }

        return Snapshot(
            sessionId = p.getLong(KEY_SESSION_ID, 0L),
            minutes = p.getInt(KEY_MINUTES, 100),
            elapsedSeconds = p.getInt(KEY_ELAPSED, 0),
            phaseName = p.getString(KEY_PHASE, "MAIN") ?: "MAIN",
            exerciseIndex = p.getInt(KEY_EX_INDEX, 0),
            statuses = statuses,
            lastUpdateMillis = lastUpdate
        )
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun minutesSinceLastUpdate(context: Context): Int {
        val last = prefs(context).getLong(KEY_LAST_UPDATE, 0L)
        if (last <= 0L) return 0
        val diff = System.currentTimeMillis() - last
        return (diff / 60000L).toInt()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
