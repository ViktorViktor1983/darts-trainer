package com.lodkin.dartstrainer.data.game501

import android.content.Context

// Хранилище настроек игры 501
object Game501SettingsStorage {
    private const val PREFS = "game501_settings"

    private const val KEY_PAIR_GAME = "pair_game"
    private const val KEY_MODE = "mode"
    private const val KEY_AUTO_OK = "auto_ok"
    private const val KEY_LEGS_PER_SET = "legs_per_set"
    private const val KEY_SETS_PER_MATCH = "sets_per_match"

    private const val KEY_SLOT_IS_BOT = "slot_is_bot"
    private const val KEY_SLOT_NAMES = "slot_names"
    private const val KEY_SLOT_BOT_IDS = "slot_bot_ids"

    // Быстрые кнопки (сохранённые 11 сумм)
    private const val KEY_QUICK_SUMS = "quick_sums"
    private const val KEY_GAMES_COUNTER = "games_counter"
    private const val KEY_SUM_FREQ = "sum_freq"   // "60:12|100:8|..."

    val DEFAULT_QUICK_SUMS = listOf(140, 100, 95, 85, 81, 120, 60, 57, 45, 41, 26)

    // ── Парная игра ──
    fun isPairGame(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PAIR_GAME, false)

    fun setPairGame(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_PAIR_GAME, value).apply()
    }

    // ── Режим игры ──
    fun getMode(context: Context): Game501Mode {
        val name = prefs(context).getString(KEY_MODE, Game501Mode.X501_DOUBLE_OUT.name)
        return runCatching { Game501Mode.valueOf(name ?: "") }.getOrDefault(Game501Mode.X501_DOUBLE_OUT)
    }

    fun setMode(context: Context, mode: Game501Mode) {
        prefs(context).edit().putString(KEY_MODE, mode.name).apply()
    }

    // ── АвтоОК ──
    fun getAutoOk(context: Context): Int = prefs(context).getInt(KEY_AUTO_OK, 3)
    fun setAutoOk(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_AUTO_OK, seconds).apply()
    }

    // ── Леги / Сеты ──
    fun getLegsPerSet(context: Context): Int = prefs(context).getInt(KEY_LEGS_PER_SET, 1)
    fun setLegsPerSet(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_LEGS_PER_SET, value).apply()
    }

    fun getSetsPerMatch(context: Context): Int = prefs(context).getInt(KEY_SETS_PER_MATCH, 1)
    fun setSetsPerMatch(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_SETS_PER_MATCH, value).apply()
    }

    // ── Слоты игроков ──
    fun getSlotIsBot(context: Context): List<Boolean> {
        val raw = prefs(context).getString(KEY_SLOT_IS_BOT, "0|1|1|1") ?: "0|1|1|1"
        return raw.split("|").map { it == "1" }
    }

    fun setSlotIsBot(context: Context, values: List<Boolean>) {
        prefs(context).edit().putString(KEY_SLOT_IS_BOT, values.joinToString("|") { if (it) "1" else "0" }).apply()
    }

    fun getSlotNames(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_SLOT_NAMES, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|")
    }

    fun setSlotNames(context: Context, values: List<String>) {
        prefs(context).edit().putString(KEY_SLOT_NAMES, values.joinToString("|")).apply()
    }

    fun getSlotBotIds(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_SLOT_BOT_IDS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").map { it.toIntOrNull() ?: 3 }
    }

    fun setSlotBotIds(context: Context, values: List<Int>) {
        prefs(context).edit().putString(KEY_SLOT_BOT_IDS, values.joinToString("|")).apply()
    }

    // ── Быстрые кнопки ──
    fun getQuickSums(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_QUICK_SUMS, "") ?: ""
        if (raw.isBlank()) return DEFAULT_QUICK_SUMS
        val list = raw.split("|").mapNotNull { it.toIntOrNull() }
        return if (list.size == 11) list else DEFAULT_QUICK_SUMS
    }

    fun setQuickSums(context: Context, values: List<Int>) {
        prefs(context).edit().putString(KEY_QUICK_SUMS, values.joinToString("|")).apply()
    }

    fun getGamesCounter(context: Context): Int = prefs(context).getInt(KEY_GAMES_COUNTER, 0)
    fun setGamesCounter(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_GAMES_COUNTER, value).apply()
    }

    // Частоты сумм: map сумма -> количество
    fun getSumFreq(context: Context): MutableMap<Int, Int> {
        val raw = prefs(context).getString(KEY_SUM_FREQ, "") ?: ""
        val map = mutableMapOf<Int, Int>()
        if (raw.isBlank()) return map
        raw.split("|").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val sum = parts[0].toIntOrNull()
                val count = parts[1].toIntOrNull()
                if (sum != null && count != null) map[sum] = count
            }
        }
        return map
    }

    fun setSumFreq(context: Context, freq: Map<Int, Int>) {
        val raw = freq.entries.joinToString("|") { "${it.key}:${it.value}" }
        prefs(context).edit().putString(KEY_SUM_FREQ, raw).apply()
    }

    // ─────────────────────────────────────────────
    // Обновление быстрых кнопок раз в 3 игры
    // ─────────────────────────────────────────────
    fun updateQuickSumsIfNeeded(context: Context) {
        val counter = getGamesCounter(context) + 1
        if (counter >= 3) {
            val freq = getSumFreq(context)
            if (freq.isNotEmpty()) {
                // Топ-11 самых частых
                val top = freq.entries
                    .sortedByDescending { it.value }
                    .take(11)
                    .map { it.key }
                    .toMutableList()
                // Если меньше 11 — добираем стандартными
                for (def in DEFAULT_QUICK_SUMS) {
                    if (top.size >= 11) break
                    if (def !in top) top.add(def)
                }
                setQuickSums(context, top.take(11))
            }
            setSumFreq(context, emptyMap())
            setGamesCounter(context, 0)
        } else {
            setGamesCounter(context, counter)
        }
    }

    // Записать сумму в частоты (только для людей)
    fun recordHumanSum(context: Context, sum: Int) {
        if (sum < 0 || sum > 180) return
        val freq = getSumFreq(context)
        freq[sum] = (freq[sum] ?: 0) + 1
        setSumFreq(context, freq)
    }

    fun resetQuickSums(context: Context) {
        prefs(context).edit()
            .remove(KEY_QUICK_SUMS)
            .remove(KEY_GAMES_COUNTER)
            .remove(KEY_SUM_FREQ)
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
