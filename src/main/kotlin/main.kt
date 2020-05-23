import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

fun main() {
    val canvas = document.getElementById("canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    val game = Game(canvas, ctx)
    game.init()

    fun render(timeStamp: Double) {
        game.draw(timeStamp)
        window.requestAnimationFrame {
            render(it)
        }

    }
    window.requestAnimationFrame {
        render(it)
    }
    println("lol")
}