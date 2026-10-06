package com.lodkin.dartstrainer.data.scoreset

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Сохранённая игра «Набор очков»
@Entity(tableName = "scoreset_games")
data class ScoreSetGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val totalScore: Int,           // итоговая сумма очков
    // 10 значений сумм за подходы, через запятую: "85,100,60,140,..."
    val approaches: String
)

@Dao
interface ScoreSetDao {

    @Insert
    suspend fun insertGame(game: ScoreSetGameEntity): Long

    @Query("SELECT * FROM scoreset_games ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<ScoreSetGameEntity>

    @Query("DELETE FROM scoreset_games")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM scoreset_games")
    suspend fun count(): Int
}

@Database(
    entities = [ScoreSetGameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ScoreSetDatabase : RoomDatabase() {
    abstract fun scoreSetDao(): ScoreSetDao

    companion object {
        @Volatile
        private var INSTANCE: ScoreSetDatabase? = null

        fun get(context: Context): ScoreSetDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScoreSetDatabase::class.java,
                    "scoreset.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий
class ScoreSetRepository(
    private val dao: ScoreSetDao
) {
    suspend fun saveGame(
        totalScore: Int,
        approaches: List<Int>
    ) {
        val entity = ScoreSetGameEntity(
            dateMillis = System.currentTimeMillis(),
            totalScore = totalScore,
            approaches = approaches.joinToString(",")
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<ScoreSetGameEntity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()
}
