package com.lodkin.dartstrainer.data.biground

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Категория нормативов
enum class BigRoundCategory {
    MALE,     // мужской
    YOUTH     // юношеский
}

// Сохранённая игра «Большой раунд»
@Entity(tableName = "biground_games")
data class BigRoundGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val categoryName: String,      // "MALE" / "YOUTH"
    val totalScore: Int,           // итоговые очки
    val totalHits: Int,            // общее число попаданий
    // Попадания за каждый подход (21 значение), через запятую: "5,3,7,..."
    val approaches: String
)

@Dao
interface BigRoundDao {

    @Insert
    suspend fun insertGame(game: BigRoundGameEntity): Long

    @Query("SELECT * FROM biground_games ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<BigRoundGameEntity>

    @Query("DELETE FROM biground_games")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM biground_games")
    suspend fun count(): Int
}

@Database(
    entities = [BigRoundGameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BigRoundDatabase : RoomDatabase() {
    abstract fun bigRoundDao(): BigRoundDao

    companion object {
        @Volatile
        private var INSTANCE: BigRoundDatabase? = null

        fun get(context: Context): BigRoundDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BigRoundDatabase::class.java,
                    "biground.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий
class BigRoundRepository(
    private val dao: BigRoundDao
) {
    suspend fun saveGame(
        category: BigRoundCategory,
        totalScore: Int,
        totalHits: Int,
        approaches: List<Int>
    ) {
        val entity = BigRoundGameEntity(
            dateMillis = System.currentTimeMillis(),
            categoryName = category.name,
            totalScore = totalScore,
            totalHits = totalHits,
            approaches = approaches.joinToString(",")
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<BigRoundGameEntity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()
}
