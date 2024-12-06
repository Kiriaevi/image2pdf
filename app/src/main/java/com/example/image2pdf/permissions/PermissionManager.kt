package com.example.image2pdf.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.image2pdf.MainActivity

class PermissionManager(context: Context, activity: FragmentActivity) {
    private val baseContext: Context = context
    private val activityCorrente: FragmentActivity = activity

    private val activityResultLauncher =
        activityCorrente.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
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
                activityCorrente.finish()
            } else {
                activityCorrente.recreate()
            }
        }
    companion object {
         val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }
    fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

}