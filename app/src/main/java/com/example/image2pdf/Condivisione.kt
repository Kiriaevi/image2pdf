package com.example.image2pdf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
            val pickMultipleMedia =
                registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(25)) { uris ->
                    // Callback is invoked after the user selects media items or closes the
                    // photo picker.
                    if (uris.isNotEmpty()) {
                        Toast.makeText(this,"FotoSelezionate",Toast.LENGTH_SHORT)
                    } else {
                        Toast.makeText(this,"Foto non selezionate",Toast.LENGTH_SHORT)
                    }
                }
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }
}