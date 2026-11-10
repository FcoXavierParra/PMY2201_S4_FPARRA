package com.example.pizzeriaapp.servicio

import com.example.pizzeriaapp.modelo.Disponibilidad
import com.example.pizzeriaapp.modelo.EstadoIngrediente
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Repartidor

// Lo que la pantalla necesita mostrar después de confirmar un pedido
data class ResultadoPedido(
    val pedido: Pedido,
    val verificacion: List<EstadoIngrediente>,
    val repartidor: Repartidor?,
    val clienteNuevo: Boolean
) {
    val faltantes: List<EstadoIngrediente>
        get() = verificacion.filter { it.disponibilidad != Disponibilidad.SUFICIENTE }
}
