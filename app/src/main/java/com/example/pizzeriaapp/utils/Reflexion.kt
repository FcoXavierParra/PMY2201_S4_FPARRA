package com.example.pizzeriaapp.utils

import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.modelo.Promocionable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

// Lee @Promocionable en la clase real del producto (null si no es promocionable)
fun promocionDe(producto: Producto): Promocionable? = producto::class.findAnnotation<Promocionable>()

// Lista propiedades y métodos de una clase en tiempo de ejecución
fun describirClase(clase: KClass<*>): List<String> {
    val propiedades = clase.memberProperties
        .sortedBy { it.name }
        .map { propiedad ->
            val privada = if (propiedad.visibility != KVisibility.PUBLIC) " [${propiedad.visibility?.name?.lowercase()}]" else ""
            "${propiedad.name}: ${nombreTipo(propiedad.returnType)}$privada"
        }
    val metodos = clase.memberFunctions
        .sortedBy { it.name }
        .map { funcion ->
            val parametros = funcion.parameters.drop(1).joinToString(", ") { "${it.name}: ${nombreTipo(it.type)}" }
            val operador = if (funcion.isOperator) " [operator]" else ""
            "${funcion.name}($parametros): ${nombreTipo(funcion.returnType)}$operador"
        }

    val lineas = mutableListOf("Clase ${clase.simpleName} · ${propiedades.size} propiedades · ${metodos.size} métodos")
    lineas += "  Propiedades:"
    propiedades.forEach { lineas += "    • $it" }
    lineas += "  Métodos:"
    metodos.forEach { lineas += "    • $it" }
    return lineas
}

// "kotlin.collections.List<modelo.Producto>" -> "List<Producto>"
private fun nombreTipo(tipo: KType): String = tipo.toString().replace(Regex("\\b[a-z]+(\\.[a-z]+)*\\."), "")
