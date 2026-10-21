package com.lodkin.dartstrainer.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lodkin.dartstrainer.data.training.InventoryStorage
import com.lodkin.dartstrainer.theme.Accent
import com.lodkin.dartstrainer.theme.GoldAccent
import com.lodkin.dartstrainer.theme.TileBg
import com.lodkin.dartstrainer.theme.TileBgDark

/**
 * Диалог выбора инвентаря для силовых упражнений.
 * Позволяет указать наличие гантелей и резинки.
 */
@Composable
fun InventoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    var hasDumbbells by remember { mutableStateOf(InventoryStorage.hasDumbbells(context)) }
    var hasBand by remember { mutableStateOf(InventoryStorage.hasResistanceBand(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TileBgDark,
        title = {
            Text(
                "Какой у тебя есть инвентарь?",
                color = GoldAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Программа подберет только те упражнения, для которых у тебя есть всё необходимое. Упражнения со своим весом доступны всегда.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))

                // Переключатель гантелей
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .clickable { hasDumbbells = !hasDumbbells }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Гантели",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = hasDumbbells,
                        onCheckedChange = { hasDumbbells = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Accent,
                            checkedTrackColor = Accent.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TileBgDark
                        )
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Переключатель резинки
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TileBg)
                        .clickable { hasBand = !hasBand }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Резинка (эспандер)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = hasBand,
                        onCheckedChange = { hasBand = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Accent,
                            checkedTrackColor = Accent.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = TileBgDark
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                InventoryStorage.setHasDumbbells(context, hasDumbbells)
                InventoryStorage.setHasResistanceBand(context, hasBand)
                onConfirm()
            }) {
                Text("СОХРАНИТЬ", color = Accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ОТМЕНА", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}
