package com.example.image2pdf

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                val chiave = it.key
                val valore = it.value
                if (chiave in REQUIRED_PERMISSIONS && valore == false) {
                    permissionGranted = false
                    Log.e("permessi", "ERRORE DI PERMESSI PER ${chiave}" )
                }
            }
            if (!permissionGranted) {
                finish()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        // aggiunto un commento a caso per testare GIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
         if (allPermissionsGranted()) {
            attendiEvento()
        } else {
            requestPermissions()
        }
    }

    fun attendiEvento(){


        val bottone1 = findViewById<Button>(R.id.bottone1)
        val bottone2 = findViewById<Button>(R.id.bottone2)
        bottone1.setOnClickListener {
            cambiaActivity(Condivisione::class.java)
        }
        bottone2.setOnClickListener {
            cambiaActivity(NuovoPDF::class.java)
        }
    }

    fun cambiaActivity(classe :Class<out Activity>){
        val intent = Intent(this,classe)
        startActivity(intent)
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,

            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
