package com.lodkin.dartstrainer.data.sector

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Сохранённая игра «Сектор»
@Entity(tableName = "sector_games")
data class SectorGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val sector: Int,           // 1..20, 25 = Bull
    val totalScore: Int,       // набранные очки
    val totalHits: Int,        // общее число попаданий
    // Попадания за каждый подход, через запятую: "5,3,7,..."
    val approaches: String
)

@Dao
interface SectorDao {

    @Insert
    suspend fun insertGame(game: SectorGameEntity): Long

    @Query("SELECT * FROM sector_games ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<SectorGameEntity>

    @Query("SELECT * FROM sector_games WHERE sector = :sector ORDER BY dateMillis DESC")
    suspend fun getGamesBySector(sector: Int): List<SectorGameEntity>

    @Query("DELETE FROM sector_games")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM sector_games")
    suspend fun count(): Int
}

@Database(
    entities = [SectorGameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SectorDatabase : RoomDatabase() {
    abstract fun sectorDao(): SectorDao

    companion object {
        @Volatile
        private var INSTANCE: SectorDatabase? = null

        fun get(context: Context): SectorDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SectorDatabase::class.java,
                    "sector.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class SectorRepository(
    private val dao: SectorDao
) {
    suspend fun saveGame(
        sector: Int,
        totalScore: Int,
        totalHits: Int,
        approaches: List<Int>
    ) {
        val entity = SectorGameEntity(
            dateMillis = System.currentTimeMillis(),
            sector = sector,
            totalScore = totalScore,
            totalHits = totalHits,
            approaches = approaches.joinToString(",")
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<SectorGameEntity> = dao.getAllGames()

    suspend fun getGamesBySector(sector: Int): List<SectorGameEntity> =
        dao.getGamesBySector(sector)

    suspend fun clearAll() = dao.clearAll()
}
