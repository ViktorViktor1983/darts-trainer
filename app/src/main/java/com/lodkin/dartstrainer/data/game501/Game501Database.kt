package com.lodkin.dartstrainer.data.game501

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.Locale

// Сохранённая игра 501
@Entity(tableName = "games_501")
data class Game501Entity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val modeName: String,              // "X501_DOUBLE_OUT" и т.д.
    val isPairGame: Boolean,
    val playerNames: String,           // "Виктор|Бот Любитель"
    val playerIsBot: String,           // "0|1"
    val winnerIndex: Int,
    val legsPlayed: Int,

    // Строки через "|" — по игрокам
    val matchDarts: String,            // "45|42"
    val matchScore: String,            // "501|498" — всего набрано очков
    val doublesHit: String,            // "3|1"
    val doublesAttempted: String,      // "8|5"
    val ppr: String                    // "62.15|55.30"
)

@Dao
interface Game501Dao {

    @Insert
    suspend fun insertGame(game: Game501Entity): Long

    @Query("SELECT * FROM games_501 ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<Game501Entity>

    @Query("SELECT * FROM games_501 WHERE id = :id")
    suspend fun getGameById(id: Long): Game501Entity?

    @Query("DELETE FROM games_501")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM games_501")
    suspend fun count(): Int
}

@Database(
    entities = [Game501Entity::class],
    version = 1,
    exportSchema = false
)
abstract class Game501Database : RoomDatabase() {
    abstract fun game501Dao(): Game501Dao

    companion object {
        @Volatile
        private var INSTANCE: Game501Database? = null

        fun get(context: Context): Game501Database =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Game501Database::class.java,
                    "game501.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class Game501Repository(
    private val dao: Game501Dao
) {
    suspend fun saveGame(game: Game501) {
        val legsPlayed = (game.currentSetNumber - 1) * game.legsPerSet + game.currentLegNumber
        val entity = Game501Entity(
            dateMillis = System.currentTimeMillis(),
            modeName = game.mode.name,
            isPairGame = game.isPairGame,
            playerNames = game.players.joinToString("|") { it.name },
            playerIsBot = game.players.joinToString("|") { if (it.isBot) "1" else "0" },
            winnerIndex = game.winnerIndex ?: -1,
            legsPlayed = legsPlayed,
            matchDarts = game.players.joinToString("|") { it.matchDarts.toString() },
            matchScore = game.players.joinToString("|") { it.matchScoreGained.toString() },
            doublesHit = game.players.joinToString("|") { it.matchDoublesHit.toString() },
            doublesAttempted = game.players.joinToString("|") { it.matchDoublesAttempted.toString() },
            ppr = game.players.joinToString("|") { computePpr(it) }
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<Game501Entity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()

    private fun computePpr(player: Player501): String {
        if (player.matchDarts < 3) return "0.00"
        val ppr = player.matchScoreGained.toDouble() / (player.matchDarts / 3.0)
        return String.format(Locale.US, "%.2f", ppr)
    }
}
