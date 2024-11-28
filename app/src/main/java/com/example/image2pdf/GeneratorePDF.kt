package com.example.image2pdf

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageProxy
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream

class GeneratorePDF(nome: String) {
    companion object {
        // Directory di salvataggio comune a tutte le istanze
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val TAG =  "GeneratorePDF"

        /**
        Il metodo che si occupa di convertire un Bitmap in Image o ImageProxy in Image. Opzionalmente
        se il booleano passato è true (default) si impegna pure a comprimere l'immagine per risparmiare spazio, viceversa se
        false non comprime nulla e si occupa solo di convertire. Può essere anche specificata la qualità desiderata, la default è 70%.
         */
        fun convertiBitMapAImg(bitmap: Bitmap, compress: Boolean, qlt: Int): Image {
            var byteArray: ByteArray? = null
            if (compress)
             byteArray = comprimiBitmap(bitmap, qlt)
            else {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                byteArray = byteArrayOutputStream.toByteArray()
            }

            val imageData = ImageDataFactory.create(byteArray)
            return Image(imageData)
        }
        fun convertiImgProxyAImg(img: ImageProxy, compress: Boolean, qlt: Int = 70): Image {
            val bitmap: Bitmap = img.toBitmap()
            return convertiBitMapAImg(bitmap, compress, qlt)
        }

        fun comprimiBitmap(bitmap: Bitmap, qlt: Int = 70): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, qlt, byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray()
        }

        /**
         * Funzione di ausilio che preleva un bitmap in ingress e lo ruota di [gradiRotazione]
         * esempio: ruotaBitmap(bitmap, 90f) -> ruota il bitmap di 90 gradi e restituisce un nuovo formato bitmap
         */
        fun ruotaBitmap(bitmap: Bitmap, gradiRotazione: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(gradiRotazione)
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            return rotatedBitmap
        }
    }
    // nome del file
    private val file: File  =File(directory, "${nome}.pdf")
    // Immagini passati in input
    // private val immaginiCatturate: MutableList<ImageProxy> = mutableListOf()
    // riferimento al documento, questo viene usato per scrivere paragrafi, testo e immagini
    private var document: Document? = null

    /** imposta l'attributo [document] del documento che si sta modificando in modo da avere un riferimento
     al file ATTENZIONE POSSIBILE BUG: il documento viene chiuso in [impostaInformazioniBase()] questo può portare
     a gravi BUG dato che il file viene chiuso dopo, suggerimento che propongo: mettere [document.close()] nel metodo [onDestroy()] */
    fun iniziaCostruzionePDF() {
        createPdf()
    }
    /* Aggiunge un paragrafo, INPUT: Stringa OUTPUT: niente */
    fun aggiungiParagrafo(paragrafo: String) {
        this.document!!.add(Paragraph(paragrafo))
    }
    /** Aggiunge una immagine, per la documentazione di iText va usata un Image, più info qui
    https://github.com/itext/itext-publications-examples-java/blob/master/src/main/java/com/itextpdf/samples/sandbox/images/MultipleImages.java
    e qui https://github.com/itext/itext-java
     */
    fun aggiungiImmagine(immagine: Image) {
        this.document!!.add(immagine)
    }
    /** Prende come input una lista di ImageProxy e li converte in formati Image (compatibili con iText), successivamente
    aggiunge l'immagine assieme ad una breve didascalia rispettivamente con i metodi [aggiungiImmagine()] e [aggiungiParagrafo()]
    Se l'input passato è una lista di ImageProxy richiama la funzione [convertiImgProxyAImg], se è un Bitmap richiama [convertiBitMapAImg]
     */
    fun <T> caricaImmagini(immaginiCatturate: List<T>, compress: Boolean = true, qlt: Int = 70) {
        var count: Int = 0
        for (item in immaginiCatturate) {
            when (item) {
                is ImageProxy -> {
                    aggiungiImmagine(convertiImgProxyAImg(item, compress, qlt))
                }
                is Bitmap -> {
                    aggiungiImmagine(convertiBitMapAImg(item,compress, qlt))
                }
                else -> {
                    Log.e(TAG, "Tipo di immagine non supportato")
                    continue
                }
            }
            aggiungiParagrafo("Immagine $count")
            count++
        }
        closePdf()
    }
    private fun closePdf() {
        try {
            this.document?.close()
            Log.e(TAG, "PDF chiuso correttamente")
        } catch (exc: Exception) {
            Log.e(TAG, "Il documento non è stato chiuso correttamente: ${exc}", exc)
        }
    }
    private fun createPdf() {
        try {
            val fileOutputStream = FileOutputStream(this.file)
            // Crea un PdfWriter che gestisce la scrittura del PDF
            val writer = PdfWriter(fileOutputStream)
            val pdfDocument = PdfDocument(writer)
            // Crea un oggetto Document per aggiungere contenuti
            this.document = Document(pdfDocument)
        } catch (exc: Exception) {
            Log.e(TAG, "Errore: ${exc}")
        }
    }

}