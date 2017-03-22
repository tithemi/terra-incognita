package ru.spbstu.terrai.players.samples

import ru.spbstu.terrai.core.*
import ru.spbstu.terrai.lab.Controller
import ru.spbstu.terrai.lab.Labyrinth

class Cross : AbstractPlayer() {
    private lateinit var currentLocation: Location

    private val roomMap = mutableMapOf<Location, Room>()

    override fun setStartLocationAndSize(location: Location, width: Int, height: Int) {
        super.setStartLocationAndSize(location, width, height)
        currentLocation = location
        roomMap[currentLocation] = Entrance
    }

    private var lastMove: Move = WalkMove(Direction.NORTH)

    private val decisions = mutableListOf<Direction>()

    private var lastDirection = Direction.NORTH

    private var wormholes = 0

    private var treasureFound = false

    override fun getNextMove(): Move {
        val newLocation = lastDirection.turnLeft() + currentLocation
        if (!roomMap.containsKey(newLocation)) {
            lastDirection = lastDirection.turnLeft()
        }
        decisions += lastDirection
        return WalkMove(lastDirection)
    }

    override fun setMoveResult(result: MoveResult) {
        println(result.status)
        val newLocation = lastDirection + currentLocation
        val room = result.room
        roomMap[newLocation] = room
        if (result.successful) {
            currentLocation = newLocation
            when(room) {
                is Wormhole -> {
                    decisions.clear()
                    wormholes++
                    currentLocation = Location(wormholes * 1000, wormholes * 1000)
                    roomMap[currentLocation] = room
                }
                is WithContent -> {
                    if (!treasureFound && result.condition.hasTreasure) {
                        decisions.clear()
                        roomMap.clear()
                        treasureFound = true
                    }
                }
                else -> currentLocation = newLocation
            }
        }
        else {
            decisions.removeAt(decisions.size - 1)
            lastDirection = lastDirection.turnBack()
        }
    }
}

fun main(args: Array<String>) {
    val lab = Labyrinth.createFromFile("labyrinths/lab7.txt")
    val player = Cross()
    val controller = Controller(lab, player)
    val result = controller.makeMoves(1000)
    if (result.exitReached) {
        println("You won!")
    }
    else {
        println("You lose!")
    }
}