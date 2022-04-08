package com.tuocwizards.tetris.models

import android.graphics.Point
import com.tuocwizards.tetris.constants.CellConstants
import com.tuocwizards.tetris.constants.FieldConstants
import com.tuocwizards.tetris.helpers.array2dOfByte
import com.tuocwizards.tetris.storage.AppPreferences

class AppModel {

    var score: Int = 0
    private var preferences: AppPreferences? = null

    var currentBlock: Block? = null
    var currentState: String = Statues.AWAITING_START.name

    private var field: Array<ByteArray> = array2dOfByte(
        FieldConstants.ROW_COUNT.value,
        FieldConstants.COLUMN_COUNT.value
    )

    fun setPreferences(preferences: AppPreferences?) {
        this.preferences = preferences
    }

    fun getCellStatus(row: Int, column: Int): Byte? {
        return field[row][column]
    }

    fun isGameAwaitingStart(): Boolean {
        return currentState == Statues.AWAITING_START.name
    }

    fun isGameActive(): Boolean {
        return currentState == Statues.ACTIVE.name
    }

    fun isGameInactive(): Boolean {
        return currentState == Statues.INACTIVE.name
    }

    fun isGameOver(): Boolean {
        return currentState == Statues.OVER.name
    }

    fun startGame() {
        if (!isGameActive()) {
            currentState = Statues.ACTIVE.name
            generateNextBlock()
        }
    }

    fun restartGame() {
        resetModel()
        startGame()
    }

    fun endGame() {
        score = 0
        currentState = Statues.OVER.name
    }

    private fun resetModel() {
        resetField(false)
        currentState = Statues.AWAITING_START.name
        score = 0
    }

    fun  generateField(action: String) {
        if (isGameActive()) {
            resetField()
            var frameNumber: Int? = currentBlock?.frameNumber
            val coordinate: Point? = Point()
            coordinate?.x = currentBlock?.position?.x
            coordinate?.y = currentBlock?.position?.y

            when (action) {
                Motions.LEFT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.minus(1)
                }
                Motions.RIGHT.name -> {
                    coordinate?.x = currentBlock?.position?.x?.plus(1)
                }
                Motions.DOWN.name -> {
                    coordinate?.y = currentBlock?.position?.y?.minus(1)
                }
                Motions.ROTATE.name -> {
                    frameNumber = frameNumber?.plus(1)
                    if (frameNumber != null && frameNumber >= currentBlock?.frameCount as Int) {
                        frameNumber = 0
                    }
                }
            }
            if (!moveValid(coordinate as Point, frameNumber)) {
                translateBlock(currentBlock?.position as Point, currentBlock?.frameNumber as Int)
                if (Motions.DOWN.name == action) {
                    boostScore()
                    persistCellData()
                    assessField()
                    generateNextBlock()
                    if (!blockAdditionPossible()) {
                        currentState = Statues.OVER.name
                        currentBlock = null
                        resetField(false)
                    }
                }
            } else {
                if (frameNumber != null) {
                    translateBlock(coordinate, frameNumber)
                    currentBlock?.setState(frameNumber, coordinate)
                }
            }
        }
    }

    private fun resetField(ephemeralCellsOnly: Boolean = true) {
        for (i in 0 until FieldConstants.ROW_COUNT.value) {
            (0 until FieldConstants.COLUMN_COUNT.value)
                .filter { !ephemeralCellsOnly || field[i][it] == CellConstants.EPHEMERAL.value }
                .forEach { field[i][it] = CellConstants.EMPTY.value}
        }
    }

    private fun persistCellData() {
        for (i in 0 until field.size) {
            for (j in 0 until field[i].size) {
                var status = getCellStatus(i, j)
                if (status == CellConstants.EPHEMERAL.value) {
                    status = currentBlock?.staticValue
                    setCellStatus(i, j, status)
                }
            }
        }
    }

    private fun assessField() {
        for (i in 0 until field.size) {
            var emptyCells = 0;
            for (j in 0 until field[i].size) {
                val status = getCellStatus(i, j)
                val isEmpty = CellConstants.EMPTY.value == status
                if (isEmpty)
                    emptyCells++
                if (emptyCells == 0)
                    shiftRows(i)
            }
        }
    }

    private fun translateBlock(position: Point, frameNuber: Int) {
        synchronized(field) {
            val shape: Array<ByteArray>? = currentBlock?.getShape(frameNuber)
            if (shape != null) {
                for (i in shape.indices) {
                    for (j in 0 until shape[i].size) {
                        val y = position.y + i
                        val x = position.x + j
                        if (shape [i][j] != CellConstants.EMPTY.value)
                            field[y][x] = shape[i][j]
                    }
                }
            }
        }

    }

    private fun blockAdditionPossible(): Boolean {
        return moveValid(currentBlock?.position as Point, currentBlock?.frameNumber)
    }

    private fun shiftRows(nToRow: Int)  {
        if (nToRow > 0) {
            for (i in nToRow - 1 downTo 0) {
                for (j in 0 until field[i].size) {
                    setCellStatus(i + 1, j, getCellStatus(i, j))
                }
            }
        }
        for (i in 0 until field[0].size) {
            setCellStatus(0, i, CellConstants.EMPTY.value)
        }
    }

    private fun setCellStatus(row: Int, column: Int, status: Byte?) {
        if (status != null)
            field[row][column] = status
    }

    private fun boostScore() {
        score += 10
        if (score > preferences?.getHighScore() as Int)
            preferences?.saveHighScore(score)
    }

    private fun generateNextBlock() {
        currentBlock = Block.createBlock()
    }

    private fun moveValid(position: Point, frameNuber: Int?): Boolean {
        val shape: Array<ByteArray>? = currentBlock?.getShape(frameNuber as Int)
        return validTranslation(position, shape as Array<ByteArray>)
    }

    private fun validTranslation(position: Point, shape: Array<ByteArray>): Boolean {
        return if ((position.y < 0 || position.x < 0) ||
            position.y + shape.size > FieldConstants.ROW_COUNT.value ||
            position.x + shape[0].size > FieldConstants.COLUMN_COUNT.value) {
            false
        } else {
            for (i in shape.indices) {
                for (j in 0 until shape[i].size) {
                    val y = position.y + i
                    val x = position.x + j
                    if (CellConstants.EMPTY.value != shape[i][j] &&
                            CellConstants.EMPTY.value != field[y][x]) {
                        return false
                    }
                }
            }
            true
        }
    }

    enum class Statues {
        AWAITING_START, ACTIVE, INACTIVE, OVER
    }

    enum class Motions {
        LEFT, RIGHT, DOWN, ROTATE
    }
}