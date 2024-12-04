package com.example.image2pdf

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.Date

class Condivisione : AppCompatActivity() {
    private lateinit var recicleView : RecyclerView
    private lateinit var dataList: ArrayList<DataClass>
    var listaNomi:ArrayList<String> = ArrayList()
    var listaDate:ArrayList<Date> = ArrayList()
    var listaFile:ArrayList<File> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_condivisione)
        creaContainer()
        val bottRicerca=findViewById<ImageButton>(R.id.cerca)
        val bottReset=findViewById<ImageButton>(R.id.gomma)
        //Ricerca pdf per nome
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

    //Ricerca del sottoinsieme che contiene la determinata stringa
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

    //Creo il container che conterrà le varie righe rappresentanti i vari pdf
    fun creaContainer(){
        recicleView = findViewById(R.id.ContenitorePdf)
        recicleView.layoutManager = LinearLayoutManager(this)
        recicleView.setHasFixedSize(true)
        dataList = arrayListOf()
        riempiStrutture()
        getData()
    }

    //Metodo per inserire i dati della directory negli array
    fun riempiStrutture(){
        val listaSpuria = GeneratorePDF.directory.listFiles()
        if(listaSpuria!=null){
            for(file in listaSpuria){
                if(file.isFile){
                    listaFile.add(file)
                    listaNomi.add(file.name)
                    listaDate.add(Date(file.lastModified()))
                }
            }
        }
    }

    //Metodo per inserire i dati riga per riga
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