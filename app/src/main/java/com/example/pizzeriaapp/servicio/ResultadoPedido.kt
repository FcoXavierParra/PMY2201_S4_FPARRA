package com.example.pizzeriaapp.servicio

import com.example.pizzeriaapp.modelo.Disponibilidad
import com.example.pizzeriaapp.modelo.EstadoIngrediente
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Repartidor

// Lo que la pantalla necesita mostrar después de procesar un pedido; refleja el estado real del dominio
data class ResultadoPedido(
    val pedido: Pedido,
    val verificacion: List<EstadoIngrediente>,
    val repartidor: Repartidor?,
    val registro: ResultadoRegistro
) {
    val clienteNuevo: Boolean
        get() = registro.esNuevo

    val faltantes: List<EstadoIngrediente>
        get() = verificacion.filter { it.disponibilidad != Disponibilidad.SUFICIENTE }

    val despachado: Boolean
        get() = repartidor != null

    // Stock suficiente pero nadie disponible para llevarlo: no se descontó nada
    val sinRepartidor: Boolean
        get() = faltantes.isEmpty() && repartidor == null
}
