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

// Сохранённая игра x01
@Entity(tableName = "games_501")
data class Game501Entity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val gameTypeName: String,
    val outModeName: String,
    val isPairGame: Boolean,
    val playerNames: String,
    val playerIsBot: String,
    val winnerIndex: Int,
    val legsPlayed: Int,

    // Строки через "|" — по игрокам
    val matchDarts: String,
    val matchScore: String,
    val doublesHit: String,
    val doublesAttempted: String,
    val ppr: String,

    // Категории сумм
    val matchCount180: String = "",
    val matchCount170plus: String = "",
    val matchCount130plus: String = "",
    val matchCount90plus: String = "",
    val matchCount57plus: String = "",
    val matchCount57minus: String = "",

    // Первые 9
    val first9Score: String = "",
    val first9Darts: String = "",

    // Набор без закрытия
    val nonCloseScore: String = "",
    val nonCloseDarts: String = "",

    // Лучший PPR за лег
    val bestLegPpr: String = "",

    // Закрытые чекауты — остатки, с которых закрывал лег.
    // Формат: у каждого игрока свой список через "+", элементы через ",".
    // Пример: "16,20,40|32,50" (первый игрок закрыл с 16, 20, 40; второй — с 32, 50)
    val closeValues: String = ""
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
    version = 4,
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
            gameTypeName = game.gameType.name,
            outModeName = game.outMode.name,
            isPairGame = game.isPairGame,
            playerNames = game.players.joinToString("|") { it.name },
            playerIsBot = game.players.joinToString("|") { if (it.isBot) "1" else "0" },
            winnerIndex = game.winnerIndex ?: -1,
            legsPlayed = legsPlayed,
            matchDarts = game.players.joinToString("|") { it.matchDarts.toString() },
            matchScore = game.players.joinToString("|") { it.matchScoreGained.toString() },
            doublesHit = game.players.joinToString("|") { it.matchDoublesHit.toString() },
            doublesAttempted = game.players.joinToString("|") { it.matchDoublesAttempted.toString() },
            ppr = game.players.joinToString("|") { computePpr(it) },

            matchCount180 = game.players.joinToString("|") { it.matchCount180.toString() },
            matchCount170plus = game.players.joinToString("|") { it.matchCount170plus.toString() },
            matchCount130plus = game.players.joinToString("|") { it.matchCount130plus.toString() },
            matchCount90plus = game.players.joinToString("|") { it.matchCount90plus.toString() },
            matchCount57plus = game.players.joinToString("|") { it.matchCount57plus.toString() },
            matchCount57minus = game.players.joinToString("|") { it.matchCount57minus.toString() },
            first9Score = game.players.joinToString("|") { it.first9Score.toString() },
            first9Darts = game.players.joinToString("|") { it.first9Darts.toString() },
            nonCloseScore = game.players.joinToString("|") { it.nonCloseScore.toString() },
            nonCloseDarts = game.players.joinToString("|") { it.nonCloseDarts.toString() },
            bestLegPpr = game.players.joinToString("|") { computeBestLegPpr(it) },

            // Закрытые чекауты: у каждого игрока — свои значения, склеенные запятыми.
            // Игроки разделены "|", значения внутри игрока — запятыми.
            closeValues = game.players.joinToString("|") { player ->
                player.listOfCloseValues.joinToString(",")
            }
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

    private fun computeBestLegPpr(player: Player501): String {
        if (player.listOfLegPpr.isEmpty()) return "0.00"
        val best = player.listOfLegPpr.maxOrNull() ?: 0.0
        return String.format(Locale.US, "%.2f", best)
    }
}
