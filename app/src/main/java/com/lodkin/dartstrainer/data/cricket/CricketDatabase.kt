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
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Locale

// Сохранённая игра по крикету
@Entity(tableName = "cricket_games")
data class CricketGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val gameType: String,            // "CRICKET"
    val cricketType: String,         // "AMERICAN" / "NO_SCORE"
    val isPairGame: Boolean,
    val playerNames: String,
    val playerIsBot: String,
    val winnerIndex: Int,
    val legsPlayed: Int,
    val totalDarts: String,
    val misses: String,
    val triples: String,
    val bullAttempts: String,
    val bullHits: String,
    val perfectRounds: String,
    val strongRounds: String,
    val mpr: String,

    // ── Незавершённые партии ──
    // true — партия завершена, false — сохранена на середине.
    val isFinished: Boolean = true,
    // JSON-состояние партии. Пусто для завершённых.
    val stateBlob: String = "",
    // Когда последний раз обновляли партию (для незавершённых).
    val lastUpdateMillis: Long = 0L
)

@Dao
interface CricketDao {

    @Insert
    suspend fun insertGame(game: CricketGameEntity): Long

    @Update
    suspend fun updateGame(game: CricketGameEntity)

    // Только завершённые — для статистики.
    @Query("SELECT * FROM cricket_games WHERE gameType = 'CRICKET' AND isFinished = 1 ORDER BY dateMillis DESC")
    suspend fun getAllGames(): List<CricketGameEntity>

    // Все партии — для списка матчей (и завершённые, и незавершённые).
    @Query("SELECT * FROM cricket_games WHERE gameType = 'CRICKET' ORDER BY dateMillis DESC")
    suspend fun getAllGamesIncludingUnfinished(): List<CricketGameEntity>

    @Query("SELECT * FROM cricket_games WHERE id = :id")
    suspend fun getGameById(id: Long): CricketGameEntity?

    // ── Незавершённые партии ──
    @Query("SELECT * FROM cricket_games WHERE gameType = 'CRICKET' AND isFinished = 0 ORDER BY lastUpdateMillis DESC")
    suspend fun getUnfinishedGames(): List<CricketGameEntity>

    @Query("SELECT COUNT(*) FROM cricket_games WHERE gameType = 'CRICKET' AND isFinished = 0")
    suspend fun countUnfinished(): Int

    @Query("DELETE FROM cricket_games WHERE id = :id")
    suspend fun deleteGameById(id: Long)

    @Query("DELETE FROM cricket_games WHERE gameType = 'CRICKET'")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM cricket_games WHERE gameType = 'CRICKET'")
    suspend fun count(): Int
}

@Database(
    entities = [CricketGameEntity::class],
    version = 4,
    exportSchema = false
)
abstract class CricketDatabase : RoomDatabase() {
    abstract fun cricketDao(): CricketDao

    companion object {
        @Volatile
        private var INSTANCE: CricketDatabase? = null

        // Миграция 3 → 4: добавлены поля isFinished, stateBlob, lastUpdateMillis.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cricket_games ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE cricket_games ADD COLUMN stateBlob TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cricket_games ADD COLUMN lastUpdateMillis INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun get(context: Context): CricketDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CricketDatabase::class.java,
                    "cricket.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

// Репозиторий — единая точка доступа
class CricketRepository(
    private val dao: CricketDao
) {

    /**
     * Сохранить завершённую партию.
     * Если [existingId] > 0 — обновляем запись (партия была сохранена как незавершённая).
     * Иначе создаём новую.
     */
    suspend fun saveGame(game: CricketGame, existingId: Long = 0L): Long {
        val entity = gameToEntity(game, isFinished = true, stateBlob = "", existingId = existingId)
        return if (existingId > 0L) {
            dao.updateGame(entity)
            existingId
        } else {
            dao.insertGame(entity)
        }
    }

    /**
     * Сохранить незавершённую партию.
     * Если [existingId] > 0 — обновляем, иначе создаём новую.
     */
    suspend fun saveUnfinishedGame(game: CricketGame, existingId: Long = 0L): Long {
        val entity = gameToEntity(
            game = game,
            isFinished = false,
            stateBlob = CricketSerializer.toJson(game),
            existingId = existingId
        )
        return if (existingId > 0L) {
            dao.updateGame(entity)
            existingId
        } else {
            dao.insertGame(entity)
        }
    }

    suspend fun getAllGames(): List<CricketGameEntity> = dao.getAllGames()

    suspend fun getAllGamesIncludingUnfinished(): List<CricketGameEntity> =
        dao.getAllGamesIncludingUnfinished()

    suspend fun getGameById(id: Long): CricketGameEntity? = dao.getGameById(id)

    suspend fun getUnfinishedGames(): List<CricketGameEntity> = dao.getUnfinishedGames()

    suspend fun countUnfinished(): Int = dao.countUnfinished()

    suspend fun deleteGame(id: Long) = dao.deleteGameById(id)

    suspend fun clearAll() = dao.clearAll()

    private fun gameToEntity(
        game: CricketGame,
        isFinished: Boolean,
        stateBlob: String,
        existingId: Long
    ): CricketGameEntity {
        val legsPlayed = computeLegsPlayed(game)
        return CricketGameEntity(
            id = if (existingId > 0L) existingId else 0L,
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
            mpr = game.players.joinToString("|") { computeMpr(it) },
            isFinished = isFinished,
            stateBlob = stateBlob,
            lastUpdateMillis = System.currentTimeMillis()
        )
    }

    private fun computeLegsPlayed(game: CricketGame): Int {
        return (game.currentSetNumber - 1) * game.legsPerSet + game.currentLegNumber
    }

    private fun computeMpr(player: CricketPlayer): String {
        if (player.matchDartsThrown < 3) return "0.00"
        val marks = player.matchHits.values.sum()
        val rounds = player.matchDartsThrown / 3.0
        return String.format(Locale.US, "%.2f", marks / rounds)
    }
}
