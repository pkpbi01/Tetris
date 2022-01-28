package com.tuocwizards.tetris

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.tuocwizards.tetris.storage.AppPreferences

class GameActivity : AppCompatActivity() {

    var appPreferences: AppPreferences? = null
    var tvHighScore: TextView? = null
    var tvCurrentScore: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        appPreferences = AppPreferences(this)

        val btnRestert = findViewById<Button>(R.id.btn_restart)
        tvCurrentScore = findViewById(R.id.tv_current_score)
        tvHighScore = findViewById(R.id.tv_high_score)
        updateHighScore()
        updateCurrentScore()
    }

    private fun updateHighScore() {
        tvHighScore?.text = "${appPreferences?.getHighScore()}"
    }

    private fun updateCurrentScore() {
        tvCurrentScore?.text = "0"
    }
}