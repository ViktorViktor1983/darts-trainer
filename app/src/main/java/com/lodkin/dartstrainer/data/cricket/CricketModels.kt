package com.lodkin.dartstrainer.data.cricket

// Сектора в крикете
enum class CricketSector(
    val number: Int,
    val label: String,
    val hasTriple: Boolean = true
) {
    S20(20, "20"),
    S19(19, "19"),
    S18(18, "18"),
    S17(17, "17"),
    S16(16, "16"),
    S15(15, "15"),
    BULL(25, "Bull", hasTriple = false);

    companion object {
        val ALL = listOf(S20, S19, S18, S17, S16, S15, BULL)
    }
}

// Тип крикета
enum class CricketType {
    AMERICAN,        // американский (с набором очков)
    NO_SCORE         // без набора очков
}

// ─────────────────────────────────────────────
// Снимок статистики одного игрока за один лег
// ─────────────────────────────────────────────
data class LegPlayerSnapshot(
    val name: String,
    val teamIndex: Int,
    val isBot: Boolean,
    val legMarks: Int,       // все метки за лег
    val darts: Int,          // дротиков за лег
    val misses: Int,         // промахов за лег
    val triples: Int,        // утроений за лег
    val score: Int           // очков за лег
)

// ─────────────────────────────────────────────
// Снимок одного лега (сохраняется при завершении лега)
// ─────────────────────────────────────────────
data class LegSnapshot(
    val setNumber: Int,
    val legNumber: Int,
    val winningTeam: Int,
    val players: List<LegPlayerSnapshot>
)

// Один игрок
data class CricketPlayer(
    val name: String,
    val isBot: Boolean = false,
    val botLevel: Int = 0,
    val teamIndex: Int = 0,

    // ── Состояние серии бота ──
    var botStreak: Double = 1.0,
    var botStreakLeft: Int = 0,

    // ── Текущий подход ──
    var turnMarks: Int = 0,
    // Сколько промахов было в текущем подходе
    var turnMisses: Int = 0,
    // Сколько утроений было в текущем подходе
    var turnTriples: Int = 0,

    // ── Накопительные за матч ──
    var matchPerfectRounds: Int = 0,
    var matchStrongRounds: Int = 0,

    // ── Статистика текущего лега ──
    var legMisses: Int = 0,
    var legTriples: Int = 0,

    val hits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    val scores: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    var totalScore: Int = 0,
    var dartsThrown: Int = 0,
    var legMarks: Int = 0,
    var legsInCurrentSet: Int = 0,
    var setsWon: Int = 0,

    var matchTotalScore: Int = 0,
    var matchDartsThrown: Int = 0,
    var matchMissesThrown: Int = 0,
    var matchTriplesHit: Int = 0,
    var matchBullAttempts: Int = 0,
    var matchBullHits: Int = 0,
    val matchHits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    val matchScores: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap()
)

// Состояние игры
data class CricketGame(
    val type: CricketType = CricketType.AMERICAN,
    val players: List<CricketPlayer> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val isFinished: Boolean = false,
    val winnerIndex: Int? = null,
    val currentTurnDarts: Int = 0,

    val legsPerSet: Int = 1,
    val setsPerMatch: Int = 1,

    val currentLegNumber: Int = 1,
    val currentSetNumber: Int = 1,

    val lastLegWinnerIndex: Int? = null,
    val lastSetWinnerIndex: Int? = null,
    val lastLegWinnerPlayerIndex: Int? = null,
    val lastLegDartsClicked: Int = 0,

    val lastLegStartingTeam: Int = 0,
    val autoOkSeconds: Int = 0,

    // ── Сессия ──
    val sessionStartTime: Long = 0L,
    val sessionForm: Double = 1.0,

    // ── История легов ──
    val legHistory: List<LegSnapshot> = emptyList(),

    val isPairGame: Boolean = false,
    val teamCount: Int = 2,
    val playersPerTeam: Int = 1
) {
    val currentPlayer: CricketPlayer?
        get() = players.getOrNull(currentPlayerIndex)

    fun playersOfTeam(team: Int): List<CricketPlayer> =
        players.filter { it.teamIndex == team }
}
