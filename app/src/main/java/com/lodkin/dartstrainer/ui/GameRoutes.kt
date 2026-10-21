package com.lodkin.dartstrainer.ui

import androidx.compose.runtime.Composable
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.aroundclock.AroundClockOrder
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.aroundclock.AroundClockTarget
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketGame
import com.lodkin.dartstrainer.data.cricket.CricketLogic
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.cricket.CricketSerializer
import com.lodkin.dartstrainer.data.freerecords.FreePlayRecordsRepository
import com.lodkin.dartstrainer.data.game501.Game501
import com.lodkin.dartstrainer.data.game501.Game501Logic
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.game501.Game501Serializer
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
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

/**
 * Маршрутизатор для экранов свободной игры (не тренировки).
 */
@Composable
fun GameRoutes(
    screen: String,
    onScreenChange: (String) -> Unit,
    playerName: String,
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    
    // Репозитории
    cricketRepository: CricketRepository,
    game501Repository: Game501Repository,
    sectorRepository: SectorRepository,
    aroundClockRepository: AroundClockRepository,
    bigRoundRepository: BigRoundRepository,
    scoreSetRepository: ScoreSetRepository,
    freePlayRecordsRepository: FreePlayRecordsRepository,
    trainingRepository: TrainingRepository,

    // Состояния игр
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
    sessionForm: Double
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
                    isTrainingMode = false,
                    trainingRepository = trainingRepository,
                    onGameFinish = { f, id ->
                        scope.launch { cricketRepository.saveGame(f, existingId = id) }
                        onCricketGameChange(f)
                        onCricketExistingIdChange(0L)
                        onScreenChange("cricket_stats")
                    },
                    onBack = {
                        onCricketGameChange(null)
                        onCricketExistingIdChange(0L)
                        onScreenChange("game_select")
                    }
                )
            } else {
                onScreenChange("cricket_setup")
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
                    isTrainingMode = false,
                    trainingRepository = trainingRepository,
                    onGameFinish = { f, id ->
                        scope.launch { game501Repository.saveGame(f, existingId = id) }
                        onGame501Change(f)
                        onGame501ExistingIdChange(0L)
                        onScreenChange("game501_stats")
                    },
                    onBack = {
                        onGame501Change(null)
                        onGame501ExistingIdChange(0L)
                        onScreenChange("game_select")
                    }
                )
            } else {
                onScreenChange("game501_setup")
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
