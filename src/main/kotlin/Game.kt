import org.w3c.dom.CanvasRenderingContext2D
import kotlin.browser.window
import kotlin.random.Random

class RenderKeeper(val minInterval: Int) {
    private var lastTriggerTime: Double = 0.0
    private var deltaAgg: Double = 0.0

    fun onRenderFrameCalled(time: Double): Double? {
        if (lastTriggerTime == 0.0) {
            lastTriggerTime = time
            return null
        }
        val delta = time - lastTriggerTime
        lastTriggerTime = time
        deltaAgg += delta
        if (deltaAgg >= minInterval) {
            return deltaAgg
        }
        return null
    }

    fun resetAgg() {
        deltaAgg = 0.0
    }
}


class Game(private val gameEnv: GameEnv,
           private val gameOptions: GameOptions,
           private val userOptions: UserOptions) {

    private lateinit var snake: Snake
    private val gameCanvasPair = gameEnv.gameCanvasPair
    private val theme = userOptions.theme
    private val gameCanvas = gameCanvasPair.first
    private val gameContext = gameCanvasPair.second
    private var gameState: GameState = GameState(0, GameStatus.Initial)

    private val cells: List<List<Cell>> = gameEnv.cells

    private val foodCreator = FoodCreator(gameEnv, gameOptions)
    private var food: Food? = null
    private lateinit var player: Player
    private val renderKeeper = RenderKeeper(gameOptions.refreshInterval)

    fun getSnake(): Snake {
        return snake
    }

    fun init(gamePlayer: Player?) {

        snake = Snake(gameOptions.snakeSize, gameOptions, userOptions, cells)
        gameContext.fillStyle = theme.snakeColor
        snake.render(gameContext)

        food = foodCreator.generateFood(snake.getParts())
        if (food != null) {
            gameContext.fillStyle = theme.foodColor
            renderFood(gameContext, food)
        }
        player = gamePlayer ?: Robot(this)
    }

    private fun update(currentTimestamp: Double) {
        val aggTime = renderKeeper.onRenderFrameCalled(currentTimestamp)

        if (aggTime != null) {
            val unitMoves = (aggTime / gameOptions.refreshInterval).toInt()
            renderKeeper.resetAgg()

            for (unit in 0 until unitMoves) {
                val snakeStatus = snake.update(food, player)
                if (snakeStatus == SnakeStatus.HasEatenFood) {
                    food = foodCreator.generateFood(snake.getParts())
                    gameState = gameState.incrementScore()
                } else if (snakeStatus == SnakeStatus.HasHitWallOrBody) {
                    gameState = gameState.gameOver()
                }
            }
        }

    }
    fun start(gameStateConsumer: (state : GameState) -> Unit) {

        fun renderAndUpdate(time: Double) {
            update(time)
            render()
            gameStateConsumer(gameState)
            if (gameState.gameStatus != GameStatus.GameOver) {
                window.requestAnimationFrame {
                    renderAndUpdate(it)
                }
            } else {
                player.over()
            }
        }
        window.requestAnimationFrame {
            renderAndUpdate(it)
        }
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

    private fun renderFood(ctx: CanvasRenderingContext2D, food: Food?) {
        if (food != null) {
            val cell = gameEnv.getCell(food.position.x, food.position.y)
            ctx.drawImage(gameEnv.foodList[food.imageIndex], cell.xcoord, cell.ycoord)
        }
    }

}

class FoodCreator(val env: GameEnv, val gameOptions: GameOptions) {

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