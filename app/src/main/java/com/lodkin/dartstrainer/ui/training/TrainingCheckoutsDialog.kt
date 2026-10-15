package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.game501.CheckoutTable
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg

private val CheckoutGray = Color(0xFF455A64)
private val CheckoutGreen = Color(0xFF4CAF50)
private val CheckoutYellow = Color(0xFFFFD54F)
private val CheckoutRed = Color(0xFFE53935)
private val CheckoutPurple = Color(0xFF9C27B0)
private val CheckoutIndigo = Color(0xFF3F51B5)
private val CheckoutTeal = Color(0xFF00BCD4)
private val CheckoutGold = Color(0xFFFFC107)
private val CheckoutDiamond = Color(0xFFB3E5FC)

private val VALID_CHECKOUTS: List<Int> by lazy {
    (2..170).filter { CheckoutTable.isCheckoutPossible(it) }
}

private fun checkoutColor(count: Int): Pair<Color, Color> {
    return when {
        count >= 100 -> CheckoutDiamond to Color(0xFF0D1117)
        count >= 50  -> CheckoutGold to Color(0xFF121212)
        count >= 30  -> CheckoutTeal to Color(0xFF121212)
        count >= 20  -> CheckoutIndigo to Color.White
        count >= 10  -> CheckoutPurple to Color.White
        count >= 5   -> CheckoutRed to Color.White
        count >= 3   -> CheckoutYellow to Color(0xFF121212)
        count >= 1   -> CheckoutGreen to Color.White
        else         -> CheckoutGray to Color.White.copy(alpha = 0.45f)
    }
}

/**
 * Диалог «Мои закрытые чекауты» для тренировочного раздела.
 *
 * Логика полностью совпадает с диалогом из свободной игры (StatsScreen.kt):
 * та же сетка 2–170, та же цветовая легенда, тот же подсчёт.
 *
 * @param closedCheckouts Map: значение чекаута → сколько раз закрыто.
 */
@Composable
internal fun TrainingCheckoutsDialog(
    closedCheckouts: Map<Int, Int>,
    onDismiss: () -> Unit
) {
    val validTotal = VALID_CHECKOUTS.size
    val closedCount = VALID_CHECKOUTS.count { (closedCheckouts[it] ?: 0) > 0 }
    val percent = if (validTotal > 0) (closedCount * 100 / validTotal) else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK", color = Accent) }
        },
        title = {
            Column {
                Text(
                    "🏆 Мои закрытые чекауты",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Закрыто $closedCount из $validTotal ($percent%)",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Только из тренировочных партий 501",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                // ── Мотивационная шапка ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TileBg)
                        .padding(12.dp)
                ) {
                    Text(
                        "Это твой личный дневник побед. Чем больше закрытий на числе — " +
                                "тем увереннее рука на этом чекауте в следующий раз.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ── Сетка чекаутов ──
                VALID_CHECKOUTS.chunked(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { value ->
                            val count = closedCheckouts[value] ?: 0
                            CheckoutCell(value = value, count = count, modifier = Modifier.weight(1f))
                        }
                        if (row.size < 5) {
                            repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Легенда:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                LegendRow(CheckoutGray, "не закрыт")
                LegendRow(CheckoutGreen, "1 раз")
                LegendRow(CheckoutYellow, "3 раза")
                LegendRow(CheckoutRed, "5 раз")
                LegendRow(CheckoutPurple, "10 раз")
                LegendRow(CheckoutIndigo, "20 раз")
                LegendRow(CheckoutTeal, "30 раз")
                LegendRow(CheckoutGold, "50 раз")
                LegendRow(CheckoutDiamond, "100 раз")
            }
        }
    )
}

@Composable
private fun CheckoutCell(value: Int, count: Int, modifier: Modifier) {
    val (bg, fg) = checkoutColor(count)
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$value", color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (count > 0) {
                Text("$count", color = fg.copy(alpha = 0.75f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 1.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
    }
}
