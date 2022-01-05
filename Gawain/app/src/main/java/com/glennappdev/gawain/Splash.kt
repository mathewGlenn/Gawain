package com.glennappdev.gawain

import android.R
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class Splash: AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var fireStore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        // if there is already an account, continue to MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        //create account if there is no account
        else {
            auth.signInAnonymously().addOnSuccessListener {
                Toast.makeText(this, "Welcome to Gawain app", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }.addOnFailureListener { e: Exception ->
                Toast.makeText(this,
                    "Error: " + e.message,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}