package com.lodkin.dartstrainer.data.game501

import org.json.JSONArray
import org.json.JSONObject

/**
 * Сериализация партии 501 в JSON-строку и обратно.
 * Нужна для сохранения прерванных партий в базу.
 *
 * Использует встроенный в Android org.json — никаких сторонних библиотек.
 */
object Game501Serializer {

    // ─────────────────────────────────────────
    // Game501 → JSON
    // ─────────────────────────────────────────
    fun toJson(game: Game501): String {
        val root = JSONObject()

        root.put("gameType", game.gameType.name)
        root.put("outMode", game.outMode.name)
        root.put("currentPlayerIndex", game.currentPlayerIndex)
        root.put("isFinished", game.isFinished)
        root.put("winnerIndex", game.winnerIndex ?: JSONObject.NULL)
        root.put("legsPerSet", game.legsPerSet)
        root.put("setsPerMatch", game.setsPerMatch)
        root.put("currentLegNumber", game.currentLegNumber)
        root.put("currentSetNumber", game.currentSetNumber)
        root.put("lastLegWinnerIndex", game.lastLegWinnerIndex ?: JSONObject.NULL)
        root.put("lastSetWinnerIndex", game.lastSetWinnerIndex ?: JSONObject.NULL)
        root.put("lastLegStartingTeam", game.lastLegStartingTeam)
        root.put("autoOkSeconds", game.autoOkSeconds)
        root.put("sessionStartTime", game.sessionStartTime)
        root.put("sessionForm", game.sessionForm)
        root.put("isPairGame", game.isPairGame)
        root.put("teamCount", game.teamCount)
        root.put("playersPerTeam", game.playersPerTeam)

        // Игроки
        val playersArr = JSONArray()
        for (p in game.players) {
            playersArr.put(playerToJson(p))
        }
        root.put("players", playersArr)

        // История легов
        val historyArr = JSONArray()
        for (snap in game.legHistory) {
            historyArr.put(legSnapshotToJson(snap))
        }
        root.put("legHistory", historyArr)

        return root.toString()
    }

