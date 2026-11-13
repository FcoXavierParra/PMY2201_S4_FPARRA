package com.example.pizzeriaapp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

// Sin valores de relleno para correo, teléfono ni zona: un dato inválido se informa y se vuelve a pedir
const val CODIGO_PAIS_POR_DEFECTO = "56"

val ZONAS_CON_COBERTURA: Set<String> = setOf("Norte", "Sur", "Centro", "Oriente", "Poniente")

// STRICT + uuuu rechaza fechas inexistentes como 31-02-2026
val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)

// nombre@dominio.com:
//  - usuario: segmentos separados por un punto, sin punto al inicio, al final ni dos seguidos
//  - dominio: segmentos que empiezan y terminan en letra o número (guiones solo al medio)
//  - extensión de al menos 2 letras
private const val SEGMENTO_USUARIO = "[a-z0-9_%+-]+"
private const val SEGMENTO_DOMINIO = "[a-z0-9]+(-+[a-z0-9]+)*"
private val PATRON_CORREO =
    Regex("^$SEGMENTO_USUARIO(\\.$SEGMENTO_USUARIO)*@$SEGMENTO_DOMINIO(\\.$SEGMENTO_DOMINIO)*\\.[a-z]{2,}$")
// Solo dígitos, espacios, guiones, paréntesis y un + inicial
private val PATRON_TELEFONO_ENTRADA = Regex("^\\+?[\\d\\s()-]+$")
// Código de país (2) + área (3) + central (3) + número (3 o 4)
private val PATRON_TELEFONO_DIGITOS = Regex("^(\\d{2})(\\d{3})(\\d{3})(\\d{3,4})$")
private val PATRON_FECHA = Regex("^\\d{2}-\\d{2}-\\d{4}$")

// Devuelve el correo normalizado o null si no cumple el formato
fun validarCorreo(correo: String?): String? {
    val normalizado = correo?.trim()?.lowercase() ?: return null
    return normalizado.takeIf { PATRON_CORREO.matches(it) }
}

// Formato uniforme +XX (XXX) XXX-XXXX. Sin código de país se asume +56.
// Los números chilenos tienen 9 dígitos, así que el último grupo queda con 3: +56 (912) 345-678
fun formatearTelefono(telefono: String?): String? {
    val texto = telefono?.trim() ?: return null
    if (!PATRON_TELEFONO_ENTRADA.matches(texto)) return null
    val digitos = texto.filter { it.isDigit() }
    val completo = if (digitos.length in 9..10) CODIGO_PAIS_POR_DEFECTO + digitos else digitos
    val (pais, area, central, numero) = PATRON_TELEFONO_DIGITOS.matchEntire(completo)?.destructured ?: return null
    return "+$pais ($area) $central-$numero"
}

// dd-mm-aaaa -> LocalDate; null si no cumple el patrón o la fecha no existe
fun parsearFecha(texto: String): LocalDate? {
    if (!PATRON_FECHA.matches(texto)) return null
    return try {
        LocalDate.parse(texto, FORMATO_FECHA)
    } catch (e: DateTimeParseException) {
        null
    }
}

// Texto -> entero dentro del rango. Lanza NumberFormatException (no es número) o IllegalArgumentException (negativo o fuera de rango)
fun convertirEntero(texto: String, rango: IntRange): Int {
    val numero = texto.trim().toInt()
    if (numero < 0) throw IllegalArgumentException("El valor no puede ser negativo ($numero).")
    if (numero !in rango) throw IllegalArgumentException("$numero está fuera del rango permitido (${rango.first}-${rango.last}).")
    return numero
}

// "  ana   PÉREZ " -> "Ana Pérez"
fun formatearNombre(nombre: String?): String =
    nombre?.trim()
        ?.split(Regex("\\s+"))
        ?.joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }
        ?.ifEmpty { null }
        ?: "Cliente sin nombre"

// Zona con su nombre oficial, o null si no tiene cobertura: no se reemplaza en silencio por otra,
// quien llama debe informar el problema y pedir una zona válida
fun validarZona(zona: String?): String? =
    ZONAS_CON_COBERTURA.find { it.equals(zona?.trim(), ignoreCase = true) }
