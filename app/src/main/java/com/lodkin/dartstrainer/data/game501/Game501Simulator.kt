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
        val first9Ppr: Double,
        // Диагностика
        val doublesHit: Int,
        val doublesAttempted: Int,
        val totalDarts: Int
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

        // legsPerSet=1, setsPerMatch=legsToPlay: остановимся ровно после
        // legsToPlay легов (в матче побеждает тот, кто первым наберёт
        // больше половины). Нам нужна ЧИСТАЯ остановка после N легов,
        // поэтому ставим setsPerMatch = legsToPlay и проверяем
        // legHistory.size в цикле ниже — выходим раньше, если набралось.
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
        val maxMoves = 1_000_000

        while (!game.isFinished && moves < maxMoves) {
            if (game.legHistory.size >= legsToPlay) break
            val idx = game.currentPlayerIndex
            game = Game501BotAI.performTurn(game, idx)
            moves++
        }

        val totalDarts = game.players.sumOf { it.matchDarts }
        val totalScore = game.players.sumOf { it.matchScoreGained }
        val totalDoublesHit = game.players.sumOf { it.matchDoublesHit }
        val totalDoublesAtt = game.players.sumOf { it.matchDoublesAttempted }

        val ppr = if (totalDarts < 3) 0.0 else totalScore.toDouble() / (totalDarts / 3.0)
        val dblAcc = if (totalDoublesAtt <= 0) 0.0
                     else totalDoublesHit.toDouble() / totalDoublesAtt * 100.0
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
            first9Ppr = first9,
            doublesHit = totalDoublesHit,
            doublesAttempted = totalDoublesAtt,
            totalDarts = totalDarts
        )
    }
}
