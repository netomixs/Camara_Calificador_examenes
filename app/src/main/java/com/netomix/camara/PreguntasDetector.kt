package com.netomix.camara

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

class PreguntasDetector {
    fun detectarCirculosPreguntas(
        image: Mat,
        minDistancia: Double,
        parametro1: Double,
        parametro2: Double,
        minRadio: Int,
        sigma:Double
    ): Mat? {
        val circles = Mat()
        var imagenBlur: Mat = Mat()
        var imagenGris =  imageMatToGray(image)
        Imgproc.GaussianBlur(imagenGris, imagenBlur, Size(9.0, 9.0), sigma, sigma)
        Imgproc.HoughCircles(
            imagenBlur, circles, Imgproc.HOUGH_GRADIENT, 1.0,
            minDistancia, parametro1, parametro2, minRadio, minRadio + 1
        )
        if (circles.empty()) {
            return null
        }
        return circles
    }
    fun convertirBitmapToMat(bitmap: Bitmap): Mat {
        val imagenMat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
        Utils.bitmapToMat(bitmap, imagenMat)
        return imagenMat
    }
    fun imageMatToGray(image: Mat): Mat {
        val imageGray = Mat()
        Imgproc.cvtColor(image.clone(), imageGray, Imgproc.COLOR_BGR2GRAY)
        return imageGray
    }
    fun detectarIncisosControl(circulos: Mat): MutableList<Pregunta> {
        var lista: MutableList<Inciso>
        var listaDePreguntas: MutableList<Pregunta>
        listaDePreguntas =
            MutableList(4) { Pregunta(MutableList<Inciso>(10) { Inciso(0.0, 0.0, 0) }, 0) }
        lista = mutableListOf()
        try {
            if (circulos != null) {
                for (i in 0 until circulos.cols()) {
                    val circleVec = circulos[0, i]
                    val center = Point(circleVec[0], circleVec[1])
                    val radius = circleVec[2].toInt()
                    var inciso: Inciso
                    inciso = Inciso(circleVec[0], circleVec[1], 0, false, radius)
                    lista.add(inciso)
                }
                var listaDeIncisios: MutableList<Inciso>
                listaDeIncisios = lista.sortedBy { it.Y }.toMutableList()
                val sublistas = listaDeIncisios.chunked(10).toMutableList()
                var generacion= sublistas[0].sortedBy { it.X }.toMutableList()
                var num1= sublistas[1].sortedBy { it.X }.toMutableList()
                var num2= sublistas[2].sortedBy { it.X }.toMutableList()
                var num3= sublistas[3].sortedBy { it.X }.toMutableList()
                listaDePreguntas.set(0, Pregunta(generacion,0))
                listaDePreguntas.set(1,Pregunta(num1,1))
                listaDePreguntas.set(2,Pregunta(num2,2))
                listaDePreguntas.set(3,Pregunta(num3,3))
                for (i in 0..sublistas[0].size-1){
                    listaDePreguntas[0].incisos[i].Op=i
                    listaDePreguntas[1].incisos[i].Op=i
                    listaDePreguntas[2].incisos[i].Op=i
                    listaDePreguntas[3].incisos[i].Op=i
                }
                listaDePreguntas.forEach { Log.d("Preguntas", "Numero" + it.numero) }
                Log.d("MENSAJE", "" + lista.size)
            } else {
                Log.d("ERROR", "")

            }
        } catch (e: Exception) {
            Log.d("ERROR", e.message.toString())
        }
        return listaDePreguntas
    }