    // ─────────────────────────────────────────
    // JSON → Game501
    // ─────────────────────────────────────────
    fun fromJson(json: String): Game501? {
        return try {
            val root = JSONObject(json)

            val gameType = GameType.valueOf(root.getString("gameType"))
            val outMode = OutMode.valueOf(root.getString("outMode"))
            val currentPlayerIndex = root.getInt("currentPlayerIndex")
            val isFinished = root.getBoolean("isFinished")
            val winnerIndex = if (root.isNull("winnerIndex")) null else root.getInt("winnerIndex")
            val legsPerSet = root.getInt("legsPerSet")
            val setsPerMatch = root.getInt("setsPerMatch")
            val currentLegNumber = root.getInt("currentLegNumber")
            val currentSetNumber = root.getInt("currentSetNumber")
            val lastLegWinnerIndex = if (root.isNull("lastLegWinnerIndex")) null else root.getInt("lastLegWinnerIndex")
            val lastSetWinnerIndex = if (root.isNull("lastSetWinnerIndex")) null else root.getInt("lastSetWinnerIndex")
            val lastLegStartingTeam = root.optInt("lastLegStartingTeam", 0)
            val autoOkSeconds = root.optInt("autoOkSeconds", 0)
            val sessionStartTime = root.optLong("sessionStartTime", 0L)
            val sessionForm = root.optDouble("sessionForm", 1.0)
            val isPairGame = root.optBoolean("isPairGame", false)
            val teamCount = root.optInt("teamCount", 2)
            val playersPerTeam = root.optInt("playersPerTeam", 1)

            // Игроки
            val playersArr = root.getJSONArray("players")
            val players = mutableListOf<Player501>()
            for (i in 0 until playersArr.length()) {
                players.add(playerFromJson(playersArr.getJSONObject(i)))
            }

            // История легов
            val historyArr = root.optJSONArray("legHistory") ?: JSONArray()
            val legHistory = mutableListOf<LegSnapshot501>()
            for (i in 0 until historyArr.length()) {
                legHistory.add(legSnapshotFromJson(historyArr.getJSONObject(i)))
            }

            Game501(
                gameType = gameType,
                outMode = outMode,
                players = players,
                currentPlayerIndex = currentPlayerIndex,
                isFinished = isFinished,
                winnerIndex = winnerIndex,
                legsPerSet = legsPerSet,
                setsPerMatch = setsPerMatch,
                currentLegNumber = currentLegNumber,
                currentSetNumber = currentSetNumber,
                lastLegWinnerIndex = lastLegWinnerIndex,
                lastSetWinnerIndex = lastSetWinnerIndex,
                lastLegStartingTeam = lastLegStartingTeam,
                autoOkSeconds = autoOkSeconds,
                sessionStartTime = sessionStartTime,
                sessionForm = sessionForm,
                legHistory = legHistory,
                isPairGame = isPairGame,
                teamCount = teamCount,
                playersPerTeam = playersPerTeam
            )
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────
    // Player501
    // ─────────────────────────────────────────
    private fun playerToJson(p: Player501): JSONObject {
        val o = JSONObject()
        o.put("name", p.name)
        o.put("isBot", p.isBot)
        o.put("botLevel", p.botLevel)
        o.put("teamIndex", p.teamIndex)
        o.put("botStreak", p.botStreak)
        o.put("botStreakLeft", p.botStreakLeft)
        o.put("score", p.score)
        o.put("turnScore", p.turnScore)
        o.put("turnDarts", p.turnDarts)
        o.put("legDarts", p.legDarts)
        o.put("legScoreGained", p.legScoreGained)
        o.put("legDoublesHit", p.legDoublesHit)
        o.put("legDoublesAttempted", p.legDoublesAttempted)
        o.put("legCount180", p.legCount180)
        o.put("legCount170plus", p.legCount170plus)
        o.put("legCount130plus", p.legCount130plus)
        o.put("legCount90plus", p.legCount90plus)
        o.put("legCount57plus", p.legCount57plus)
        o.put("legCount57minus", p.legCount57minus)
        o.put("matchDarts", p.matchDarts)
        o.put("matchScoreGained", p.matchScoreGained)
        o.put("matchDoublesHit", p.matchDoublesHit)
        o.put("matchDoublesAttempted", p.matchDoublesAttempted)
        o.put("matchCount180", p.matchCount180)
        o.put("matchCount170plus", p.matchCount170plus)
        o.put("matchCount130plus", p.matchCount130plus)
        o.put("matchCount90plus", p.matchCount90plus)
        o.put("matchCount57plus", p.matchCount57plus)
        o.put("matchCount57minus", p.matchCount57minus)
        o.put("nonCloseScore", p.nonCloseScore)
        o.put("nonCloseDarts", p.nonCloseDarts)
        o.put("first9Score", p.first9Score)
        o.put("first9Darts", p.first9Darts)
        o.put("listOfCloseValues", intListToJson(p.listOfCloseValues))
        o.put("listOfLegDarts", intListToJson(p.listOfLegDarts))
        o.put("listOfLegPpr", doubleListToJson(p.listOfLegPpr))
        o.put("listOfFirst9Ppr", doubleListToJson(p.listOfFirst9Ppr))
        o.put("legsInCurrentSet", p.legsInCurrentSet)
        o.put("setsWon", p.setsWon)
        return o
    }

    private fun playerFromJson(o: JSONObject): Player501 {
        val p = Player501(
            name = o.getString("name"),
            isBot = o.getBoolean("isBot"),
            botLevel = o.getInt("botLevel"),
            teamIndex = o.getInt("teamIndex")
        )
        p.botStreak = o.optDouble("botStreak", 1.0)
        p.botStreakLeft = o.optInt("botStreakLeft", 0)
        p.score = o.optInt("score", 501)
        p.turnScore = o.optInt("turnScore", 0)
        p.turnDarts = o.optInt("turnDarts", 0)
        p.legDarts = o.optInt("legDarts", 0)
        p.legScoreGained = o.optInt("legScoreGained", 0)
        p.legDoublesHit = o.optInt("legDoublesHit", 0)
        p.legDoublesAttempted = o.optInt("legDoublesAttempted", 0)
        p.legCount180 = o.optInt("legCount180", 0)
        p.legCount170plus = o.optInt("legCount170plus", 0)
        p.legCount130plus = o.optInt("legCount130plus", 0)
        p.legCount90plus = o.optInt("legCount90plus", 0)
        p.legCount57plus = o.optInt("legCount57plus", 0)
        p.legCount57minus = o.optInt("legCount57minus", 0)
        p.matchDarts = o.optInt("matchDarts", 0)
        p.matchScoreGained = o.optInt("matchScoreGained", 0)
        p.matchDoublesHit = o.optInt("matchDoublesHit", 0)
        p.matchDoublesAttempted = o.optInt("matchDoublesAttempted", 0)
        p.matchCount180 = o.optInt("matchCount180", 0)
        p.matchCount170plus = o.optInt("matchCount170plus", 0)
        p.matchCount130plus = o.optInt("matchCount130plus", 0)
        p.matchCount90plus = o.optInt("matchCount90plus", 0)
        p.matchCount57plus = o.optInt("matchCount57plus", 0)
        p.matchCount57minus = o.optInt("matchCount57minus", 0)
        p.nonCloseScore = o.optInt("nonCloseScore", 0)
        p.nonCloseDarts = o.optInt("nonCloseDarts", 0)
        p.first9Score = o.optInt("first9Score", 0)
        p.first9Darts = o.optInt("first9Darts", 0)
        p.listOfCloseValues.clear()
        p.listOfCloseValues.addAll(intListFromJson(o.optJSONArray("listOfCloseValues")))
        p.listOfLegDarts.clear()
        p.listOfLegDarts.addAll(intListFromJson(o.optJSONArray("listOfLegDarts")))
        p.listOfLegPpr.clear()
        p.listOfLegPpr.addAll(doubleListFromJson(o.optJSONArray("listOfLegPpr")))
        p.listOfFirst9Ppr.clear()
        p.listOfFirst9Ppr.addAll(doubleListFromJson(o.optJSONArray("listOfFirst9Ppr")))
        p.legsInCurrentSet = o.optInt("legsInCurrentSet", 0)
        p.setsWon = o.optInt("setsWon", 0)
        return p
    }

    // ─────────────────────────────────────────
    // LegSnapshot501
    // ─────────────────────────────────────────
    private fun legSnapshotToJson(snap: LegSnapshot501): JSONObject {
        val o = JSONObject()
        o.put("setNumber", snap.setNumber)
        o.put("legNumber", snap.legNumber)
        o.put("winningTeam", snap.winningTeam)
        val arr = JSONArray()
        for (p in snap.players) {
            arr.put(legPlayerSnapshotToJson(p))
        }
        o.put("players", arr)
        return o
    }

    private fun legSnapshotFromJson(o: JSONObject): LegSnapshot501 {
        val arr = o.optJSONArray("players") ?: JSONArray()
        val players = mutableListOf<LegPlayerSnapshot501>()
        for (i in 0 until arr.length()) {
            players.add(legPlayerSnapshotFromJson(arr.getJSONObject(i)))
        }
        return LegSnapshot501(
            setNumber = o.getInt("setNumber"),
            legNumber = o.getInt("legNumber"),
            winningTeam = o.getInt("winningTeam"),
            players = players
        )
    }

    private fun legPlayerSnapshotToJson(p: LegPlayerSnapshot501): JSONObject {
        val o = JSONObject()
        o.put("name", p.name)
        o.put("teamIndex", p.teamIndex)
        o.put("isBot", p.isBot)
        o.put("darts", p.darts)
        o.put("scoreGained", p.scoreGained)
        o.put("doublesHit", p.doublesHit)
        o.put("doublesAttempted", p.doublesAttempted)
        o.put("count180", p.count180)
        o.put("count170plus", p.count170plus)
        o.put("count130plus", p.count130plus)
        o.put("count90plus", p.count90plus)
        o.put("count57plus", p.count57plus)
        o.put("count57minus", p.count57minus)
        return o
    }

    private fun legPlayerSnapshotFromJson(o: JSONObject): LegPlayerSnapshot501 {
        return LegPlayerSnapshot501(
            name = o.getString("name"),
            teamIndex = o.getInt("teamIndex"),
            isBot = o.getBoolean("isBot"),
            darts = o.getInt("darts"),
            scoreGained = o.getInt("scoreGained"),
            doublesHit = o.getInt("doublesHit"),
            doublesAttempted = o.getInt("doublesAttempted"),
            count180 = o.optInt("count180", 0),
            count170plus = o.optInt("count170plus", 0),
            count130plus = o.optInt("count130plus", 0),
            count90plus = o.optInt("count90plus", 0),
            count57plus = o.optInt("count57plus", 0),
            count57minus = o.optInt("count57minus", 0)
        )
    }

    // ─────────────────────────────────────────
    // Хелперы для списков
    // ─────────────────────────────────────────
    private fun intListToJson(list: List<Int>): JSONArray {
        val arr = JSONArray()
        for (v in list) arr.put(v)
        return arr
    }

    private fun doubleListToJson(list: List<Double>): JSONArray {
        val arr = JSONArray()
        for (v in list) arr.put(v)
        return arr
    }

    private fun intListFromJson(arr: JSONArray?): List<Int> {
        if (arr == null) return emptyList()
        val out = mutableListOf<Int>()
        for (i in 0 until arr.length()) out.add(arr.getInt(i))
        return out
    }

    private fun doubleListFromJson(arr: JSONArray?): List<Double> {
        if (arr == null) return emptyList()
        val out = mutableListOf<Double>()
        for (i in 0 until arr.length()) out.add(arr.getDouble(i))
        return out
    }
}
