package com.example.pizzeriaapp.modelo

// Map ingrediente -> cantidad disponible, encapsulado en la clase
class Inventario(stockInicial: Map<String, Int>) {
    private val stock: MutableMap<String, Int> = stockInicial.toMutableMap()

    fun disponible(ingrediente: String): Int = stock[ingrediente] ?: 0

    fun existencias(): Map<String, Int> = stock.toMap()

    fun verificar(necesarios: Map<String, Int>): List<EstadoIngrediente> =
        necesarios.map { (ingrediente, cantidad) -> EstadoIngrediente(ingrediente, cantidad, disponible(ingrediente)) }

    // Todo o nada: primero comprueba todos los ingredientes y solo entonces modifica el stock
    fun descontar(necesarios: Map<String, Int>) {
        val faltantes = verificar(necesarios).filter { it.disponibilidad != Disponibilidad.SUFICIENTE }
        check(faltantes.isEmpty()) {
            "stock insuficiente de ${faltantes.joinToString(", ") { it.ingrediente }}: no se descontó ningún ingrediente"
        }
        for ((ingrediente, cantidad) in necesarios) {
            stock[ingrediente] = disponible(ingrediente) - cantidad
        }
    }

    fun reponer(ingrediente: String, cantidad: Int) {
        if (cantidad < 0) throw IllegalArgumentException("La cantidad a reponer no puede ser negativa ($cantidad)")
        stock[ingrediente] = disponible(ingrediente) + cantidad
    }
}
