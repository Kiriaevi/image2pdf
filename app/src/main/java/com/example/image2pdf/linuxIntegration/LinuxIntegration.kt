package com.example.image2pdf.linuxIntegration

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.image2pdf.R
import fi.iki.elonen.NanoHTTPD.SOCKET_READ_TIMEOUT
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class linuxIntegration : AppCompatActivity() {
    companion object {
        private val TAG = "LINUXINT"
        private val porta = 8080
    }
    private var serverExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var server: ServerNanoHttpd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_linux_integration)
        /* TODO GRAVE @AETORO @AETORO, io come testo predefinito ho messo il mio indirizzo IP locale,
            ovviamente questa è una stronzata, bisognerebbe mettere l'indirizzo IP dell'utente reale, per
            fare ciò ho scritto un metodo grazie a ziogpt che trova l'IPV4 dell'utente e lo restituisce, si
            trova nel companion Object di ServerNanoHttpd e si chiama [ipLocale]. L'output di questo dovrebbe
            andare al posto del mio IP placeholder che ho messo. In generale migliorare la grafica
            che è orribile.
            * */
        try {
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