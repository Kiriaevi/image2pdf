package com.example.image2pdf

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageProxy
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.io.source.ByteArrayOutputStream
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream

class Condivisione(nome: String) : AppCompatActivity() {

    companion object {
        // Directory di salvataggio comune a tutte le istanze
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    }
    // nome del file
    private val file: File
    // Immagini passati in input
    // private val immaginiCatturate: MutableList<ImageProxy> = mutableListOf()
    // riferimento al documento, questo viene usato per scrivere paragrafi, testo e immagini
    private var document: Document? = null
    // costruttore strano di kotlin
    init {
         this.file = File(directory, "${nome}.pdf")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
    }
    /* imposta l'attributo [document] del documento che si sta modificando in modo da avere un riferimento
     al file ATTENZIONE POSSIBILE BUG: il documento viene chiuso in [impostaInformazioniBase()] questo può portare
     a gravi BUG dato che il file viene chiuso dopo, suggerimento che propongo: mettere [document.close()] nel metodo [onDestroy()] */
    fun iniziaCostruzionePDF() {
        createPdf()
    }
    /* Prende come input una lista di ImageProxy e li converte in formati Image (compatibili con iText), successivamente
    aggiunge l'immagine assieme ad una breve didascalia rispettivamente con i metodi [aggiungiImmagine()] e [aggiungiParagrafo()]
     */
    fun impostaInformazioniBase(immaginiCatturate: MutableList<ImageProxy>) {
        var count: Int = 0
        for(i in immaginiCatturate) {
            aggiungiImmagine(convertiImgProxyABitMap(i))
            aggiungiParagrafo("Immagine ${count}")
            count++
        }
        this.document!!.close()
        Toast.makeText(baseContext,
            "Il documento è stato chiuso",
            Toast.LENGTH_SHORT).show()
    }

    /*
    Il metodo che si occupa di convertire un ImageProxy in Image, probabilmente il maggiore colpevole dei cali di prestazione
    e della pesantezza generale del PDF, FIXATO(comprimere le immagini): dare un'occhiata alla documentazione e vedere se si è in grado di evitare questo enorme overhead.
     */
    private fun convertiImgProxyABitMap(img: ImageProxy): Image {
        val bitmap: Bitmap = img.toBitmap()
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageData = ImageDataFactory.create(byteArray)
        return Image(imageData)
    }

    /* Aggiunge un paragrafo, INPUT: Stringa OUTPUT: niente */
    fun aggiungiParagrafo(paragrafo: String) {
        this.document!!.add(Paragraph(paragrafo))
    }
    /* Aggiunge una immagine, per la documentazione di iText va usata un Image, più info qui
    https://github.com/itext/itext-publications-examples-java/blob/master/src/main/java/com/itextpdf/samples/sandbox/images/MultipleImages.java
    e qui https://github.com/itext/itext-java
     */
    fun aggiungiImmagine(immagine: Image) {
        this.document!!.add(immagine)
    }
    private fun createPdf() {
        try {
            // Crea un FileOutputStream
            val fileOutputStream = FileOutputStream(this.file)
            // Crea un PdfWriter che gestisce la scrittura del PDF
            val writer = PdfWriter(fileOutputStream)
            // Crea un PdfDocument associandolo al PdfWriter
            val pdfDocument = PdfDocument(writer)
            // Crea un oggetto Document per aggiungere contenuti
            this.document = Document(pdfDocument)
        } catch (exc: Exception) {
            // Gestione degli errori
            Toast.makeText(baseContext, "Errore nella creazione del PDF", Toast.LENGTH_SHORT).show()
        }
    }
}