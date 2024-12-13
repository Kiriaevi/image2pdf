package com.example.image2pdf

import java.io.File

//Questa classe conterrà i dati da inserire nella tabella
data class DataClass(var titolo : String,var data : String, var file : File,var utilizzo : Int)
//Utilizzo è un valore numerico che da una "priorità" all'elemento