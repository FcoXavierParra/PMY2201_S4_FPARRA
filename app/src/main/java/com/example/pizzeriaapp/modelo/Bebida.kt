package com.example.pizzeriaapp.modelo

// La bebida se descuenta del inventario con su propio nombre
class Bebida(nombre: String, precio: Int) :
    Producto(nombre, precio, Categoria.BEBIDA, mapOf(nombre to 1))
