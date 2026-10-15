package com.lodkin.dartstrainer.ui

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.ErrorColor
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark
import java.io.File

/**
 * Общие настройки приложения.
 *
 * Здесь только то, что касается всего приложения:
 * сброс данных, «О программе», темы и язык (позже).
 *
 * Настройки тренировки — в TrainingSettingsScreen (кнопка ⚙ на экране тренировки).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    onFactoryReset: () -> Unit
) {
    val context = LocalContext.current
    var showConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .padding(16.dp)
    ) {
        // Верхняя панель
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TileBgDark)
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("← Назад", color = Accent, fontSize = 15.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Настройки",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Кнопка «О программе» ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBg)
                    .clickable { onOpenAbout() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ℹ  О программе",
                        color = GoldAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "как всё устроено и зачем это нужно",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Скоро появятся: выбор темы (тёмная / светлая), выбор языка, " +
                        "дополнительные настройки звука и уведомлений.",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp
            )

            Spacer(Modifier.height(24.dp))

            // Кнопка сброса
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TileBgDark)
                    .clickable { showConfirm = true }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Сброс до заводских настроек",
                        color = ErrorColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Удалить все данные и настройки",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = TileBgDark,
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    factoryReset(context)
                    onFactoryReset()
                }) {
                    Text("УДАЛИТЬ ВСЁ", color = ErrorColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Отмена", color = Accent)
                }
            },
            title = {
                Text(
                    "Сбросить всё?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Будут удалены:\n" +
                            "• все имена игроков\n" +
                            "• вся статистика крикета\n" +
                            "• вся статистика x01\n" +
                            "• все настройки\n\n" +
                            "Приложение вернётся к состоянию первого запуска.\n" +
                            "Это действие нельзя отменить.",
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        )
    }
}

// Полный сброс
private fun factoryReset(context: Context) {
    val prefsDir = File(context.filesDir.parentFile, "shared_prefs")
    if (prefsDir.exists() && prefsDir.isDirectory) {
        prefsDir.listFiles()?.forEach { file -> file.delete() }
    }
    context.databaseList().forEach { dbName ->
        context.deleteDatabase(dbName)
    }
}
