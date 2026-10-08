package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.SettingsStorage
import com.lodkin.dartstrainer.data.aroundclock.AroundClockDatabase
import com.lodkin.dartstrainer.data.aroundclock.AroundClockRepository
import com.lodkin.dartstrainer.data.biground.BigRoundDatabase
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.data.cricket.CricketGameEntity
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Entity
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.data.scoreset.ScoreSetDatabase
import com.lodkin.dartstrainer.data.scoreset.ScoreSetRepository
import com.lodkin.dartstrainer.data.sector.SectorDatabase
import com.lodkin.dartstrainer.data.sector.SectorRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsMenuScreen(
    cricketRepository: CricketRepository,
    reloadKey: Int = 0,
    onResumeGame501: (Game501Entity) -> Unit = {},
    onResumeCricket: (CricketGameEntity) -> Unit = {},
    onCricket: () -> Unit,
    on501: () -> Unit,
    onSector: () -> Unit,
    onBigRound: () -> Unit,
    onScoreSet: () -> Unit,
    onAroundClock: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val game501Repo = remember {
        Game501Repository(Game501Database.get(context).game501Dao())
    }
    val sectorRepo = remember {
        SectorRepository(SectorDatabase.get(context).sectorDao())
    }
    val bigRoundRepo = remember {
        BigRoundRepository(BigRoundDatabase.get(context).bigRoundDao())
    }
    val scoreSetRepo = remember {
        ScoreSetRepository(ScoreSetDatabase.get(context).scoreSetDao())
    }
    val aroundClockRepo = remember {
        AroundClockRepository(AroundClockDatabase.get(context).aroundClockDao())
    }
    val ownerName = remember { SettingsStorage.getPlayerName(context).trim() }

    var avg501Ppr by remember { mutableStateOf<String?>(null) }
    var avgCricketMpr by remember { mutableStateOf<String?>(null) }
    var bestSectorScore by remember { mutableStateOf<String?>(null) }
    var bestBigRoundScore by remember { mutableStateOf<String?>(null) }
    var bestScoreSetScore by remember { mutableStateOf<String?>(null) }
    var bestAroundClock by remember { mutableStateOf<String?>(null) }

    var unfinished501 by remember { mutableStateOf<List<Game501Entity>>(emptyList()) }
    var unfinishedCricket by remember { mutableStateOf<List<CricketGameEntity>>(emptyList()) }

    var showUnfinishedDialog501 by remember { mutableStateOf(false) }
    var showUnfinishedDialogCricket by remember { mutableStateOf(false) }

    var localReloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey, localReloadKey) {
        // ── 501: средний PPR ──
        val games501 = game501Repo.getAllGames()
        var pprSum = 0.0
        var pprCount = 0
        for (g in games501) {
            if (!g.isFinished) continue
            if (g.outModeName == "STRAIGHT_OUT") continue
            val bots = parseStringList(g.playerIsBot)
            val names = parseStringList(g.playerNames)
            val pprList = parseDoubleList(g.ppr)
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (isOwner(bots[i], name, ownerName)) {
                    pprList.getOrNull(i)?.let { pprSum += it; pprCount++ }
                    break
                }
            }
        }
        avg501Ppr = if (pprCount > 0) "%.2f".format(Locale.US, pprSum / pprCount) else "—"

        unfinished501 = games501.filter { !it.isFinished }
            .sortedByDescending { it.lastUpdateMillis }

        // ── Крикет: средний MPR ──
        val gamesCricket = cricketRepository.getAllGamesIncludingUnfinished()
        var mprSum = 0.0
        var mprCount = 0
        for (g in gamesCricket) {
            if (!g.isFinished) continue
            val bots = parseStringList(g.playerIsBot)
            val names = parseStringList(g.playerNames)
            val mprList = parseDoubleList(g.mpr)
            for (i in bots.indices) {
                val name = names.getOrNull(i) ?: ""
                if (isOwner(bots[i], name, ownerName)) {
                    mprList.getOrNull(i)?.let { mprSum += it; mprCount++ }
                    break
                }
            }
        }
        avgCricketMpr = if (mprCount > 0) "%.2f".format(Locale.US, mprSum / mprCount) else "—"

        unfinishedCricket = gamesCricket.filter { !it.isFinished }
            .sortedByDescending { it.lastUpdateMillis }

        val sectorGames = sectorRepo.getAllGames()
        val bestSector = sectorGames.maxOfOrNull { it.totalScore }
        bestSectorScore = if (bestSector != null && bestSector > 0) bestSector.toString() else "—"

        val bigRoundGames = bigRoundRepo.getAllGames()
        val bestBig = bigRoundGames.maxOfOrNull { it.totalScore }
        bestBigRoundScore = if (bestBig != null && bestBig > 0) bestBig.toString() else "—"

        val scoreSetGames = scoreSetRepo.getAllGames()
        val bestScoreSet = scoreSetGames.maxOfOrNull { it.totalScore }
        bestScoreSetScore = if (bestScoreSet != null && bestScoreSet > 0) bestScoreSet.toString() else "—"

        val aroundGames = aroundClockRepo.getAllGames()
        val completedGames = aroundGames.filter { it.completed }
        val best = completedGames.minOfOrNull { it.totalDarts }
        bestAroundClock = if (best != null && best > 0) best.toString() else "—"
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ── ШАПКА (заголовок и подпись ×2, кнопка Назад длиннее) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text("←", color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Статистика",
                    color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold
                )
                if (ownerName.isNotBlank()) {
                    Text("игрок: $ownerName", color = Accent, fontSize = 22.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── СЕТКА 2 x 3 ──
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "501",
                metric = avg501Ppr ?: "...",
                metricLabel = "Средний PPR",
                badge = unfinished501.size,
                onClick = on501,
                onBadgeClick = { showUnfinishedDialog501 = true },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Крикет",
                metric = avgCricketMpr ?: "...",
                metricLabel = "Средний MPR",
                badge = unfinishedCricket.size,
                onClick = onCricket,
                onBadgeClick = { showUnfinishedDialogCricket = true },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "Сектор",
                metric = bestSectorScore ?: "...",
                metricLabel = "Лучший результат",
                badge = 0,
                onClick = onSector,
                onBadgeClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Большой раунд",
                metric = bestBigRoundScore ?: "...",
                metricLabel = "Лучший результат",
                badge = 0,
                onClick = onBigRound,
                onBadgeClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "Набор очков",
                metric = bestScoreSetScore ?: "...",
                metricLabel = "Лучший результат",
                badge = 0,
                onClick = onScoreSet,
                onBadgeClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Кругосветка",
                metric = bestAroundClock ?: "...",
                metricLabel = "Лучший результат",
                badge = 0,
                onClick = onAroundClock,
                onBadgeClick = {},
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }

    if (showUnfinishedDialog501) {
        UnfinishedDialog501(
            games = unfinished501,
            onResume = { entity ->
                showUnfinishedDialog501 = false
                onResumeGame501(entity)
            },
            onDelete = { entity ->
                scope.launch {
                    game501Repo.deleteGame(entity.id)
                    localReloadKey++
                }
            },
            onDismiss = { showUnfinishedDialog501 = false }
        )
    }

    if (showUnfinishedDialogCricket) {
        UnfinishedDialogCricket(
            games = unfinishedCricket,
            onResume = { entity ->
                showUnfinishedDialogCricket = false
                onResumeCricket(entity)
            },
            onDelete = { entity ->
                scope.launch {
                    cricketRepository.deleteGame(entity.id)
                    localReloadKey++
                }
            },
            onDismiss = { showUnfinishedDialogCricket = false }
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    metric: String,
    metricLabel: String,
    badge: Int,
    onClick: () -> Unit,
    onBadgeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(TileBg)
                .clickable { onClick() }
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                metric,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                metricLabel,
                color = Accent,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }

        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ErrorColor)
                    .clickable { onBadgeClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "⏸ $badge",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UnfinishedDialog501(
    games: List<Game501Entity>,
    onResume: (Game501Entity) -> Unit,
    onDelete: (Game501Entity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отложить", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "Незавершённые партии 501",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (games.size == 1) "Можно продолжить или удалить."
                    else "Найдено ${games.size} партий.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (games.isEmpty()) {
                Text(
                    "Пока нет незавершённых партий.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    games.forEach { entity ->
                        UnfinishedCard501(
                            entity = entity,
                            onResume = { onResume(entity) },
                            onDelete = { onDelete(entity) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun UnfinishedCard501(
    entity: Game501Entity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val names = entity.playerNames.split("|")
    val scores = entity.matchScore.split("|")
    val dateStr = formatDate(entity.lastUpdateMillis.takeIf { it > 0 } ?: entity.dateMillis)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1A1A))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸ НЕЗАВЕРШЕНА",
                color = ErrorColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                dateStr,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            names.joinToString("  ·  "),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            buildString {
                append("Легов: ${entity.legsPlayed}")
                if (scores.size == names.size && scores.isNotEmpty()) {
                    append("   ·   очков: ")
                    append(scores.joinToString(" / "))
                }
            },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .clickable { onResume() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onDelete() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Удалить",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UnfinishedDialogCricket(
    games: List<CricketGameEntity>,
    onResume: (CricketGameEntity) -> Unit,
    onDelete: (CricketGameEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отложить", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "Незавершённые партии в крикет",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (games.size == 1) "Можно продолжить или удалить."
                    else "Найдено ${games.size} партий.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            if (games.isEmpty()) {
                Text(
                    "Пока нет незавершённых партий.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    games.forEach { entity ->
                        UnfinishedCardCricket(
                            entity = entity,
                            onResume = { onResume(entity) },
                            onDelete = { onDelete(entity) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    )
}

@Composable
private fun UnfinishedCardCricket(
    entity: CricketGameEntity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val names = entity.playerNames.split("|")
    val mprs = entity.mpr.split("|")
    val dateStr = formatDate(entity.lastUpdateMillis.takeIf { it > 0 } ?: entity.dateMillis)
    val mode = if (entity.cricketType == "AMERICAN") "Американский" else "Без набора"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF3A1A1A))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "⏸ НЕЗАВЕРШЕНА",
                color = ErrorColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                dateStr,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            names.joinToString("  ·  "),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(2.dp))
        Text(
            buildString {
                append(mode)
                append("   ·   MPR: ")
                append(mprs.joinToString(" / ") { "%.2f".format(Locale.US, it.toDoubleOrNull() ?: 0.0) })
            },
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent)
                    .clickable { onResume() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Продолжить",
                    color = Color(0xFF121212),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TileBgDark)
                    .clickable { onDelete() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Удалить",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun parseStringList(s: String): List<String> =
    if (s.isBlank()) emptyList() else s.split("|")

private fun parseDoubleList(s: String): List<Double> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

private fun isOwner(isBotFlag: String, name: String, ownerName: String): Boolean {
    if (isBotFlag != "0") return false
    if (ownerName.isBlank()) return true
    return name.equals(ownerName, ignoreCase = true)
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd.MM HH:mm", Locale.US)
    return fmt.format(Date(millis))
}
