package com.example.image2pdf.linuxIntegration

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.image2pdf.R

class linuxIntegration : AppCompatActivity() {
    companion object {
        private val TAG = "LINUXINT"
        private val porta = 8080
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_linux_integration)

        try {
            avviaServer()
        } catch(exc: Exception) {
            Toast.makeText(this, "Si è verificato un errore nella creazione del server", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Si è verificato un errore nell'avvio del server ${exc}", exc)
        }
    }
}