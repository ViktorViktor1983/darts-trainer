package com.lodkin.dartstrainer.data.game501

// Быстрый симулятор матча x01 без UI.
// Поддерживает как одиночный прогон (simulate), так и мультипрогон
// по списку уровней (simulateMany).
object Game501Simulator {

    data class SimResult(
        val botLevel: Int,
        val totalLegs: Int,
        val ppr: Double,
        val doublesAccuracy: Double,
        val dartsPerLeg: Double,
        val first9Ppr: Double
    )

    // Обратная совместимость: один бот — один результат.
    fun simulate(
        botLevel: Int,
        legsToPlay: Int = 200
    ): SimResult = simulateSingle(botLevel, legsToPlay)

    // Мультипрогон: по одному уровню за раз, возвращаем список результатов.
    fun simulateMany(
        botLevels: List<Int>,
        legsToPlay: Int = 200
    ): List<SimResult> = botLevels.map { simulateSingle(it, legsToPlay) }

    private fun simulateSingle(botLevel: Int, legsToPlay: Int): SimResult {
        val playerA = Player501(name = "Bot A", isBot = true, botLevel = botLevel, teamIndex = 0)
        val playerB = Player501(name = "Bot B", isBot = true, botLevel = botLevel, teamIndex = 1)

        var game = Game501Logic.newGame(
            gameType = GameType.X501,
            outMode = OutMode.DOUBLE_OUT,
            players = listOf(playerA, playerB),
            legsPerSet = 1,
            setsPerMatch = legsToPlay,
            isPairGame = false,
            startingTeamIndex = 0
        )

        var moves = 0
        val maxMoves = 300_000

        while (!game.isFinished && moves < maxMoves) {
            val idx = game.currentPlayerIndex
            game = Game501BotAI.performTurn(game, idx)
            moves++
            if (game.currentPlayerIndex == idx && !game.isFinished &&
                game.legHistory.size < legsToPlay && moves > 10_000) break
        }

        val totalDarts = game.players.sumOf { it.matchDarts }
        val totalScore = game.players.sumOf { it.matchScoreGained }
        val totalDoublesHit = game.players.sumOf { it.matchDoublesHit }
        val totalDoublesAtt = game.players.sumOf { it.matchDoublesAttempted }

        val ppr = if (totalDarts < 3) 0.0 else totalScore.toDouble() / (totalDarts / 3.0)
        val dblAcc = if (totalDoublesAtt <= 0) 0.0 else totalDoublesHit.toDouble() / totalDoublesAtt * 100.0
        val legs = game.legHistory.size
        val dartsPerLeg = if (legs <= 0) 0.0 else totalDarts.toDouble() / (legs * 2.0)
        val allFirst9 = game.players.flatMap { it.listOfFirst9Ppr }
        val first9 = if (allFirst9.isEmpty()) 0.0 else allFirst9.average()

        return SimResult(
            botLevel = botLevel,
            totalLegs = legs,
            ppr = ppr,
            doublesAccuracy = dblAcc,
            dartsPerLeg = dartsPerLeg,
            first9Ppr = first9
        )
    }
}
