package com.tuocwizards.tetris.models

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

    private fun setCellStatus(row: Int, column: Int, status: Byte?) {
        if (status != null)
            field[row][column] = status
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

    private fun boostScore() {
        score += 10
        if (score > preferences?.getHighScore() as Int)
            preferences?.saveHighScore(score)
    }

    enum class Statues {
        AWAITING_START, ACTIVE, INACTIVE, OVER
    }

    enum class Motions {
        LEFT, RIGHT, DOWN, ROTATE
    }
}