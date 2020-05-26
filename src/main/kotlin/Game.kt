import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import kotlin.math.ceil
import kotlin.random.Random

class Cell(val xcoord: Double,
           val ycoord: Double,
           val cx: Int,
           val cy: Int
) {

    companion object {
        const val CELL_SIZE: Double = 18.0
        const val YCELLS: Int = 30
        const val XCELLS: Int = 60
        val RENDER_INTERVAL: Double = ceil((1000 / 60).toDouble())
    }
}
data class Theme(val boardShade1: String,
                 val boardShade2: String,
                 val snakeColor: String,
                 val foodColor: String
)

data class Dimension(val width: Int, val height: Int)

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

val DEFAULT_THEME = Theme("#070709", "#121417", "#0466c8", "#0466c8" )

class Game(private val gameCanvasPair: Pair<HTMLCanvasElement, CanvasRenderingContext2D>,
           private val boardCanvasPair: Pair<HTMLCanvasElement, CanvasRenderingContext2D>,
           val foodList: List<Image>,
           private val theme: Theme = DEFAULT_THEME) {

    private val ycells = Cell.YCELLS
    private val xcells = Cell.XCELLS
    private val cw = Cell.CELL_SIZE
    private val snake = Snake(initialLength = 6)

    private val gameCanvas = gameCanvasPair.first
    private val gameContext = gameCanvasPair.second

    private val boardCanvas = boardCanvasPair.first
    private val boardContext = boardCanvasPair.second

    val width = ((xcells * Cell.CELL_SIZE)).toInt()
    val height = ((ycells * Cell.CELL_SIZE)).toInt()

    var score: Int = 0
    var isGameOver: Boolean = false

    init {
        gameCanvas.width = width
        gameCanvas.height = height

        boardCanvas.width = width
        boardCanvas.height = height
    }
    // cells of all position
    private lateinit var cells: List<MutableList<Cell>>
    private lateinit var food: Food
    init {
        // initialize cells for reference on the static
        // cell in the game
        val cellsList = mutableListOf<MutableList<Cell>>()
        for (y in 0 until ycells) {
            cellsList.add(mutableListOf())
        }
        for (y in 0 until ycells) {
            val cellRow = cellsList[y]
            for (x in 0 until xcells) {
                val ycoord = if (y == 0) y.toDouble() else (cw * y)
                val xcoord = if (x == 0) x.toDouble() else (cw * x)
                cellRow.add(Cell(xcoord, ycoord, x, y))
            }
        }
        cells = cellsList.toList()

    }
    private var elapseInterval = 0.0
    private var lastTimestamp = 0.0

    fun drawBoard() {

        for (y in cells.indices) {
            val row = cells[y]
            val startColor = if ( y % 2 == 0 ) theme.boardShade1 else theme.boardShade2
            val endColor = if ( y % 2 != 0 ) theme.boardShade1 else theme.boardShade2
            for (x in row.indices) {
                val cell = row[x]
                val color = if (x % 2 == 0) startColor else endColor
                boardContext.fillStyle = color
                boardContext.clearRect(cell.xcoord,  cell.ycoord,  cw , cw)
                boardContext.fillRect(cell.xcoord,  cell.ycoord,  cw , cw)
            }
        }
    }
    var foodPosition: CellPosition? = null
    fun init() {
        drawBoard()
        snake.init(this)
        gameContext.fillStyle = theme.snakeColor
        snake.render(gameContext)

        food = Food(this)
        foodPosition = food.generateFood(snake)
        gameContext.fillStyle = theme.foodColor
        food.render(gameContext)
    }
    fun getCell(x: Int, y: Int): Cell {
        return cells[y][x]
    }
    var aggregatedInterval = 0.0
    fun draw(timeStamp: Double) {

        if (lastTimestamp == 0.0) {
            lastTimestamp = timeStamp
            return
        }
        val diff = timeStamp - lastTimestamp
        aggregatedInterval += diff

        if (aggregatedInterval >= Cell.RENDER_INTERVAL) {
            clearGameCanvas()
            snake.clear(gameContext)
            val hasEatenFood = snake.update(aggregatedInterval)
            if (hasEatenFood) {
                foodPosition = food.generateFood(snake)
            }
            gameContext.fillStyle = theme.snakeColor
            snake.render(gameContext)

            gameContext.fillStyle = theme.foodColor
            food.render(gameContext)
            lastTimestamp = timeStamp
            aggregatedInterval = 0.0
        }
    }

    private fun clearGameCanvas() {
        gameContext.clearRect(0.0, 0.0, gameCanvas.width * 1.0, gameCanvas.height * 1.0)
    }

    fun onDirectionChange(direction: Direction) {
        snake.changeDirection(direction)
    }
}


class Food(val game: Game) {

    private lateinit var food: CellPosition
    private var foodImg: Image? = null
    fun generateFood(snake: Snake): CellPosition {
        while (true) {
            val x = Random.nextInt(0, Cell.XCELLS )
            val y = Random.nextInt(0, Cell.YCELLS )
            if (snake.isPartInPosition(x, y) ) {
                continue
            } else {
                food = CellPosition(x, y)
                break
            }
        }
        foodImg = game.foodList[Random.nextInt(0, game.foodList.size)]
        return food
    }

    fun render(ctx: CanvasRenderingContext2D) {
        val cell = game.getCell(food.x, food.y)

        if (foodImg != null) {
            ctx.drawImage(foodImg!!, cell.xcoord, cell.ycoord)
        }

    }

}