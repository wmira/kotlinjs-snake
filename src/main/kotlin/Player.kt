import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.window
import kotlin.random.Random

interface Player {
    fun getNextDirection(): Direction?
    fun over(): Unit
}

class Human: Player {

    private var nextDirection: Direction? = null
    private var listener = EventListener {
        val e = it as KeyboardEvent
        setInput(e.keyCode)
    }

    init {
        window.addEventListener("keyup", listener)
    }

    fun setInput(keyInput: Int): Unit {
        nextDirection = when(keyInput) {
            37 -> Direction.LEFT
            38 -> Direction.UP
            39 -> Direction.RIGHT
            40 -> Direction.DOWN
            else -> null
        }
    }

    override fun getNextDirection(): Direction? {
        return nextDirection
    }

    override fun over() {
         window.removeEventListener("keyup", listener)
    }

}

class Robot(val game: Game): Player {

    /** Noob AI */
    override fun getNextDirection(): Direction? {
        val food = game.getFood()
        val snake = game.getSnake()
        val parts = snake.getParts()
        val headPart = parts[0]
        val headPos = headPart.next
        val headX = headPos.x
        val headY = headPos.y
        val currentDir = snake.getParts()[0].direction
        if (food != null) {
            val foodPos = food.position
            val foodX = foodPos.x
            val foodY = foodPos.y
            val rightDir = if (foodX > headX && headX + 1 < xCells ) Direction.RIGHT else null
            val downDir = if (foodY > headY && headY + 1 < yCells) Direction.DOWN else null
            val leftDir = if (foodX < headX && headX - 1 >= 0) Direction.LEFT else null
            val upDir = if (foodY < headY && headY - 1 >= 0 ) Direction.UP else null
            val available: List<Direction> = listOfNotNull(rightDir, downDir, leftDir, upDir)
                    .filter{
                        !snake.hitsWallOrPart(headPart.toNextCell(it).move(1.0)) && isNextValid(currentDir, it)
                    }

            if (available.isNotEmpty()) {
                val nextDir = available[Random.nextInt(0, available.size)]
                return nextDir
            }
        }
        // make sure we dont hit wall

        val otherDirs = Direction.values().asList().filter { it -> !snake.hitsWallOrPart(headPart.toNextCell(it).move(1.0)) && isNextValid(currentDir, it) }
        if (otherDirs.isNotEmpty()) {
            return otherDirs[Random.nextInt(0, otherDirs.size)]
        }
        return currentDir

    }

    private fun isNextValid(current: Direction, next: Direction): Boolean {
        if ( (current == Direction.LEFT && next == Direction.RIGHT) ||
                (current == Direction.RIGHT && next == Direction.LEFT) ||
                (current == Direction.DOWN && next == Direction.UP) ||
                (current == Direction.UP && next == Direction.DOWN) ) {
            return false
        }
        return true
    }
    override  fun over() {
        // noop
    }
}