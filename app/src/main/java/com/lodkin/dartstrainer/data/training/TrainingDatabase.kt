package com.lodkin.dartstrainer.data.training

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Calendar

/**
 * База данных тренировочного раздела.
 *
 * Версия 3 (13.10.2026) — добавлена таблица training_records.
 * Версия 4 (14.10.2026) — в training_records добавлены поля bestFloat и extraInt.
 * Версия 5 (16.10.2026) — добавлена таблица training_checkouts.
 */

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateMillis: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val levelAtStart: Int,
    val mode: String,
    val focus: String?,
    val isControl: Boolean,
    val isCompleted: Boolean
)

@Entity(tableName = "exercise_results")
data class ExerciseResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val blockType: String,
    val exerciseId: String,
    val score: Int,
    val dartsThrown: Int,
    val timestamp: Long,
    val durationSeconds: Int = 0,
    val detailsJson: String = ""
)

@Entity(tableName = "postponed_doubles")
data class PostponedDoubleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val doubleNumber: Int,
    val postponedCount: Int,
    val lastPostponedAt: Long,
    val isResolved: Boolean
)

@Entity(tableName = "weakness_snapshots")
data class WeaknessSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val type: String,
    val currentValue: Float,
    val normValue: Float,
    val priority: Int
)

@Entity(tableName = "potential_snapshots")
data class PotentialSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val level: Int,
    val potential: Int,
    val weakestType: String,
    val scoreValue: Float,
    val doublesValue: Float,
    val accuracyValue: Float,
    val treblesValue: Float,
    val timestamp: Long
)

@Entity(tableName = "control_matches")
data class ControlMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val gameType: String,
    val botLevel: Int,
    val playerWon: Boolean,
    val playerScore: Float,
    val botScore: Float,
    val legsPlayed: Int,
    val timestamp: Long
)

/**
 * Рекорд упражнения в тренировке.
 * Ключ — exerciseId (например, "aroundclock_doubles", "game501_leg").
 */
@Entity(tableName = "training_records")
data class TrainingRecordEntity(
    @PrimaryKey val exerciseId: String,
    val bestScore: Int,
    val bestDarts: Int,
    val bestFloat: Float = 0f,
    val extraInt: Int = 0,
    val achievedAt: Long
)

/**
 * Закрытый чекаут в тренировочной партии 501.
 *
 * Одна запись = один остаток, с которого игрок закрывал лег.
 * Несколько закрытий одного остатка → счётчик count растёт.
 *
 * Эти записи НЕ сбрасываются после сдачи нормативов — как рекорды.
 */
@Entity(tableName = "training_checkouts")
data class TrainingCheckoutEntity(
    @PrimaryKey val value: Int,       // остаток 2..170
    val count: Int,                   // сколько раз закрыт
    val lastUpdated: Long             // когда последний раз закрывал
)

/**
 * Модель для статистики по месяцам.
 */
data class MonthlyDartsStat(
    val month: String,
    val darts: Int
)

// ============================================================
// DAO
// ============================================================

@Dao
interface TrainingSessionDao {
    @Insert
    suspend fun insert(session: TrainingSessionEntity): Long

    @Update
    suspend fun update(session: TrainingSessionEntity)

    @Query("SELECT * FROM training_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TrainingSessionEntity?

    @Query("SELECT * FROM training_sessions ORDER BY dateMillis DESC LIMIT 1")
    suspend fun getLast(): TrainingSessionEntity?

    @Query("SELECT * FROM training_sessions WHERE isCompleted = 1 ORDER BY dateMillis DESC")
    suspend fun getAllCompleted(): List<TrainingSessionEntity>

    @Query("SELECT COUNT(*) FROM training_sessions WHERE isCompleted = 1")
    suspend fun getCompletedCount(): Int

    @Query("SELECT * FROM training_sessions ORDER BY dateMillis DESC")
    suspend fun getAll(): List<TrainingSessionEntity>

    @Query("DELETE FROM training_sessions")
    suspend fun clearAll()
}

// Вспомогательный класс для запроса по месяцам
data class DartsByMonthRaw(
    @ColumnInfo(name = "month") val month: String,
    @ColumnInfo(name = "totalDarts") val totalDarts: Int
)

@Dao
interface ExerciseResultDao {
    @Insert
    suspend fun insert(result: ExerciseResultEntity): Long

