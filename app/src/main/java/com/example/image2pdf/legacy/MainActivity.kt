package com.example.image2pdf.legacy

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.image2pdf.Condivisione
import com.example.image2pdf.NuovoPDF
import com.example.image2pdf.R
import com.example.image2pdf.linuxIntegration.linuxIntegration

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // aggiunto un commento a caso per testare GIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#575656")))
        attendiEvento()
    }
    fun attendiEvento(){
        val bottone1 = findViewById<Button>(R.id.bottone1)
        val bottone2 = findViewById<Button>(R.id.bottone2)
        val bottone3 = findViewById<Button>(R.id.bottone3)
        bottone1.setOnClickListener {
            cambiaActivity(Condivisione::class.java)
        }
        bottone2.setOnClickListener {
            cambiaActivity(NuovoPDF::class.java)
        }
        bottone3.setOnClickListener {
            cambiaActivity(linuxIntegration::class.java)
        }
    }
    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }
}
