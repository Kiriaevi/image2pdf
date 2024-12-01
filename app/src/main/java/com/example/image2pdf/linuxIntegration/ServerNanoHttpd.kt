package com.example.image2pdf.linuxIntegration

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.image2pdf.GeneratorePDF
import fi.iki.elonen.NanoHTTPD
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.file.Files


class ServerNanoHttpd(port: Int): NanoHTTPD(port) {

    private val sb: StringBuilder = StringBuilder()
    private val immaginiCatturate = mutableListOf<Bitmap>()
    companion object {
        private val TAG: String = "NETWORKING"
    }
    init {
        start(SOCKET_READ_TIMEOUT, false)
        Log.e("SERVERMIO","\nInizializzato! Point your browsers to http://${ipLocale()}:8080/ \n")
    }

    fun ipLocale(): String {
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val inetAddresses = networkInterface.inetAddresses
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    // Filtra solo gli indirizzi IPv4 e scarta IPv6
                    if (!inetAddress.isLoopbackAddress && inetAddress is InetAddress && inetAddress.hostAddress.indexOf(":") == -1) {
                        return inetAddress.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Indirizzo IP non trovato"
    }
    override fun serve(session: IHTTPSession): Response {
        // Gestisci solo le richieste POST
        if (session.method == Method.POST) {
            var risposta: Response? = null
            sb.append("Richiesta ricevuta! \n")
            sb.clear()
            // Parametri per il corpo della richiesta
            val params = mutableMapOf<String, String>()
            try {
                // Analizza il corpo della richiesta per estrarre i file
                session.parseBody(params)
                if (params.isNotEmpty()) {
                    for ((chiave,valore) in params)
                    {
                        sb.append("$chiave ricevuto! \n")
                        aggiungiInRAM(valore)
                    }
                } else {
                    sb.append("Nessun valore ricevuto, riprova con un input valido \n")
                }
                sb.append("Tutti i valori sono stati ricevuti, invio delle immagini al generatore di PDF \n")
                val generatorePDF = GeneratorePDF("NomeACaso")
                generatorePDF.iniziaCostruzionePDF()
                val path = generatorePDF.caricaImmagini(immaginiCatturate, deepCopy = true)
                immaginiCatturate.clear()
                sb.append("PDF creato, lo recupero e lo invio \n")
                risposta = recuperaPDF(path)
            } catch (e: IOException) {
                sb.append("Errore nell'elaborazione della richiesta.\n")
                e.printStackTrace()
            }
            Log.e(TAG, sb.toString())
            return risposta!!
        } else {
            // Risposta in caso di metodo non supportato
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, "text/plain", "Metodo non supportato.")
        }
    }

    private fun recuperaPDF(pdfPath: String): Response {
        val pdfFile = File(pdfPath)
        if (!pdfFile.exists()) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain", "PDF non trovato"
            )
        }

        // Crea un InputStream per il PDF
        val pdfInputStream = FileInputStream(pdfFile)

        // Restituisce il PDF come risposta con un InputStream
        return newChunkedResponse(
            Response.Status.OK,
            "application/pdf",  // Tipo di contenuto PDF
            pdfInputStream // Restituisce l'InputStream del PDF
        )
    }


    private fun aggiungiInRAM(path :String) {
        val file = File(path)
        val inputStream = file.inputStream()
        val bitmap = BitmapFactory.decodeStream(inputStream)
        // Se il bitmap è stato creato correttamente
        bitmap?.let {
            immaginiCatturate.add(bitmap)
        } ?: {
            Log.e(TAG, "Errore nell'elaborazione di $path")
            val e: Exception = Exception("Errore nell'elaborazione di $path")
            e.printStackTrace()
        }
    }
}
