import Cell.Companion.CELL_SIZE
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

    fun move(totalToMove: Double, direction: Direction, max: Double): Coordinate {
        return when(direction) {
            Direction.RIGHT -> moveX(totalToMove, max)
            Direction.DOWN -> moveY(totalToMove, max)
            Direction.LEFT -> moveX(totalToMove * -1, max)
            Direction.UP -> moveY(totalToMove * -1, max)
        }
    }
}

class SnakePart(val direction: Direction,
                val start: CellPosition,
                val next: CellPosition,
                val coordinate: Coordinate,
                val prevDirection: Direction) {

    fun isCompletelyInNextCell(): Boolean {
        return when(direction) {
            Direction.RIGHT -> coordinate.x == (next.x.toInt() * Cell.CELL_SIZE)
            Direction.LEFT -> coordinate.x == (next.x.toInt() * Cell.CELL_SIZE) - CELL_SIZE
            Direction.DOWN -> coordinate.y == (next.y.toInt() * CELL_SIZE)
            Direction.UP -> coordinate.y == ((next.y * CELL_SIZE) - CELL_SIZE)
        }

    }
    fun toNextCell(newDirection: Direction): SnakePart {
        return SnakePart(newDirection, next, next.nextPosition(newDirection), coordinate, direction)
    }

    override fun toString(): String {
        return "SnakePart(direction=$direction, start=$start, next=$next, coordinate=$coordinate, prevDirection=$prevDirection)"
    }

}

class Snake(private val initialLength: Int = 0,
            private val initialDirection: Direction = Direction.RIGHT) {
    private var snake = mutableListOf<SnakePart>()
    private lateinit var game: Game
    private var direction = initialDirection
    private var nextDirection: Direction? = null
    private val cellPerSecond = (CELL_SIZE * 1)/4
    private val turns = mutableMapOf<CellPosition, Direction>()

    fun init(gameInstance: Game) {
        val y = 0
        game = gameInstance
        for (x in 0 until initialLength) {
            val cellPosition = CellPosition(initialLength - 1 - x, y)
            val cell = game.getCell(cellPosition.x ,y)
            snake.add(SnakePart(initialDirection, cellPosition , cellPosition.nextPosition(direction), Coordinate(cell.xcoord, cell.ycoord), initialDirection))
        }
    }

    fun render(): List<Cell> {
        return snake.map {
            // FIXME should not be a cell
            Cell(it.coordinate.x, it.coordinate.y, Cell.CELL_SIZE.toInt(), Cell.CELL_SIZE.toInt())
        }
    }

    fun head(): SnakePart {
        return snake[snake.size - 1]
    }

    fun update(interval: Double): Unit {
        val totalToMove = cellPerSecond / interval;
        val head = snake[0]

        if (head.isCompletelyInNextCell()) {
            if (nextDirection != null) {
                turns[head.next] = nextDirection!!
                direction = nextDirection!!
                nextDirection = null
            }
            snake = snake.mapIndexed { index, part ->
                val startCell = part.next
                if (index == 1) {
                    println("Turn contains ${part.next} ${turns.containsKey(part.next)} ${turns[part.next]}")
                }
                val directionToUse = if (turns.containsKey(part.next)) turns[part.next] else part.direction
                val nextCell = startCell.nextPosition(direction)
                if (turns.containsKey(part.next) && index == snake.size - 1) {
                    turns.remove(part.next)
                }
                if (index == 1) {
                    println("USING DIRECTION For index 1  ${directionToUse}")
                }
                part.toNextCell(directionToUse!!)
            }.toMutableList()
            return
        }

        snake = snake.mapIndexed { index, part ->

            val maxMove = when(part.direction) {
               Direction.RIGHT -> part.next.x * Cell.CELL_SIZE
               Direction.LEFT -> (part.next.x ) * Cell.CELL_SIZE
               Direction.DOWN -> part.next.y * Cell.CELL_SIZE
               Direction.UP -> part.next.y * Cell.CELL_SIZE
            }
            if (index == 0) {
                println("part direction ${part.direction} ${maxMove} ${part.next.x}")
            }
            SnakePart(part.direction, part.start, part.next, part.coordinate.move(totalToMove, part.direction, maxMove), part.prevDirection);
        }.toMutableList()

    }

    fun changeDirection(direction: Direction) {
        nextDirection = direction
    }
}
