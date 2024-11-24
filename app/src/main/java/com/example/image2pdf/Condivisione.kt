package com.example.image2pdf

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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
            val intentIm = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentIm.type="image/*"
            intentIm.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            startActivityForResult(intentIm,1)
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }
}