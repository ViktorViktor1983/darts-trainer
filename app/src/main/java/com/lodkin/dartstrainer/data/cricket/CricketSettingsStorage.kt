package com.lodkin.dartstrainer.data.cricket

import android.content.Context

// Хранилище последних настроек игры в крикет
// Сохраняется между матчами и при перезапуске приложения
object CricketSettingsStorage {
    private const val PREFS = "cricket_settings"

    private const val KEY_PAIR_GAME = "pair_game"
    private const val KEY_CRICKET_TYPE = "cricket_type"
    private const val KEY_AUTO_OK = "auto_ok"
    private const val KEY_LEGS_PER_SET = "legs_per_set"
    private const val KEY_SETS_PER_MATCH = "sets_per_match"

    private const val KEY_SLOT_IS_BOT = "slot_is_bot"      // "0|1|1|1"
    private const val KEY_SLOT_NAMES = "slot_names"        // "Виктор|Любитель|Игрок 3|Игрок 4"
    private const val KEY_SLOT_BOT_IDS = "slot_bot_ids"    // "3|3|3|3"

    // ── Парная игра ──
    fun isPairGame(context: Context): Boolean =
        prefs(context).getBoolean(KEY_PAIR_GAME, false)

    fun setPairGame(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_PAIR_GAME, value).apply()
    }

    // ── Тип крикета ──
    fun getCricketType(context: Context): CricketType {
        val name = prefs(context).getString(KEY_CRICKET_TYPE, CricketType.AMERICAN.name)
        return runCatching { CricketType.valueOf(name ?: "") }.getOrDefault(CricketType.AMERICAN)
    }

    fun setCricketType(context: Context, type: CricketType) {
        prefs(context).edit().putString(KEY_CRICKET_TYPE, type.name).apply()
    }

    // ── АвтоОК ──
    fun getAutoOk(context: Context): Int =
        prefs(context).getInt(KEY_AUTO_OK, 3)

    fun setAutoOk(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_AUTO_OK, seconds).apply()
    }

    // ── Леги / Сеты ──
    fun getLegsPerSet(context: Context): Int =
        prefs(context).getInt(KEY_LEGS_PER_SET, 1)

    fun setLegsPerSet(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_LEGS_PER_SET, value).apply()
    }

    fun getSetsPerMatch(context: Context): Int =
        prefs(context).getInt(KEY_SETS_PER_MATCH, 1)

    fun setSetsPerMatch(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_SETS_PER_MATCH, value).apply()
    }

    // ── Слоты игроков ──
    fun getSlotIsBot(context: Context): List<Boolean> {
        val raw = prefs(context).getString(KEY_SLOT_IS_BOT, "0|1|1|1") ?: "0|1|1|1"
        return raw.split("|").map { it == "1" }
    }

    fun setSlotIsBot(context: Context, values: List<Boolean>) {
        val raw = values.joinToString("|") { if (it) "1" else "0" }
        prefs(context).edit().putString(KEY_SLOT_IS_BOT, raw).apply()
    }

    fun getSlotNames(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_SLOT_NAMES, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|")
    }

    fun setSlotNames(context: Context, values: List<String>) {
        val raw = values.joinToString("|")
        prefs(context).edit().putString(KEY_SLOT_NAMES, raw).apply()
    }

    fun getSlotBotIds(context: Context): List<Int> {
        val raw = prefs(context).getString(KEY_SLOT_BOT_IDS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|").map { it.toIntOrNull() ?: 3 }
    }

    fun setSlotBotIds(context: Context, values: List<Int>) {
        val raw = values.joinToString("|")
        prefs(context).edit().putString(KEY_SLOT_BOT_IDS, raw).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
