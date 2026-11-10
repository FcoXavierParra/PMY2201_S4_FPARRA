package com.example.pizzeriaapp

import com.example.pizzeriaapp.modelo.Disponibilidad
import com.example.pizzeriaapp.modelo.Inventario
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InventarioTest {

    @Test
    fun `stock exacto alcanza y queda en cero`() {
        val inventario = Inventario(mapOf("Queso" to 2))
        assertEquals(Disponibilidad.SUFICIENTE, inventario.verificar(mapOf("Queso" to 2)).single().disponibilidad)
        inventario.descontar(mapOf("Queso" to 2))
        assertEquals(0, inventario.disponible("Queso"))
    }

    @Test
    fun `clasifica insuficiente y agotado`() {
        val detalle = Inventario(mapOf("Queso" to 1, "Piña" to 0)).verificar(mapOf("Queso" to 3, "Piña" to 1))
        assertEquals(Disponibilidad.INSUFICIENTE, detalle[0].disponibilidad)
        assertEquals(2, detalle[0].faltante)
        assertEquals(Disponibilidad.AGOTADO, detalle[1].disponibilidad)
    }

    @Test
    fun `si falta un ingrediente no se descuenta ninguno`() {
        val inventario = Inventario(mapOf("Masa" to 10, "Queso" to 1))
        assertFailsWith<IllegalStateException> { inventario.descontar(mapOf("Masa" to 2, "Queso" to 5)) }
        assertEquals(10, inventario.disponible("Masa"))
        assertEquals(1, inventario.disponible("Queso"))
    }

    @Test
    fun `reponer cantidades negativas se rechaza`() {
        val inventario = Inventario(mapOf("Queso" to 1))
        assertFailsWith<IllegalArgumentException> { inventario.reponer("Queso", -5) }
        inventario.reponer("Queso", 4)
        assertEquals(5, inventario.disponible("Queso"))
    }
}
