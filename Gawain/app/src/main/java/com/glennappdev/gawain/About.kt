package com.glennappdev.gawain

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val backButton: CardView = findViewById(R.id.btnBack)

        backButton.setOnClickListener{
            finish()
        }
    }
}