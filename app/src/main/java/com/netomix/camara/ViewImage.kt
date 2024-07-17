package com.netomix.camara

import android.annotation.SuppressLint
import android.app.TaskStackBuilder
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import com.netomix.camara.core.Core
import com.netomix.camara.core.Mat
import com.netomix.camara.core.Point
import com.netomix.camara.core.Rect
import com.netomix.camara.core.Scalar

import org.opencv.core.*
import com.netomix.camara.core.Core.*
import org.opencv.imgproc.Imgproc

class ViewImage : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var seekBarminDistancia: SeekBar
    lateinit var seekBarminRadio: SeekBar
    lateinit var seekBarParametro1: SeekBar
    lateinit var seekBarParametro2: SeekBar
    lateinit var seekBarKarnel: SeekBar
    lateinit var seekBarSigma: SeekBar
    lateinit var textViewMindist: TextView
    lateinit var textViewMinRadio: TextView
    lateinit var textViewParametro1: TextView
    lateinit var textViewParametro2: TextView
    lateinit var textViewKarnel: TextView
    lateinit var textViewSigma: TextView
    lateinit var button: Button
    var minDistancia: Double = 21.0;
    var parametro1: Double = 2.0;
    var parametro2: Double = 11.4
    var minRadio: Int = 7
    var karnel: Double = 12.4;
    var sigma: Double = 0.9;
    var imageMat: Mat = Mat()
    var imageMatOriginal: Mat = Mat()
    lateinit var listaPregunta: MutableList<Pregunta>
    lateinit var listaIncisoControl:MutableList<Pregunta>
    var detector: PreguntasDetector = PreguntasDetector()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)
        imageView = findViewById(R.id.imageView)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        button = findViewById(R.id.button)
        button.setOnClickListener {

        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        try {
            var imageUri: Uri;
            if (intent != null) {
                var urlImage = intent.getStringExtra(
                    "URL"
                ).toString()
                imageUri = Uri.parse(urlImage)
                println(imageUri)
                val matrix = Matrix()
                matrix.postRotate(90f)
               var bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                val drawable = resources.getDrawable(R.drawable.test4, null)
                //var bitmap = BitmapFactory.decodeResource(resources, R.drawable.test4)

                bitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap = Bitmap.createScaledBitmap(bitmap, 765, 1020, true)
                imageMatOriginal = detector.convertirBitmapToMat(bitmap)
                imageMat = imageMatOriginal
                inizilializarComponentes()
            }
        } catch (e: Exception) {
            Log.d("ERROR", e.message.toString())
        }

        seekBarminDistancia?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBarminRadio?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBarParametro1?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                parametro1 = (seekBarParametro1.progress.toDouble()) / 10
                if (parametro1 > parametro2) {
                    parametro2 = parametro1
                }
                seekBarParametro2.progress = ((parametro2) * 10).toInt()
                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBarParametro2?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                parametro2 = (seekBarParametro2.progress.toDouble()) / 10
                if (parametro2 < parametro1) {
                    parametro1 = parametro2
                }
                seekBarParametro1.progress = ((parametro1) * 10).toInt()

                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBarKarnel?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        seekBarSigma?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                actualizarDatos()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    fun inizilializarComponentes() {
        textViewParametro1 = findViewById(R.id.textViewParametro1)
        textViewParametro2 = findViewById(R.id.textViewParametro2)
        textViewKarnel = findViewById(R.id.textViewMKarnel)
        textViewSigma = findViewById(R.id.textViewSigma)
        textViewMinRadio = findViewById(R.id.textViewMinRadio)
        textViewMindist = findViewById(R.id.textViewMinDis)
        seekBarminRadio = findViewById(R.id.seekBarMinRadio)
        seekBarminDistancia = findViewById(R.id.seekBarMinDIstancia)
        seekBarParametro1 = findViewById(R.id.seekBarParametro1)
        seekBarParametro2 = findViewById(R.id.seekBarParametro2)
        seekBarKarnel = findViewById(R.id.seekBarKarnel)
        seekBarSigma = findViewById(R.id.seekBarSigma)
        seekBarminRadio.progress = minRadio;
        seekBarminDistancia.progress = ((minDistancia) * 10).toInt()
        seekBarParametro1.progress = ((parametro1) * 10).toInt()
        seekBarParametro2.progress = ((parametro2) * 10).toInt()
        seekBarKarnel.progress = ((karnel)).toInt()
        seekBarSigma.progress = ((sigma) * 10).toInt()
        actualizarDatos()
    }

    fun actualizarDatos() {
        minRadio = seekBarminRadio.progress;
        minDistancia = (seekBarminDistancia.progress.toDouble()) / 10
        karnel = (seekBarKarnel.progress.toDouble())
        sigma = (seekBarSigma.progress.toDouble()) / 10
        textViewMinRadio.setText("" + minRadio)
        textViewMindist.setText("" + minDistancia)
        textViewParametro1.setText("" + parametro1)
        textViewParametro2.setText("" + parametro2)
        textViewKarnel.setText("" + karnel)
        textViewSigma.setText("" + sigma)
        generarResultado(
            imageMatOriginal,
            minDistancia,
            parametro1,
            parametro2,
            minRadio,
            sigma
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        val upIntent = NavUtils.getParentActivityIntent(this)
        upIntent?.let {
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this)
                    .addNextIntentWithParentStack(upIntent)
                    .startActivities()
            } else {
                NavUtils.navigateUpTo(this, upIntent)
            }
            return true
        }
        return super.onSupportNavigateUp()
    }

    @SuppressLint("SuspiciousIndentation")
    fun generarResultado(
        image: Mat,
        minDistancia: Double,
        parametro1: Double,
        parametro2: Double,
        minRadio: Int,
        sigma: Double
    ) {
        var imagenParaMostrarPreguntas: Mat = Mat()
        var imagenParaMostrarControl: Mat = Mat()
        var imagenConPreguntas = recortarImagenPregunta(image)
        var imagenconControl = recortarImagenDeId(image)
        var cordenadasCirculosPreguntas =
            detector.detectarCirculosPreguntas(
                imagenConPreguntas,
                minDistancia,
                parametro1,
                parametro2,
                minRadio,
                sigma
            )
        var cooredenadasControl = detector.detectarCirculosPreguntas(
            imagenconControl,
            minDistancia,
            parametro1,
            parametro2,
            minRadio,
            sigma
        )
        if (cooredenadasControl != null) {
            var listaControl = detector.detectarIncisosControl(cooredenadasControl)
            var imagenconFiltroControl = detector.filtrodefinitivo(imagenconControl)
            listaIncisoControl=detector.idetificarRespuestas(imagenconFiltroControl,listaControl)
        }
        if (cordenadasCirculosPreguntas != null) {
            var listaPreguntas = detector.detectarIncisosPreguntas(cordenadasCirculosPreguntas)
            var imagenConFiltroPreguntas = detector.filtrodefinitivo(imagenConPreguntas)
            listaPregunta = detector.idetificarRespuestas(imagenConFiltroPreguntas, listaPreguntas)
        }
      var resultado=  dibujarEnImagen(image,listaIncisoControl,listaPregunta)

        dibujar(resultado)
    }


    fun dibujar(image: Mat) {
        val imagenAdibujar: Mat = image.clone()
        var bitmap = detector.convertirMatToBitMap(imagenAdibujar)
        imageView.setImageBitmap(bitmap)
    }


    companion object {
        var verde: Scalar = Scalar(0.0, 255.0, 0.0)
        var rojo: Scalar = Scalar(255.0, 0.0, 0.0)
        var negro: Scalar = Scalar(0.0, 0.0, 0.0)
        fun recortarImagenDeId(image: Mat): Mat {
            var imageCLone = image.clone()
            val alto = image.rows()
            val ancho = image.cols()
            val corte = (alto * 0.25).toInt()
            val corteHorizontal = (ancho * (1.0 / 7.0)).toInt()
            val rectSuperior = Rect(corteHorizontal, 0, ancho - 2 * corteHorizontal, corte)
            val parteSuperior = Mat(imageCLone, rectSuperior)
            return parteSuperior
        }
        fun recortarImagenPregunta(image: Mat): Mat {
            var imageCLone = image.clone()
            var recorte: Mat = Mat()
            val alto = image.rows()
            val ancho = image.cols()
            val corteVertical = (alto * 0.25).toInt()
            val corteVerticalSuperios = (alto * 0.20).toInt()
            val corteHorizontal = (ancho * (1.0 / 7.0)).toInt()
            val rectInferior = Rect(
                corteHorizontal,
                corteVertical,
                ancho - 2 * corteHorizontal,
                alto - 2 * corteVerticalSuperios
            )
            val parteInferior = Mat(imageCLone, rectInferior)
            return parteInferior
        }

        fun dibujarPreguntas(lista: MutableList<Pregunta>, image: Mat): Mat {
            var imageRsultado=image.clone()
            lista.forEach {
                var numero = it.numero
                it.incisos.forEach {
                    var centro: Point = Point(it.X, it.Y)
                    if (it.Contestada) {
                        if(it.correcta){
                            Imgproc.circle(imageRsultado, centro, it.size, verde )

                        }else{
                            Imgproc.circle(imageRsultado, centro, it.size, rojo,  )
                        }

                    } else {
                        Imgproc.circle(imageRsultado, centro, it.size, negro, 2)
                    }
                }
                Imgproc.putText(
                    imageRsultado, "" + it.numero, Point(
                        (it.incisos[0].X - 40) as Double,
                        it.incisos[0].Y as Double
                    ), 1, 1.0, rojo, 2
                )
            }
            return imageRsultado
        }

    }

    fun dibujarEnImagen(
        image: Mat,
        control: MutableList<Pregunta>,
        listaPreguntas: MutableList<Pregunta>
    ): Mat {
        var imagen = image.clone()
        var imagenControl = recortarImagenDeId(imagen)
        var imagenPreguntas = recortarImagenPregunta(imagen)
        imagenPreguntas = dibujarPreguntas(listaPreguntas, imagenPreguntas)
        imagenControl = dibujarPreguntas(control, imagenControl)
        val destino = Mat()
        val origen = ArrayList<Mat>()
        origen.add(imagenControl)
        origen.add(imagenPreguntas)
        Core.vconcat(origen, destino)
        return destino
    }

    fun guardarPreguntas(control: MutableList<Pregunta>, listaPreguntas: MutableList<Pregunta>) {

    }
}