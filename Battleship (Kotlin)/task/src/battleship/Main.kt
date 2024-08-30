package battleship

import kotlin.math.abs


fun main() {
    val mymap = mutableMapOf<Char, MutableList<Char>>()
    val ships = mapOf(
        "Aircraft Carrier" to 5,
        "Battleship" to 4,
        "Submarine" to 3,
        "Cruiser" to 3,
        "Destroyer" to 2
    )

    // Create keys from 'A' to 'J' (10 keys)
    for (i in 0 until 10) {
        val key = 'A' + i
        mymap[key] = MutableList(10) { '~' } // Initialize each list with '~'
    }

    printGrid(mymap)

    for ((shipName, shipSize) in ships) {
        var validPlacement = false
        while (!validPlacement) {
            println("Enter the coordinates of the $shipName ($shipSize cells):")
            print("> ")
            val input = readLine() ?: ""
            val coordinates = input.split(" ")

            if (coordinates.size == 2) {
                val firstp = coordinates[0]
                val lastp = coordinates[1]

                if (firstp.length <= 3 && lastp.length <= 3) {
                    val startRow = firstp[0]
                    val startCol = firstp.substring(1).toIntOrNull()
                    val endRow = lastp[0]
                    val endCol = lastp.substring(1).toIntOrNull()

                    if (startCol != null && endCol != null && startCol in 1..10 && endCol in 1..10 &&
                        startRow in mymap.keys && endRow in mymap.keys
                    ) {
                        val length = if (startRow == endRow) {
                            abs(endCol - startCol) + 1
                        } else if (startCol == endCol) {
                            abs(endRow - startRow) + 1
                        } else {
                            0
                        }

                        if (length == shipSize) {
                            if (canPlaceShip(mymap, startRow, endRow, startCol, endCol)) {
                                placeShip(mymap, startRow, endRow, startCol, endCol)
                                validPlacement = true
                                printGrid(mymap)
                            } else {
                                println("Error! You placed it too close to another one. Try again:")
                            }
                        } else {
                            println("Error! Wrong length of the $shipName! Try again:")
                        }
                    } else {
                        println("Invalid input format.")
                    }
                } else {
                    println("Invalid input format. Please enter coordinates in the format 'D5 E3'.")
                }
            } else {
                println("Please enter exactly two coordinates.")
            }
        }
    }

    println("The game starts!")
    printFogOfWar(mymap)

    var end = false
    while (!end) {
        println("Take a shot!")
        print("> ")
        val input2 = readLine() ?: ""
        if (input2.length <= 3) {
            val targetRow = input2[0]
            val targetCol = input2.substring(1).toIntOrNull()
            if (targetRow in 'A'..'J' && targetCol != null && targetCol in 1..10) {
                shooting(mymap, targetRow, targetCol)
                if (isAllShipsSunk(mymap)) {
                    end = true
                    println("You sank the last ship. You won. Congratulations!")
                }
            } else {
                println("Error! You entered the wrong coordinates! Try again:")
            }
        } else {
            println("Error! You entered the wrong coordinates! Try again:")
        }
    }
}

fun printGrid(mymap: Map<Char, List<Char>>) {
    println("  1 2 3 4 5 6 7 8 9 10")
    for (i in mymap.keys) {
        print("$i ")
        for (j in mymap[i]!!) {
            print("$j ")
        }
        println()
    }
    println()
}

fun printFogOfWar(mymap: Map<Char, List<Char>>) {
    println("  1 2 3 4 5 6 7 8 9 10")
    for (i in mymap.keys) {
        print("$i ")
        for (j in mymap[i]!!) {
            print(if (j == 'O') "~ " else "$j ")
        }
        println()
    }
    println()
}

fun canPlaceShip(mymap: MutableMap<Char, MutableList<Char>>, r1: Char, r2: Char, c1: Int, c2: Int): Boolean {
    val (startRow, endRow) = if (r1 <= r2) r1 to r2 else r2 to r1
    val (startCol, endCol) = if (c1 <= c2) c1 to c2 else c2 to c1

    for (row in (startRow - 1)..(endRow + 1)) {
        if (row !in 'A'..'J') continue
        for (col in (startCol - 1)..(endCol + 1)) {
            if (col !in 1..10) continue
            if (mymap[row]?.get(col - 1) != '~') return false
        }
    }
    return true
}

fun placeShip(mymap: MutableMap<Char, MutableList<Char>>, r1: Char, r2: Char, c1: Int, c2: Int) {
    val (startRow, endRow) = if (r1 <= r2) r1 to r2 else r2 to r1
    val (startCol, endCol) = if (c1 <= c2) c1 to c2 else c2 to c1

    for (row in startRow..endRow) {
        for (col in startCol..endCol) {
            mymap[row]?.set(col - 1, 'O')
        }
    }
}

fun shooting(mymap: MutableMap<Char, MutableList<Char>>, row: Char, column: Int) {
    when (mymap[row]?.get(column - 1)) {
        'O' -> {
            mymap[row]?.set(column - 1, 'X')
            printFogOfWar(mymap)
            if (isShipSunk(mymap, row, column)) {
                println("You sank a ship!")
            } else {
                println("You hit a ship!")
            }
        }
        'X' -> {
            printFogOfWar(mymap)
            println("You hit a ship!")
        }
        else -> {
            mymap[row]?.set(column - 1, 'M')
            printFogOfWar(mymap)
            println("You missed!")
        }
    }
}

fun isShipSunk(mymap: MutableMap<Char, MutableList<Char>>, row: Char, column: Int): Boolean {
    val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
    val queue = mutableListOf(row to column)
    val visited = mutableSetOf<Pair<Char, Int>>()

    while (queue.isNotEmpty()) {
        val (r, c) = queue.removeAt(0)
        if (r to c in visited) continue
        visited.add(r to c)

        for ((dr, dc) in directions) {
            val newRow = r + dr.toChar().code
            val newCol = c + dc
            if (newRow in 'A'..'J' && newCol in 1..10) {
                when (mymap[newRow]?.get(newCol - 1)) {
                    'O' -> return false
                    'X' -> queue.add(newRow to newCol)
                }
            }
        }
    }
    return true
}

fun isAllShipsSunk(mymap: MutableMap<Char, MutableList<Char>>): Boolean {
    return mymap.values.flatten().none { it == 'O' }
}