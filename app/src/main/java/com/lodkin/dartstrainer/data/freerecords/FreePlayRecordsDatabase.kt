package com.lodkin.dartstrainer.data.freerecords

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
 * Рекорды свободной игры («Просто поиграть»).
 *
 * Отдельная маленькая БД. Не пересекается с training_records —
 * там свои рекорды, здесь свои.
 *
 * Ключи:
 *   • "game501_leg"     — 501: лучший лег (минимум дротиков, PPR справочно).
 *   • "game501_match"   — 501: лучший матч (максимум PPR, % удвоений справочно).
 *   • "cricket_leg"     — Крикет: лучший лег (максимум MPR).
 *   • "cricket_match"   — Крикет: лучший матч (максимум MPR).
 *
 * При добавлении новых игр — просто добавляем новые ключи, структура
 * таблицы не меняется.
 */
@Entity(tableName = "free_records")
data class FreePlayRecordEntity(
    @PrimaryKey val recordKey: String,
    val bestInt: Int = 0,
    val bestFloat: Float = 0f,
    val extraInt: Int = 0,
    val achievedAt: Long = 0L
)

@Dao
interface FreePlayRecordDao {
    @Insert
    suspend fun insert(item: FreePlayRecordEntity)

    @Update
    suspend fun update(item: FreePlayRecordEntity)

    @Query("SELECT * FROM free_records WHERE recordKey = :key LIMIT 1")
    suspend fun getByKey(key: String): FreePlayRecordEntity?

    @Query("SELECT * FROM free_records")
    suspend fun getAll(): List<FreePlayRecordEntity>

    @Query("DELETE FROM free_records")
    suspend fun clearAll()
}

@Database(
    entities = [FreePlayRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FreePlayRecordsDatabase : RoomDatabase() {
    abstract fun recordDao(): FreePlayRecordDao

    companion object {
        @Volatile
        private var INSTANCE: FreePlayRecordsDatabase? = null

        fun get(context: Context): FreePlayRecordsDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FreePlayRecordsDatabase::class.java,
                    "free_records.db"
                ).build().also { INSTANCE = it }
            }
    }
}

/**
 * Репозиторий рекордов свободной игры.
 *
 * Каждый tryUpdate* возвращает true, если рекорд побит —
 * чтобы UI мог показать праздничный диалог.
 *
 * При первом результате (рекорда ещё не было) тоже возвращает true —
 * UI сам решит, показать «первый результат» тихо или как рекорд.
 * Чтобы отличить «первый» от «побитого», используйте hasRecord(key).
 */
class FreePlayRecordsRepository(
    private val db: FreePlayRecordsDatabase
) {

    companion object {
        const val KEY_501_LEG = "game501_leg"
        const val KEY_501_MATCH = "game501_match"
        const val KEY_CRICKET_LEG = "cricket_leg"
        const val KEY_CRICKET_MATCH = "cricket_match"
    }

    /** Получить запись рекорда по ключу. null — рекорда ещё нет. */
    suspend fun get(key: String): FreePlayRecordEntity? = db.recordDao().getByKey(key)

    /** Есть ли уже рекорд по этому ключу. */
    suspend fun hasRecord(key: String): Boolean = db.recordDao().getByKey(key) != null

    suspend fun getAll(): List<FreePlayRecordEntity> = db.recordDao().getAll()

    suspend fun clearAll() = db.recordDao().clearAll()

    // ── 501 ──

    /**
     * 501 — лучший лег. Сравниваем по минимуму дротиков.
     * @return true, если рекорд установлен (первый или побит).
     */
    suspend fun tryUpdate501Leg(darts: Int, ppr: Float): Boolean {
        val existing = db.recordDao().getByKey(KEY_501_LEG)
        val now = System.currentTimeMillis()
        if (existing == null) {
            db.recordDao().insert(
                FreePlayRecordEntity(
                    recordKey = KEY_501_LEG,
                    bestInt = darts,
                    bestFloat = ppr,
                    achievedAt = now
                )
            )
            return true
        }
        if (existing.bestInt <= 0 || darts < existing.bestInt) {
            db.recordDao().update(
                existing.copy(bestInt = darts, bestFloat = ppr, achievedAt = now)
            )
            return true
        }
        return false
    }

    /**
     * 501 — лучший матч. Сравниваем по максимуму PPR матча.
     * @return true, если рекорд установлен (первый или побит).
     */
    suspend fun tryUpdate501Match(ppr: Float, doublesPercent: Int): Boolean {
        val existing = db.recordDao().getByKey(KEY_501_MATCH)
        val now = System.currentTimeMillis()
        if (existing == null) {
            db.recordDao().insert(
                FreePlayRecordEntity(
                    recordKey = KEY_501_MATCH,
                    bestFloat = ppr,
                    extraInt = doublesPercent,
                    achievedAt = now
                )
            )
            return true
        }
        if (ppr > existing.bestFloat) {
            db.recordDao().update(
                existing.copy(bestFloat = ppr, extraInt = doublesPercent, achievedAt = now)
            )
            return true
        }
        return false
    }

    // ── Крикет ──

    /**
     * Крикет — лучший лег. Сравниваем по максимуму MPR лега.
     * @return true, если рекорд установлен (первый или побит).
     */
    suspend fun tryUpdateCricketLeg(mpr: Float): Boolean {
        val existing = db.recordDao().getByKey(KEY_CRICKET_LEG)
        val now = System.currentTimeMillis()
        if (existing == null) {
            db.recordDao().insert(
                FreePlayRecordEntity(
                    recordKey = KEY_CRICKET_LEG,
                    bestFloat = mpr,
                    achievedAt = now
                )
            )
            return true
        }
        if (mpr > existing.bestFloat) {
            db.recordDao().update(
                existing.copy(bestFloat = mpr, achievedAt = now)
            )
            return true
        }
        return false
    }

    /**
     * Крикет — лучший матч. Сравниваем по максимуму MPR матча.
     * @return true, если рекорд установлен (первый или побит).
     */
    suspend fun tryUpdateCricketMatch(mpr: Float): Boolean {
        val existing = db.recordDao().getByKey(KEY_CRICKET_MATCH)
        val now = System.currentTimeMillis()
        if (existing == null) {
            db.recordDao().insert(
                FreePlayRecordEntity(
                    recordKey = KEY_CRICKET_MATCH,
                    bestFloat = mpr,
                    achievedAt = now
                )
            )
            return true
        }
        if (mpr > existing.bestFloat) {
            db.recordDao().update(
                existing.copy(bestFloat = mpr, achievedAt = now)
            )
            return true
        }
        return false
    }
}