    @SuppressLint("SuspiciousIndentation")
    fun obtenerNumeroControl(image: Mat, lista:MutableList<Pregunta> ):String{
        var noControl=""
        var listaControl=idetificarRespuestas(image,lista)
            listaControl.forEach {
                if(it.numero==0){
                    it.incisos.forEach{
                        if(it.Contestada){
                            noControl=noControl+(it.Op+16)

                        }
                    }
                }else{
                    it.incisos.forEach{
                        if(it.Contestada){
                            noControl=noControl+it.Op

                        }
                    }
                }
            }
        return  noControl
    }
    @SuppressLint("SuspiciousIndentation")
    fun detectarIncisosPreguntas(circulos: Mat): MutableList<Pregunta> {
        Log.d("MENSAJE", "HOLA")
        var lista: MutableList<Inciso>
        var listaDePreguntas: MutableList<Pregunta>
        listaDePreguntas =
            MutableList(60) { Pregunta(MutableList<Inciso>(4) { Inciso(0.0, 0.0, 0) }, 0) }
        lista = mutableListOf()
        try {
            if (circulos != null) {
                for (i in 0 until circulos.cols()) {
                    val circleVec = circulos[0, i]
                    val center = Point(circleVec[0], circleVec[1])
                    val radius = circleVec[2].toInt()
                    var inciso: Inciso
                    inciso = Inciso(circleVec[0], circleVec[1], 0, false, radius)
                    lista.add(inciso)
                }
                var listaDeIncisios: MutableList<Inciso>
                listaDeIncisios = lista.sortedBy { it.Y }.toMutableList()

                var index = 0;
                val sublistas = listaDeIncisios.chunked(12).toMutableList()
                sublistas.forEach {
                    var listaIncisosMutable = it.toMutableList()
                    listaIncisosMutable = listaIncisosMutable.sortedBy { it.X }.toMutableList()
                    var preguntaIncisosList = listaIncisosMutable.chunked(4)
                    var listaPreguntas1 = preguntaIncisosList[0].toMutableList()
                    var listaPreguntas2 = preguntaIncisosList[1].toMutableList()
                    var listaPreguntas3 = preguntaIncisosList[2].toMutableList()
                    for (k in 0..listaPreguntas1.size - 1) {
                        listaPreguntas1[k].Op = k
                        listaPreguntas2[k].Op = k
                        listaPreguntas3[k].Op = k
                    }
                    var pregunta =
                        Pregunta(listaPreguntas1, index)
                    var pregunta2 =
                        Pregunta(listaPreguntas2.toMutableList(), index + 20)
                    var pregunta3 =
                        Pregunta(listaPreguntas3, index + 40)
                    listaDePreguntas.set(pregunta.numero, pregunta)
                    listaDePreguntas.set(pregunta2.numero, pregunta2)
                    listaDePreguntas.set(pregunta3.numero, pregunta3)
                    index++
                }
                listaDePreguntas.forEach { Log.d("Preguntas", "Numero" + it.numero) }
                Log.d("MENSAJE", "" + lista.size)
            } else {
                Log.d("ERROR", "")

            }
        } catch (e: Exception) {
            Log.d("ERROR", e.message.toString())
        }
        return listaDePreguntas
    }
    fun idetificarRespuestas(image: Mat, lista: MutableList<Pregunta>): MutableList<Pregunta> {
        for (i in 0..lista.size - 1) {
            lista[i] = estaContestada( (image), lista[i])
        }
        return lista
    }

