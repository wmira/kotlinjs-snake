import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Image
import kotlin.dom.clear
import kotlin.random.Random



val DEFAULT_THEME = Theme("#070709", "#121417", "#0466c8", "#0466c8" )
//val foodList: List<Image>,
//private val theme: Theme = DEFAULT_THEME

class Game(val gameEnv: GameEnv,
           val gameOptions: GameOptions,
           val userOptions: UserOptions) {

    private val yCells = gameOptions.yCells
    private val xCells = gameOptions.xCells
    private val cellSize = gameOptions.cellSize
    private lateinit var snake: Snake
    private val gameCanvasPair = gameEnv.gameCanvasPair
    private val boardCanvasPair = gameEnv.boardCanvasPair
    private val theme = userOptions.theme

    private val gameCanvas = gameCanvasPair.first
    private val gameContext = gameCanvasPair.second

    private val boardCanvas = boardCanvasPair.first
    private val boardContext = boardCanvasPair.second

    private val width = ((xCells * cellSize)).toInt()
    private val height = ((yCells * cellSize)).toInt()

    private val gameState: GameState = GameState(0, GameStatus.Initial)

//    init {
//        gameCanvas.width = width
//        gameCanvas.height = height
//
//        boardCanvas.width = width
//        boardCanvas.height = height
//    }
    // cells of all position
    private lateinit var cells: List<List<Cell>>
//    private lateinit var food: Food
//    init {

//
//    }
    private var elapseInterval = 0.0

    var foodPosition: CellPosition? = null

    fun init() {

        gameCanvas.width = width
        gameCanvas.height = height

        boardCanvas.width = width
        boardCanvas.height = height

        initCells()
        initDrawBoard()
        initEntities()
    }
    private fun initCells() {

        val cellsList = mutableListOf<MutableList<Cell>>()
        for (y in 0 until yCells) {
            cellsList.add(mutableListOf())
        }
        for (y in 0 until yCells) {
            val cellRow = cellsList[y]
            for (x in 0 until xCells) {
                val ycoord = if (y == 0) y.toDouble() else (cellSize * y)
                val xcoord = if (x == 0) x.toDouble() else (cellSize * x)
                cellRow.add(Cell(xcoord, ycoord, x, y))
            }
        }

        cells = cellsList.map {
            it.toList()
        }
    }
    private fun initDrawBoard() {
        gameCanvas.clear()
        boardCanvas.clear()
        for (y in cells.indices) {
            val row = cells[y]
            val color1 = theme.boardShade1
            val color2 = if (userOptions.showCellLines) theme.boardShade2 else color1
            val startColor = if ( y % 2 == 0 ) color1 else color2
            val endColor = if ( y % 2 != 0 ) color1 else color2
            for (x in row.indices) {
                val cell = row[x]
                val color = if (x % 2 == 0) startColor else endColor
                boardContext.fillStyle = color
                boardContext.clearRect(cell.xcoord,  cell.ycoord,  cellSize , cellSize)
                boardContext.fillRect(cell.xcoord,  cell.ycoord,  cellSize , cellSize)
            }
        }
    }

    private fun initEntities() {
        snake = Snake(gameOptions.snakeSize, gameOptions, userOptions, cells)
        //snake.init()
        gameContext.fillStyle = theme.snakeColor
        snake.render(gameContext)

//        food = Food(this)
//        foodPosition = food.generateFood(snake)
//        gameContext.fillStyle = theme.foodColor
//        food.render(gameContext)
    }

    private var lastTimestamp = 0.0
    private var deltaAgg = 0.0

    /**
     * @param delta the number of millis elapsed before we were initially called
     */
    fun update(currentTimestamp: Double): GameState {
        if (lastTimestamp == 0.0) {
            lastTimestamp = currentTimestamp
        }
        val delta = currentTimestamp - lastTimestamp
        lastTimestamp = currentTimestamp
        deltaAgg += delta
        if (deltaAgg < gameOptions.refreshInterval) {
            return GameState(0, GameStatus.Initial)
        }

        val snakeUnitsToMove = (deltaAgg / gameOptions.refreshInterval).toInt()
        deltaAgg = 0.0
        //for (unit in 0 until snakeUnitsToMove) {
            snake.update()
        //}

        return GameState(0, GameStatus.Initial)
    }
    fun render(): Boolean {
//        if (isGameOver) {
//            return isGameOver
//        }
//        if (lastTimestamp == 0.0) {
//            lastTimestamp = timeStamp
//        }
//        val diff = timeStamp - lastTimestamp
//        aggregatedInterval += diff
//
//        if (aggregatedInterval >= Cell.RENDER_INTERVAL) {
//
//            val action = snake.update(aggregatedInterval)
//            if (action == SnakeAction.EatenFood) {
//                foodPosition = food.generateFood(snake)
//            }
//            if (action == SnakeAction.Moved) {
                clearGameCanvas()
                gameContext.fillStyle = theme.snakeColor
                snake.render(gameContext)

//                gameContext.fillStyle = theme.foodColor
//                food.render(gameContext)
//                lastTimestamp = timeStamp

//            }
//            if (action == SnakeAction.HitWallOrPart) {
//                isGameOver = true
//            }
//        }
//        return isGameOver
        return false
    }
//
    private fun clearGameCanvas() {
        gameContext.clearRect(0.0, 0.0, gameCanvas.width * 1.0, gameCanvas.height * 1.0)
    }
//
//    fun onDirectionChange(direction: Direction) {
//        snake.changeDirection(direction)
//    }
}
//
//
//class Food(val game: Game) {
//
//    private lateinit var food: CellPosition
//    private var foodImg: Image? = null
//    fun generateFood(snake: Snake): CellPosition {
//        while (true) {
//            val x = Random.nextInt(0, Cell.XCELLS )
//            val y = Random.nextInt(0, Cell.YCELLS )
//            if (snake.isPartInPosition(x, y) ) {
//                continue
//            } else {
//                food = CellPosition(x, y)
//                break
//            }
//        }
//        foodImg = game.foodList[Random.nextInt(0, game.foodList.size)]
//        return food
//    }
//
//    fun render(ctx: CanvasRenderingContext2D) {
//        val cell = game.getCell(food.x, food.y)
//
//        if (foodImg != null) {
//            ctx.drawImage(foodImg!!, cell.xcoord, cell.ycoord)
//        }
//
//    }
//
//}