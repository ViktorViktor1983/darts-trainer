package com.lodkin.dartstrainer.data.training

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update

/**
 * База данных тренировочного раздела.
 *
 * Здесь: Entity (таблицы) + Dao (запросы) + Database + Repository.
 * Всё в одном файле — как принято в проекте.
 *
 * Версия 1. Если понадобится менять структуру — версия +1 и миграция.
 */

// ============================================================
// ENTITY — ТАБЛИЦЫ
// ============================================================

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateMillis: Long,
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val levelAtStart: Int,
    val mode: String,               // TrainingMode.name
    val focus: String?,             // WeaknessType.name, может быть null
    val isControl: Boolean,
    val isCompleted: Boolean
)

@Entity(tableName = "exercise_results")
data class ExerciseResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val blockType: String,          // BlockType.name
    val exerciseId: String,
    val score: Int,
    val dartsThrown: Int,
    val timestamp: Long,
    val detailsJson: String = ""
)

@Entity(tableName = "postponed_doubles")
data class PostponedDoubleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val doubleNumber: Int,          // 1..20, 21 = Bull
    val postponedCount: Int,
    val lastPostponedAt: Long,
    val isResolved: Boolean
)

@Entity(tableName = "weakness_snapshots")
data class WeaknessSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val type: String,               // WeaknessType.name
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
    val weakestType: String,        // WeaknessType.name
    val scoreValue: Float,
    val doublesValue: Float,
    val accuracyValue: Float,
    val timestamp: Long
)

@Entity(tableName = "control_matches")
data class ControlMatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val gameType: String,           // ControlGameType.name
    val botLevel: Int,
    val playerWon: Boolean,
    val playerScore: Float,
    val botScore: Float,
    val legsPlayed: Int,
    val timestamp: Long
)

// ============================================================
// DAO — ЗАПРОСЫ К ТАБЛИЦАМ
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
}

@Dao
interface ExerciseResultDao {
    @Insert
    suspend fun insert(result: ExerciseResultEntity): Long

    @Query("SELECT * FROM exercise_results WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getBySession(sessionId: Long): List<ExerciseResultEntity>

    @Query("SELECT * FROM exercise_results WHERE exerciseId = :exerciseId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestByExercise(exerciseId: String, limit: Int): List<ExerciseResultEntity>

    @Query("DELETE FROM exercise_results WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
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
}

@Dao
interface WeaknessSnapshotDao {
    @Insert
    suspend fun insert(item: WeaknessSnapshotEntity): Long

    @Query("SELECT * FROM weakness_snapshots WHERE sessionId = :sessionId ORDER BY priority ASC")
    suspend fun getBySession(sessionId: Long): List<WeaknessSnapshotEntity>

    @Query("SELECT * FROM weakness_snapshots ORDER BY id DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<WeaknessSnapshotEntity>
}

@Dao
interface PotentialSnapshotDao {
    @Insert
    suspend fun insert(item: PotentialSnapshotEntity): Long

    @Query("SELECT * FROM potential_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PotentialSnapshotEntity?

    @Query("SELECT * FROM potential_snapshots ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<PotentialSnapshotEntity>
}

@Dao
interface ControlMatchDao {
    @Insert
    suspend fun insert(item: ControlMatchEntity): Long

    @Query("SELECT * FROM control_matches WHERE sessionId = :sessionId")
    suspend fun getBySession(sessionId: Long): List<ControlMatchEntity>

    @Query("SELECT * FROM control_matches ORDER BY timestamp DESC")
    suspend fun getAll(): List<ControlMatchEntity>
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
        ControlMatchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun sessionDao(): TrainingSessionDao
    abstract fun exerciseDao(): ExerciseResultDao
    abstract fun postponedDao(): PostponedDoubleDao
    abstract fun weaknessDao(): WeaknessSnapshotDao
    abstract fun potentialDao(): PotentialSnapshotDao
    abstract fun controlDao(): ControlMatchDao

    companion object {
        @Volatile
        private var INSTANCE: TrainingDatabase? = null

        fun getInstance(context: Context): TrainingDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "training_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

// ============================================================
// REPOSITORY — УДОБНАЯ ОБЁРТКА НАД DAO
// ============================================================

/**
 * Репозиторий работает с доменными моделями (TrainingSession, ExerciseResult и т.д.),
 * а внутри конвертирует их в Entity и обратно.
 *
 * Это значит, что экраны и логика работают с чистыми моделями, а не с таблицами.
 */
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
}

// ============================================================
// КОНВЕРТЕРЫ Entity <-> Model
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