    fun estaContestada(image: Mat, pregunta: Pregunta): Pregunta {
        pregunta.incisos.forEach {
           incisoContestado(image, it)
        }
        return pregunta
    }
    fun incisoContestado(image: Mat, inciso: Inciso) {
        try {
            var x = inciso.X - inciso.size
            var y = inciso.Y - inciso.size
            var seccion = Mat(image, Rect(x.toInt(), y.toInt(), inciso.size * 2, inciso.size * 2))
            val intecidad = Core.mean(seccion).`val`[0]
            inciso.intencidad = intecidad
            if( inciso.intencidad<100){
                inciso.Contestada=true
            }

        } catch (e: Exception) {

        }

    }
    fun aplicarUmbral(image: Mat, umbral:Double): Mat {
        var imagen=image.clone()

        Imgproc.threshold(imagen, imagen, umbral, 255.0, Imgproc.THRESH_BINARY)
        return  imagen
    }
    fun autoAjustar(list: MutableList<Pregunta>): MutableList<Pregunta> {

        var XInicial = list[0].incisos[0].X
        var YInicial = list[0].incisos[0].Y
        var rangoX = 185;
        var rangoY = 30;
        var numIncisos = 3;
        var size = list[0].incisos[0].size
        for (i in 0..list.size - 1) {
            list[i].numero = i
            if (i < 20) {

                for (j in 0..numIncisos) {
                    list[i].incisos[j].size = size
                    list[i].incisos[j].Op = j
                    list[i].incisos[j].X = XInicial + (j * 30)
                    list[i].incisos[j].Y = YInicial + (i * rangoY)
                }
            }
            if (i > 20 && i < 40) {
                for (j in 0..numIncisos) {
                    list[i].incisos[j].size = size
                    list[i].incisos[j].Op = j
                    list[i].incisos[j].X = XInicial + (j * 30) + 185
                    list[i].incisos[j].Y = YInicial + ((i - 20) * rangoY)
                }
            }
            if (i > 60) {
                for (j in 0..numIncisos) {
                    list[i].incisos[j].size = size
                    list[i].incisos[j].Op = j
                    list[i].incisos[j].X = XInicial + (j * 30) + (2 * 185)
                    list[i].incisos[j].Y = YInicial + ((i - 40) * rangoY)
                }
            }
        }
        return list
    }
    fun alinearPuntos(imagen: Mat, puntosOriginales: Array<Point>, puntosAlineados: Array<Point>): Mat {
        // Crear dos matrices de puntos de 2x3 para los puntos originales y los puntos alineados
        val srcMat = Mat(2, 3, CvType.CV_32F, Scalar(0.0))
        val dstMat = Mat(2, 3, CvType.CV_32F, Scalar(0.0))

        // Llenar las matrices con los puntos
        for (i in 0..2) {
            srcMat.put(0, i, puntosOriginales[i].x)
            srcMat.put(1, i, puntosOriginales[i].y)
            dstMat.put(0, i, puntosAlineados[i].x)
            dstMat.put(1, i, puntosAlineados[i].y)
        }

        // Calcular la matriz de transformación afín
        val transformacion = Imgproc.getAffineTransform(srcMat as MatOfPoint2f?,
            dstMat as MatOfPoint2f?
        )

        // Crear una imagen de salida del mismo tamaño que la imagen de entrada
        val imagenAlineada = Mat()

        // Aplicar la transformación afín a la imagen
        Imgproc.warpAffine(imagen, imagenAlineada, transformacion, imagen.size())

        // Retornar la imagen con los puntos alineados
        return imagenAlineada
    }

    fun eliminarBordesFinos(imagen: Mat): Mat {
        // Crear una imagen de salida del mismo tamaño que la imagen de entrada
        val imagenSinBordes = Mat()

        // Crear un elemento estructurante para la erosión
        val elementoEstructurante = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))

        // Aplicar la erosión a la imagen
        Imgproc.erode(imagen, imagenSinBordes, elementoEstructurante)

        // Retornar la imagen sin bordes finos
        return imagenSinBordes
    }
    fun filtrodefinitivo(image: Mat): Mat {
        var imageResult= Mat()
        imageResult=image.clone()
        imageResult=imageMatToGray(imageResult)
        Imgproc.adaptiveThreshold(imageResult, imageResult, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 2.0)
    return  imageResult
    }
  /*  fun compararPreguntas(respuestaCorrecta: MutableList<RespuestaCorrecta>,respuestasAlumno:MutableList<Pregunta>):MutableList<Resultados> {
        var resultado: MutableList<Resultados>
        resultado = mutableListOf()
        for (i in 0..respuestaCorrecta.size - 1) {
            resultado.add(Resultados(i, false))
            var numeroContestada = 0;
            for (j in 0..3) {
                if (respuestasAlumno[i].incisos[j].Contestada) {
                    numeroContestada++
                    if (respuestaCorrecta[i].correcta == respuestasAlumno[i].incisos[j].Op) {
                        resultado[i].correcta = true
                    } else {
                        resultado[i].correcta = false
                    }
                }
            }
            if (numeroContestada > 1) {
                resultado[i].correcta = false
            }
        }
        return resultado
    }*/
    fun convertirMatToBitMap(imageMat: Mat): Bitmap {
        val imageBitmap =
            Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, imageBitmap)
        return imageBitmap
    }
}