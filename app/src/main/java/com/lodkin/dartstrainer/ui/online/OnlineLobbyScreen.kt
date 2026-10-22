package com.lodkin.dartstrainer.ui.online

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.lodkin.dartstrainer.data.training.DartsNorms
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.DarkBg
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

@Composable
fun OnlineLobbyScreen(
    onBack: () -> Unit,
    onStartSearch: () -> Unit,
    onPlayFriend: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("online_prefs", android.content.Context.MODE_PRIVATE) }

    // Состояния
    var nickname by remember { mutableStateOf(prefs.getString("nickname", "") ?: "") }
    var selectedLevel by remember { mutableStateOf(prefs.getInt("level", 1)) }
    var is501 by remember { mutableStateOf(prefs.getBoolean("is501", true)) }
    var legs by remember { mutableStateOf(prefs.getInt("legs", 3)) }
    var sets by remember { mutableStateOf(prefs.getInt("sets", 1)) }
    var saveToTraining by remember { mutableStateOf(prefs.getBoolean("save_to_training", false)) }

    // Диалоги
    var showNicknameDialog by remember { mutableStateOf(nickname.isBlank()) }
    var showLevelDialog by remember { mutableStateOf(false) }
    var showFriendDialog by remember { mutableStateOf(false) }

    // Сохранение настроек при изменении
    fun saveSettings() {
        prefs.edit()
            .putString("nickname", nickname)
            .putInt("level", selectedLevel)
            .putBoolean("is501", is501)
            .putInt("legs", legs)
            .putInt("sets", sets)
            .putBoolean("save_to_training", saveToTraining)
            .apply()
    }

    LaunchedEffect(nickname, selectedLevel, is501, legs, sets, saveToTraining) {
        saveSettings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Шапка
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(TileBgDark)
                    .clickable { onBack() }.padding(horizontal = 16.dp, vertical = 10.dp)
            ) { Text("← Назад", color = Accent, fontSize = 15.sp) }
            Spacer(Modifier.width(12.dp))
            Text("Онлайн-игра", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
        }

        Spacer(Modifier.height(20.dp))

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            
            // БЛОК ПРОФИЛЯ
            Text("ВАШ ПРОФИЛЬ", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TileBg).padding(16.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().clickable { showNicknameDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text("Никнейм:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(if (nickname.isBlank()) "Задать ✏️" else nickname, color = if (nickname.isBlank()) GoldAccent else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth().clickable { showLevelDialog = true }, verticalAlignment = Alignment.CenterVertically) {
                        Text("Уровень:", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(DartsNorms.getLevelName(selectedLevel) + " (PPR " + getPprRange(selectedLevel) + ")", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // НАСТРОЙКИ МАТЧА
            Text("НАСТРОЙКИ МАТЧА", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(TileBgDark).padding(16.dp)) {
                Column {
                    // Выбор игры
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (is501) Accent else TileBg).clickable { is501 = true }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("501", color = if (is501) DarkBg else Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (!is501) Accent else TileBg).clickable { is501 = false }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("Крикет", color = if (!is501) DarkBg else Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    // Сеты и Леги
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumberPicker("СЕТЫ", sets, { sets = it }, Modifier.weight(1f))
                        NumberPicker("ЛЕГИ", legs, { legs = it }, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))

                    // Статистика
                    Row(modifier = Modifier.fillMaxWidth().clickable { saveToTraining = !saveToTraining }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Сохранять в тренировку", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Если выключено — пойдет в обычную статистику", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Switch(checked = saveToTraining, onCheckedChange = { saveToTraining = it }, colors = SwitchDefaults.colors(checkedThumbColor = Accent, checkedTrackColor = Accent.copy(alpha = 0.5f)))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // КНОПКИ ПОИСКА
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(16.dp))
                .background(if (nickname.isNotBlank()) GoldAccent else TileBg)
                .clickable(enabled = nickname.isNotBlank()) { onStartSearch() },
            contentAlignment = Alignment.Center
        ) {
            Text("🔍 НАЙТИ СОПЕРНИКА", color = if (nickname.isNotBlank()) DarkBg else Color.White.copy(alpha = 0.5f), fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp))
                .background(TileBg)
                .clickable(enabled = nickname.isNotBlank()) { showFriendDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Text("👥 Играть с другом", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
    }

    // ── ДИАЛОГИ ──

    if (showNicknameDialog) {
        var input by remember { mutableStateOf(nickname) }
        AlertDialog(
            onDismissRequest = { if (nickname.isNotBlank()) showNicknameDialog = false },
            confirmButton = {
                TextButton(onClick = { 
                    val trimmed = input.trim().replace(" ", "_")
                    if (trimmed.isNotBlank()) { nickname = trimmed; showNicknameDialog = false } 
                }) { Text("Сохранить", color = Accent) }
            },
            dismissButton = {
                if (nickname.isNotBlank()) {
                    TextButton(onClick = { showNicknameDialog = false }) { Text("Отмена", color = Color.White.copy(alpha = 0.6f)) }
                }
            },
            title = { Text("Ваш никнейм в онлайне", color = Color.White) },
            text = {
                Column {
                    Text("Придумайте уникальное имя, по которому вас будут узнавать другие игроки.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = input, onValueChange = { if (it.length <= 15) input = it },
                        singleLine = true, placeholder = { Text("Например: Vitya_501", color = TileBg) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Accent, unfocusedBorderColor = TileBg, cursorColor = Accent)
                    )
                }
            }
        )
    }

    if (showLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLevelDialog = false },
            confirmButton = { TextButton(onClick = { showLevelDialog = false }) { Text("OK", color = Accent) } },
            title = { Text("Выберите ваш уровень", color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    for (i in 1..16) {
                        val isSelected = selectedLevel == i
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Accent.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedLevel = i }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(DartsNorms.getLevelName(i), color = if (isSelected) Accent else Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                            Text("PPR ${getPprRange(i)}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                }
            }
        )
    }

    if (showFriendDialog) {
        var friendInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showFriendDialog = false },
            confirmButton = {
                TextButton(onClick = { showFriendDialog = false; onPlayFriend() }) { Text("Пригласить", color = Accent) }
            },
            dismissButton = { TextButton(onClick = { showFriendDialog = false }) { Text("Отмена", color = Color.White.copy(alpha = 0.6f)) } },
            title = { Text("Играть с другом", color = Color.White) },
            text = {
                Column {
                    Text("Введите никнейм друга, чтобы отправить ему приглашение.", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = friendInput, onValueChange = { friendInput = it },
                        singleLine = true, placeholder = { Text("Никнейм друга", color = TileBg) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Accent, unfocusedBorderColor = TileBg, cursorColor = Accent)
                    )
                }
            }
        )
    }
}

@Composable
private fun NumberPicker(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(TileBg)
                .clickable { expanded = true }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text("$value", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("▼", color = Accent, fontSize = 10.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(DarkBg)) {
            (1..10).forEach { n ->
                DropdownMenuItem(
                    text = { Text("$n", color = if (n == value) Accent else Color.White, fontWeight = if (n == value) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { onValueChange(n); expanded = false }
                )
            }
        }
    }
}

private fun getPprRange(level: Int): String {
    return when (level) {
        1 -> "22-26"
        2 -> "28-32"
        3 -> "33-37"
        4 -> "38-42"
        5 -> "43-47"
        6 -> "48-52"
        7 -> "53-57"
        8 -> "58-62"
        9 -> "63-67"
        10 -> "68-72"
        11 -> "73-77"
        12 -> "78-82"
        13 -> "83-87"
        14 -> "88-92"
        15 -> "93-97"
        16 -> "98+"
        else -> "?"
    }
}
