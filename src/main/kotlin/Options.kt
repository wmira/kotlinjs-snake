import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image

data class GameEnv(val gameCanvasPair: Pair<HTMLCanvasElement, CanvasRenderingContext2D>,
                   val boardCanvasPair: Pair<HTMLCanvasElement, CanvasRenderingContext2D>,
                   val foodList: List<Image>,
                   val xCells: Int,
                   val yCells: Int) {

    val cells = {
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

        cellsList.map {
            it.toList()
        }
    }.invoke()

    fun getCell(x: Int, y: Int): Cell {
        return cells[y][x]
    }
}


data class GameOptions(val cellSize: Double,
                       val snakeSize: Int,
                       val refreshInterval: Int,
                       val dimension: Dimension,
                       val maxMove: Int = -1)

sealed class GameSpeed() {
    object Normal: GameSpeed()
    object Fast: GameSpeed()
    object Slow: GameSpeed()
    object Brutal: GameSpeed()
}

data class UserOptions(val theme: Theme,
                       val gameSpeed: GameSpeed,
                       val showCellLines: Boolean)