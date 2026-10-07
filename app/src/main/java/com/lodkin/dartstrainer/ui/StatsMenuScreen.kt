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

    // Метрики. null = ещё грузится, "—" = нет данных.
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

        Spacer(Modifier.height(20.dp))

        // ── СЕТКА 2 x 3 ──
        // Ряд 1: 501 | Крикет
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsCard(
                title = "501",
                metric = avg501Ppr ?: "...",
                metricLabel = "Средний PPR",
                onClick = on501,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Крикет",
                metric = avgCricketMpr ?: "...",
                metricLabel = "Средний MPR",
                onClick = onCricket,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))

        // Ряд 2: Сектор | Большой раунд
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsCard(
                title = "Сектор",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onSector,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Большой раунд",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onBigRound,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))

        // Ряд 3: Набор очков | Кругосветка
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatsCard(
                title = "Набор очков",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onScoreSet,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "Кругосветка",
                metric = "—",
                metricLabel = "Лучший результат",
                onClick = onAroundClock,
                modifier = Modifier.weight(1f)
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
            .clip(RoundedCornerShape(14.dp))
            .background(TileBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            color = Accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            metric,
            color = GoldAccent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            metricLabel,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
    }
}

// ─────────────────────────────────────────────
// Хелперы (локальные, чтобы файл был самодостаточным)
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
