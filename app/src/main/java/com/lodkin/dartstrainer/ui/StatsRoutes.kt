package com.lodkin.dartstrainer.ui

import androidx.compose.runtime.Composable
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.cricket.CricketSerializer
import com.lodkin.dartstrainer.data.freerecords.FreePlayRecordsRepository
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.game501.Game501Serializer
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.ui.aroundclock.AroundClockStatsScreen
import com.lodkin.dartstrainer.ui.biground.BigRoundStatsScreen
import com.lodkin.dartstrainer.ui.cricket.CricketStatsScreen
import com.lodkin.dartstrainer.ui.game501.Game501StatsScreen
import com.lodkin.dartstrainer.ui.scoreset.ScoreSetStatsScreen
import com.lodkin.dartstrainer.ui.sector.SectorStatsScreen

/**
 * Маршрутизатор для экранов статистики.
 * Вынесен из MainActivity, чтобы уменьшить его размер.
 */
@Composable
fun StatsRoutes(
    screen: String,
    onScreenChange: (String) -> Unit,
    onResumeGame501: (com.lodkin.dartstrainer.data.game501.Game501Entity) -> Unit,
    onResumeCricket: (com.lodkin.dartstrainer.data.cricket.CricketGameEntity) -> Unit,
    cricketRepository: CricketRepository,
    game501Repository: Game501Repository,
    sectorRepository: SectorRepository,
    aroundClockRepository: AroundClockRepository,
    bigRoundRepository: BigRoundRepository,
    scoreSetRepository: ScoreSetRepository,
    freePlayRecordsRepository: FreePlayRecordsRepository,
    statsMenuKey: Int,
    onStatsMenuKeyChange: (Int) -> Unit
) {
    when (screen) {
        "stats_menu" -> StatsMenuScreen(
            cricketRepository = cricketRepository,
            reloadKey = statsMenuKey,
            onResumeGame501 = onResumeGame501,
            onResumeCricket = onResumeCricket,
            onCricket = { onScreenChange("stats_cricket") },
            on501 = { onScreenChange("stats_501") },
            onSector = { onScreenChange("sector_stats") },
            onBigRound = { onScreenChange("biground_stats") },
            onScoreSet = { onScreenChange("scoreset_stats") },
            onAroundClock = { onScreenChange("aroundclock_stats") },
            onBack = { onScreenChange("main") }
        )

        "stats_cricket" -> StatsScreen(
            repository = cricketRepository,
            freePlayRecordsRepository = freePlayRecordsRepository,
            initialTab = 0, showTabs = false,
            onResumeCricket = onResumeCricket,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )

        "stats_501" -> StatsScreen(
            repository = cricketRepository,
            freePlayRecordsRepository = freePlayRecordsRepository,
            initialTab = 1, showTabs = false,
            onResumeGame501 = onResumeGame501,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )

        "sector_stats" -> SectorStatsScreen(
            repository = sectorRepository,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )

        "aroundclock_stats" -> AroundClockStatsScreen(
            repository = aroundClockRepository,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )

        "biground_stats" -> BigRoundStatsScreen(
            repository = bigRoundRepository,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )

        "scoreset_stats" -> ScoreSetStatsScreen(
            repository = scoreSetRepository,
            onBack = {
                onStatsMenuKeyChange(statsMenuKey + 1)
                onScreenChange("stats_menu")
            }
        )
    }
}
