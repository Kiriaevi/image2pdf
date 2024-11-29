package com.example.image2pdf

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class NuovoPDF : AppCompatActivity() {

    companion object {
        private var arrayOfBitmap = mutableListOf<Bitmap>()//Raccoglitore di foto di famiglia
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuovopdf)
        attendiEvento()
    }

    fun attendiEvento(){
        val bottone1 = findViewById<Button>(R.id.bc_1)
        val bottone2 = findViewById<Button>(R.id.bc_2)
        bottone1.setOnClickListener {
            cambiaActivity(Fotocamera::class.java)
        }
        bottone2.setOnClickListener{
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
                val gen=GeneratorePDF(ris)//Andrà inserito quello scelto dall'utente
                gen.iniziaCostruzionePDF()
                gen.caricaImmagini(arrayOfBitmap)
            }
        }
    }
}