package com.example.image2pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.compose.ui.layout.Layout
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.io.File

//Queste due classi servono ad adattare i dati al RecycleView, si ereditano le classi e si modificano opportunamente
//View Holder Class serve per la singola riga
//Adapter Class per creare l'insieme di righe
//In generale l'adapter View dovrebbe essere ottima per gestire lunghi elenchi

class AdapterClass(private val context : Context, private val listaDati :ArrayList<DataClass>) : RecyclerView.Adapter<AdapterClass.ViewHolderClass>() {

    //Classe che definisce la logica della singola riga
    class ViewHolderClass(itemView: View,listaDati: ArrayList<DataClass>):RecyclerView.ViewHolder(itemView) {
        val name:TextView = itemView.findViewById(R.id.NomePdf)
        val data:TextView = itemView.findViewById(R.id.Datapdf)
        val condividi:ImageButton = itemView.findViewById(R.id.condividiPdf)
        init {
            condividi.setOnClickListener {
                val position = adapterPosition
                //TODO codice interno per la condivisione
                val filePdf : File = listaDati[position].file
                condividiPdf(itemView.context,filePdf)
            }
        }

        //Purtoppo hanno cambiato tutta la gestione dei permessi per la condivisione da android 10, per questo motivo va configurato
        //Un file provider anche nel manifest (fatto)
        private fun condividiPdf(context: Context,file : File) {
            val fileUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //Android 10 e versioni successive
                FileProvider.getUriForFile(
                    context,
                    "com.example.image2pdf.fileprovider",
                    file
                )
            } else {
                // Su versioni precedenti
                Uri.fromFile(file)
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Permessi temporanei per la lettura
            }
            context.startActivity(Intent.createChooser(shareIntent,"Condividi il file PDF"))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val ItemView = LayoutInflater.from(parent.context).inflate(R.layout.rigatabella, parent,false)
        return ViewHolderClass(ItemView,listaDati)
    }

    override fun getItemCount(): Int {
        return listaDati.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val oggettiCorrenti = listaDati[position]
        holder.data.text= oggettiCorrenti.data
        holder.name.text=oggettiCorrenti.titolo
    }
}