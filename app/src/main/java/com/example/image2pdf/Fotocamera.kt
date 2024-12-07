package com.example.image2pdf
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.image2pdf.databinding.ActivityFotocameraBinding
import com.example.image2pdf.permissions.PermissionManager
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// https://developer.android.com/codelabs/camerax-getting-started#1
// NON HO FINITO, CONTINUA QUI -> https://developer.android.com/codelabs/camerax-getting-started#4
class Fotocamera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityFotocameraBinding

    private var numImmagini: Int = 0
    // valori relativi al ciclo di vita di una fotocamera
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var pdfExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture ?= null
    private var camera: Camera?= null
    private val immaginiCatturate: CopyOnWriteArrayList<Bitmap> = CopyOnWriteArrayList()

    companion object {
        private const val TAG = "FOTOCAMERAX"
        private val capabilities: MutableMap<String, Boolean> = mutableMapOf(
            "FLASHLIGHT" to false
        )
        // quello che l'utente vuole dalla fotocamera, ad esempio se clicco il bottone
        // per la torcia significa che l'utente vuole utilizzarla, qui viene impostato
        private val richiesta_utente: MutableMap<String, Boolean> = mutableMapOf(
            "FLASHLIGHT" to false,
            "MAX_QUALITY" to false
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityFotocameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        enableEdgeToEdge()

        gestisciPermessi()
    }
    private fun gestisciPermessi() {
        val gestorePermessi : PermissionManager = PermissionManager(this.baseContext, this)
        if (gestorePermessi.allPermissionsGranted()) {
            impostaLogicaDeiBottoni()
            startCamera()
        } else {
            gestorePermessi.requestPermissions()
        }
    }
    private fun impostaLogicaDeiBottoni() {
        val bottoneScatta = findViewById<ImageButton>(R.id.image_capture_button)
        val bottoneStampa = findViewById<ImageButton>(R.id.Save)
        val bottoneFlash = findViewById<ImageButton>(R.id.flashButton)
        val bottoneTornaIndietro = findViewById<ImageButton>(R.id.comeBack)
        val bottoneMaxQualita = findViewById<ImageButton>(R.id.quality)

        bottoneMaxQualita.setOnClickListener { modificaQualita() }
        bottoneScatta.setOnClickListener { takePhoto() }
        bottoneStampa.setOnClickListener { richiediNome() }
        bottoneFlash.setOnClickListener { modificaTorcia() }
        bottoneTornaIndietro.setOnClickListener { finish() }
    }

    //Questa funzione serve per richiamare l'activity che chiederà il nome per creare il pdf
    private fun richiediNome(){
        val actRes=Intent(this,SceltaNome::class.java)
        startActivityForResult(actRes,1)
    }

    private fun modificaTorcia() {
        richiesta_utente["FLASHLIGHT"] = !richiesta_utente["FLASHLIGHT"]!!
        updateCameraProvider()
    }
    private fun modificaQualita() {
        val isQualityMax = richiesta_utente["MAX_QUALITY"]
        richiesta_utente["MAX_QUALITY"] = !richiesta_utente["MAX_QUALITY"]!!
        val buttoneHd = findViewById<ImageButton>(R.id.quality)
        //Cambio l'immagine
        if (isQualityMax == false) {
            val image = resources.getDrawable(R.drawable.hd_941_svgrepo_com,null)
            buttoneHd.setImageDrawable(image)
            Toast.makeText(baseContext, "Modalità qualità massima: ATTIVA", Toast.LENGTH_SHORT).show()
        }
        else {
            val image = resources.getDrawable(R.drawable.sd_943_svgrepo_com,null)
            buttoneHd.setImageDrawable(image)
            Toast.makeText(baseContext, "Modalità qualità massima: DISATTIVATA", Toast.LENGTH_SHORT).show()
        }
        updateCameraProvider()
    }
    private fun stampaPDF(nomePdf : String) {
        // potrei usare isEmpty sulla lista immaginiCatturate ma cerco di evitare cicli che
        // potrebbero rallentare la UI dell'utente nel main thread (esagerazione).
        if (islistaImmaginiCatturateVuota()) {
            Toast.makeText(baseContext, "Inserisci almeno un'immagine", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            // il PDF viene compilato concorrentemente da un thread a parte
            pdfExecutor.execute {
                val riferimentoAlCostruttorePDF = GeneratorePDF(nomePdf)
                val canWrite : Boolean = riferimentoAlCostruttorePDF.iniziaCostruzionePDF()
                if (canWrite) {
                    riferimentoAlCostruttorePDF.caricaImmagini(immaginiCatturate, true, deepCopy = true)
                    Log.d(TAG, "PDF CREATO, CHIUSURA THREAD")
                    runOnUiThread {
                        Toast.makeText(baseContext, "PDF CREATO, è in DOCUMENTS", Toast.LENGTH_SHORT).show()
                    }
                    numImmagini = 0
                } else
                    runOnUiThread {
                        Toast.makeText(baseContext, "ERRORE, FILE PDF ESISTE GIÀ, RIPROVA CON UN NOME DIVERSO", Toast.LENGTH_SHORT).show()
                        richiediNome()
                    }
            }
        }
        catch (exc: Exception) {
            val message = "ERRORE NELLA CREAZIONE DELL'ISTANZA AL PDF: ${exc}"
            Toast.makeText(baseContext, "${message}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "${message}: ${exc}", exc)
        }
    }

    private fun islistaImmaginiCatturateVuota(): Boolean {
        return numImmagini == 0
    }

    private fun takePhoto() {
        // usa l'istanza di imageCapture se definita, se null allora fai un return
        // senza il return l'applicazione crasha
        // https://developer.android.com/reference/kotlin/androidx/camera/core/ImageCapture
        val imageCapture = this.imageCapture ?: return
        cameraExecutor.execute {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(baseContext,
                        "Cattura foto fallita",
                        Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
                // Se lo scatto va a buon fine aggiungi l'ImageProxy di output alla lista [immaginiCatturate]
                override fun
                        onCaptureSuccess(image: ImageProxy) {
                    Toast.makeText(baseContext,
                        "Cattura foto riuscita",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "IMMAGINE CATTURATA ${image}")
                    numImmagini++
                    // ruota l'immagine, comprimila e salvala in un Bitmap
                    val bitmapOutput: Bitmap = gestisciFoto(image)
                    immaginiCatturate.add(bitmapOutput)
                }
            }
        )
        }
    }
    private fun gestisciFoto(image: ImageProxy): Bitmap {
        try {
            val gradiRotazione = image.imageInfo.rotationDegrees
            // ruoto l'immagine
            val rotatedBitmap = GeneratorePDF.ruotaBitmap(image.toBitmap(), gradiRotazione.toFloat())
            // comprimo l'immagine ruotata
            val compressedByteArray = GeneratorePDF.comprimiBitmap(rotatedBitmap)
            // creo il Bitmap dall'immagine ruotata e compressa
            val compressedBitmap = BitmapFactory.decodeByteArray(compressedByteArray, 0, compressedByteArray.size)
            return compressedBitmap
        } finally {
            image.close()
        }
    }
    private fun setCapabilities(infoCamera: CameraInfo) {
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
        if (richiesta_utente["MAX_QUALITY"] == false) {
            Log.e(TAG, "SIAMO NELLA MODALITÀ CON LATENZA MINIMA")
            return ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        }
        else {
            Log.e(TAG, "SIAMO NELLA MODALITÀ CON MASSIMA QUALITÀ")
            return ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        }
    }

    private fun checkTorcia(): Int {
        if (capabilities["FLASHLIGHT"] == false) {
            Toast.makeText(baseContext,
                "La tua fotocamera non supporta il FLASH",
                Toast.LENGTH_SHORT).show()
            return ImageCapture.FLASH_MODE_OFF
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
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
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
            val camera = cameraProvider.bindToLifecycle(
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
        pdfExecutor.shutdown()
    }
    //TODO: per qualche motivo il compilatore dava errore e quindi ho commentato tutto
    //Questa serve per gestire il nome da inserire (Non ho inserito le robe multithread //TODO KIRIAEVI NON HO GESTITO LE ROBE CONCORRENTI, MA NON PERNSO CHE SIA NECESSARIO | Non penso
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1 && data != null){
            val ris = data.getStringExtra("RITORNO")
            if(ris!=null){
                if(ris!="")
                    stampaPDF(ris)
            }
        }
    }
}