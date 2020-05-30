import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document
import kotlin.browser.window

const val xCells = 40
const val yCells = 22
const val cellSize = 18.0
const val width = ((xCells * cellSize)).toInt()
const val height = ((yCells * cellSize)).toInt()

val gameOptions = GameOptions(cellSize, 6, 1000/60.toInt(),  Dimension(width, height), -1)
val userOptions =  UserOptions(DEFAULT_THEME, GameSpeed.Normal, true);

fun prepareNewGame(gameEnv: GameEnv) {


    val game: Game = Game(gameEnv, gameOptions, userOptions)
    val scoreCont = document.getElementById("scoreCont")
    val player = Human()
    var lastGameState: GameState = GameState(0, GameStatus.Initial)
    game.init()

    fun render(timeStamp: Double) {

        lastGameState = game.update(timeStamp)
        scoreCont!!.innerHTML = "${lastGameState.score}"
        game.render()
        if (lastGameState.gameStatus != GameStatus.GameOver) {
            window.requestAnimationFrame {
                render(it)
            }
        }


    }
    val newGameBtn = document.getElementById("newGameBtn")
    newGameBtn!!.addEventListener("click", {
        newGameBtn.setAttribute("disabled", "true")
        game.initEntities(player)
        window.requestAnimationFrame {
            render(it)
        }
    })
    window.addEventListener("keyup", EventListener {
        val e = it as KeyboardEvent;
        if (lastGameState.gameStatus != GameStatus.GameOver) {
            player.setInput(e.keyCode)
        }
    })
}

fun main() {
    val canvas = document.getElementById("canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    val boardCanvas = document.getElementById("board") as HTMLCanvasElement
    val boardCtx = boardCanvas.getContext("2d") as CanvasRenderingContext2D

    val gameCanvasPair = Pair(canvas, ctx)
    val boardCanvasPair = Pair(boardCanvas, boardCtx)
    val foodList: List<Image> = FoodImage.FOOD_IMAGES.map {
        val image = Image();
        image.src = it
        image
    }
    val gameEnv = GameEnv(gameCanvasPair, boardCanvasPair, foodList, xCells, yCells)

    val gameDiv = document.getElementById("game")
    gameDiv!!.setAttribute("style", "width: ${width}px; height: ${height}px;")

    prepareNewGame(gameEnv)






}