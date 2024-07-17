package com.netomix.camara

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recording
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.netomix.camara.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader


import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    lateinit var contexto:Context
    private var imageCapture: ImageCapture? = null
    lateinit var preview:Preview
    lateinit var  flashChackBox: CheckBox
     //   Log.e(TAG, "OpenCV initialization failed")
    //} else {
      //  Log.d(TAG, "OpenCV initialization succeeded")
   // }

    private var recording: Recording? = null
    private var onFlas=false;
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        imageCapture=ImageCapture.Builder().build()
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed.")
        } else {
            Log.d(TAG, "OpenCV initialization succeeded.")
        }
        flashChackBox=findViewById(R.id.checkBoxFlqash)
        flashChackBox.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
            onFlas=flashChackBox.isChecked
        }
        contexto=this
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

      //cambiar()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
fun cambiar(){
      val intent = Intent(contexto, ViewImage::class.java )
   startActivity(intent)
}
    @SuppressLint("RestrictedApi")
    private fun takePhoto() {
        try {

            // Get a stable reference of the modifiable image capture use case
            val imageCapture = imageCapture

           //Nombre del archivo
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            //Configuracion de la imagen
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Examen")
                }
            }

            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues)
                .build()
                //TOmar foto
            if (imageCapture != null) {
                if (imageCapture != null) {
                    if(onFlas){
                        imageCapture.camera?.cameraControl?.enableTorch(true)
                        imageCapture.camera?.cameraInfo?.exposureState?.exposureCompensationRange?.lower

                    }else{
                        imageCapture.camera?.cameraControl?.enableTorch(false)
                    }

                }
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                        }

                        override fun
                                onImageSaved(output: ImageCapture.OutputFileResults){

                            try {
                                val intent = Intent(contexto, ViewImage::class.java )
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                intent.putExtra("URL",output.savedUri.toString())

                                startActivity(intent)

                            }catch (e: Exception){

                                println(e)
                            }
                        }
                    }
                )
            }
        }catch (e:Exception ){
            Log.e("Error",e.message.toString())
        }

    }



    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
           preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
           cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview,imageCapture,imageAnalysis)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}