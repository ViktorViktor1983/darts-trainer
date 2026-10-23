package com.lodkin.dartstrainer.ui

import androidx.compose.runtime.Composable
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketLogic
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.freerecords.FreePlayRecordsRepository
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Logic
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.data.training.BlockType
import com.lodkin.dartstrainer.data.training.ExerciseResult
import com.lodkin.dartstrainer.data.training.TrainingBotLevelStorage
import com.lodkin.dartstrainer.data.training.TrainingRepository
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockGameScreen
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockSetupScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundSetupScreen
import com.lodkin.dartstrainer.ui.cricket.CricketGameScreen
import com.lodkin.dartstrainer.ui.cricket.CricketSetupScreen
import com.lodkin.dartstrainer.ui.cricket.CricketStatsScreen
import com.lodkin.dartstrainer.ui.game501.Game501Screen
import com.lodkin.dartstrainer.ui.game501.Game501SetupScreen
import com.lodkin.dartstrainer.ui.game501.Game501StatsScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetGameScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetSetupScreen
import com.lodkin.dartstrainer.ui.sector.SectorGameScreen
import com.lodkin.dartstrainer.ui.sector.SectorSetupScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundGameScreen
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GameRoutes(
    screen: String,
    onScreenChange: (String) -> Unit,
    playerName: String,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    
    cricketRepository: CricketRepository,
    game501Repository: Game501Repository,
    sectorRepository: SectorRepository,
    aroundClockRepository: AroundClockRepository,
    bigRoundRepository: BigRoundRepository,
    scoreSetRepository: ScoreSetRepository,
    freePlayRecordsRepository: FreePlayRecordsRepository,
    trainingRepository: TrainingRepository,

    cricketGame: CricketGame?,
    onCricketGameChange: (CricketGame?) -> Unit,
    cricketExistingId: Long,
    onCricketExistingIdChange: (Long) -> Unit,
    
    game501: Game501?,
    onGame501Change: (Game501?) -> Unit,
    game501ExistingId: Long,
    onGame501ExistingIdChange: (Long) -> Unit,
    
    sectorToPlay: Int?,
    onSectorToPlayChange: (Int?) -> Unit,
    
    aroundClockTarget: AroundClockTarget?,
    onAroundClockTargetChange: (AroundClockTarget?) -> Unit,
    aroundClockOrder: AroundClockOrder?,
    onAroundClockOrderChange: (AroundClockOrder?) -> Unit,
    aroundClockHits: Int,
    onAroundClockHitsChange: (Int) -> Unit,
    
    bigRoundCategory: BigRoundCategory?,
    onBigRoundCategoryChange: (BigRoundCategory?) -> Unit,

    sessionStartTime: Long,
    sessionForm: Double,

    isTraining501: Boolean = false,
    isTrainingCricket: Boolean = false,
    isOnlineGame: Boolean = false,
    trainingSessionId: Long = 0L,
    exerciseStartMillis: Long = 0L,
    isNormativesExercise: Boolean = false,
    onTrainingGameFinished: (playerWon: Boolean, isGame501: Boolean) -> Unit = { _, _ -> }
) {
    when (screen) {
        "game_select" -> GameSelectScreen(
            onCricket = { onScreenChange("cricket_setup") },
            on501 = { onScreenChange("game501_setup") },
            onSector = { onScreenChange("sector_setup") },
            onAroundClock = { onScreenChange("aroundclock_setup") },
            onBigRound = { onScreenChange("biground_setup") },
            onScoreSet = { onScreenChange("scoreset_setup") },
            onBack = { onScreenChange("main") }
        )

        "cricket_setup" -> CricketSetupScreen(
            playerName = playerName,
            onStartGame = { type, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds ->
                val newGame = CricketLogic.newGame(
                    type, players, legsPerSet, setsPerMatch, isPairGame,
                    startingTeam, autoOkSeconds, sessionStartTime, sessionForm
                )
                onCricketGameChange(newGame)
                onCricketExistingIdChange(0L)
                onScreenChange("cricket_game")
            },
            onBack = { onScreenChange("game_select") }
        )

        "cricket_game" -> {
            val g = cricketGame
            if (g != null) {
                CricketGameScreen(
                    repository = cricketRepository,
                    freePlayRecordsRepository = freePlayRecordsRepository,
                    initialGame = g,
                    existingId = cricketExistingId,
                    isTrainingMode = isTrainingCricket,
                    isOnlineGame = isOnlineGame, // ПЕРЕДАЕМ ФЛАГ
                    trainingRepository = trainingRepository,
                    onGameFinish = { f: CricketGame, id: Long ->
                        if (isTrainingCricket) {
                            val playerWon = f.winnerIndex == 0
                            val player = f.players.getOrNull(0)
                            val mpr = player?.let { p ->
                                if (p.matchDartsThrown > 0) {
                                    val marks = p.matchHits.values.sum().toFloat()
                                    marks / (p.matchDartsThrown / 3f)
                                } else 0f
                            } ?: 0f
                            val mprStr = String.format(Locale.US, "%.1f", mpr)
                            val dur = if (exerciseStartMillis > 0L) ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt() else 0
                            val botPlayer = f.players.getOrNull(1)
                            if (botPlayer?.isBot == true) {
                                TrainingBotLevelStorage.updateLevelCricket(context, botPlayer.botLevel, playerWon)
                            }
                            
                            val c9r = player?.matchPerfectRounds ?: 0
                            val c6r = player?.matchStrongRounds ?: 0
                            val miss = player?.matchMissesThrown ?: 0
                            val tripl = player?.matchTriplesHit ?: 0
                            val bulla = player?.matchBullAttempts ?: 0
                            val bullh = player?.matchBullHits ?: 0

                            val details = "won=$playerWon;mpr=$mprStr" +
                                    ";c9r=$c9r;c6r=$c6r;miss=$miss;tripl=$tripl;bulla=$bulla;bullh=$bullh"

                            scope.launch {
                                trainingRepository.insertExerciseResult(
                                    ExerciseResult(
                                        sessionId = trainingSessionId,
                                        blockType = BlockType.CRICKET,
                                        exerciseId = "cricket",
                                        score = mpr.toInt(),
                                        dartsThrown = player?.matchDartsThrown ?: 0,
                                        timestamp = System.currentTimeMillis(),
                                        durationSeconds = dur,
                                        detailsJson = details
                                    )
                                )
                            }
                            onTrainingGameFinished(playerWon, false)
                        } else {
                            scope.launch { cricketRepository.saveGame(f, existingId = id) }
                            onCricketGameChange(f)
                            onCricketExistingIdChange(0L)
                            onScreenChange(if (isOnlineGame) "online_lobby" else "cricket_stats")
                        }
                    },
                    onBack = {
                        onCricketGameChange(null)
                        onCricketExistingIdChange(0L)
                        onScreenChange(
                            when {
                                isOnlineGame -> "online_lobby"
                                isTrainingCricket -> "training_free_next"
                                else -> "game_select"
                            }
                        )
                    }
                )
            } else {
                onScreenChange(if (isOnlineGame) "online_lobby" else "cricket_setup")
            }
        }

        "cricket_stats" -> {
            val g = cricketGame
            if (g != null) {
                CricketStatsScreen(
                    game = g,
                    onPlayAgain = { onScreenChange("cricket_setup") },
                    onBackToMenu = {
                        onCricketGameChange(null)
                        onScreenChange("main")
                    }
                )
            } else {
                onScreenChange("main")
            }
        }

        "game501_setup" -> Game501SetupScreen(
            playerName = playerName,
            onStartGame = { gameType, outMode, players, legsPerSet, setsPerMatch, isPairGame, startingTeam, autoOkSeconds ->
                val newGame = Game501Logic.newGame(
                    gameType, outMode, players, legsPerSet, setsPerMatch, isPairGame,
                    startingTeam, autoOkSeconds, sessionStartTime, sessionForm
                )
                onGame501Change(newGame)
                onGame501ExistingIdChange(0L)
                onScreenChange("game501_game")
            },
            onBack = { onScreenChange("game_select") }
        )

        "game501_game" -> {
            val g = game501
            if (g != null) {
                Game501Screen(
                    repository = game501Repository,
                    freePlayRecordsRepository = freePlayRecordsRepository,
                    initialGame = g,
                    existingId = game501ExistingId,
                    isTrainingMode = isTraining501,
                    isOnlineGame = isOnlineGame,
                    trainingRepository = trainingRepository,
                    onGameFinish = { f: Game501, id: Long ->
                        if (isTraining501) {
                            val playerWon = f.winnerIndex == 0
                            val player = f.players.getOrNull(0)
                            val ppr = player?.let { p -> Game501Logic.matchPpr(p) } ?: 0.0
                            val dbl = player?.let { p -> Game501Logic.doublesAccuracy(p) } ?: 0.0
                            val pprStr = String.format(Locale.US, "%.1f", ppr)
                            val dblStr = dbl.toInt().toString()
                            val closeValues = player?.listOfCloseValues ?: emptyList()
                            val dur = if (exerciseStartMillis > 0L) ((System.currentTimeMillis() - exerciseStartMillis) / 1000L).toInt() else 0
                            val botPlayer = f.players.getOrNull(1)
                            if (botPlayer?.isBot == true) {
                                TrainingBotLevelStorage.updateLevel501(context, botPlayer.botLevel, playerWon)
                            }
                            
                            val c180 = player?.matchCount180 ?: 0
                            val c170 = player?.matchCount170plus ?: 0
                            val c130 = player?.matchCount130plus ?: 0
                            val c90 = player?.matchCount90plus ?: 0
                            val c57p = player?.matchCount57plus ?: 0
                            val c57m = player?.matchCount57minus ?: 0
                            val first9 = player?.listOfFirst9Ppr?.average() ?: 0.0

                            val details = "won=$playerWon;ppr=$pprStr;dbl=$dblStr" +
                                    ";c180=$c180;c170=$c170;c130=$c130;c90=$c90;c57p=$c57p;c57m=$c57m" +
                                    ";first9=${String.format(Locale.US, "%.2f", first9)}"

                            scope.launch {
                                trainingRepository.insertExerciseResult(
                                    ExerciseResult(
                                        sessionId = trainingSessionId,
                                        blockType = BlockType.GAME_501,
                                        exerciseId = "game501",
                                        score = ppr.toInt(),
                                        dartsThrown = player?.matchDarts ?: 0,
                                        timestamp = System.currentTimeMillis(),
                                        durationSeconds = dur,
                                        detailsJson = details
                                    )
                                )
                                closeValues.forEach { value ->
                                    trainingRepository.recordCheckout(value)
                                }
                            }
                            onTrainingGameFinished(playerWon, true)
                        } else {
                            scope.launch { game501Repository.saveGame(f, existingId = id) }
                            onGame501Change(f)
                            onGame501ExistingIdChange(0L)
                            onScreenChange(if (isOnlineGame) "online_lobby" else "game501_stats")
                        }
                    },
                    onBack = {
                        onGame501Change(null)
                        onGame501ExistingIdChange(0L)
                        onScreenChange(
                            when {
                                isOnlineGame -> "online_lobby"
                                isTraining501 -> "training_free_next"
                                else -> "game_select"
                            }
                        )
                    }
                )
            } else {
                onScreenChange(if (isOnlineGame) "online_lobby" else "game501_setup")
            }
        }

        "game501_stats" -> {
            val g = game501
            if (g != null) {
                Game501StatsScreen(
                    game = g,
                    onPlayAgain = { onScreenChange("game501_setup") },
                    onBackToMenu = {
                        onGame501Change(null)
                        onScreenChange("main")
                    }
                )
            } else {
                onScreenChange("main")
            }
        }

        "sector_setup" -> SectorSetupScreen(
            sectorRepository = sectorRepository,
            onStartGame = { s ->
                onSectorToPlayChange(s)
                onScreenChange("sector_game")
            },
            onOpenStats = { onScreenChange("sector_stats") },
            onBack = { onScreenChange("game_select") }
        )

        "sector_game" -> {
            val s = sectorToPlay
            if (s != null) {
                SectorGameScreen(
                    sector = s, repository = sectorRepository,
                    isTrainingMode = false,
                    onFinish = { _ ->
                        onSectorToPlayChange(null)
                        onScreenChange("game_select")
                    },
                    onBack = { onScreenChange("game_select") }
                )
            } else {
                onScreenChange("sector_setup")
            }
        }

        "aroundclock_setup" -> AroundClockSetupScreen(
            playerName = playerName,
            repository = aroundClockRepository,
            onStartGame = { t, o, h ->
                onAroundClockTargetChange(t)
                onAroundClockOrderChange(o)
                onAroundClockHitsChange(h)
                onScreenChange("aroundclock_game")
            },
            onOpenStats = { onScreenChange("aroundclock_stats") },
            onBack = { onScreenChange("game_select") }
        )

        "aroundclock_game" -> {
            val t = aroundClockTarget
            val o = aroundClockOrder
            if (t != null && o != null) {
                AroundClockGameScreen(
                    target = t, order = o, hitsRequired = aroundClockHits,
                    repository = aroundClockRepository,
                    onFinish = { td, c, sr ->
                        scope.launch {
                            aroundClockRepository.saveGame(t, o, aroundClockHits, td, c, sr)
                        }
                        onAroundClockTargetChange(null)
                        onAroundClockOrderChange(null)
                        onAroundClockHitsChange(1)
                        onScreenChange("game_select")
                    },
                    onBack = { onScreenChange("game_select") }
                )
            } else {
                onScreenChange("aroundclock_setup")
            }
        }

        "biground_setup" -> BigRoundSetupScreen(
            playerName = playerName,
            repository = bigRoundRepository,
            onStartGame = { c ->
                onBigRoundCategoryChange(c)
                onScreenChange("biground_game")
            },
            onOpenStats = { onScreenChange("biground_stats") },
            onBack = { onScreenChange("game_select") }
        )

        "biground_game" -> {
            val c = bigRoundCategory
            if (c != null) {
                BigRoundGameScreen(
                    category = c, repository = bigRoundRepository,
                    isTrainingMode = false,
                    onFinish = { _ ->
                        onBigRoundCategoryChange(null)
                        onScreenChange("game_select")
                    },
                    onBack = { onScreenChange("game_select") }
                )
            } else {
                onScreenChange("biground_setup")
            }
        }

        "scoreset_setup" -> ScoreSetSetupScreen(
            playerName = playerName,
            repository = scoreSetRepository,
            onStartGame = { onScreenChange("scoreset_game") },
            onOpenStats = { onScreenChange("scoreset_stats") },
            onBack = { onScreenChange("game_select") }
        )

        "scoreset_game" -> ScoreSetGameScreen(
            repository = scoreSetRepository,
            isTrainingMode = false,
            onFinish = { ts, ap ->
                scope.launch { scoreSetRepository.saveGame(ts, ap) }
                onScreenChange("game_select")
            },
            onBack = { onScreenChange("game_select") }
        )
    }
}
