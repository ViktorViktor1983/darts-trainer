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

// Один игрок
data class CricketPlayer(
    val name: String,
    val isBot: Boolean = false,
    val botLevel: Int = 0,
    val teamIndex: Int = 0,

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

    // Кто начинал текущий лег (0 или 1) — для чередования
    val lastLegStartingTeam: Int = 0,

    val isPairGame: Boolean = false,
    val teamCount: Int = 2,
    val playersPerTeam: Int = 1
) {
    val currentPlayer: CricketPlayer?
        get() = players.getOrNull(currentPlayerIndex)

    fun playersOfTeam(team: Int): List<CricketPlayer> =
        players.filter { it.teamIndex == team }
}
