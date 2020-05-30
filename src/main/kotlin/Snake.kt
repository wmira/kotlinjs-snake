
import org.w3c.dom.CanvasRenderingContext2D

class CellPosition(val x: Int, val y: Int) {

    fun nextPosition(direction: Direction): CellPosition {
        return getNextCellPosition(direction, x, y)
    }

    override fun toString(): String {
        return "CellPosition(x=$x, y=$y)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as CellPosition

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
    companion object {
        fun getNextCellPosition(direction: Direction, x: Int, y: Int): CellPosition {
            return when(direction) {
                Direction.RIGHT -> CellPosition(x + 1, y)
                Direction.DOWN -> CellPosition(x, y + 1)
                Direction.LEFT -> CellPosition(x - 1, y)
                Direction.UP -> CellPosition(x, y - 1)
            }
        }
    }
}

class Coordinate(val x: Double, val y: Double) {
    fun moveX(move: Double): Coordinate {
        return Coordinate((x + move), y);
    }

    fun moveY(totalToMove: Double): Coordinate {

        return Coordinate(x, y + totalToMove )
    }

    fun move(totalToMove: Double, direction: Direction): Coordinate {
        return when(direction) {
            Direction.RIGHT -> moveX(totalToMove)
            Direction.DOWN -> moveY(totalToMove)
            Direction.LEFT -> {
                return Coordinate(x - (totalToMove), y);
            }
            Direction.UP -> {
                return Coordinate(x, y - totalToMove)
            }
        }
    }

