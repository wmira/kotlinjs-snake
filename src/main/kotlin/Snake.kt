
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.max
import kotlin.math.min

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
            Direction.RIGHT -> coordinate.x == next.x * cellSize * 1.0
            Direction.LEFT -> coordinate.x == next.x * cellSize * 1.0
            Direction.DOWN -> coordinate.y == next.y * cellSize * 1.0
            Direction.UP -> coordinate.y == next.y * cellSize * 1.0
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

//sealed class SnakeAction {
//    object EatenFood : SnakeAction()
//    object HitWallOrPart: SnakeAction()
//    object Moved: SnakeAction()
//}

class Snake(private val initialLength: Int = 1,
            private val gameOptions: GameOptions,
            val userOptions: UserOptions,
            private val cells: List<List<Cell>>) {
    private val initialDirection: Direction = Direction.RIGHT
    private var snake = mutableListOf<SnakePart>()
    private lateinit var game: Game
    private var direction = initialDirection
    private var nextDirection: Direction? = null
    private val cellPerSecond = (gameOptions.cellSize * 2.5)
    private val turns = mutableMapOf<CellPosition, Direction>()
    private val cellSize = gameOptions.cellSize
    private val CELL_SIZE = cellSize
    private val movePerTick: Double =  0.0 // cellPerSecond / Cell.RENDER_INTERVAL
    // private val gameOver = false
    private var snakeStatus: SnakeStatus = SnakeStatus.Initial

    init {
        val y = 0
        for (x in 0 until initialLength) {
            val cellPosition = CellPosition(initialLength - 1 - x, y)
            val cell = cells[y][cellPosition.x]

            snake.add(SnakePart(initialDirection, cellPosition , cellPosition.nextPosition(direction), Coordinate(cell.xcoord, cell.ycoord), CELL_SIZE))
        }
    }

    fun render(ctx: CanvasRenderingContext2D): Unit {
        snake.forEachIndexed { index, part ->
            // draw the move
            ctx.fillRect(part.coordinate.x,  part.coordinate.y, CELL_SIZE , CELL_SIZE)
            // now check if we need to render on the next cell (if we are turning)
            if (index > 0) {
                val prevSnake = snake[index - 1]
                if (prevSnake.direction !== part.direction) {
                    when (part.direction) {
                        Direction.RIGHT -> {
                            ctx.fillRect(part.next.x * CELL_SIZE, part.coordinate.y, CELL_SIZE, CELL_SIZE)
                        }
                        Direction.LEFT -> {
                            ctx.fillRect(part.next.x  * CELL_SIZE, part.coordinate.y, CELL_SIZE, CELL_SIZE)
                        }
                        Direction.DOWN -> {
                            ctx.fillRect(part.coordinate.x, part.next.y * CELL_SIZE, CELL_SIZE, CELL_SIZE)
                        }
                        Direction.UP -> {
                            ctx.fillRect(part.coordinate.x,  part.next.y  * CELL_SIZE, CELL_SIZE, CELL_SIZE)
                        }
                    }
                }
            }
        }
    }

    fun isPartInPosition(x: Int, y: Int): Boolean {
        return snake.filter {
            it.start.x == x && it.start.y == y
        }.isNotEmpty()
    }
    fun clear(gameContext: CanvasRenderingContext2D) {
        snake.forEachIndexed { index, part ->
            gameContext.clearRect(part.coordinate.x, part.coordinate.y, CELL_SIZE, CELL_SIZE)
        }
    }
    fun update(): SnakeStatus {
        println("MOVING SINGLE UNIT")
        return moveSingleUnit()
    }
    private fun moveSingleUnit(): SnakeStatus {
        val unitSize = when(userOptions.gameSpeed) {
            GameSpeed.Slow -> 2.0
            GameSpeed.Normal -> 2.5
            GameSpeed.Fast -> 4.0
            GameSpeed.Brutal -> 9.0
        }
        snake = snake.mapIndexed { index, part ->
            if (!part.isEatenPart) {
                val newPart = part.move(unitSize)
                println("MOVING? ${newPart}")
                newPart!!
            } else {
                part
            }
        }.toMutableList()

        return SnakeStatus.HasMoved
    }
//    fun update(interval: Double): SnakeAction {
//        val elapse = interval.toInt()
//        //val totalToMove =  interval/1000 * cellPerSecond;
//        val moveMultipler = (elapse - (elapse % Cell.RENDER_INTERVAL))/Cell.RENDER_INTERVAL
//        val totalToMove = movePerTick * moveMultipler
//        val head = snake[0]
//
//        if (head.isCompletelyInNextCell()) {
//            if (nextDirection != null) {
//                turns[head.next] = nextDirection!!
//                direction = nextDirection!!
//                nextDirection = null
//            }
//            val foodPosition = game.foodPosition
//            val hasEatenFood = foodPosition != null && foodPosition == head.next
//
//            val newSnake = snake.mapIndexed { index, part ->
//                val startCell = part.next
//
//                val directionToUse = if (turns.containsKey(part.next)) turns[part.next] else part.direction
//                val nextCell = startCell.nextPosition(directionToUse!!)
//
//                if (turns.containsKey(part.next) && index == snake.size - 1) {
//                    turns.remove(part.next)
//                }
//                part.toNextCell(directionToUse!!)
//            }.toMutableList()
//            if (hasEatenFood) {
//                // add a part at the end same position as last
//                val lastPart = newSnake[newSnake.size - 1]
//                val eatenPart = SnakePart(lastPart.direction, lastPart.start, lastPart.start, lastPart.coordinate, true)
//                newSnake.add(eatenPart)
//            }
//            snake = newSnake
//            return if (hasEatenFood) SnakeAction.EatenFood else SnakeAction.Moved
//        }
//
//        // check if we hit something if we move
//        val possibleHeadThatHit = head.move(totalToMove)
//        val isGameOver = hitsWallOrPart(possibleHeadThatHit)
//        if (isGameOver) {
//            return SnakeAction.HitWallOrPart
//        }
//        snake = snake.mapIndexed { index, part ->
//            if (!part.isEatenPart) {
//                val newPart = part.move(totalToMove)
//                newPart!!
//            } else {
//                part
//            }
//        }.toMutableList()
//        return SnakeAction.Moved
//    }
//
//    private fun hitsWallOrPart(possibleHeadThatHit: SnakePart): Boolean {
//        val x = possibleHeadThatHit.coordinate.x
//        val y = possibleHeadThatHit.coordinate.y
//
//        if (x < 0 || (x + CELL_SIZE) > Cell.XCELLS * CELL_SIZE ||
//                y < 0 || (y + CELL_SIZE) > Cell.YCELLS * CELL_SIZE) {
//            return true
//        }
//        // check if we hit
//        return snake.filterIndexed { index, snakePart ->
//            index != 0 && (snakePart.start == possibleHeadThatHit.start ||
//                    snakePart.next == possibleHeadThatHit.next)
//        }.isNotEmpty()
//    }
//
//    private fun addNewPart(head: SnakePart): SnakePart {
//        val nextCell = head.next.nextPosition(direction)
//        val cell = game.getCell(nextCell.x, nextCell.y)
//        return SnakePart(direction, nextCell, nextCell.nextPosition(direction), Coordinate(cell.xcoord, cell.ycoord))
//    }
//
//    fun changeDirection(newDirection: Direction) {
//        nextDirection = when(newDirection) {
//            Direction.LEFT -> if (direction == Direction.RIGHT) null else newDirection
//            Direction.RIGHT -> if (direction == Direction.LEFT) null else newDirection
//            Direction.DOWN-> if (direction == Direction.UP) null else newDirection
//            Direction.UP -> if (direction == Direction.DOWN) null else newDirection
//        }
//    }
}
