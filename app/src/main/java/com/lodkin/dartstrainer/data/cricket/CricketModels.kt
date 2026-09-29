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
    AMERICAN,        // с набором очков
    NO_SCORE         // без набора очков
}

// Один игрок
data class CricketPlayer(
    val name: String,
    val isBot: Boolean = false,
    val botLevel: Int = 0,
    val teamIndex: Int = 0,

    // для каждого сектора: сколько попаданий (0..3) в текущем леге
    val hits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // для каждого сектора: сколько очков набрал в текущем леге
    val scores: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // общая сумма очков в текущем леге
    var totalScore: Int = 0,
    // количество бросков в текущем леге
    var dartsThrown: Int = 0,
    // Леги, выигранные в ТЕКУЩЕМ сете
    var legsInCurrentSet: Int = 0,
    // Сеты, выигранные в МАТЧЕ
    var setsWon: Int = 0,

    // ── Накопительные данные за ВЕСЬ МАТЧ ──
    var matchTotalScore: Int = 0,
    var matchDartsThrown: Int = 0,
    // Промахи за матч (для статистики точности)
    var matchMissesThrown: Int = 0,
    // Попадания в утроения за матч
    var matchTriplesHit: Int = 0,
    // Bull — статистика (пока не используется, пригодится позже)
    var matchBullAttempts: Int = 0,
    var matchBullHits: Int = 0,
    // Сумма попаданий по секторам за матч (в метках)
    val matchHits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // Сумма очков по секторам за матч
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

    // ── Настройки матча ──
    val legsPerSet: Int = 1,
    val setsPerMatch: Int = 1,

    // Текущий номер лега/сета
    val currentLegNumber: Int = 1,
    val currentSetNumber: Int = 1,

    // Кто выиграл последний лег (teamIndex) — для показа диалога
    val lastLegWinnerIndex: Int? = null,
    // Кто выиграл последний сет (teamIndex)
    val lastSetWinnerIndex: Int? = null,
    // Какой конкретный игрок сделал победный бросок в леге
    val lastLegWinnerPlayerIndex: Int? = null,
    // Сколько кликов сделал игрок в победном ходу
    val lastLegDartsClicked: Int = 0,

    // ── Парная игра ──
    val isPairGame: Boolean = false,
    val teamCount: Int = 2,
    val playersPerTeam: Int = 1
) {
    val currentPlayer: CricketPlayer?
        get() = players.getOrNull(currentPlayerIndex)

    fun playersOfTeam(team: Int): List<CricketPlayer> =
        players.filter { it.teamIndex == team }
}
