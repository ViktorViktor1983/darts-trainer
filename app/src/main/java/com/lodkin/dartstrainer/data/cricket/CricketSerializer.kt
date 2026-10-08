package com.lodkin.dartstrainer.data.cricket

import org.json.JSONArray
import org.json.JSONObject

/**
 * Сериализация партии крикета в JSON-строку и обратно.
 * Нужна для сохранения прерванных партий в базу.
 *
 * Использует встроенный в Android org.json — никаких сторонних библиотек.
 */
object CricketSerializer {

    // ─────────────────────────────────────────
    // CricketGame → JSON
    // ─────────────────────────────────────────
    fun toJson(game: CricketGame): String {
        val root = JSONObject()

        root.put("type", game.type.name)
        root.put("currentPlayerIndex", game.currentPlayerIndex)
        root.put("isFinished", game.isFinished)
        root.put("winnerIndex", game.winnerIndex ?: JSONObject.NULL)
        root.put("currentTurnDarts", game.currentTurnDarts)
        root.put("legsPerSet", game.legsPerSet)
        root.put("setsPerMatch", game.setsPerMatch)
        root.put("currentLegNumber", game.currentLegNumber)
        root.put("currentSetNumber", game.currentSetNumber)
        root.put("lastLegWinnerIndex", game.lastLegWinnerIndex ?: JSONObject.NULL)
        root.put("lastSetWinnerIndex", game.lastSetWinnerIndex ?: JSONObject.NULL)
        root.put("lastLegWinnerPlayerIndex", game.lastLegWinnerPlayerIndex ?: JSONObject.NULL)
        root.put("lastLegDartsClicked", game.lastLegDartsClicked)
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
    // JSON → CricketGame
    // ─────────────────────────────────────────
    fun fromJson(json: String): CricketGame? {
        return try {
            val root = JSONObject(json)

            val type = CricketType.valueOf(root.getString("type"))
            val currentPlayerIndex = root.getInt("currentPlayerIndex")
            val isFinished = root.getBoolean("isFinished")
            val winnerIndex = if (root.isNull("winnerIndex")) null else root.getInt("winnerIndex")
            val currentTurnDarts = root.getInt("currentTurnDarts")
            val legsPerSet = root.getInt("legsPerSet")
            val setsPerMatch = root.getInt("setsPerMatch")
            val currentLegNumber = root.getInt("currentLegNumber")
            val currentSetNumber = root.getInt("currentSetNumber")
            val lastLegWinnerIndex = if (root.isNull("lastLegWinnerIndex")) null else root.getInt("lastLegWinnerIndex")
            val lastSetWinnerIndex = if (root.isNull("lastSetWinnerIndex")) null else root.getInt("lastSetWinnerIndex")
            val lastLegWinnerPlayerIndex = if (root.isNull("lastLegWinnerPlayerIndex")) null else root.getInt("lastLegWinnerPlayerIndex")
            val lastLegDartsClicked = root.optInt("lastLegDartsClicked", 0)
            val lastLegStartingTeam = root.optInt("lastLegStartingTeam", 0)
            val autoOkSeconds = root.optInt("autoOkSeconds", 0)
            val sessionStartTime = root.optLong("sessionStartTime", 0L)
            val sessionForm = root.optDouble("sessionForm", 1.0)
            val isPairGame = root.optBoolean("isPairGame", false)
            val teamCount = root.optInt("teamCount", 2)
            val playersPerTeam = root.optInt("playersPerTeam", 1)

            // Игроки
            val playersArr = root.getJSONArray("players")
            val players = mutableListOf<CricketPlayer>()
            for (i in 0 until playersArr.length()) {
                players.add(playerFromJson(playersArr.getJSONObject(i)))
            }

            // История легов
            val historyArr = root.optJSONArray("legHistory") ?: JSONArray()
            val legHistory = mutableListOf<LegSnapshot>()
            for (i in 0 until historyArr.length()) {
                legHistory.add(legSnapshotFromJson(historyArr.getJSONObject(i)))
            }

            CricketGame(
                type = type,
                players = players,
                currentPlayerIndex = currentPlayerIndex,
                isFinished = isFinished,
                winnerIndex = winnerIndex,
                currentTurnDarts = currentTurnDarts,
                legsPerSet = legsPerSet,
                setsPerMatch = setsPerMatch,
                currentLegNumber = currentLegNumber,
                currentSetNumber = currentSetNumber,
                lastLegWinnerIndex = lastLegWinnerIndex,
                lastSetWinnerIndex = lastSetWinnerIndex,
                lastLegWinnerPlayerIndex = lastLegWinnerPlayerIndex,
                lastLegDartsClicked = lastLegDartsClicked,
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
    // CricketPlayer
    // ─────────────────────────────────────────
    private fun playerToJson(p: CricketPlayer): JSONObject {
        val o = JSONObject()
        o.put("name", p.name)
        o.put("isBot", p.isBot)
        o.put("botLevel", p.botLevel)
        o.put("teamIndex", p.teamIndex)

        o.put("botStreak", p.botStreak)
        o.put("botStreakLeft", p.botStreakLeft)

        o.put("turnMarks", p.turnMarks)
        o.put("turnMisses", p.turnMisses)
        o.put("turnTriples", p.turnTriples)

        o.put("matchPerfectRounds", p.matchPerfectRounds)
        o.put("matchStrongRounds", p.matchStrongRounds)

        o.put("legMisses", p.legMisses)
        o.put("legTriples", p.legTriples)

        o.put("hits", sectorIntMapToJson(p.hits))
        o.put("scores", sectorIntMapToJson(p.scores))
        o.put("totalScore", p.totalScore)
        o.put("dartsThrown", p.dartsThrown)
        o.put("legMarks", p.legMarks)
        o.put("legsInCurrentSet", p.legsInCurrentSet)
        o.put("setsWon", p.setsWon)

        o.put("matchTotalScore", p.matchTotalScore)
        o.put("matchDartsThrown", p.matchDartsThrown)
        o.put("matchMissesThrown", p.matchMissesThrown)
        o.put("matchTriplesHit", p.matchTriplesHit)
        o.put("matchBullAttempts", p.matchBullAttempts)
        o.put("matchBullHits", p.matchBullHits)
        o.put("matchHits", sectorIntMapToJson(p.matchHits))
        o.put("matchScores", sectorIntMapToJson(p.matchScores))
        return o
    }

    private fun playerFromJson(o: JSONObject): CricketPlayer {
        val p = CricketPlayer(
            name = o.getString("name"),
            isBot = o.getBoolean("isBot"),
            botLevel = o.optInt("botLevel", 0),
            teamIndex = o.getInt("teamIndex")
        )

        p.botStreak = o.optDouble("botStreak", 1.0)
        p.botStreakLeft = o.optInt("botStreakLeft", 0)

        p.turnMarks = o.optInt("turnMarks", 0)
        p.turnMisses = o.optInt("turnMisses", 0)
        p.turnTriples = o.optInt("turnTriples", 0)

        p.matchPerfectRounds = o.optInt("matchPerfectRounds", 0)
        p.matchStrongRounds = o.optInt("matchStrongRounds", 0)

        p.legMisses = o.optInt("legMisses", 0)
        p.legTriples = o.optInt("legTriples", 0)

        p.hits.clear()
        p.hits.putAll(sectorIntMapFromJson(o.optJSONObject("hits")))
        p.scores.clear()
        p.scores.putAll(sectorIntMapFromJson(o.optJSONObject("scores")))

        p.totalScore = o.optInt("totalScore", 0)
        p.dartsThrown = o.optInt("dartsThrown", 0)
        p.legMarks = o.optInt("legMarks", 0)
        p.legsInCurrentSet = o.optInt("legsInCurrentSet", 0)
        p.setsWon = o.optInt("setsWon", 0)

        p.matchTotalScore = o.optInt("matchTotalScore", 0)
        p.matchDartsThrown = o.optInt("matchDartsThrown", 0)
        p.matchMissesThrown = o.optInt("matchMissesThrown", 0)
        p.matchTriplesHit = o.optInt("matchTriplesHit", 0)
        p.matchBullAttempts = o.optInt("matchBullAttempts", 0)
        p.matchBullHits = o.optInt("matchBullHits", 0)

        p.matchHits.clear()
        p.matchHits.putAll(sectorIntMapFromJson(o.optJSONObject("matchHits")))
        p.matchScores.clear()
        p.matchScores.putAll(sectorIntMapFromJson(o.optJSONObject("matchScores")))

        return p
    }

    // ─────────────────────────────────────────
    // LegSnapshot
    // ─────────────────────────────────────────
    private fun legSnapshotToJson(snap: LegSnapshot): JSONObject {
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

    private fun legSnapshotFromJson(o: JSONObject): LegSnapshot {
        val arr = o.optJSONArray("players") ?: JSONArray()
        val players = mutableListOf<LegPlayerSnapshot>()
        for (i in 0 until arr.length()) {
            players.add(legPlayerSnapshotFromJson(arr.getJSONObject(i)))
        }
        return LegSnapshot(
            setNumber = o.getInt("setNumber"),
            legNumber = o.getInt("legNumber"),
            winningTeam = o.getInt("winningTeam"),
            players = players
        )
    }

    private fun legPlayerSnapshotToJson(p: LegPlayerSnapshot): JSONObject {
        val o = JSONObject()
        o.put("name", p.name)
        o.put("teamIndex", p.teamIndex)
        o.put("isBot", p.isBot)
        o.put("legMarks", p.legMarks)
        o.put("darts", p.darts)
        o.put("misses", p.misses)
        o.put("triples", p.triples)
        o.put("score", p.score)
        return o
    }

    private fun legPlayerSnapshotFromJson(o: JSONObject): LegPlayerSnapshot {
        return LegPlayerSnapshot(
            name = o.getString("name"),
            teamIndex = o.getInt("teamIndex"),
            isBot = o.getBoolean("isBot"),
            legMarks = o.getInt("legMarks"),
            darts = o.getInt("darts"),
            misses = o.getInt("misses"),
            triples = o.getInt("triples"),
            score = o.getInt("score")
        )
    }

    // ─────────────────────────────────────────
    // MutableMap<CricketSector, Int>
    // ─────────────────────────────────────────
    private fun sectorIntMapToJson(map: Map<CricketSector, Int>): JSONObject {
        val o = JSONObject()
        for ((sector, value) in map) {
            o.put(sector.name, value)
        }
        return o
    }

    private fun sectorIntMapFromJson(o: JSONObject?): Map<CricketSector, Int> {
        val result = mutableMapOf<CricketSector, Int>()
        // Сначала все сектора = 0, потом перезаписываем, что есть в JSON
        for (s in CricketSector.ALL) result[s] = 0
        if (o != null) {
            for (s in CricketSector.ALL) {
                if (o.has(s.name)) result[s] = o.getInt(s.name)
            }
        }
        return result
    }
}
