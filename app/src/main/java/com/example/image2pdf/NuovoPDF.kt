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
            //Andrà gestito il nome con un fragment da cui eventualmente invocherò il codice sottostante
            //E lo darò come extra
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
            val gen=GeneratorePDF("NOMEPROVVISORIO")//Andrà inserito quello scelto dall'utente

            //Richiamo fragment
            //TODO Disattivare i pulsanti quando viene generato il fragment, esiste una funzione apposita, ma per farlo mi sa che è
            //necessario spostare i bottoni in una companion class o come attributi della classe per renderli visibili
            val fragment = SceltaNome() // Crea una nuova istanza del Fragment
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frammentonascosto, fragment) // Usa `replace` per sostituire un Fragment esistente
            transaction.commit()
            //Codice da adattare
            gen.iniziaCostruzionePDF()
            gen.caricaImmagini(arrayOfBitmap)
            Toast.makeText(baseContext, "${arrayOfBitmap.size}", Toast.LENGTH_SHORT).show()
        }
    }
}