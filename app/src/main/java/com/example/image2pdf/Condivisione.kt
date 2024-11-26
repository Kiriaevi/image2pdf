package com.example.image2pdf

import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream

//TODO SPOSTARE IL CAZZO DI CODICE (Giangiu è il tuo momento)
//Tolto parametro alla classe perchè creava conflitti giustamente
class Condivisione : AppCompatActivity() {

    //In dataList per ora ci metto i dati artificiali, appena riusciremo automatizzeremo l'ingresso
    private lateinit var recicleView : RecyclerView
    private lateinit var dataList: ArrayList<DataClass>
    lateinit var listaTitoli:Array<String>
    lateinit var listaDate:Array<String>

    /*
    companion object {
        // Directory di salvataggio comune a tutte le istanze
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    }
    // nome del file
    private val file: File

    // costruttore strano di kotlin
    init {
         this.file = File(directory, "${nome}.pdf")
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_condivisione)
        //Andranno aggiunti qui in un metodo i dati da inserire, per ora inserisco dei dati sperimentali
        listaTitoli= arrayOf("Laurea","Diploma","Dmitri","Laurea","Diploma","Dmitri","Laurea","Diploma","Dmitri","Laurea","Diploma","Dmitri","Laurea","Diploma","Dmitri","Laurea","Diploma","Dmitri")
        listaDate= arrayOf("01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024","01/01/2024")
        recicleView=findViewById<RecyclerView>(R.id.ContenitorePdf)
        recicleView.layoutManager = LinearLayoutManager(this)
        recicleView.setHasFixedSize(true)
        dataList = arrayListOf<DataClass>()
        getData()
    }

    fun getData(){
        for(i in listaTitoli.indices){
            val dataClass = DataClass(listaTitoli[i],listaDate[i])
            dataList.add(dataClass)
        }
        recicleView.adapter = AdapterClass(dataList)
    }

    /*
    fun iniziaCostruzionePDF() {
        createPdf()
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
            val document = Document(pdfDocument)

            // Aggiungi un paragrafo con il testo "Hello World" al PDF
            document.add(Paragraph("Paragrafo scritto tramite iText dal codice del progettino di ambienti"))

            // Chiudi il documento per completare la scrittura
            document.close()

            // Mostra un messaggio di successo
            Toast.makeText(baseContext, "PDF creato con successo", Toast.LENGTH_SHORT).show()
        } catch (exc: Exception) {
            // Gestione degli errori
            Toast.makeText(baseContext, "Errore nella creazione del PDF", Toast.LENGTH_SHORT).show()
        }
    }
     */
}