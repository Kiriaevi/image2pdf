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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.ExperimentalZeroShutterLag
import androidx.camera.core.ImageCapture
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

// https://developer.android.com/codelabs/camerax-getting-started#1
// NON HO FINITO, CONTINUA QUI -> https://developer.android.com/codelabs/camerax-getting-started#4
class Fotocamera : AppCompatActivity() {
    /*
    Lo spiego prima che mi tartassa qualcuno i maroni per la classe ActivityFotocameraBinding
    Nel build.gradle ho abilitato i bindings, una funzione per cui sei in grado di riferirti
    ad ogni elemento denotato nei file xml delle activity secondo il loro id. Ad esempio
    ho creato un PreviewView a cui ho assegnato un ID come "viewFinder", per fare riferimento
    a questo o si usa il metodo della prof  che minimamente non ricordo OPPURE si usano i bindings
    di android. I bindings creano delle classi APPOSTA che contengono i dati dell'XML dell'activity,
    nel mio caso, dato che ho creato la activity: activity_fotocamera, se faccio
    import com.example.image2pdf.databinding.ActivityFotocameraBinding, ho accesso a tutte le cose con ID
    e quindi ho avuto il permesso di fare questo:
    setSurfaceProvider(viewBinding.viewFinder.surfaceProvider), ovvero viewBinding.viewFinder
     */
    private lateinit var viewBinding: ActivityFotocameraBinding

    // valori relativi al ciclo di vita di una fotocamera
    private val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    private val preview = Preview.Builder()
        .build()
        .also {
            it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
        }


    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

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
        //setContentView(R.layout.activity_fotocamera)
        val bottoneScatta = findViewById<Button>(R.id.image_capture_button)
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "hello_world.pdf")
         bottoneScatta.setOnClickListener { createPdf(file) }
        // bottoneScatta.setOnClickListener { takePhoto() }
        startCamera()
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

        // Set up image capture listener, which is triggered after photo has
        // been taken
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

        // ora che conosco i dati della fotocamera in uso posso aggiornare
        // l'oggetto Camera, per farlo richiamo la funziona updateCameraProvider()
        updateCameraProvider()
    }

    private fun updateCameraProvider() {
        var imageCapture: ImageCapture? = null
        // Se possiedi la torcia e l'utente vuole utilizzarla allora attivala
        val torcia = if (capabilities["FLASHLIGHT"] == true &&
            richiesta_utente["FLASHLIGHT"] == true ) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }

        if (capabilities["ZERO_SHUTTER_LAG"] == true) {
            Log.e(TAG, "SIAMO NELLA MODALITÀ CON ZERO_SHUTTER")
            @ExperimentalZeroShutterLag
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                .setFlashMode(torcia)
                .build()
        } else if(capabilities["ZERO_SHUTTER_LAG"] == false) {
            Log.e(TAG, "SIAMO NELLA MODALITÀ SENZA ZERO_SHUTTER")
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setFlashMode(torcia)
                .build()
        }
        this.imageCapture = imageCapture
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            imageCapture = creaImageCapture(true)
            setCameraCycle()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun creaImageCapture(primaInizializzazione: Boolean): ImageCapture?  {
        if (primaInizializzazione) {
            return ImageCapture.Builder()
                .build()
        }
        return null
    }

    private fun setCameraCycle() {
        try {
            // IMPOSTO VARIABILI DI AUSILIO
            val cameraProvider = this.cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            var camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)
            // Imposta tutti i parametri della fotocamera in base a quello che supporta
            setCapabilities(camera.cameraInfo)
            // stacca la vecchia istanza della fotocamera e la ricrea da capo con una nuova
            // che possiede tutte le informazioni aggiornate
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(this,
                cameraSelector, preview, imageCapture)
        } catch(exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    fun createPdf(file: File) {
        try {
            // Crea un FileOutputStream
            val fileOutputStream = FileOutputStream(file)

            // Crea un PdfWriter che gestisce la scrittura del PDF
            val writer = PdfWriter(fileOutputStream)

            // Crea un PdfDocument associandolo al PdfWriter
            val pdfDocument = PdfDocument(writer)

            // Crea un oggetto Document per aggiungere contenuti
            val document = Document(pdfDocument)

            // Aggiungi un paragrafo con il testo "Hello World" al PDF
            document.add(Paragraph("Paragrafo scritto tramite iText dal codice del progettino di ambienti"))

            // Chiudi il documento per completare la scrittura
            document.close()

            // Mostra un messaggio di successo
            Toast.makeText(baseContext, "PDF creato con successo", Toast.LENGTH_SHORT).show()
        } catch (exc: Exception) {
            // Gestione degli errori
            Toast.makeText(baseContext, "Errore nella creazione del PDF", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}