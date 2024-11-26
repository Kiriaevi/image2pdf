package com.example.image2pdf
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.image2pdf.databinding.ActivityFotocameraBinding
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// https://developer.android.com/codelabs/camerax-getting-started#1
// NON HO FINITO, CONTINUA QUI -> https://developer.android.com/codelabs/camerax-getting-started#4
class Fotocamera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityFotocameraBinding

    // valori relativi al ciclo di vita di una fotocamera
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture ?= null
    private var camera: Camera?= null
    companion object {
        private const val TAG = "FOTOCAMERAX"
        // ci serve nella funzione takePhoto(), per  salvare le immagini con un timestamp
        private const val FILENAME_FORMAT = "dd-MM-yyyy-HH-mm-ss-SSS"
        private val capabilities: MutableMap<String, Boolean> = mutableMapOf(
            "ZERO_SHUTTER_LAG" to false,
            "FLASHLIGHT" to false
        )
        // quello che l'utente vuole dalla fotocamera, ad esempio se clicco il bottone
        // per la torcia significa che l'utente vuole utilizzarla, qui viene impostato
        private val richiesta_utente: MutableMap<String, Boolean> = mutableMapOf(
            "FLASHLIGHT" to false
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityFotocameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        enableEdgeToEdge()

        impostaLogicaDeiBottoni()
        /* Definisce un executor, e quindi un thread, ad eseguire in maniera asincrona una determinata azione,
           in questo caso la gestione della fotocamera */
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()

    }
    private fun impostaLogicaDeiBottoni() {
        val bottoneScatta = findViewById<Button>(R.id.image_capture_button)
        val bottoneStampa = findViewById<Button>(R.id.stampaPDF)
        val bottoneFlash = findViewById<ImageButton>(R.id.flashButton)

        bottoneScatta.setOnClickListener { takePhoto() }
        bottoneStampa.setOnClickListener { stampaPDF() }
        bottoneFlash.setOnClickListener { modificaTorcia() }
    }
    private fun modificaTorcia() {
        richiesta_utente["FLASHLIGHT"] = !richiesta_utente["FLASHLIGHT"]!!
        //imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
        updateCameraProvider()
    }

    private fun stampaPDF() {
        val riferimentoAlCostruttorePDF: NuovoPdf = NuovoPdf("outputPDF")
        // in questo metodo passiamo l'array di BITMAP
        //riferimentoAlCostruttorePDF.impostaInformazioniBase()
        riferimentoAlCostruttorePDF.iniziaCostruzionePDF()
    }

    private fun takePhoto() {
        // usa l'istanza di imageCapture se definita, se null allora fai un return
        // senza il return l'applicazione crasha
        // https://developer.android.com/reference/kotlin/androidx/camera/core/ImageCapture
        val imageCapture = this.imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.ITALY)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // TODO: RIMUOVERE OPZIONI DI OUTPUT E SALVATAGGIO IN DIRECTORY, SALVARE FILE IN RAM, NON DISCO
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext,
                        "Cattura foto fallita",
                        Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    Toast.makeText(baseContext,
                        "Cattura foto riuscita",
                        Toast.LENGTH_SHORT).show()
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun setCapabilities(infoCamera: CameraInfo) {
        @ExperimentalZeroShutterLag
        if ( infoCamera.isZslSupported )
            capabilities["ZERO_SHUTTER_LAG"] = true
        if ( infoCamera.hasFlashUnit() )
            capabilities["FLASHLIGHT"] = true
    }

    private fun updateCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        /* camera provider fornisce un'istanza alla fotocamera, con questa siamo in grado di chiamare metodi come
         unbind, unbindAll e bindToLifeCycle */
        val cameraProvider = cameraProviderFuture.get()
        var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        // associo la vista sul dispositivo alla fotocamera
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }
        // stacca la vecchia configurazione
        cameraProvider.unbindAll()
        // crea la nuova configurazione
        this.imageCapture = creaImageCapture(false)
        // attacca la configurazione nuova con tutto aggiornato
        this.camera = cameraProvider.bindToLifecycle(this,
            cameraSelector, preview, this.imageCapture)
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        this.imageCapture = creaImageCapture(true)
        cameraProviderFuture.addListener({
            this.imageCapture
            setCameraCycle()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun creaImageCapture(primaInizializzazione: Boolean): ImageCapture?  {
        if (primaInizializzazione) {
            return ImageCapture.Builder()
                .build()
        }
        // Se possiedi la torcia e l'utente vuole utilizzarla allora attivala
        val torcia = checkTorcia()
        /* Se possiedi la funzione sperimentale ZERO_SHUTTER_LAG attivala,
         altrimenti imposta la qualità migliore possibile CAPTURE_MODE_MAXIMIZE_QUALITY */
        val captureMode: Int = setCaptureMode()
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(captureMode)
            .setFlashMode(torcia)
            .build()
        return imageCapture
    }

    private fun setCaptureMode(): Int {
        @ExperimentalZeroShutterLag
        if (capabilities["ZERO_SHUTTER_LAG"] == true) {
            Log.e(TAG, "SIAMO NELLA MODALITÀ CON ZERO_SHUTTER")
            return ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG
        }
        else {
            return ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        }
    }

    private fun checkTorcia(): Int {
        if (capabilities["FLASHLIGHT"] == false) {
            Toast.makeText(baseContext,
                "La tua fotocamera non supporta il FLASH",
                Toast.LENGTH_SHORT).show()
        }

        return if ( richiesta_utente["FLASHLIGHT"] == true )
            ImageCapture.FLASH_MODE_ON
         else
            return ImageCapture.FLASH_MODE_OFF
    }
    /*
        Definisco la preview, ovvero il luogo dell'applicazione in cui il buffer della fotocamera verrà connessa, assieme
        al ciclo di vita della fotocamera
    */
    private fun setCameraCycle() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            /* camera provider fornisce un'istanza alla fotocamera, con questa siamo in grado di chiamare metodi come
             unbind, unbindAll e bindToLifeCycle */
            val cameraProvider = cameraProviderFuture.get()
            var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            // associo la vista sul dispositivo alla fotocamera
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            /* Con bindToLifeCycle sono in grado di legare l'istanza della mia fotocamera. La fotocamera comprende informazioni
            come ad esempio se si vuole la torcia, quale protocollo usare (ZERO_SHUTTER_LAG), e se siamo con la fotocamera anteriore
            o posteriore.
            */
            var camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)
            // Imposta tutti i parametri della fotocamera in base a quello che supporta
            setCapabilities(camera.cameraInfo)
            updateCameraProvider()
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}