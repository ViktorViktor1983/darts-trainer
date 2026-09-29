package com.lodkin.dartstrainer.data.cricket

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

// Сохранённая игра
@Entity(tableName = "cricket_games")
data class CricketGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val type: String,              // "AMERICAN" или "NO_SCORE"
    val playerNames: String,       // "Viktor|Bot1"
    val playerIsBot: String,       // "0|1"
    val winnerIndex: Int,
    val playerScores: String,      // "48|30"
    val playerDarts: String,       // "45|42"
    val playerHitsJson: String,    // JSON с попаданиями по секторам
    val playerScoresJson: String   // JSON с очками по секторам
)

@Dao
interface CricketDao {

    @Insert
    suspend fun insertGame(game: CricketGameEntity): Long

    @Query("SELECT * FROM cricket_games ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<CricketGameEntity>

    @Query("SELECT * FROM cricket_games WHERE id = :id")
    suspend fun getGameById(id: Long): CricketGameEntity?

    @Query("DELETE FROM cricket_games")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cricket_games")
    suspend fun count(): Int
}

@Database(
    entities = [CricketGameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CricketDatabase : RoomDatabase() {
    abstract fun cricketDao(): CricketDao

    companion object {
        @Volatile
        private var INSTANCE: CricketDatabase? = null

        fun get(context: Context): CricketDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CricketDatabase::class.java,
                    "cricket.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class CricketRepository(
    private val dao: CricketDao
) {
    suspend fun saveGame(game: CricketGame) {
        val entity = CricketGameEntity(
            dateMillis = System.currentTimeMillis(),
            type = game.type.name,
            playerNames = game.players.joinToString("|") { it.name },
            playerIsBot = game.players.joinToString("|") { if (it.isBot) "1" else "0" },
            winnerIndex = game.winnerIndex ?: -1,
            playerScores = game.players.joinToString("|") { it.totalScore.toString() },
            playerDarts = game.players.joinToString("|") { it.dartsThrown.toString() },
            playerHitsJson = serializeHits(game.players),
            playerScoresJson = serializeSectorScores(game.players)
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<CricketGameEntity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()

    private fun serializeHits(players: List<CricketPlayer>): String {
        // Формат: "20:3,19:2;20:3,19:2" — для каждого игрока
        return players.joinToString(";") { player ->
            CricketSector.ALL.joinToString(",") { sector ->
                "${sector.label}:${player.hits[sector] ?: 0}"
            }
        }
    }

    private fun serializeSectorScores(players: List<CricketPlayer>): String {
        return players.joinToString(";") { player ->
            CricketSector.ALL.joinToString(",") { sector ->
                "${sector.label}:${player.scores[sector] ?: 0}"
            }
        }
    }
}
