import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
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

    val game = Game(gameCanvasPair, boardCanvasPair)
    game.init()
    var counter = 0
    fun render(timeStamp: Double) {
        counter ++
        game.draw(timeStamp)
        if (counter < 400) {
            window.requestAnimationFrame {
                render(it)
            }
       }
    }
    window.addEventListener("keyup", EventListener {
        val e = it as KeyboardEvent;
        println("Keycode pressed ${e.keyCode}");

        when (e.keyCode) {
            37 -> game.onDirectionChange(Direction.LEFT)
            38 -> game.onDirectionChange(Direction.UP)
            39 -> game.onDirectionChange(Direction.RIGHT)
            40 -> game.onDirectionChange(Direction.DOWN)
            else -> {
                // do nothing
            }
        }
    })
    window.requestAnimationFrame {
        render(it)
    }
    println("lol")
}