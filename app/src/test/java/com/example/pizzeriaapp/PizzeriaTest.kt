package com.example.pizzeriaapp

import com.example.pizzeriaapp.datos.DatosIniciales
import com.example.pizzeriaapp.modelo.Disponibilidad
import com.example.pizzeriaapp.modelo.EstadoPedido
import com.example.pizzeriaapp.modelo.Inventario
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Registro
import com.example.pizzeriaapp.modelo.Repartidor
import com.example.pizzeriaapp.servicio.Pizzeria
import kotlin.test.Test
import com.example.pizzeriaapp.modelo.Cliente
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PizzeriaTest {

    private fun repartidor(nombre: String, zona: String, distanciaKm: Double) =
        Repartidor(nombre, "+56 (900) 000-000", "r@pizzeria.cl", zona, distanciaKm)

    private fun pizzeria(stock: Map<String, Int>, repartidores: List<Repartidor> = emptyList()) =
        Pizzeria(listOf(pizza()), Inventario(stock), repartidores, Registro())

    @Test
    fun `los pedidos previos descuentan su consumo del stock inicial`() {
        val datos = DatosIniciales()
        // Stock inicial de masa 25; los 3 pedidos previos usan 5 pizzas
        assertEquals(20, datos.pizzeria.inventario.disponible("Masa"))
        assertEquals(15, datos.pizzeria.inventario.disponible("Queso"))
    }

    @Test
    fun `un pedido sin stock suficiente no cambia el inventario ni el estado`() {
        val pizzeria = pizzeria(mapOf("Masa" to 5, "Queso" to 1))
        val pedido = Pedido(cliente(), List(2) { pizza() }, FECHA_EN_PROMOCION)
        val detalle = pizzeria.prepararPedido(pedido)
        assertEquals(Disponibilidad.INSUFICIENTE, detalle.first { it.ingrediente == "Queso" }.disponibilidad)
        assertEquals(5, pizzeria.inventario.disponible("Masa"))
        assertEquals(EstadoPedido.EN_PREPARACION, pedido.estado)
    }

    @Test
    fun `con stock exacto el pedido queda listo y el stock en cero`() {
        val pizzeria = pizzeria(mapOf("Masa" to 2, "Queso" to 2))
        val pedido = Pedido(cliente(), List(2) { pizza() }, FECHA_EN_PROMOCION)
        pizzeria.prepararPedido(pedido)
        assertEquals(EstadoPedido.LISTO, pedido.estado)
        assertEquals(0, pizzeria.inventario.disponible("Queso"))
    }

    @Test
    fun `asignacion prioriza la zona y luego la menor distancia`() {
        val surLejos = repartidor("Sur lejos", "Sur", 3.0)
        val surCerca = repartidor("Sur cerca", "Sur", 1.5)
        val centro = repartidor("Centro", "Centro", 0.5)
        val pizzeria = pizzeria(emptyMap(), listOf(surLejos, centro, surCerca))

        assertSame(surCerca, pizzeria.buscarRepartidor("Sur"))     // misma zona, el más cercano
        assertSame(centro, pizzeria.buscarRepartidor("Norte"))     // nadie en la zona: el más cercano
    }

    @Test
    fun `sin repartidores disponibles no se asigna`() {
        val pizzeria = pizzeria(mapOf("Masa" to 1, "Queso" to 1))
        val pedido = Pedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        pizzeria.prepararPedido(pedido)
        assertNull(pizzeria.despachar(pedido))
        assertEquals(EstadoPedido.LISTO, pedido.estado)
    }

    @Test
    fun `confirmar un pedido registra al cliente, descuenta el stock y lo despacha`() {
        val sur = repartidor("Sur", "Sur", 1.0)
        val pizzeria = pizzeria(mapOf("Masa" to 2, "Queso" to 2), listOf(sur))
        val resultado = pizzeria.confirmarPedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertTrue(resultado.clienteNuevo)
        assertSame(sur, resultado.repartidor)
        assertEquals(EstadoPedido.EN_CAMINO, resultado.pedido.estado)
        assertEquals(1, pizzeria.inventario.disponible("Masa"))
    }

    @Test
    fun `tras reponer los faltantes el mismo pedido se puede volver a procesar`() {
        val pizzeria = pizzeria(mapOf("Masa" to 1, "Queso" to 0), listOf(repartidor("Sur", "Sur", 1.0)))
        val fallido = pizzeria.confirmarPedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertEquals(listOf("Queso"), fallido.faltantes.map { it.ingrediente })
        assertNull(fallido.repartidor)

        fallido.faltantes.forEach { pizzeria.reponer(it.ingrediente, it.faltante) }
        val reintento = pizzeria.procesarPedido(fallido.pedido)
        assertTrue(reintento.faltantes.isEmpty())
        assertEquals(EstadoPedido.EN_CAMINO, reintento.pedido.estado)
    }

    @Test
    fun `despacho fallido sin repartidor no modifica stock ni estado`() {
        val pizzeria = pizzeria(mapOf("Masa" to 2, "Queso" to 2))   // stock alcanza, pero no hay repartidores
        val resultado = pizzeria.confirmarPedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertTrue(resultado.sinRepartidor)
        assertFalse(resultado.despachado)
        assertEquals(EstadoPedido.EN_PREPARACION, resultado.pedido.estado)
        assertEquals(listOf(EstadoPedido.EN_PREPARACION), resultado.pedido.historial())
        assertEquals(2, pizzeria.inventario.disponible("Masa"))
        assertEquals(2, pizzeria.inventario.disponible("Queso"))
    }

    @Test
    fun `un pedido ya despachado no se vuelve a procesar`() {
        val pizzeria = pizzeria(mapOf("Masa" to 2, "Queso" to 2), listOf(repartidor("Sur", "Sur", 1.0)))
        val resultado = pizzeria.confirmarPedido(cliente(), listOf(pizza()), FECHA_EN_PROMOCION)
        assertFailsWith<IllegalStateException> { pizzeria.procesarPedido(resultado.pedido) }
        assertEquals(1, pizzeria.inventario.disponible("Masa"))   // se descontó una sola vez
    }

    @Test
    fun `cliente registrado con datos nuevos actualiza telefono direccion y zona`() {
        val pizzeria = pizzeria(emptyMap())
        val original = Cliente("Ana Pérez", "+56 (912) 345-678", "ana@mail.com", "Calle 1", "Sur")
        pizzeria.registrarCliente(original)

        val conCambios = Cliente("ANA PÉREZ", "+56 (987) 654-321", "Ana@Mail.com", "Av. Norte 50", "Norte")
        val registro = pizzeria.registrarCliente(conCambios)
        assertFalse(registro.esNuevo)
        assertSame(original, registro.cliente)
        assertEquals(listOf("teléfono", "dirección", "zona"), registro.datosActualizados)
        assertEquals("+56 (987) 654-321", original.telefono)
        assertEquals("Av. Norte 50", original.direccion)
        assertEquals("Norte", original.zona)
        assertEquals("Ana Pérez", original.nombre)   // nombre y correo identifican al cliente y no cambian
        assertEquals(1, pizzeria.cantidadClientes)

        val sinCambios = pizzeria.registrarCliente(conCambios)
        assertTrue(sinCambios.datosActualizados.isEmpty())
    }

    @Test
    fun `registrar un cliente repetido reutiliza el existente`() {
        val pizzeria = pizzeria(emptyMap())
        val primero = pizzeria.registrarCliente(cliente("Ana Pérez", "ana@mail.com"))
        val repetido = pizzeria.registrarCliente(cliente("ANA PÉREZ", "Ana@Mail.com"))
        assertTrue(primero.esNuevo)
        assertFalse(repetido.esNuevo)
        assertSame(primero.cliente, repetido.cliente)
        assertEquals(1, pizzeria.cantidadClientes)
    }
}
