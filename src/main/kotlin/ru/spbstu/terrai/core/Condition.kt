package ru.spbstu.terrai.core

data class Condition(val items: MutableList<Item>, val exitReached: Boolean) {
    constructor(): this(false)

    constructor(exitReached: Boolean): this(arrayListOf(), exitReached)

    val hasTreasure get() = Treasure in items
    val hasBomb get() = Bomb in items
}