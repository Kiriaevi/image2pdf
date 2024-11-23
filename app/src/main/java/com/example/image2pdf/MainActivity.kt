package com.example.image2pdf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // aggiunto un commento a caso per testare GIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        attendiEvento()
    }

    fun attendiEvento(){
        val bottone1 = findViewById<Button>(R.id.bottone1)
        val bottone2 = findViewById<Button>(R.id.bottone2)
        bottone1.setOnClickListener {
            cambiaActivity(NuovoPdf::class.java)
        }
        bottone2.setOnClickListener {
            cambiaActivity(Condivisione::class.java)
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }

}
