package com.example.image2pdf

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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