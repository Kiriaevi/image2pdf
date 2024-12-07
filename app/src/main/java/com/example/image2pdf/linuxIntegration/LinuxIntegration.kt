package com.example.image2pdf.linuxIntegration

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.image2pdf.R
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class linuxIntegration : AppCompatActivity() {
    companion object {
        private val TAG = "NETWORKING"
        private val porta = 8080
    }
    private var serverExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var server: ServerNanoHttpd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_linux_integration)

        val bottoneStampa = findViewById<TextView>(R.id.comando)
        bottoneStampa.setText("curl -X POST -F file1=@/path/file/1 -F file2=@/path/file/2 --output nomeFileOutput.pdf http://${ServerNanoHttpd.ipLocale()}:8080")
        try {
            Log.e(TAG, "IP LOCALE: ${ServerNanoHttpd.ipLocale()}")
            serverExecutor.execute{avviaServer()}
        } catch(exc: Exception) {
            Toast.makeText(this, "Si è verificato un errore nella creazione del server", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Si è verificato un errore nell'avvio del server ${exc}", exc)
        }
    }

    private fun avviaServer() {
        server = ServerNanoHttpd(porta)

    }
    override fun onDestroy() {
        super.onDestroy()
        server?.let{
            server!!.stop()
        } ?: run {
            Log.e(TAG, "Il server è nullo, non è stato mai avviato!")
            throw NullPointerException("Il server è nullo, non è stato mai avviato!")
        }
    }

}