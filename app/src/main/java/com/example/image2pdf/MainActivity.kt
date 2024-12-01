package com.example.image2pdf

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.image2pdf.linuxIntegration.linuxIntegration

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
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#575656")))
         if (allPermissionsGranted()) {
            attendiEvento()
        } else {
            requestPermissions()
        }
    }
    fun attendiEvento(){
        val bottone1 = findViewById<Button>(R.id.bottone1)
        val bottone2 = findViewById<Button>(R.id.bottone2)
        val bottone3 = findViewById<Button>(R.id.bottone3)
        bottone1.setOnClickListener {
            cambiaActivity(Condivisione::class.java)
        }
        bottone2.setOnClickListener {
            cambiaActivity(NuovoPDF::class.java)
        }
        bottone3.setOnClickListener {
            //TODO: cambia l'icona in tux_linux, è già nella directory da te creata
            cambiaActivity(linuxIntegration::class.java)
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
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
