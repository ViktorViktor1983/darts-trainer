package com.lodkin.dartstrainer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.lodkin.dartstrainer.data.cricket.CricketRepository
import com.lodkin.dartstrainer.data.game501.Game501Database
import com.lodkin.dartstrainer.data.game501.Game501Repository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.util.Locale

/**
 * Новый экран статистики — меню с 6 карточками игр.
 * Тап по карточке открывает подробную статистику этой игры.
 */
@Composable
fun StatsMenuScreen(
    cricketRepository: CricketRepository,
    onCricket: () -> Unit,
    on501: () -> Unit,
    onSector: () -> Unit,
    onBigRound: () -> Unit,
    onScoreSet: () -> Unit,
    onAroundClock: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val game501Repo = remember {
        Game501Repository(Game501Database.get(context).game501Dao())
    }
    val ownerName = remember { SettingsStorage.getPlayerName(context).trim() }

    var avg501Ppr by remember { mutableStateOf<String?>(null) }
    var avgCricketMpr by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // ── 501: средний PPR за всё время ──
        val games501 = game501Repo.getAllGames()
        var pprSum = 0.0
        var pprCount = 0
        for (g in games501) {
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

        // ── Крикет: средний MPR за всё время ──
        val gamesCricket = cricketRepository.getAllGames()
        var mprSum = 0.0
        var mprCount = 0
        for (g in gamesCricket) {
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
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ── ШАПКА ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text("←", color = Accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Статистика",
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                if (ownerName.isNotBlank()) {
                    Text("игрок: $ownerName", color = Accent, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── СЕТКА 2 x 3 ──
        // Ряд 1
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "501",
                metric = avg501Ppr ?: "...",
                metricLabel = "Средний PPR",
                onClick = on501,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Крикет",
                metric = avgCricketMpr ?: "...",
                metricLabel = "Средний MPR",
                onClick = onCricket,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(10.dp))

        // Ряд 2
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "Сектор",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onSector,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Большой раунд",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onBigRound,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(10.dp))

        // Ряд 3
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "Набор очков",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onScoreSet,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatsCard(
                title = "Кругосветка",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onAroundClock,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

// ─────────────────────────────────────────────
// Карточка одной игры
// ─────────────────────────────────────────────
@Composable
private fun StatsCard(
    title: String,
    metric: String,
    metricLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
}

// ─────────────────────────────────────────────
// Хелперы
// ─────────────────────────────────────────────
private fun parseStringList(s: String): List<String> =
    if (s.isBlank()) emptyList() else s.split("|")

private fun parseDoubleList(s: String): List<Double> =
    if (s.isBlank()) emptyList() else s.split("|").map { it.toDoubleOrNull() ?: 0.0 }

private fun isOwner(isBotFlag: String, name: String, ownerName: String): Boolean {
    if (isBotFlag != "0") return false
    if (ownerName.isBlank()) return true
    return name.equals(ownerName, ignoreCase = true)
}
