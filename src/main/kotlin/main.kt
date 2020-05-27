import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import kotlin.browser.document
import kotlin.browser.window



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
    val gameEnv = GameEnv(gameCanvasPair, boardCanvasPair, foodList)
    val gameOptions = GameOptions(18.0, 6, 1000/60.toInt(), 40, 30)
    val userOptions =  UserOptions(DEFAULT_THEME, GameSpeed.Brutal, true);
    val game = Game(gameEnv, gameOptions, userOptions)

    game.init()


    fun render(timeStamp: Double) {
        game.update(timeStamp)
        game.render()

        window.requestAnimationFrame {
            render(it)
        }


    }
//    window.addEventListener("keyup", EventListener {
//        val e = it as KeyboardEvent;
//        when (e.keyCode) {
//            37 -> game.onDirectionChange(Direction.LEFT)
//            38 -> game.onDirectionChange(Direction.UP)
//            39 -> game.onDirectionChange(Direction.RIGHT)
//            40 -> game.onDirectionChange(Direction.DOWN)
//            else -> {
//                // do nothing
//            }
//        }
//    })
    window.requestAnimationFrame {
        render(it)
    }
}