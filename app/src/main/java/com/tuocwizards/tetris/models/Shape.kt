package com.tuocwizards.tetris.models

enum class Shape(val frameNumber: Int, val startPosition: Int) {
    Tetromino(2, 2) {
        override fun getFrame(frameNumber: Int): Frame {
            return when (frameNumber) {
                0 -> Frame(4).addRow("1111")
                1 -> Frame(1)
                    .addRow("1")
                    .addRow("1")
                    .addRow("1")
                    .addRow("1")
                else -> throw IllegalAccessException("$frameNumber is an invalid frame number.")
            }
        }
    };
    abstract fun getFrame(frameNumber: Int): Frame
}