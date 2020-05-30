import org.w3c.dom.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.dom.clear

const val xCells = 40
const val yCells = 22
const val cellSize = 18.0
const val width = ((xCells * cellSize)).toInt()
const val height = ((yCells * cellSize)).toInt()
var speed: GameSpeed = GameSpeed.Normal
val DEFAULT_THEME = Theme("#070709", "#121417", "#0466c8", "#0466c8" )
val gameOptions = GameOptions(cellSize, 6, 1000/60.toInt(),  Dimension(width, height), -1)
val userOptions =  UserOptions(DEFAULT_THEME, speed, true);

fun setText(elementId: String, text: String) {
    val el = document.getElementById(elementId)
    if (el != null) {
        el.innerHTML = text
    }
}

fun setAttribute(elementId: String, attr: String, value: String) {
    val el = document.getElementById(elementId)
    el?.setAttribute(attr, value)
}
fun deleteAttribute(elementId: String, attr: String) {
    val el = document.getElementById(elementId)
    el?.removeAttribute(attr)
}

fun updateGameHud(gameState: GameState) {

    setText("scoreCont", gameState.score.toString())
    if (gameState.gameStatus == GameStatus.GameOver) {
        setText("gameStatus", "Game Over")
        deleteAttribute("newGameBtn", "disabled")
    }
}

fun initDrawBoard(gameEnv: GameEnv, userOpts: UserOptions) {
    val gameCanvas = gameEnv.gameCanvasPair.first
    val boardCanvas = gameEnv.boardCanvasPair.first
    val boardContext = gameEnv.boardCanvasPair.second

    val cells = gameEnv.cells
    val theme = userOpts.theme
    gameCanvas.clear()
    boardCanvas.clear()

    gameCanvas.width = width
    gameCanvas.height = height

    boardCanvas.width = width
    boardCanvas.height = height


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

fun startNewGame(gameEnv: GameEnv, game: Game, player: Player) {

    setText("gameStatus", "Score")
    setText("scoreCont", "0")
    //setAttribute("newGameBtn", "disabled", "disabled")
    val btn = document.getElementById("newGameBtn") as HTMLButtonElement

    game.init(player)
    game.start() {
        updateGameHud(it)
    }

}

fun getElement(id: String): Element? {
    return document.getElementById(id)
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
    gameDiv?.setAttribute("style", "width: ${width}px; height: ${height}px;")

    initDrawBoard(gameEnv, userOptions)

    // listen to some options
    val speedEl = getElement("speed") as HTMLSelectElement
    speedEl?.addEventListener("change", {
        val value = speedEl.value
        if (value == "Fast") {
            speed = GameSpeed.Fast
        }
        if (value == "Normal") {
            speed = GameSpeed.Normal
        }
        if (value == "Brutal") {
            speed = GameSpeed.Brutal
        }
    })

    document.getElementById("newGameBtn")?.addEventListener("click", {
        startNewGame(gameEnv, Game(gameEnv, gameOptions, userOptions), Human())

    })
}


//    fun render(timeStamp: Double) {
//
//        lastGameState = game.update(timeStamp)
//        scoreCont!!.innerHTML = "${lastGameState.score}"
//        game.render()
//        if (lastGameState.gameStatus != GameStatus.GameOver) {
//            window.requestAnimationFrame {
//                render(it)
//            }
//        }
//
//
//    }
//    val newGameBtn = document.getElementById("newGameBtn")
//    newGameBtn!!.addEventListener("click", {
//        newGameBtn.setAttribute("disabled", "true")
//        game.initEntities(player)
//        window.requestAnimationFrame {
//            render(it)
//        }
//    })