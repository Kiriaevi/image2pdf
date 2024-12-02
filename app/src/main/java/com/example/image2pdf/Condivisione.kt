package com.example.image2pdf

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Date

class Condivisione : AppCompatActivity() {
    // un'altra modifica
    //In dataList per ora ci metto i dati artificiali, appena riusciremo automatizzeremo l'ingresso
    private lateinit var recicleView : RecyclerView
    private lateinit var dataList: ArrayList<DataClass>
    var listaNomi:ArrayList<String> = ArrayList<String>()
    var listaDate:ArrayList<Date> = ArrayList<Date>()
    var listaFile:ArrayList<File> = ArrayList<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_condivisione)
        val dSalv = GeneratorePDF.directory
        creaContainer()

        val bottRicerca=findViewById<ImageButton>(R.id.cerca)
        val bottReset=findViewById<ImageButton>(R.id.gomma)
        //Ricerca pdf particolari
        bottRicerca.setOnClickListener {
            if(!findViewById<TextView>(R.id.textView).text.toString().isBlank()){
                val arrayFiltrato = ArrayList<DataClass>()
                filtra(arrayFiltrato,findViewById<TextView>(R.id.textView).text.toString())
                recicleView.adapter = AdapterClass(this,arrayFiltrato)
            }
        }
        bottReset.setOnClickListener {
            recicleView.adapter = AdapterClass(this,dataList)
        }
    }

    //Ricerca nel sottoinsieme
    fun filtra(arrayFiltrare : ArrayList<DataClass>,key : String){
        var posizione = 0
        for (data in dataList){
            var stringaFilt = data.titolo
            if(stringaFilt.length<=4){
                posizione++
                continue
            }
            if(stringaFilt.contains(key)){
                val attuale = dataList.get(posizione)
                val dati = DataClass(attuale.titolo,attuale.data,attuale.file)
                arrayFiltrare.add(dati)
            }
            posizione++
        }
    }

    //Sto facendo sta cosa, ma è altamente inefficente, la renderò più efficente spostando tutti i dati in un database
    //E aggiornando quest'ultimo a ogni dataEntry o dataExit, per far ciò la cartella dovrà essere interna all'app, per non avere
    //inconsistenze (Salvo root o cose strane ovviamente), in ogni caso con pochi dati è gestibile

    fun creaContainer(){
        val dSalv = GeneratorePDF.directory
        recicleView = findViewById<RecyclerView>(R.id.ContenitorePdf)
        recicleView.layoutManager = LinearLayoutManager(this)
        recicleView.setHasFixedSize(true)
        dataList = arrayListOf<DataClass>()
        riempiStrutture()
        getData()
    }

    //MetodoTemporaneo per inserire i dati della directory
    fun riempiStrutture(){
        val listaSpuria = GeneratorePDF.directory.listFiles()
        if(listaSpuria!=null){
            for(file in listaSpuria){
                //Toast.makeText(this,file.name,Toast.LENGTH_SHORT).show()
                if(file.isFile){
                    listaFile.add(file)
                    listaNomi.add(file.name)
                    listaDate.add(Date(file.lastModified()))//Riempio e cucino
                }
            }
        }
    }


    fun getData(){
        for(i in listaNomi.indices){
            val anno = listaDate[i].year+1900
            val mese = listaDate[i].month+1
            val dataClass = DataClass(listaNomi[i],"$anno/$mese",listaFile[i])
            dataList.add(dataClass)
        }
        recicleView.adapter = AdapterClass(this,dataList)
    }
}