    override fun toString(): String {
        return "Coordinate(x=$x, y=$y)"
    }

}

class SnakePart(val direction: Direction,
                val start: CellPosition,
                val next: CellPosition,
                val coordinate: Coordinate,
                private val cellSize: Double,
                val isEatenPart: Boolean = false) {
//    private val maxMove = when(direction) {
//        Direction.RIGHT -> next.x * cellSize
//        Direction.LEFT -> (next.x) * cellSize
//        Direction.DOWN -> next.y * cellSize
//        Direction.UP -> (next.y) * cellSize
//    }
    fun isCompletelyInNextCell(): Boolean {
        return when(direction) {
            Direction.RIGHT -> coordinate.x == next.x * cellSize
            Direction.LEFT -> coordinate.x == next.x * cellSize
            Direction.DOWN -> coordinate.y == next.y * cellSize
            Direction.UP -> coordinate.y == next.y * cellSize
        }

    }
    fun toNextCell(newDirection: Direction): SnakePart {
        return SnakePart(newDirection, next, next.nextPosition(newDirection), coordinate, cellSize)
    }

    override fun toString(): String {
        return "SnakePart(direction=$direction, start=$start, next=$next, coordinate=$coordinate)"
    }

    fun move(moveTotal: Double): SnakePart {
        val nextCoordinate = coordinate.move(moveTotal, direction)
        return SnakePart(direction, start, next, nextCoordinate, cellSize)
    }
}

class Snake(private val initialLength: Int = 1,
            private val gameOptions: GameOptions,
            private val userOptions: UserOptions,
            private val cells: List<List<Cell>>) {

    private val initialDirection: Direction = Direction.RIGHT
    private var parts = mutableListOf<SnakePart>()
    private lateinit var game: Game
    private var nextDirection: Direction? = null // change to Player then getInput, player is passed from update
    private val turns = mutableListOf<Turn>()
    private val cellSize = gameOptions.cellSize
    private var totalMove = 0

    init {
        val y = 0
        for (x in 0 until initialLength) {
            val cellPosition = CellPosition(initialLength - 1 - x, y)
            val cell = cells[y][cellPosition.x]

            parts.add(SnakePart(initialDirection, cellPosition , cellPosition.nextPosition(initialDirection), Coordinate(cell.xcoord, cell.ycoord), cellSize))
        }
    }

    fun getParts(): List<SnakePart> {
        return parts
    }

    fun render(ctx: CanvasRenderingContext2D): Unit {
        parts.forEachIndexed { index, part ->
            // draw the move
            ctx.fillRect(part.coordinate.x,  part.coordinate.y, cellSize , cellSize)
            // now check if we need to render on the next cell (if we are turning)
            if (index > 0) {
                val prevSnake = parts[index - 1]
                if (prevSnake.direction !== part.direction) {
                    when (part.direction) {
                        Direction.RIGHT -> {
                            ctx.fillRect(part.next.x * cellSize, part.coordinate.y, cellSize, cellSize)
                        }
                        Direction.LEFT -> {
                            ctx.fillRect(part.next.x  * cellSize, part.coordinate.y, cellSize, cellSize)
                        }
                        Direction.DOWN -> {
                            ctx.fillRect(part.coordinate.x, part.next.y * cellSize, cellSize, cellSize)
                        }
                        Direction.UP -> {
                            ctx.fillRect(part.coordinate.x,  part.next.y  * cellSize, cellSize, cellSize)
                        }
                    }
                }
            }
        }
    }

    private val unitSize = when(userOptions.gameSpeed) {
        GameSpeed.Slow -> 1.0
        GameSpeed.Normal -> 2.0
        GameSpeed.Fast -> 6.0
        GameSpeed.Brutal -> 9.0
    }

    private fun getNewDirection(current: Direction): Direction {
        val head = parts[0]
        val headAtEndOfCell = head.isCompletelyInNextCell()
        if (headAtEndOfCell && nextDirection != null) {
            val newDirection = when(nextDirection!!) {
                Direction.LEFT -> if (current == Direction.RIGHT) current else nextDirection!!
                Direction.RIGHT -> if (current == Direction.LEFT) current else nextDirection!!
                Direction.DOWN-> if (current == Direction.UP) current else nextDirection!!
                Direction.UP -> if (current == Direction.DOWN) current else nextDirection!!
            }
            turns.add(Turn(head.next, newDirection))
            nextDirection = null
            return newDirection
        }
        return current //direction
    }
    private fun getTurnForCell(cell: CellPosition, currentTurns: List<Turn>): Turn? {
        for (turn in currentTurns) {
            if (turn.cellPosition == cell) {
                return turn
            }
        }
        return null
    }
    fun hitsWallOrPart(possibleHeadThatHit: SnakePart): Boolean {
        val x = possibleHeadThatHit.coordinate.x
        val y = possibleHeadThatHit.coordinate.y

        if (x < 0 || (x + cellSize) > xCells * cellSize ||
                y < 0 || (y + cellSize) > yCells * cellSize) {
            return true
        }
        // check if we hit
        return parts.filterIndexed { index, snakePart ->
            index != 0 && (snakePart.start == possibleHeadThatHit.start ||
                    snakePart.next == possibleHeadThatHit.next)
        }.isNotEmpty()
    }

    fun update(food: Food?, player: Player): SnakeStatus {
        if (gameOptions.maxMove > 0 && totalMove == gameOptions.maxMove) {
            return SnakeStatus.HasHitWallOrBody
        }
        totalMove += 1
        val head = parts[0]
        val headAtEndOfCell = head.isCompletelyInNextCell()

        var snakePartToMove = parts
        var hasEatenFood = false

        if (headAtEndOfCell) {
            val newDirection = getDirectionFromPlayer(player, parts) // getNewDirection(head.direction)
            val nextMoveIsGameOver = headAtEndOfCell && hitsWallOrPart(head.toNextCell(newDirection).move(unitSize))
            if (nextMoveIsGameOver) {
                return SnakeStatus.HasHitWallOrBody
            }
            val foodPosition = food?.position
            hasEatenFood = foodPosition != null && foodPosition == head.next
            snakePartToMove = parts.mapIndexed { index, part ->
                //val startCell = part.next
                //val directionToUse = if (turns.containsKey(part.next)) turns[part.next] else part.direction
                var directionToUse = part.direction
                if (index == 0) {
                    directionToUse = newDirection
                } else {
                    val turn = getTurnForCell(part.next, turns)
                    if (turn != null) {
                        directionToUse = turn.direction
                    }
                }
                part.toNextCell(directionToUse!!)
            }.toMutableList()

            // see if we have processed the turn
            val cellOfLastPart = parts[snakePartToMove.size - 1].next
            if (turns.size > 0 && turns[0].cellPosition.equals(cellOfLastPart)) {
                turns.removeAt(0)
            }
            // if we have eaten
            if (hasEatenFood) {
                // add a part at the end same position as last
                val lastPart = snakePartToMove[snakePartToMove.size - 1]
                val eatenPart = SnakePart(lastPart.direction, lastPart.start, lastPart.start, lastPart.coordinate, gameOptions.cellSize, true)
                snakePartToMove.add(eatenPart)
            }
        }

        parts = snakePartToMove.mapIndexed { index, part ->
            if (!part.isEatenPart) {
                val newPart = part.move(unitSize)
                newPart!!
            } else {
                part
            }
        }.toMutableList()

        if (hasEatenFood) {
            return SnakeStatus.HasEatenFood
        }
        return SnakeStatus.HasMoved
    }

    private fun getDirectionFromPlayer(player: Player, snakeParts: List<SnakePart>): Direction {
        val head = snakeParts[0]
        val headAtEndOfCell = head.isCompletelyInNextCell()
        val current = head.direction
        val directionFromPlayer = player.getNextDirection()
        if (headAtEndOfCell && directionFromPlayer != null) {
            val newDirection = when(directionFromPlayer) {
                Direction.LEFT -> if (current == Direction.RIGHT) current else directionFromPlayer
                Direction.RIGHT -> if (current == Direction.LEFT) current else directionFromPlayer
                Direction.DOWN-> if (current == Direction.UP) current else directionFromPlayer
                Direction.UP -> if (current == Direction.DOWN) current else directionFromPlayer
            }
            turns.add(Turn(head.next, newDirection))
            nextDirection = null
            return newDirection
        }
        return current //direction
    }

}
