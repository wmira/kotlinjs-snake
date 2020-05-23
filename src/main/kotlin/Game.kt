import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement

interface Entity {
    fun cells()
}

class Cell(val xcoord: Double,
           val ycoord: Double,
           val cx: Int,
           val cy: Int) {
    fun moveX(move: Double): Cell {
        return Cell(xcoord + move, ycoord, cx, cy)
    }

    companion object {
        const val CELL_SIZE: Double = 16.0
        const val MARGIN: Double = 0.4
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class Game(val canvas: HTMLCanvasElement, val ctx: CanvasRenderingContext2D) {

    private val ycells = 30
    private val xcells = 60
    private val margin = Cell.MARGIN
    private val cw = Cell.CELL_SIZE
    private val snake = Snake(initialLength =4)

    init {
        canvas.width = ((xcells * Cell.CELL_SIZE)).toInt()
        canvas.height = ((ycells * Cell.CELL_SIZE)).toInt()
    }
    // cells of all position
    var cells = mutableListOf<MutableList<Cell>>()

    init {
        for (y in 0 until ycells) {
            cells.add(mutableListOf())
        }
        for (y in 0 until ycells) {
            val cellRow = cells[y]
            for (x in 0 until xcells) {
                val ycoord = if (y == 0) y.toDouble() else (cw * y) + margin
                val xcoord = if (x == 0) x.toDouble() else (cw * x) + margin
                val cell = Cell(xcoord, ycoord, x, y)
                cellRow.add(cell)
                ctx.fillRect(cell.xcoord,  cell.ycoord,  cw , cw)
            }
        }
        // draw snake

    }
    private var elapseInterval = 0.0
    private var lastTimestamp = 0.0
    private var minInterval = 1000/120

    fun drawCells() {

        ctx.fillStyle = "black"
        for (y in 0 until cells.size) {
            val row = cells[y]
            for (x in 0 until row.size) {
                val cell = row[x]
                ctx.clearRect(cell.xcoord,  cell.ycoord,  cw , cw)
                ctx.fillRect(cell.xcoord,  cell.ycoord,  cw , cw)
            }
        }
    }
    private fun drawRows(row: Int) {

        ctx.fillStyle = "black"
        val row = cells[row]
        for (x in 0 until row.size) {
            val cell = row[x]
            ctx.clearRect(cell.xcoord,  cell.ycoord,  cw , cw)
            ctx.fillRect(cell.xcoord,  cell.ycoord,  cw , cw)
        }

    }

    fun init() {
        snake.init(this)
        ctx.fillStyle = "pink"
        snake.render().forEach { cell ->
            ctx.fillRect(cell.xcoord,  cell.ycoord,  cw , cw)
        }
    }
    fun getCell(x: Int, y: Int): Cell {
        return cells[y][x]
    }
    var count = 0
    fun draw(timeStamp: Double) {

        if (lastTimestamp == 0.0) {
            lastTimestamp = timeStamp
            return
        }
        val diff = timeStamp - lastTimestamp

        if (diff >= minInterval) {
            clearCanvas()
            drawCells()
            // drawRows(0)
            snake.update(diff)

            ctx.fillStyle = "pink"
            snake.render().forEach { cell ->
                ctx.fillRect(cell.xcoord, cell.ycoord, cw, cw)
            }
            lastTimestamp = timeStamp
        }
    }

    private fun clearCanvas() {
        ctx.clearRect(0.0, 0.0, canvas.width * 1.0, canvas.height * 1.0)

    }


}