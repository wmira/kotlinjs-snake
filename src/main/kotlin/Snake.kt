import Cell.Companion.CELL_SIZE
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.math.max
import kotlin.math.min

class CellPosition(val x: Int, val y: Int) {

    fun nextPosition(direction: Direction): CellPosition {
        return when(direction) {
            Direction.RIGHT -> CellPosition(x + 1, y)
            Direction.DOWN -> CellPosition(x, y + 1)
            Direction.LEFT -> CellPosition(x - 1, y)
            Direction.UP -> CellPosition(x, y - 1)
        }
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
            Direction.RIGHT -> coordinate.x == (next.x.toInt() * CELL_SIZE)
            Direction.LEFT -> coordinate.x == (next.x.toInt() * CELL_SIZE)
            Direction.DOWN -> coordinate.y == (next.y.toInt() * CELL_SIZE)
            Direction.UP -> coordinate.y == (next.y.toInt() * CELL_SIZE)
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
    private val cellPerSecond = (CELL_SIZE * 20)
    private val turns = mutableMapOf<CellPosition, Direction>()

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

    fun update(interval: Double): Unit {
        val totalToMove =  interval/1000 * cellPerSecond;
        val head = snake[0]

        if (head.isCompletelyInNextCell()) {
            if (nextDirection != null) {
                turns[head.next] = nextDirection!!
                direction = nextDirection!!
                nextDirection = null
            }
            snake = snake.mapIndexed { index, part ->
                val startCell = part.next

                val directionToUse = if (turns.containsKey(part.next)) turns[part.next] else part.direction
                val nextCell = startCell.nextPosition(directionToUse!!)

                if (turns.containsKey(part.next) && index == snake.size - 1) {
                    turns.remove(part.next)
                }
                part.toNextCell(directionToUse!!)
            }.toMutableList()
            return
        }

        snake = snake.mapIndexed { index, part ->
            val newPart = part.move(totalToMove)
            newPart!!
        }.toMutableList()

    }

    fun changeDirection(direction: Direction) {
        nextDirection = direction
    }
}
