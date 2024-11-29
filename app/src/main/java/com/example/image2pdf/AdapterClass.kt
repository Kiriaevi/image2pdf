package com.example.image2pdf

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.compose.ui.layout.Layout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

//Queste due classi servono ad adattare i dati al RecycleView, si ereditano le classi e si modificano opportunamente
//View Holder Class serve per la singola riga
//Adapter Class per creare l'insieme di righe
//In generale l'adapter View dovrebbe essere ottima per gestire lunghi elenchi

class AdapterClass(private val listaDati :ArrayList<DataClass>) : RecyclerView.Adapter<AdapterClass.ViewHolderClass>() {

    class ViewHolderClass(itemView: View):RecyclerView.ViewHolder(itemView) {
        val name:TextView = itemView.findViewById(R.id.NomePdf)
        val data:TextView = itemView.findViewById(R.id.Datapdf)
        val condividi:ImageButton = itemView.findViewById(R.id.condividiPdf)
        init {
            condividi.setOnClickListener {
                val position = adapterPosition
                //TODO codice interno per la condivisione
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val ItemView = LayoutInflater.from(parent.context).inflate(R.layout.rigatabella, parent,false)
        return ViewHolderClass(ItemView)
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