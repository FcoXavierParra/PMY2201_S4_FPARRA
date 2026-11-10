package com.example.pizzeriaapp

import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.EstadoPedido
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Repartidor
import com.example.pizzeriaapp.utils.CORREO_POR_DEFECTO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PedidoTest {

    private fun repartidor(nombre: String) = Repartidor(nombre, "+56 (900) 000-000", "r@pizzeria.cl", "Sur", 1.0)

    @Test
    fun `sumar pedidos del mismo cliente recalcula el descuento por volumen`() {
        val ana = cliente()
        val a = Pedido(ana, List(2) { pizza() }, FECHA_FUERA_PROMOCION)
        val b = Pedido(ana, List(2) { pizza() }, FECHA_FUERA_PROMOCION)
        assertEquals(0, a.calcularTotal().descuentoVolumen)
        val combinado = a + b
        assertEquals(4, combinado.productos.size)
        assertEquals(4000, combinado.calcularTotal().descuentoVolumen)
    }

    @Test
    fun `no se combinan pedidos de clientes distintos`() {
        val a = Pedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        val b = Pedido(cliente("Luis", "luis@mail.com"), listOf(pizza()), FECHA_EN_PROMOCION)
        assertFailsWith<IllegalArgumentException> { a + b }
    }

    @Test
    fun `desestructuracion de cliente y pedido`() {
        val ana = cliente()
        val (nombre, correo, telefono) = ana
        assertEquals(Triple("Ana Pérez", "ana@mail.com", "+56 (912) 345-678"), Triple(nombre, correo, telefono))

        val (clientePedido, productos, total) = Pedido(ana, List(3) { pizza() }, FECHA_FUERA_PROMOCION)
        assertEquals(ana, clientePedido)
        assertEquals(3, productos.size)
        assertEquals(35700, total)
    }

    @Test
    fun `los estados no se pueden saltar`() {
        val pedido = Pedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertFailsWith<IllegalStateException> { pedido.cambiarEstado(EstadoPedido.EN_CAMINO) }
        pedido.cambiarEstado(EstadoPedido.LISTO)
        assertEquals(listOf(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO), pedido.historial())
    }

    @Test
    fun `solo el repartidor asignado entrega y solo si el pedido va en camino`() {
        val pedido = Pedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        val asignado = repartidor("Andrea")
        val otro = repartidor("Carlos")
        pedido.cambiarEstado(EstadoPedido.LISTO)

        assertFailsWith<IllegalStateException> { asignado.entregarPedido(pedido) }   // aún no está asignado
        asignado.tomarPedido(pedido)
        assertFailsWith<IllegalStateException> { otro.entregarPedido(pedido) }      // no es su pedido
        assertFailsWith<IllegalStateException> { asignado.tomarPedido(pedido) }     // ya está en ruta

        asignado.entregarPedido(pedido)
        assertEquals(EstadoPedido.ENTREGADO, pedido.estado)
        assertFailsWith<IllegalStateException> { asignado.entregarPedido(pedido) }   // ya fue entregado
    }

    @Test
    fun `notas ausentes y correo por defecto quedan en null`() {
        val pedido = Pedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertNull(pedido.notas)
        assertNull(pedido.repartidor)
        val sinCorreo = Cliente("Eva", "+56 (911) 111-111", CORREO_POR_DEFECTO, "Calle 1", "Centro")
        assertNull(sinCorreo.correoNotificaciones)
    }
}
