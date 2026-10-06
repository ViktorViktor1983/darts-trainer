package com.lodkin.dartstrainer.ui.biground

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.biground.BigRoundCategory
import com.lodkin.dartstrainer.data.biground.BigRoundGameEntity
import com.lodkin.dartstrainer.data.biground.BigRoundRepository
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BigRoundStatsScreen(
    repository: BigRoundRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var games by remember { mutableStateOf<List<BigRoundGameEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reloadKey) {
        loading = true
        games = withContext(Dispatchers.IO) {
            repository.getAllGames()
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // ── Шапка ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Статистика · Большой раунд",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка…", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        } else if (games.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Пока нет сыгранных матчей",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Сыграй партию в «Большой раунд» —\nрезультат появится здесь.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                games.forEach { g ->
                    MatchRow(g)
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TileBgDark)
                        .clickable { showResetDialog = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Сбросить статистику",
                        color = ErrorColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { repository.clearAll() }
                        reloadKey++
                    }
                    showResetDialog = false
                }) { Text("УДАЛИТЬ", color = ErrorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена", color = Accent)
                }
            },
            title = {
                Text("Сбросить статистику?", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Все матчи по «Большому раунду» будут удалены. Отменить действие нельзя.",
                    color = Color.White
                )
            }
        )
    }
}

@Composable
private fun MatchRow(game: BigRoundGameEntity) {
    val dateStr = formatDate(game.dateMillis)
    val categoryStr = if (game.categoryName == BigRoundCategory.MALE.name) "Мужской" else "Юношеский"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TileBg)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                dateStr,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    categoryStr,
                    color = Accent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "· попаданий: ${game.totalHits}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${game.totalScore}",
                color = GoldAccent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "очков",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )
        }
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)
    return fmt.format(Date(millis))
}
