package com.example.pizzeriaapp.modelo

// Resultado de comparar lo que necesita un pedido con lo que hay en el inventario
data class EstadoIngrediente(val ingrediente: String, val necesario: Int, val disponible: Int) {

    val disponibilidad: Disponibilidad
        get() = when {
            disponible >= necesario -> Disponibilidad.SUFICIENTE
            disponible == 0 -> Disponibilidad.AGOTADO
            else -> Disponibilidad.INSUFICIENTE
        }

    val faltante: Int
        get() = (necesario - disponible).coerceAtLeast(0)
}
