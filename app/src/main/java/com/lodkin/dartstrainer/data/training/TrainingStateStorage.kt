package com.lodkin.dartstrainer.data.training

import android.content.Context

/**
 * Хранилище состояния активной тренировки.
 *
 * Нужно, чтобы игрок мог случайно выйти из приложения (или свернуть)
 * и вернуться к тренировке с того же места.
 *
 * Правила:
 *   • Если с последнего обновления прошло больше 3 часов — состояние
 *     считается устаревшим. ВАЖНО: load() его не удаляет — решение
 *     принимает MainActivity (записать в статистику и очистить).
 *   • При завершении тренировки состояние очищается.
 *   • Сохраняется позиция в цепочке + список упражнений + флаги блоков.
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
    private const val KEY_MAIN_EXERCISES = "main_exercises"
    private const val KEY_PSYCHOLOGY_ENABLED = "psychology_enabled"
    private const val KEY_STRENGTH_ENABLED = "strength_enabled"

    private const val MAX_IDLE_MILLIS = 3L * 60 * 60 * 1000

    // Разделители для сериализации списка упражнений.
    private const val EX_SEP = "|"
    private const val EX_FIELD_SEP = ":"

    data class Snapshot(
        val sessionId: Long,
        val minutes: Int,
        val elapsedSeconds: Int,
        val phaseName: String,
        val exerciseIndex: Int,
        val statuses: Map<Int, String>,
        /** id → минуты. Пустой список — восстановить из defaultMainExercises(). */
        val mainExerciseIds: List<Pair<String, Int>>,
        val psychologyEnabled: Boolean,
        val strengthEnabled: Boolean,
        val lastUpdateMillis: Long
    )

    /**
     * Сохранить состояние тренировки.
     *
     * @param mainExerciseIds список пар (id упражнения → минуты).
     *                        Пустой — не сохраняется, при загрузке будет пусто.
     */
    fun save(
        context: Context,
        sessionId: Long,
        minutes: Int,
        elapsedSeconds: Int,
        phaseName: String,
        exerciseIndex: Int,
        statuses: Map<Int, String>,
        mainExerciseIds: List<Pair<String, Int>> = emptyList(),
        psychologyEnabled: Boolean = false,
        strengthEnabled: Boolean = false
    ) {
        val statusString = statuses.entries.joinToString(",") { (idx, status) -> "$idx:$status" }
        val exString = mainExerciseIds.joinToString(EX_SEP) { (id, m) ->
            "$id$EX_FIELD_SEP$m"
        }

        prefs(context).edit()
            .putBoolean(KEY_ACTIVE, true)
            .putLong(KEY_SESSION_ID, sessionId)
            .putInt(KEY_MINUTES, minutes)
            .putInt(KEY_ELAPSED, elapsedSeconds)
            .putString(KEY_PHASE, phaseName)
            .putInt(KEY_EX_INDEX, exerciseIndex)
            .putString(KEY_STATUSES, statusString)
            .putString(KEY_MAIN_EXERCISES, exString)
            .putBoolean(KEY_PSYCHOLOGY_ENABLED, psychologyEnabled)
            .putBoolean(KEY_STRENGTH_ENABLED, strengthEnabled)
            .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            .apply()
    }

    /**
     * Загрузить состояние. ВАЖНО: не проверяет срок 3 часа — это делает
     * MainActivity через isExpired(). Если снапшот просрочен, MainActivity
     * сам решит: записать в статистику и очистить.
     *
     * Возвращает null только если состояние вообще отсутствует.
     */
    fun load(context: Context): Snapshot? {
        val p = prefs(context)
        val active = p.getBoolean(KEY_ACTIVE, false)
        if (!active) return null

        val lastUpdate = p.getLong(KEY_LAST_UPDATE, 0L)
        if (lastUpdate <= 0L) return null

        val statusRaw = p.getString(KEY_STATUSES, "") ?: ""
        val statuses = parseStatuses(statusRaw)

        val exRaw = p.getString(KEY_MAIN_EXERCISES, "") ?: ""
        val exercises = parseExercises(exRaw)

        return Snapshot(
            sessionId = p.getLong(KEY_SESSION_ID, 0L),
            minutes = p.getInt(KEY_MINUTES, 100),
            elapsedSeconds = p.getInt(KEY_ELAPSED, 0),
            phaseName = p.getString(KEY_PHASE, "MAIN") ?: "MAIN",
            exerciseIndex = p.getInt(KEY_EX_INDEX, 0),
            statuses = statuses,
            mainExerciseIds = exercises,
            psychologyEnabled = p.getBoolean(KEY_PSYCHOLOGY_ENABLED, false),
            strengthEnabled = p.getBoolean(KEY_STRENGTH_ENABLED, false),
            lastUpdateMillis = lastUpdate
        )
    }

    /**
     * Просрочен ли снапшот (прошло больше 3 часов).
     */
    fun isExpired(snapshot: Snapshot): Boolean {
        val now = System.currentTimeMillis()
        return (now - snapshot.lastUpdateMillis) > MAX_IDLE_MILLIS
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

    // ─────────────────────────────────────────────
    // Сериализация / парсинг
    // ─────────────────────────────────────────────

    private fun parseStatuses(raw: String): Map<Int, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val idx = parts[0].toIntOrNull() ?: return@mapNotNull null
                idx to parts[1]
            } else null
        }.toMap()
    }

    private fun parseExercises(raw: String): List<Pair<String, Int>> {
        if (raw.isBlank()) return emptyList()
        return raw.split(EX_SEP).mapNotNull { entry ->
            val parts = entry.split(EX_FIELD_SEP)
            if (parts.size == 2) {
                val id = parts[0]
                val minutes = parts[1].toIntOrNull() ?: return@mapNotNull null
                if (id.isBlank()) return@mapNotNull null
                id to minutes
            } else null
        }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
