package ru.spbstu.terrai.lab

import ru.spbstu.terrai.core.*
import java.util.*

class Controller(private val lab: Labyrinth, private val player: Player) {

    private var playerLocation = lab.entrances.let {
        it[random.nextInt(it.size)]
    }.apply {
        player.setStartLocationAndSize(this, lab.width, lab.height)
    }

    private var playerCondition: Condition = Condition()

    var moves = 0

    internal val playerPath = mutableMapOf(0 to playerLocation)

    data class GameResult(val moves: Int, val exitReached: Boolean)

    fun makeMoves(moveLimit: Int): GameResult {
        var wallCount = 0
        while (moves < moveLimit) {
            val oldMoves = moves
            val moveResult = makeMove()
            val newMoves = moves
            wallCount = if (oldMoves == newMoves) wallCount + 1 else 0
            if (wallCount >= 100) return moveResult
            playerPath[moves] = playerLocation
            if (moveResult.exitReached) return moveResult
        }
        return GameResult(moves, exitReached = false)
    }

    fun makeMove(): GameResult {
        println(lab.width)
        println(lab.height)
        for (i in -1..lab.height) {
            for (j in -1..lab.width)
                if (Location(j, i) == playerLocation)
                    print("X")
                else when(lab[Location(j, i)]) {
                    Exit -> print("E")
                    Entrance -> print("S")
                    Wall -> print("#")
                    is Wormhole -> print("D")
                    WithContent(Treasure) -> print("T")
                    WithContent(Bomb) -> print("B")
                    else -> print(" ")
                }
            println()
        }

        if (playerCondition.exitReached) return GameResult(moves, exitReached = true)
        val move = player.getNextMove()
        val moveResult = when (move) {
            WaitMove -> {
                MoveResult(lab[playerLocation], playerCondition, true, "Nothing changes")
            }
            is WalkMove -> {
                var newLocation = move.direction + playerLocation
                var newRoom = lab[newLocation]
                val (movePossible, status) = when (newRoom) {
                    Empty, Entrance -> true to "Empty room appears"
                    Wall -> {
                        if (playerCondition.hasBomb && newLocation.x != -1 && newLocation.y != -1
                                && newLocation.x != lab.width && newLocation.y != lab.height) {
                            newLocation = move.direction + playerLocation
                            playerCondition.items.remove(Bomb)
                            newRoom = Empty
                            true to "You bombed wall"
                        }
                        else {
                            newLocation = playerLocation
                            false to "Wall prevents from moving"
                        }
                    }
                    is WithContent -> {
                        val content = newRoom.content
                        when (content) {
                            is Treasure -> {
                                playerCondition.items.add(content)
                                newRoom.content = null
                                true to "Treasure found"
                            }
                            is Bomb -> {
                                playerCondition.items.add(content)
                                newRoom.content = null
                                true to "Bomb found"
                            }
                            null -> true to "Empty room appears"
                            else -> throw UnsupportedOperationException("Unsupported content: $content")
                        }
                    }
                    Exit -> {
                        if (playerCondition.hasTreasure) {
                            playerCondition = playerCondition.copy(exitReached = true)
                            true to "Exit reached, you won"
                        }
                        else {
                            true to "Exit reached but you do not have a treasure"
                        }
                    }
                    is Wormhole -> {
                        newLocation = lab.wormholeMap[newLocation]!!
                        true to "Fall into wormhole!"
                    }
                }
                playerLocation = newLocation
                MoveResult(newRoom, playerCondition, movePossible, status)
            }
        }
        player.setMoveResult(moveResult)
        if (moveResult.successful) {
            moves++
        }
        return GameResult(moves, playerCondition.exitReached)
    }

    companion object {
        val random = Random(Calendar.getInstance().timeInMillis)
    }
}