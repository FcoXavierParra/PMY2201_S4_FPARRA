package com.example.pizzeriaapp.modelo

// Agregado que se cobra aparte y descuenta una unidad del insumo indicado
class IngredienteExtra(nombre: String, precio: Int, insumo: String) :
    Producto(nombre, precio, Categoria.INGREDIENTE, mapOf(insumo to 1))
