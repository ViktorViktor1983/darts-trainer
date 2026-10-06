package com.lodkin.dartstrainer.data.aroundclock

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Целевой режим игры
enum class AroundClockTarget {
    SINGLE,   // одинарные сектора
    DOUBLE,   // удвоения
    TRIPLE    // утроения
}

// Порядок секторов
enum class AroundClockOrder {
    ORDERED,  // 1, 2, 3, ..., 20, Bull
    RANDOM    // случайный порядок, каждый сектор один раз
}

// Сохранённая игра «Кругосветка»
@Entity(tableName = "aroundclock_games")
data class AroundClockGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val targetName: String,       // "SINGLE" / "DOUBLE" / "TRIPLE"
    val orderName: String,        // "ORDERED" / "RANDOM"
    val hitsRequired: Int,        // 1, 2 или 3 попадания на сектор
    val totalDarts: Int,          // всего потрачено дротиков на весь круг
    // Успел ли игрок пройти до конца (все 21 цель: 1..20 + Bull)
    val completed: Boolean,
    // Результаты по каждому сектору: "1:2,2:3,3:1,25:5"
    // 1:2 — сектор 1 (одинарный), потрачено 2 дротика
    // 25:5 — Bull, потрачено 5 дротиков
    val sectorResults: String
)

@Dao
interface AroundClockDao {

    @Insert
    suspend fun insertGame(game: AroundClockGameEntity): Long

    @Query("SELECT * FROM aroundclock_games ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<AroundClockGameEntity>

    @Query("DELETE FROM aroundclock_games")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM aroundclock_games")
    suspend fun count(): Int
}

@Database(
    entities = [AroundClockGameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AroundClockDatabase : RoomDatabase() {
    abstract fun aroundClockDao(): AroundClockDao

    companion object {
        @Volatile
        private var INSTANCE: AroundClockDatabase? = null

        fun get(context: Context): AroundClockDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AroundClockDatabase::class.java,
                    "aroundclock.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class AroundClockRepository(
    private val dao: AroundClockDao
) {
    suspend fun saveGame(
        target: AroundClockTarget,
        order: AroundClockOrder,
        hitsRequired: Int,
        totalDarts: Int,
        completed: Boolean,
        sectorResults: Map<Int, Int>   // сектор → потрачено дротиков
    ) {
        val entity = AroundClockGameEntity(
            dateMillis = System.currentTimeMillis(),
            targetName = target.name,
            orderName = order.name,
            hitsRequired = hitsRequired,
            totalDarts = totalDarts,
            completed = completed,
            sectorResults = sectorResults.entries
                .joinToString(",") { "${it.key}:${it.value}" }
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<AroundClockGameEntity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()
}
