package com.netomix.camara

import android.graphics.Point
import android.net.Uri




data class Inciso (var X:Double=0.0, var Y:Double=0.0, var Op:Int=0, var Contestada:Boolean=false, var size:Int=0, var intencidad:Double=0.0,var correcta:Boolean=false)
data class Pregunta(var incisos: MutableList<Inciso>, var numero:Int)
data class  ExamenAlumno(var NumeroControl:MutableList<Pregunta>,var Respuestas:MutableList<Pregunta>,var examenURI:Uri)