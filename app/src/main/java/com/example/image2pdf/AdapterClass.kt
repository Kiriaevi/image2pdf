package com.example.image2pdf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.LinkedList
import java.util.Queue

//Queste due classi servono ad adattare i dati al RecycleView, si ereditano le classi e si modificano opportunamente
//View Holder Class serve per la singola riga
//Adapter Class per creare l'insieme di righe
//In generale l'adapter View dovrebbe essere ottima per gestire lunghi elenchi

class AdapterClass(private val context : Context, private val listaDati :ArrayList<DataClass>) : RecyclerView.Adapter<AdapterClass.ViewHolderClass>() {
    //Classe che definisce la logica della singola riga
    class ViewHolderClass(context: Context,adapterClass: AdapterClass,itemView: View,listaDati: ArrayList<DataClass>):RecyclerView.ViewHolder(itemView) {
        val name:Button = itemView.findViewById(R.id.NomePdf)
        val data:TextView = itemView.findViewById(R.id.Datapdf)
        val condividi:ImageButton = itemView.findViewById(R.id.condividiPdf)
        val elimina:ImageButton = itemView.findViewById(R.id.eliminaPdf)

        val memoria=15 //Costante che ci dice il numero di foto importanti

        init {
            name.setOnClickListener {
                val position = adapterPosition
                val filePdf : File = listaDati[position].file
                incrementaUtilizzi(context,listaDati[position].titolo)
                apriPdf(itemView.context,filePdf)
            }
            condividi.setOnClickListener {
                val position = adapterPosition
                val filePdf : File = listaDati[position].file
                incrementaUtilizzi(context,listaDati[position].titolo)
                context.getSharedPreferences("datiUtilizzo",Context.MODE_PRIVATE)
                condividiPdf(itemView.context,filePdf)
            }
            elimina.setOnClickListener {
                val position = adapterPosition
                //Elimino da file system
                val fileRimuovere = listaDati.get(position).file
                val rimozione = fileRimuovere.delete()
                if(rimozione){
                    //Elimino dalla tabella
                    listaDati.removeAt(position)
                    adapterClass.notifyItemRemoved(position)
                }
                else{
                    Toast.makeText(itemView.context,"Rimozione fallita",Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Purtoppo hanno cambiato tutta la gestione dei permessi per la condivisione da android 10, per questo motivo va configurato
        //Un file provider anche nel manifest (fatto)
        private fun condividiPdf(context: Context,file : File) {
            val fileUri: Uri = ottieniUri(context,file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Permessi temporanei per la lettura
            }
            context.startActivity(Intent.createChooser(shareIntent,"Condividi il file PDF"))
        }
        private fun apriPdf(context: Context,file : File) {
            val fileUri: Uri = ottieniUri(context,file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // Permessi temporanei per la lettura
            }
            context.startActivity(Intent.createChooser(viewIntent,"Condividi il file PDF"))
        }

        fun ottieniUri(context: Context,file: File) : Uri{
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
            return fileUri
        }

        fun incrementaUtilizzi(context : Context, nome : String){
            val sharedPref = context.getSharedPreferences("datiUtilizzo",Context.MODE_PRIVATE)
            val modificatore = sharedPref.edit()
            var coda = scaricadaFile(context,"dataCoda.ser")
            if(coda==null)
                coda=LinkedList()
            //Sono importanti solo gli ultimi 30 click
            if(coda.size>=memoria){
                val el = coda.remove()
                modificatore.putInt(el,sharedPref.getInt(el,0)-1)
                modificatore.apply()
                //Elemento non piu importante
            }
            coda.add(nome)
            caricasuFile(context,coda,"dataCoda.ser")
            modificatore.putInt(nome,sharedPref.getInt(nome,0)+1)
            modificatore.apply()
        }

        //Funzioni che caricano/scaricano la coda da file
        fun scaricadaFile(context: Context, fileName: String): Queue<String>? {
            try {
                // Ottieni l'input stream dal file
                val fileInputStream = context.openFileInput(fileName)

                // Crea un ObjectInputStream per leggere l'oggetto
                val objectInputStream = ObjectInputStream(fileInputStream)

                // Leggi l'oggetto dal file e castalo a Queue<String>
                val queue = objectInputStream.readObject() as Queue<String>

                // Chiudi gli stream
                objectInputStream.close()
                fileInputStream.close()

                println("Coda caricata con successo.")
                return queue
            } catch (e: Exception) {
                return LinkedList()
            }
            return LinkedList()
        }


        fun caricasuFile(context: Context, queue: Queue<String>, fileName: String) {
            try {
                // Ottieni il percorso per scrivere nella memoria interna privata dell'app
                val fileOutputStream: FileOutputStream

                fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

                // Crea l'ObjectOutputStream per serializzare l'oggetto
                val objectOutputStream = ObjectOutputStream(fileOutputStream)

                // Scrivi la coda nel file
                objectOutputStream.writeObject(queue)

                // Chiudi gli stream
                objectOutputStream.close()
                fileOutputStream.close()

            } catch (e: Exception) {
                //Non è cosi importante il caricamento se non succede non distruggo l'app
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val ItemView = LayoutInflater.from(parent.context).inflate(R.layout.rigatabella, parent,false)
        return ViewHolderClass(context,this,ItemView,listaDati)
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