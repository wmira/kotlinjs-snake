data class Theme(val boardShade1: String,
                 val boardShade2: String,
                 val snakeColor: String,
                 val foodColor: String)

data class Dimension(val width: Int, val height: Int)

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

sealed class GameStatus {
    object GameOver: GameStatus()
    object Replay: GameStatus()
    object Running: GameStatus()
    object Initial: GameStatus()
}

sealed class SnakeStatus {
    object HasHitWallOrBody: SnakeStatus()
    object HasEatenFood: SnakeStatus()
    object HasMoved: SnakeStatus()
    object Initial: SnakeStatus()
}

class GameState(val score: Int,
                     val gameStatus: GameStatus){
    fun incrementScore(): GameState {
        return GameState(score + 1, gameStatus)
    }
    fun gameOver(): GameState {
        return GameState(score, GameStatus.GameOver)
    }
}

data class Cell(val xcoord: Double,
                val ycoord: Double,
                val cx: Int,
                val cy: Int
)
data class Turn(val cellPosition: CellPosition,
                val direction: Direction)

data class SnakeSnapshot(val snakeParts: List<SnakePart>,
                         val turns: List<Turn>,
                         val gameState: GameState,
                         val foodPositions: List<CellPosition>)

data class Food(val position: CellPosition, val imageIndex: Int)
