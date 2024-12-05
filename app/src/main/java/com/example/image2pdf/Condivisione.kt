package com.example.image2pdf

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.image2pdf.linuxIntegration.linuxIntegration
import java.io.File
import java.util.Date

class Condivisione : AppCompatActivity() {
    companion object {
        private var arrayOfBitmap = mutableListOf<Bitmap>()//Qui dentro ci saranno le foto scelte
    }
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
        val bottPing=findViewById<ImageButton>(R.id.pinguino)
        val bottCam=findViewById<ImageButton>(R.id.camera)
        val bottGall=findViewById<ImageButton>(R.id.galleria)
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
        bottPing.setOnClickListener {
            cambiaActivity(linuxIntegration::class.java)
        }
        bottCam.setOnClickListener {
            cambiaActivity(Fotocamera::class.java)
        }
        bottGall.setOnClickListener {
            val intentIm = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intentIm.type="image/*"
            intentIm.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
            startActivityForResult(intentIm,1)
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
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
            val dataClass = DataClass(listaNomi[i],"$mese/$anno",listaFile[i])
            dataList.add(dataClass)
        }
        recicleView.adapter = AdapterClass(this,dataList)
    }

    //Req=1 gestisce l'intent implicito
    //Req=2 gestisce il ritorno del nome
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null) {
            // Verifica se ci sono più immagini
            val imagesUri = data.clipData
            arrayOfBitmap.clear()//Pulisco l'array ogni volta che acquisico cose nuove(Sarebbe meglio farlo dopo aver creato il pdf)
            if (imagesUri != null) {
                // Se sono state selezionate più immagini
                for (i in 0 until imagesUri.itemCount) {
                    val imageUri = imagesUri.getItemAt(i).uri
                    val stream = contentResolver.openInputStream(imageUri)
                    arrayOfBitmap.add(BitmapFactory.decodeStream(stream))
                }
            } else {
                // Se c'è una sola immagine
                val imageUri = data.data
                if (imageUri != null) {
                    val stream = contentResolver.openInputStream(imageUri)
                    arrayOfBitmap.add(BitmapFactory.decodeStream(stream))
                }
            }
            val intentRis = Intent(this,SceltaNome::class.java)
            startActivityForResult(intentRis,2)
        }
        else if(requestCode == 2 && data != null){
            val ris = data.getStringExtra("RITORNO")
            if(ris!=null){
                val gen=GeneratorePDF(ris)
                gen.iniziaCostruzionePDF()
                gen.caricaImmagini(arrayOfBitmap)
            }
        }
    }

    //Al rientro devo aggiornare la lista, questo poichè potrebbe essere cambiata
    override fun onResume() {
        super.onResume()
        pulisci()
        creaContainer()
    }
    fun pulisci(){
        dataList.clear()
        listaFile.clear()
        listaNomi.clear()
        listaDate.clear()
    }
}