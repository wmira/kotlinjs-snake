import Cell.Companion.CELL_SIZE
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
    fun moveX(move: Double, max: Double): Coordinate {
        return Coordinate(min(x + move, max), y);
    }

    fun moveY(totalToMove: Double, max: Double): Coordinate {

        return Coordinate(x, min(y + totalToMove, max))
    }

    fun move(totalToMove: Double, direction: Direction, maxMove: Double): Coordinate {
        return when(direction) {
            Direction.RIGHT -> moveX(totalToMove, maxMove)
            Direction.DOWN -> moveY(totalToMove, maxMove)
            Direction.LEFT -> {
                return Coordinate(max(x - (totalToMove), maxMove), y);
            }
            Direction.UP -> {
                return Coordinate(x, max(y - totalToMove, maxMove))
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
                val coordinate: Coordinate) {
    private val maxMove = when(direction) {
        Direction.RIGHT -> next.x * CELL_SIZE
        Direction.LEFT -> (next.x) * CELL_SIZE
        Direction.DOWN -> next.y * CELL_SIZE
        Direction.UP -> (next.y) * CELL_SIZE
    }
    fun isCompletelyInNextCell(): Boolean {
        return when(direction) {
            Direction.RIGHT -> coordinate.x == next.x * CELL_SIZE
            Direction.LEFT -> coordinate.x == next.x * CELL_SIZE
            Direction.DOWN -> coordinate.y == next.y * CELL_SIZE
            Direction.UP -> coordinate.y == next.y * CELL_SIZE
        }

    }
    fun toNextCell(newDirection: Direction): SnakePart {
        return SnakePart(newDirection, next, next.nextPosition(newDirection), coordinate)
    }

    override fun toString(): String {
        return "SnakePart(direction=$direction, start=$start, next=$next, coordinate=$coordinate)"
    }

    fun move(moveTotal: Double): SnakePart {
        val nextCoordinate = coordinate.move(moveTotal, direction, maxMove)
        return SnakePart(direction, start, next, nextCoordinate)
    }
}

class Snake(private val initialLength: Int = 0,
            private val initialDirection: Direction = Direction.RIGHT) {
    private var snake = mutableListOf<SnakePart>()
    private lateinit var game: Game
    private var direction = initialDirection
    private var nextDirection: Direction? = null
    private val cellPerSecond = (CELL_SIZE * 2)
    private val turns = mutableMapOf<CellPosition, Direction>()
    private val movePerTick: Double = cellPerSecond / Cell.RENDER_INTERVAL

    fun init(gameInstance: Game) {
        val y = 0
        game = gameInstance
        for (x in 0 until initialLength) {
            val cellPosition = CellPosition(initialLength - 1 - x, y)
            val cell = game.getCell(cellPosition.x ,y)
            snake.add(SnakePart(initialDirection, cellPosition , cellPosition.nextPosition(direction), Coordinate(cell.xcoord, cell.ycoord)))
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
            //println("CLEARING ${part}")
            gameContext.clearRect(part.coordinate.x, part.coordinate.y, CELL_SIZE, CELL_SIZE)
        }
    }
    fun update(interval: Double): Boolean {
        val elapse = interval.toInt()
        //val totalToMove =  interval/1000 * cellPerSecond;
        val moveMultipler = (elapse - (elapse % 17))/17
        val totalToMove = movePerTick * moveMultipler
        val head = snake[0]

        if (head.isCompletelyInNextCell()) {
            if (nextDirection != null) {
                turns[head.next] = nextDirection!!
                direction = nextDirection!!
                nextDirection = null
            }
            val foodPosition = game.foodPosition
            val hasEatenFood = foodPosition != null && foodPosition.equals(head.next)
            // this looks wrong, should be added on the tail as an overlay and revealed.
            val newSnakePart = if (hasEatenFood) mutableListOf(addNewPart(head)) else mutableListOf()
            val partToMerge = snake.mapIndexed { index, part ->
                val startCell = part.next

                val directionToUse = if (turns.containsKey(part.next)) turns[part.next] else part.direction
                val nextCell = startCell.nextPosition(directionToUse!!)

                if (turns.containsKey(part.next) && index == snake.size - 1) {
                    turns.remove(part.next)
                }
                part.toNextCell(directionToUse!!)
            }.toMutableList()
            newSnakePart.addAll(partToMerge)
            snake = newSnakePart
            return hasEatenFood;
        }

        snake = snake.mapIndexed { index, part ->
            val newPart = part.move(totalToMove)
            newPart!!
        }.toMutableList()
        return false
    }

    private fun addNewPart(head: SnakePart): SnakePart {
        val nextCell = head.next.nextPosition(direction)
        val cell = game.getCell(nextCell.x, nextCell.y)
        return SnakePart(direction, nextCell, nextCell.nextPosition(direction), Coordinate(cell.xcoord, cell.ycoord))
    }

    fun changeDirection(newDirection: Direction) {
        nextDirection = when(newDirection) {
            Direction.LEFT -> if (direction == Direction.RIGHT) null else newDirection
            Direction.RIGHT -> if (direction == Direction.LEFT) null else newDirection
            Direction.DOWN-> if (direction == Direction.UP) null else newDirection
            Direction.UP -> if (direction == Direction.DOWN) null else newDirection
        }
    }
}
