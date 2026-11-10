package com.example.pizzeriaapp.modelo

import com.example.pizzeriaapp.utils.CORREO_POR_DEFECTO

class Cliente(
    nombre: String,
    telefono: String,
    correo: String,
    val direccion: String,
    val zona: String
) : Usuario(nombre, telefono, correo) {

    // Solo los clientes con correo válido reciben notificaciones (null si no tiene)
    val correoNotificaciones: String?
        get() = correo.takeIf { it != CORREO_POR_DEFECTO }

    // Desestructuración: val (nombre, correo, telefono) = cliente
    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono

    // Mismo cliente = mismo nombre y mismo correo, sin importar mayúsculas
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return nombre.equals(other.nombre, ignoreCase = true) && correo.equals(other.correo, ignoreCase = true)
    }

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + correo.lowercase().hashCode()

    override fun toString(): String = "$nombre <$correo>"
}
