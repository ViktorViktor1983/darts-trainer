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
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    val closeValues: String = "",

    // ── Незавершённые партии ──
    // true — партия завершена, false — сохранена на середине.
    val isFinished: Boolean = true,
    // JSON-состояние партии (сериализованное Game501). Пусто для завершённых.
    val stateBlob: String = "",
    // Когда последний раз обновляли партию (для незавершённых).
    val lastUpdateMillis: Long = 0L
)

@Dao
interface Game501Dao {

    @Insert
    suspend fun insertGame(game: Game501Entity): Long

    @Update
    suspend fun updateGame(game: Game501Entity)

    @Query("SELECT * FROM games_501 ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<Game501Entity>

    @Query("SELECT * FROM games_501 WHERE id = :id")
    suspend fun getGameById(id: Long): Game501Entity?

    // ── Незавершённые партии ──
    @Query("SELECT * FROM games_501 WHERE isFinished = 0 ORDER BY lastUpdateMillis DESC")
    suspend fun getUnfinishedGames(): List<Game501Entity>

    @Query("SELECT * FROM games_501 WHERE isFinished = 0 AND gameTypeName = :gameTypeName AND outModeName = :outModeName ORDER BY lastUpdateMillis DESC")
    suspend fun getUnfinishedGamesByType(gameTypeName: String, outModeName: String): List<Game501Entity>

    @Query("SELECT COUNT(*) FROM games_501 WHERE isFinished = 0")
    suspend fun countUnfinished(): Int

    @Query("DELETE FROM games_501 WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM games_501")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM games_501")
    suspend fun count(): Int
}

@Database(
    entities = [Game501Entity::class],
    version = 5,
    exportSchema = false
)
abstract class Game501Database : RoomDatabase() {
    abstract fun game501Dao(): Game501Dao

    companion object {
        @Volatile
        private var INSTANCE: Game501Database? = null

        // Миграция 4 → 5: добавлены поля isFinished, stateBlob, lastUpdateMillis.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games_501 ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE games_501 ADD COLUMN stateBlob TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE games_501 ADD COLUMN lastUpdateMillis INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): Game501Database =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Game501Database::class.java,
                    "game501.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class Game501Repository(
    private val dao: Game501Dao
) {

    /**
     * Сохранить завершённую партию.
     * Если [existingId] > 0 — партия уже была сохранена как незавершённая,
     * и мы её просто обновляем (завершаем).
     * Иначе создаём новую запись.
     */
    suspend fun saveGame(game: Game501, existingId: Long = 0L): Long {
        val legsPlayed = (game.currentSetNumber - 1) * game.legsPerSet + game.currentLegNumber
        val entity = Game501Entity(
            id = if (existingId > 0L) existingId else 0L,
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

            closeValues = game.players.joinToString("|") { player ->
                player.listOfCloseValues.joinToString(",")
            },

            isFinished = true,
            stateBlob = "",
            lastUpdateMillis = System.currentTimeMillis()
        )

        return if (existingId > 0L) {
            dao.updateGame(entity)
            existingId
        } else {
            dao.insertGame(entity)
        }
    }

    /**
     * Сохранить незавершённую партию.
     * Если [existingId] > 0 — обновляем запись, иначе создаём новую.
     * Возвращает id сохранённой партии.
     */
    suspend fun saveUnfinishedGame(game: Game501, existingId: Long = 0L): Long {
        val legsPlayed = (game.currentSetNumber - 1) * game.legsPerSet + game.currentLegNumber
        val entity = Game501Entity(
            id = if (existingId > 0L) existingId else 0L,
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

            closeValues = game.players.joinToString("|") { player ->
                player.listOfCloseValues.joinToString(",")
            },

            isFinished = false,
            stateBlob = Game501Serializer.toJson(game),
            lastUpdateMillis = System.currentTimeMillis()
        )

        return if (existingId > 0L) {
            dao.updateGame(entity)
            existingId
        } else {
            dao.insertGame(entity)
        }
    }

    suspend fun getAllGames(): List<Game501Entity> = dao.getAllGames()

    suspend fun getGameById(id: Long): Game501Entity? = dao.getGameById(id)

    suspend fun getUnfinishedGames(): List<Game501Entity> = dao.getUnfinishedGames()

    suspend fun getUnfinishedGamesByType(gameTypeName: String, outModeName: String): List<Game501Entity> =
        dao.getUnfinishedGamesByType(gameTypeName, outModeName)

    suspend fun countUnfinished(): Int = dao.countUnfinished()

    suspend fun deleteGame(id: Long) = dao.deleteGameById(id)

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
