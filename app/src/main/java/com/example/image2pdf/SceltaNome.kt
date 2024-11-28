package com.example.image2pdf

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SceltaNome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scelta_nome)
        val accetta = findViewById<Button>(R.id.butFragment1)
        val rifiuta = findViewById<Button>(R.id.butFragment2)
        val resultintent = Intent(this,NuovoPDF::class.java)
        accetta.setOnClickListener {
            val testo = findViewById<TextView>(R.id.fragText1)
            if(testo.text.toString()=="")
                Toast.makeText(this,"Inserisci un nome valido",Toast.LENGTH_SHORT)
            else{
                resultintent.putExtra("RITORNO",testo.text.toString())
                setResult(RESULT_OK, resultintent)
                finish()
            }
        }
        rifiuta.setOnClickListener {
            resultintent.putExtra("RITORNO","")
            setResult(RESULT_OK, resultintent)
            finish()
        }
    }
}