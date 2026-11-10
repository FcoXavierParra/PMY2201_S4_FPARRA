package com.example.pizzeriaapp.modelo

import com.example.pizzeriaapp.utils.Totales
import com.example.pizzeriaapp.utils.calcularTotales
import java.time.LocalDate

class Pedido(
    val cliente: Cliente,
    val productos: List<Producto>,
    val fecha: LocalDate,
    val notas: String? = null,
    estadoInicial: EstadoPedido = EstadoPedido.EN_PREPARACION
) {
    val id: Int = siguienteId++

    var estado: EstadoPedido = estadoInicial
        private set

    var repartidor: Repartidor? = null
        private set

    private val historialEstados: MutableList<EstadoPedido> = mutableListOf(estadoInicial)

    fun calcularTotal(): Totales = calcularTotales(productos, fecha)

    // Agrupa los productos repetidos (usa equals/hashCode de Producto): Producto -> cantidad
    fun detalleProductos(): Map<Producto, Int> = productos.groupingBy { it }.eachCount()

    // Solo se avanza al estado siguiente: En preparación → Listo → En camino → Entregado
    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        if (nuevoEstado != estado.siguiente()) {
            throw IllegalStateException("Pedido #$id: no se puede pasar de ${estado.etiqueta} a ${nuevoEstado.etiqueta}")
        }
        estado = nuevoEstado
        historialEstados.add(nuevoEstado)
    }

    fun asignarRepartidor(asignado: Repartidor) {
        cambiarEstado(EstadoPedido.EN_CAMINO)
        repartidor = asignado
    }

    fun historial(): List<EstadoPedido> = historialEstados.toList()

    // pedidoA + pedidoB: nuevo pedido del mismo cliente con todos los productos; el total se recalcula
    operator fun plus(otro: Pedido): Pedido {
        require(cliente == otro.cliente) {
            "solo se combinan pedidos del mismo cliente (${cliente.nombre} y ${otro.cliente.nombre})"
        }
        require(estado == EstadoPedido.EN_PREPARACION && otro.estado == EstadoPedido.EN_PREPARACION) {
            "solo se combinan pedidos que siguen en preparación"
        }
        val notasCombinadas = listOfNotNull(notas, otro.notas).joinToString(" / ").ifEmpty { null }
        return Pedido(cliente, productos + otro.productos, fecha, notasCombinadas)
    }

    // Desestructuración: val (cliente, productos, total) = pedido
    operator fun component1(): Cliente = cliente
    operator fun component2(): List<Producto> = productos
    operator fun component3(): Int = calcularTotal().total

    companion object {
        private var siguienteId = 1001
    }
}