    @Query("SELECT * FROM exercise_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getBySession(sessionId: Long): List<ExerciseResultEntity>

    @Query("SELECT * FROM exercise_results WHERE exerciseId = :exerciseId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestByExercise(exerciseId: String, limit: Int): List<ExerciseResultEntity>

    @Query("SELECT * FROM exercise_results WHERE exerciseId = :exerciseId ORDER BY timestamp ASC")
    suspend fun getAllByExercise(exerciseId: String): List<ExerciseResultEntity>

    @Query("DELETE FROM exercise_results WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)

    @Query("DELETE FROM exercise_results")
    suspend fun clearAll()

    // ── НОВЫЕ ЗАПРОСЫ ДЛЯ ИНТЕНСИВНОСТИ ──

    @Query("SELECT SUM(dartsThrown) FROM exercise_results")
    suspend fun getTotalDartsThrown(): Int?

    @Query("SELECT SUM(dartsThrown) FROM exercise_results WHERE timestamp >= :startOfWeekMillis")
    suspend fun getDartsThrownSince(startOfWeekMillis: Long): Int?

    // Группируем по месяцам. timestamp / 1000 переводит миллисекунды в секунды для strftime
    @Query("SELECT strftime('%Y-%m', timestamp / 1000, 'unixepoch', 'localtime') as month, SUM(dartsThrown) as totalDarts FROM exercise_results GROUP BY month ORDER BY month DESC")
    suspend fun getDartsByMonth(): List<DartsByMonthRaw>
}

@Dao
interface PostponedDoubleDao {
    @Insert
    suspend fun insert(item: PostponedDoubleEntity): Long

    @Update
    suspend fun update(item: PostponedDoubleEntity)

    @Query("SELECT * FROM postponed_doubles WHERE isResolved = 0 ORDER BY lastPostponedAt ASC")
    suspend fun getUnresolved(): List<PostponedDoubleEntity>

    @Query("SELECT * FROM postponed_doubles WHERE doubleNumber = :doubleNumber AND isResolved = 0 LIMIT 1")
    suspend fun getUnresolvedByNumber(doubleNumber: Int): PostponedDoubleEntity?

    @Query("SELECT * FROM postponed_doubles ORDER BY lastPostponedAt DESC")
    suspend fun getAll(): List<PostponedDoubleEntity>

    @Query("DELETE FROM postponed_doubles")
    suspend fun clearAll()
}

@Dao
interface WeaknessSnapshotDao {
    @Insert
    suspend fun insert(item: WeaknessSnapshotEntity): Long

    @Query("SELECT * FROM weakness_snapshots WHERE sessionId = :sessionId ORDER BY priority ASC")
    suspend fun getBySession(sessionId: Long): List<WeaknessSnapshotEntity>

    @Query("SELECT * FROM weakness_snapshots ORDER BY id DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<WeaknessSnapshotEntity>

    @Query("DELETE FROM weakness_snapshots")
    suspend fun clearAll()
}

@Dao
interface PotentialSnapshotDao {
    @Insert
    suspend fun insert(item: PotentialSnapshotEntity): Long

    @Query("SELECT * FROM potential_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PotentialSnapshotEntity?

    @Query("SELECT * FROM potential_snapshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<PotentialSnapshotEntity>

    @Query("DELETE FROM potential_snapshots")
    suspend fun clearAll()
}

@Dao
interface ControlMatchDao {
    @Insert
    suspend fun insert(item: ControlMatchEntity): Long

    @Query("SELECT * FROM control_matches WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<ControlMatchEntity>

    @Query("SELECT * FROM control_matches ORDER BY timestamp DESC")
    suspend fun getAll(): List<ControlMatchEntity>

    @Query("DELETE FROM control_matches")
    suspend fun clearAll()
}

@Dao
interface TrainingRecordDao {
    @Insert
    suspend fun insert(item: TrainingRecordEntity)

    @Update
    suspend fun update(item: TrainingRecordEntity)

    @Query("SELECT * FROM training_records WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getById(exerciseId: String): TrainingRecordEntity?

    @Query("SELECT * FROM training_records")
    suspend fun getAll(): List<TrainingRecordEntity>

    @Query("DELETE FROM training_records")
    suspend fun clearAll()
}

@Dao
interface TrainingCheckoutDao {
    @Insert
    suspend fun insert(item: TrainingCheckoutEntity)

    @Update
    suspend fun update(item: TrainingCheckoutEntity)

    @Query("SELECT * FROM training_checkouts WHERE value = :value LIMIT 1")
    suspend fun getByValue(value: Int): TrainingCheckoutEntity?

    @Query("SELECT * FROM training_checkouts ORDER BY value ASC")
    suspend fun getAll(): List<TrainingCheckoutEntity>

    @Query("DELETE FROM training_checkouts")
    suspend fun clearAll()
}

// ============================================================
// DATABASE
// ============================================================

@Database(
    entities = [
        TrainingSessionEntity::class,
        ExerciseResultEntity::class,
        PostponedDoubleEntity::class,
        WeaknessSnapshotEntity::class,
        PotentialSnapshotEntity::class,
        ControlMatchEntity::class,
        TrainingRecordEntity::class,
        TrainingCheckoutEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun sessionDao(): TrainingSessionDao
    abstract fun exerciseDao(): ExerciseResultDao
    abstract fun postponedDao(): PostponedDoubleDao
    abstract fun weaknessDao(): WeaknessSnapshotDao
    abstract fun potentialDao(): PotentialSnapshotDao
    abstract fun controlDao(): ControlMatchDao
    abstract fun recordDao(): TrainingRecordDao
    abstract fun checkoutDao(): TrainingCheckoutDao

    companion object {
        @Volatile
        private var INSTANCE: TrainingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE exercise_results ADD COLUMN durationSeconds INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS training_records (" +
                            "exerciseId TEXT NOT NULL PRIMARY KEY, " +
                            "bestScore INTEGER NOT NULL DEFAULT 0, " +
                            "bestDarts INTEGER NOT NULL DEFAULT 0, " +
                            "achievedAt INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE training_records ADD COLUMN bestFloat REAL NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE training_records ADD COLUMN extraInt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS training_checkouts (" +
                            "value INTEGER NOT NULL PRIMARY KEY, " +
                            "count INTEGER NOT NULL DEFAULT 0, " +
                            "lastUpdated INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        fun getInstance(context: Context): TrainingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "training_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

// ============================================================
// REPOSITORY
// ============================================================

class TrainingRepository(private val db: TrainingDatabase) {

    // ---------- Тренировочные сессии ----------

    suspend fun insertSession(session: TrainingSession): Long {
        return db.sessionDao().insert(session.toEntity())
    }

    suspend fun updateSession(session: TrainingSession) {
        db.sessionDao().update(session.toEntity())
    }

    suspend fun getSessionById(id: Long): TrainingSession? {
        return db.sessionDao().getById(id)?.toModel()
    }

    suspend fun getLastSession(): TrainingSession? {
        return db.sessionDao().getLast()?.toModel()
    }

    suspend fun getCompletedSessionCount(): Int {
        return db.sessionDao().getCompletedCount()
    }

    suspend fun getAllCompletedSessions(): List<TrainingSession> {
        return db.sessionDao().getAllCompleted().map { it.toModel() }
    }

    // ---------- Результаты упражнений ----------

    suspend fun insertExerciseResult(result: ExerciseResult): Long {
        return db.exerciseDao().insert(result.toEntity())
    }

    suspend fun getExerciseResults(sessionId: Long): List<ExerciseResult> {
        return db.exerciseDao().getBySession(sessionId).map { it.toModel() }
    }

    suspend fun getLatestExerciseResults(exerciseId: String, limit: Int): List<ExerciseResult> {
        return db.exerciseDao().getLatestByExercise(exerciseId, limit).map { it.toModel() }
    }

    suspend fun getAllExerciseResultsByExerciseId(exerciseId: String): List<ExerciseResult> {
        return db.exerciseDao().getAllByExercise(exerciseId).map { it.toModel() }
    }

    // ---------- НОВЫЕ МЕТОДЫ ДЛЯ ИНТЕНСИВНОСТИ ----------

    /** Сколько всего дротиков брошено за всё время. */
    suspend fun getTotalDartsThrown(): Int {
        return db.exerciseDao().getTotalDartsThrown() ?: 0
    }

    /** Сколько дротиков брошено с начала текущей недели (с понедельника). */
    suspend fun getDartsThrownThisWeek(): Int {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        return db.exerciseDao().getDartsThrownSince(startOfWeek) ?: 0
    }

    /** Статистика бросков по месяцам (от новых к старым). */
    suspend fun getDartsByMonth(): List<MonthlyDartsStat> {
        return db.exerciseDao().getDartsByMonth().map {
            MonthlyDartsStat(month = it.month, darts = it.totalDarts)
        }
    }

    // ---------- Отложенные даблы ----------

    suspend fun insertPostponed(item: PostponedDouble): Long {
        return db.postponedDao().insert(item.toEntity())
    }

    suspend fun updatePostponed(item: PostponedDouble) {
        db.postponedDao().update(item.toEntity())
    }

    suspend fun getUnresolvedDoubles(): List<PostponedDouble> {
        return db.postponedDao().getUnresolved().map { it.toModel() }
    }

    suspend fun getUnresolvedDoubleByNumber(doubleNumber: Int): PostponedDouble? {
        return db.postponedDao().getUnresolvedByNumber(doubleNumber)?.toModel()
    }

    // ---------- Снимки слабых мест ----------

    suspend fun insertWeakness(snapshot: WeaknessSnapshot): Long {
        return db.weaknessDao().insert(snapshot.toEntity())
    }

    suspend fun getWeaknessesForSession(sessionId: Long): List<WeaknessSnapshot> {
        return db.weaknessDao().getBySession(sessionId).map { it.toModel() }
    }

    // ---------- Снимки уровня и потенциала ----------

    suspend fun insertPotential(snapshot: PotentialSnapshot): Long {
        return db.potentialDao().insert(snapshot.toEntity())
    }

    suspend fun getLastPotential(): PotentialSnapshot? {
        return db.potentialDao().getLast()?.toModel()
    }

    suspend fun getLatestPotentials(limit: Int): List<PotentialSnapshot> {
        return db.potentialDao().getLatest(limit).map { it.toModel() }
    }

    // ---------- Контрольные матчи ----------

    suspend fun insertControlMatch(match: ControlMatch): Long {
        return db.controlDao().insert(match.toEntity())
    }

    suspend fun getControlMatches(sessionId: Long): List<ControlMatch> {
        return db.controlDao().getBySession(sessionId).map { it.toModel() }
    }

    suspend fun getAllControlMatches(): List<ControlMatch> {
        return db.controlDao().getAll().map { it.toModel() }
    }

    // ---------- Закрытые чекауты тренировки ----------

    suspend fun recordCheckout(value: Int) {
        if (value < 2 || value > 170) return
        val existing = db.checkoutDao().getByValue(value)
        val now = System.currentTimeMillis()
        if (existing == null) {
            db.checkoutDao().insert(
                TrainingCheckoutEntity(value = value, count = 1, lastUpdated = now)
            )
        } else {
            db.checkoutDao().update(
                existing.copy(count = existing.count + 1, lastUpdated = now)
            )
        }
    }

    suspend fun getAllCheckouts(): Map<Int, Int> {
        return db.checkoutDao().getAll().associate { it.value to it.count }
    }

    // ---------- Рекорды тренировки (общие) ----------

    suspend fun getTrainingRecord(exerciseId: String): TrainingRecordEntity? {
        return db.recordDao().getById(exerciseId)
    }

    suspend fun updateTrainingRecordIfBetter(
        exerciseId: String,
        score: Int,
        darts: Int,
        lowerDartsBetter: Boolean
    ) {
        val existing = db.recordDao().getById(exerciseId)
        val now = System.currentTimeMillis()

        if (existing == null) {
            db.recordDao().insert(
                TrainingRecordEntity(
                    exerciseId = exerciseId,
                    bestScore = score,
                    bestDarts = darts,
                    achievedAt = now
                )
            )
            return
        }

        val isBetter = if (lowerDartsBetter) darts < existing.bestDarts
        else score > existing.bestScore

        if (isBetter) {
            db.recordDao().update(
                existing.copy(
                    bestScore = score,
                    bestDarts = darts,
                    achievedAt = now
                )
            )
        }
    }

    // ---------- Рекорды 501 в тренировке ----------

    suspend fun update501LegRecord(darts: Int, ppr: Float): Boolean {
        val key = "game501_leg"
        val existing = db.recordDao().getById(key)
        val now = System.currentTimeMillis()

        if (existing == null || existing.bestDarts <= 0 || darts < existing.bestDarts) {
            if (existing == null) {
                db.recordDao().insert(
                    TrainingRecordEntity(
                        exerciseId = key,
                        bestScore = 0,
                        bestDarts = darts,
                        bestFloat = ppr,
                        extraInt = 0,
                        achievedAt = now
                    )
                )
            } else {
                db.recordDao().update(
                    existing.copy(
                        bestDarts = darts,
                        bestFloat = ppr,
                        achievedAt = now
                    )
                )
            }
            return true
        }
        return false
    }

    suspend fun update501MatchRecord(ppr: Float, doublesPercent: Int): Boolean {
        val key = "game501_match"
        val existing = db.recordDao().getById(key)
        val now = System.currentTimeMillis()

        if (existing == null || ppr > existing.bestFloat) {
            if (existing == null) {
                db.recordDao().insert(
                    TrainingRecordEntity(
                        exerciseId = key,
                        bestScore = 0,
                        bestDarts = 0,
                        bestFloat = ppr,
                        extraInt = doublesPercent,
                        achievedAt = now
                    )
                )
            } else {
                db.recordDao().update(
                    existing.copy(
                        bestFloat = ppr,
                        extraInt = doublesPercent,
                        achievedAt = now
                    )
                )
            }
            return true
        }
        return false
    }

    // ---------- Рекорды Крикета в тренировке ----------

    suspend fun updateCricketLegRecord(mpr: Float): Boolean {
        val key = "cricket_leg"
        val existing = db.recordDao().getById(key)
        val now = System.currentTimeMillis()

        if (existing == null || mpr > existing.bestFloat) {
            if (existing == null) {
                db.recordDao().insert(
                    TrainingRecordEntity(
                        exerciseId = key,
                        bestScore = 0,
                        bestDarts = 0,
                        bestFloat = mpr,
                        extraInt = 0,
                        achievedAt = now
                    )
                )
            } else {
                db.recordDao().update(
                    existing.copy(
                        bestFloat = mpr,
                        achievedAt = now
                    )
                )
            }
            return true
        }
        return false
    }

    suspend fun updateCricketMatchRecord(mpr: Float): Boolean {
        val key = "cricket_match"
        val existing = db.recordDao().getById(key)
        val now = System.currentTimeMillis()

        if (existing == null || mpr > existing.bestFloat) {
            if (existing == null) {
                db.recordDao().insert(
                    TrainingRecordEntity(
                        exerciseId = key,
                        bestScore = 0,
                        bestDarts = 0,
                        bestFloat = mpr,
                        extraInt = 0,
                        achievedAt = now
                    )
                )
            } else {
                db.recordDao().update(
                    existing.copy(
                        bestFloat = mpr,
                        achievedAt = now
                    )
                )
            }
            return true
        }
        return false
    }

    // ---------- Полный сброс ----------

    suspend fun clearAllTrainingData() {
        db.exerciseDao().clearAll()
        db.sessionDao().clearAll()
        db.postponedDao().clearAll()
        db.weaknessDao().clearAll()
        db.potentialDao().clearAll()
        db.controlDao().clearAll()
        db.recordDao().clearAll()
        db.checkoutDao().clearAll()
    }

    suspend fun clearStatisticsAfterNormatives() {
        db.exerciseDao().clearAll()
        db.sessionDao().clearAll()
        db.postponedDao().clearAll()
        db.weaknessDao().clearAll()
        db.potentialDao().clearAll()
        db.controlDao().clearAll()
    }
}

// ============================================================
// КОНВЕРТЕРЫ
// ============================================================

private fun TrainingSession.toEntity() = TrainingSessionEntity(
    id = id,
    dateMillis = dateMillis,
    plannedMinutes = plannedMinutes,
    actualMinutes = actualMinutes,
    levelAtStart = levelAtStart,
    mode = mode.name,
    focus = focus?.name,
    isControl = isControl,
    isCompleted = isCompleted
)

private fun TrainingSessionEntity.toModel() = TrainingSession(
    id = id,
    dateMillis = dateMillis,
    plannedMinutes = plannedMinutes,
    actualMinutes = actualMinutes,
    levelAtStart = levelAtStart,
    mode = TrainingMode.valueOf(mode),
    focus = focus?.let { WeaknessType.valueOf(it) },
    isControl = isControl,
    isCompleted = isCompleted
)

private fun ExerciseResult.toEntity() = ExerciseResultEntity(
    id = id,
    sessionId = sessionId,
    blockType = blockType.name,
    exerciseId = exerciseId,
    score = score,
    dartsThrown = dartsThrown,
    timestamp = timestamp,
    durationSeconds = durationSeconds,
    detailsJson = detailsJson
)

private fun ExerciseResultEntity.toModel() = ExerciseResult(
    id = id,
    sessionId = sessionId,
    blockType = BlockType.valueOf(blockType),
    exerciseId = exerciseId,
    score = score,
    dartsThrown = dartsThrown,
    timestamp = timestamp,
    durationSeconds = durationSeconds,
    detailsJson = detailsJson
)

private fun PostponedDouble.toEntity() = PostponedDoubleEntity(
    id = id,
    sessionId = sessionId,
    doubleNumber = doubleNumber,
    postponedCount = postponedCount,
    lastPostponedAt = lastPostponedAt,
    isResolved = isResolved
)

private fun PostponedDoubleEntity.toModel() = PostponedDouble(
    id = id,
    sessionId = sessionId,
    doubleNumber = doubleNumber,
    postponedCount = postponedCount,
    lastPostponedAt = lastPostponedAt,
    isResolved = isResolved
)

private fun WeaknessSnapshot.toEntity() = WeaknessSnapshotEntity(
    id = id,
    sessionId = sessionId,
    type = type.name,
    currentValue = currentValue,
    normValue = normValue,
    priority = priority
)

private fun WeaknessSnapshotEntity.toModel() = WeaknessSnapshot(
    id = id,
    sessionId = sessionId,
    type = WeaknessType.valueOf(type),
    currentValue = currentValue,
    normValue = normValue,
    priority = priority
)

private fun PotentialSnapshot.toEntity() = PotentialSnapshotEntity(
    id = id,
    sessionId = sessionId,
    level = level,
    potential = potential,
    weakestType = weakestType.name,
    scoreValue = scoreValue,
    doublesValue = doublesValue,
    accuracyValue = accuracyValue,
    treblesValue = treblesValue,
    timestamp = timestamp
)

private fun PotentialSnapshotEntity.toModel() = PotentialSnapshot(
    id = id,
    sessionId = sessionId,
    level = level,
    potential = potential,
    weakestType = WeaknessType.valueOf(weakestType),
    scoreValue = scoreValue,
    doublesValue = doublesValue,
    accuracyValue = accuracyValue,
    treblesValue = treblesValue,
    timestamp = timestamp
)

private fun ControlMatch.toEntity() = ControlMatchEntity(
    id = id,
    sessionId = sessionId,
    gameType = gameType.name,
    botLevel = botLevel,
    playerWon = playerWon,
    playerScore = playerScore,
    botScore = botScore,
    legsPlayed = legsPlayed,
    timestamp = timestamp
)

private fun ControlMatchEntity.toModel() = ControlMatch(
    id = id,
    sessionId = sessionId,
    gameType = ControlGameType.valueOf(gameType),
    botLevel = botLevel,
    playerWon = playerWon,
    playerScore = playerScore,
    botScore = botScore,
    legsPlayed = legsPlayed,
    timestamp = timestamp
)
