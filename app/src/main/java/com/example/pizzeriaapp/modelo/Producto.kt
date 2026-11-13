package com.example.pizzeriaapp.modelo

// Base de todo lo que se vende. insumos: ingrediente del inventario -> unidades por producto
abstract class Producto(
    val nombre: String,
    val precio: Int,
    val categoria: Categoria,
    val insumos: Map<String, Int>
) {
    init {
        require(precio > 0) { "El precio de $nombre debe ser mayor a 0" }
    }

    // Cuántas pizzas aporta el producto al descuento por volumen
    open fun unidadesPizza(): Int = 0

    // Lo que contiene el producto (ingredientes de una pizza o productos de un combo)
    open fun composicion(): List<String> = emptyList()

    // == llama a equals(): dos productos son el mismo si coinciden nombre (sin importar mayúsculas),
    // categoría y clase concreta. La clase distingue una PizzaEspecial (con promoción) de una Pizza
    // homónima, para que no se agrupen ni se pierda el descuento que corresponde a cada una.
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto || other.javaClass != javaClass) return false
        return nombre.equals(other.nombre, ignoreCase = true) && categoria == other.categoria
    }

    final override fun hashCode(): Int =
        31 * (31 * nombre.lowercase().hashCode() + categoria.hashCode()) + javaClass.name.hashCode()

    override fun toString(): String = "$nombre (${categoria.etiqueta})"
}
