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
}