package com.example.pizzeriaapp.modelo

class Repartidor(
    nombre: String,
    telefono: String,
    correo: String,
    val zona: String,
    val distanciaKm: Double
) : Usuario(nombre, telefono, correo) {

    var disponible: Boolean = true
        private set

    fun tomarPedido(pedido: Pedido) {
        check(disponible) { "$nombre está en ruta y no puede tomar el pedido #${pedido.id}" }
        pedido.asignarRepartidor(this)
        disponible = false
    }

    // Solo entrega pedidos asignados a este repartidor que estén en camino
    fun entregarPedido(pedido: Pedido) {
        check(pedido.repartidor === this) { "el pedido #${pedido.id} no está asignado a $nombre" }
        check(pedido.estado == EstadoPedido.EN_CAMINO) {
            "el pedido #${pedido.id} está ${pedido.estado.etiqueta.lowercase()}, no en camino"
        }
        pedido.cambiarEstado(EstadoPedido.ENTREGADO)
        disponible = true
    }
}
