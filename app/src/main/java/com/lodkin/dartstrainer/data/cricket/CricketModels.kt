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
    // для каждого сектора: сколько попаданий (0..3)
    val hits: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // для каждого сектора: сколько очков набрал
    val scores: MutableMap<CricketSector, Int> = CricketSector.ALL.associateWith { 0 }.toMutableMap(),
    // общая сумма очков
    var totalScore: Int = 0,
    // количество бросков (для статистики)
    var dartsThrown: Int = 0
)

// Состояние игры
data class CricketGame(
    val type: CricketType = CricketType.AMERICAN,
    val players: List<CricketPlayer> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val isFinished: Boolean = false,
    val winnerIndex: Int? = null,
    // количество дротиков в текущем подходе (для статистики)
    val currentTurnDarts: Int = 0
) {
    val currentPlayer: CricketPlayer?
        get() = players.getOrNull(currentPlayerIndex)
}
