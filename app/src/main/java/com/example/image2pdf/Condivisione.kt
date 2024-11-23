package com.example.image2pdf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Condivisione : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_condivisione)
        attendiEvento()
    }

    fun attendiEvento(){
        val bottone1 = findViewById<Button>(R.id.bc_1)
        val bottone2 = findViewById<Button>(R.id.bc_2)
        bottone1.setOnClickListener {
            cambiaActivity(Fotocamera::class.java)
        }
        bottone2.setOnClickListener{
            //Intent implicito per accedere ai file
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }
}