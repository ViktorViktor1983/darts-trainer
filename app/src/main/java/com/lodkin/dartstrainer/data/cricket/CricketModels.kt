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
    // для каждого сектора: сколько попаданий (0..3) в текущем леге
    val hits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // для каждого сектора: сколько очков набрал в текущем леге
    val scores: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // общая сумма очков в текущем леге
    var totalScore: Int = 0,
    // количество бросков в текущем леге (для статистики)
    var dartsThrown: Int = 0,
    // Леги, выигранные в ТЕКУЩЕМ сете (сбрасывается при выигрыше сета)
    var legsInCurrentSet: Int = 0,
    // Сеты, выигранные в МАТЧЕ (не сбрасывается до конца игры)
    var setsWon: Int = 0,

    // ── Накопительные данные за ВЕСЬ МАТЧ (не сбрасываются при новом леге) ──
    // Общая сумма очков за матч
    var matchTotalScore: Int = 0,
    // Общее количество бросков за матч
    var matchDartsThrown: Int = 0,
    // Сумма попаданий по секторам за матч
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
    // количество дротиков в текущем подходе (для статистики)
    val currentTurnDarts: Int = 0,

    // ── Настройки матча (леги и сеты) ──
    val legsPerSet: Int = 1,
    val setsPerMatch: Int = 1,

    // Текущий номер лега/сета (для отображения)
    val currentLegNumber: Int = 1,
    val currentSetNumber: Int = 1,

    // Кто выиграл последний лег (для показа диалога)
    val lastLegWinnerIndex: Int? = null,
    // Кто выиграл последний сет (для показа диалога)
    val lastSetWinnerIndex: Int? = null
) {
    val currentPlayer: CricketPlayer?
        get() = players.getOrNull(currentPlayerIndex)
}
