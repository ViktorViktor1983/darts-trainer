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

// Сохранённая игра по крикету
@Entity(tableName = "cricket_games")
data class CricketGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val gameType: String,            // "CRICKET" (на будущее — "501")
    val cricketType: String,         // "AMERICAN" / "NO_SCORE"
    val isPairGame: Boolean,         // парная или одиночная
    val playerNames: String,         // "Виктор|Бот Любитель"
    val playerIsBot: String,         // "0|1"
    val winnerIndex: Int,
    val legsPlayed: Int,             // сколько легов было в матче
    val totalDarts: String,          // "45|42"
    val misses: String,              // "12|8"
    val triples: String,             // "5|3" (включая T-Bull? — считаем все утроения 15-20)
    val bullAttempts: String,        // "4|2" прицельных
    val bullHits: String,            // "2|1"
    val perfectRounds: String,       // "1|0" идеальных (8-9 меток)
    val strongRounds: String,        // "3|2" сильных (6-7 меток)
    val mpr: String                  // "1.85|2.10"
)

@Dao
interface CricketDao {

    @Insert
    suspend fun insertGame(game: CricketGameEntity): Long

    @Query("SELECT * FROM cricket_games WHERE gameType = 'CRICKET' ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<CricketGameEntity>

    @Query("SELECT * FROM cricket_games WHERE id = :id")
    suspend fun getGameById(id: Long): CricketGameEntity?

    @Query("DELETE FROM cricket_games WHERE gameType = 'CRICKET'")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cricket_games WHERE gameType = 'CRICKET'")
    suspend fun count(): Int
}

@Database(
    entities = [CricketGameEntity::class],
    version = 2,
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
        val legsPlayed = computeLegsPlayed(game)
        val entity = CricketGameEntity(
            dateMillis = System.currentTimeMillis(),
            gameType = "CRICKET",
            cricketType = game.type.name,
            isPairGame = game.isPairGame,
            playerNames = game.players.joinToString("|") { it.name },
            playerIsBot = game.players.joinToString("|") { if (it.isBot) "1" else "0" },
            winnerIndex = game.winnerIndex ?: -1,
            legsPlayed = legsPlayed,
            totalDarts = game.players.joinToString("|") { it.matchDartsThrown.toString() },
            misses = game.players.joinToString("|") { it.matchMissesThrown.toString() },
            triples = game.players.joinToString("|") { it.matchTriplesHit.toString() },
            bullAttempts = game.players.joinToString("|") { it.matchBullAttempts.toString() },
            bullHits = game.players.joinToString("|") { it.matchBullHits.toString() },
            perfectRounds = game.players.joinToString("|") { it.matchPerfectRounds.toString() },
            strongRounds = game.players.joinToString("|") { it.matchStrongRounds.toString() },
            mpr = game.players.joinToString("|") { computeMpr(it) }
        )
        dao.insertGame(entity)
    }

    suspend fun getAllGames(): List<CricketGameEntity> = dao.getAllGames()

    suspend fun clearAll() = dao.clearAll()

    // Сколько всего легов было в матче.
    // Формула: (номер сета - 1) * legsPerSet + номер лега.
    // Если матч завершён, то номер сета и лега уже актуальны для последнего лега.
    private fun computeLegsPlayed(game: CricketGame): Int {
        return (game.currentSetNumber - 1) * game.legsPerSet + game.currentLegNumber
    }

    private fun computeMpr(player: CricketPlayer): String {
        if (player.matchDartsThrown < 3) return "0.00"
        val marks = player.matchHits.values.sum()
        val rounds = player.matchDartsThrown / 3.0
        return "%.2f".format(marks / rounds)
    }
}
