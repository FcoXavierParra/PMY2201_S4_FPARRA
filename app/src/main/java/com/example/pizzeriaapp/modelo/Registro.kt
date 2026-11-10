package com.example.pizzeriaapp.modelo

// Colección sin duplicados: decide qué es "repetido" con equals() y hashCode() de cada entidad
class Registro<T> {
    private val elementos: MutableSet<T> = linkedSetOf()

    val cantidad: Int
        get() = elementos.size

    // false si ya existía un elemento igual
    fun agregar(elemento: T): Boolean = elementos.add(elemento)

    // Devuelve el elemento ya registrado que es igual al recibido (null si no existe)
    fun buscar(elemento: T): T? = elementos.find { it == elemento }

    fun todos(): List<T> = elementos.toList()
}
