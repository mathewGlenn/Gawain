package com.glennappdev.gawain

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val back: CardView = findViewById(R.id.btnBack)
        back.setOnClickListener{
            finish()
        }

    }

    fun manageSubjects(view: android.view.View) {
        startActivity(Intent(this, ManageSubjects::class.java))
    }

    fun about(view: android.view.View) {
        //startActivity(Intent(this, About::class.java))
    }
}