import Cell.Companion.CELL_SIZE

data class SnakePart(val position: Position, val cell: Cell)

class Snake(private val initialLength: Int = 0,
            private val initialDirection: Direction = Direction.RIGHT) {
    private var snake = mutableListOf<SnakePart>()
    private var prevSnake = mutableListOf<SnakePart>()
    private lateinit var game: Game

    fun init(gameInstance: Game) {
        val y = 0
        game = gameInstance
        for (x in 0 until initialLength) {
            val cell = game.getCell(x, y)
            snake.add(SnakePart(Position(x, y), cell))
        }
    }

    fun render(): List<Cell> {
        return snake.map {
            it.cell
        }
    }

    fun previousSnake(): List<SnakePart> {
        return prevSnake
    }

    fun head(): SnakePart {
        return snake[snake.size - 1]
    }

    fun tip(): Position {
        return snake[snake.size - 1].position
    }
    private val widthPerSec = (CELL_SIZE * 1.5)/1

    var count = 0
    fun update(interval: Double): Unit {
        val move = widthPerSec / interval;
        val head = head()
        count++

        if ((head.cell.moveX(move).xcoord + CELL_SIZE) > ((CELL_SIZE * 60))) {
            return
        }
        prevSnake = snake
        snake = snake.map {
            SnakePart(it.position, it.cell.moveX(move));
        }.toMutableList()


    }
}