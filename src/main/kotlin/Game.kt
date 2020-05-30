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

    private val yCells = gameEnv.yCells
    private val xCells = gameEnv.xCells
    private val cellSize = gameOptions.cellSize
    private lateinit var snake: Snake
    private val gameCanvasPair = gameEnv.gameCanvasPair
    private val boardCanvasPair = gameEnv.boardCanvasPair
    private val theme = userOptions.theme

    private val gameCanvas = gameCanvasPair.first
    private val gameContext = gameCanvasPair.second

    private val boardCanvas = boardCanvasPair.first
    private val boardContext = boardCanvasPair.second

    private val width = gameOptions.dimension.width
    private val height = gameOptions.dimension.height

    private var gameState: GameState = GameState(0, GameStatus.Initial)

    private val cells: List<List<Cell>> = gameEnv.cells
    private var lastTimestamp = 0.0
    private var deltaAgg = 0.0

    private val foodCreator = FoodCreator(gameEnv, gameOptions)
    private var food: Food? = null
    private lateinit var player: Player

    fun getSnake(): Snake {
        return snake
    }
    fun init() {

        gameCanvas.width = width
        gameCanvas.height = height

        boardCanvas.width = width
        boardCanvas.height = height

        initDrawBoard()
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

    fun initEntities(humanPlayer: Player) {

        snake = Snake(gameOptions.snakeSize, gameOptions, userOptions, cells)

        //snake.init()
        gameContext.fillStyle = theme.snakeColor
        snake.render(gameContext)

        food = foodCreator.generateFood(snake.getParts())
        if (food != null) {
            gameContext.fillStyle = theme.foodColor
            renderFood(gameContext, food)
        }
        player = Robot(this)
    }

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
            return gameState
        }

        val snakeUnitsToMove = (deltaAgg / gameOptions.refreshInterval).toInt()
        deltaAgg = 0.0
        for (unit in 0 until snakeUnitsToMove) {
            val snakeStatus = snake.update(food, player)
            if (snakeStatus == SnakeStatus.HasEatenFood) {
                food = foodCreator.generateFood(snake.getParts())
                gameState = gameState.incrementScore()
            } else if (snakeStatus == SnakeStatus.HasHitWallOrBody) {
                return gameState.gameOver()
            }
            // continue
        }
        return gameState
    }
    fun render() {

        clearGameCanvas()
        gameContext.fillStyle = theme.snakeColor
        snake.render(gameContext)

        gameContext.fillStyle = theme.foodColor
        renderFood(gameContext, food)

    }

    fun getFood(): Food? {
        return food
    }

    private fun clearGameCanvas() {
        gameContext.clearRect(0.0, 0.0, gameCanvas.width * 1.0, gameCanvas.height * 1.0)
    }
//
//    fun onDirectionChange(direction: Direction) {
//        snake.changeDirection(direction)
//    }

    private fun renderFood(ctx: CanvasRenderingContext2D, food: Food?) {
        if (food != null) {
            val cell = gameEnv.getCell(food.position.x, food.position.y)
            ctx.drawImage(gameEnv.foodList[food.imageIndex], cell.xcoord, cell.ycoord)
        }
    }

}

class FoodCreator(val env: GameEnv, val gameOptions: GameOptions) {


    private var foodImg: Image? = null
    private fun hasSnakePart(snakeParts: List<SnakePart>, x: Int, y: Int): Boolean {
        for (part in snakeParts) {
            if (part.start.x == x && part.start.y == y ) {
                return true
            }
        }
        return false
    }
    fun generateFood(snakeParts: List<SnakePart>): Food? {
        val maxIter = env.xCells * env.yCells
        var position: CellPosition? = null
        for(i in 0 until maxIter) {
            val x = Random.nextInt(0, env.xCells )
            val y = Random.nextInt(0, env.yCells )
            if (hasSnakePart(snakeParts, x, y)) {
                continue
            } else {
                position = CellPosition(x, y)
                break
            }
        }
        if (position != null) {
            return Food(position, Random.nextInt(0, env.foodList.size))
        }
        return null
    }

}