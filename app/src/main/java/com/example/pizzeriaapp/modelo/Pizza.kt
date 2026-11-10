package com.example.pizzeriaapp.modelo

open class Pizza(nombre: String, precio: Int, ingredientes: Map<String, Int>) :
    Producto(nombre, precio, Categoria.PIZZA, ingredientes) {

    override fun unidadesPizza(): Int = 1

    override fun composicion(): List<String> = insumos.keys.toList()
}
