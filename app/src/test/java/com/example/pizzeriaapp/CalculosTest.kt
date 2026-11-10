package com.example.pizzeriaapp

import com.example.pizzeriaapp.modelo.Bebida
import com.example.pizzeriaapp.modelo.Combo
import com.example.pizzeriaapp.utils.calcularTotales
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalculosTest {

    @Test
    fun `tres pizzas no tienen descuento por volumen`() {
        val totales = calcularTotales(List(3) { pizza() }, FECHA_FUERA_PROMOCION)
        assertEquals(30000, totales.subtotal)
        assertEquals(0, totales.descuentoVolumen)
        assertEquals(5700, totales.impuesto)
        assertEquals(35700, totales.total)
    }

    @Test
    fun `cuatro pizzas tienen 10 por ciento de descuento`() {
        val totales = calcularTotales(List(4) { pizza() }, FECHA_FUERA_PROMOCION)
        assertEquals(4000, totales.descuentoVolumen)
        assertEquals(6840, totales.impuesto)   // 19 % de 36.000
        assertEquals(42840, totales.total)
    }

    @Test
    fun `promocion aplica solo a productos promocionables dentro del periodo`() {
        val productos = listOf(pizzaEspecial(), pizza())
        assertEquals(1500, calcularTotales(productos, FECHA_EN_PROMOCION).descuentoPromocion)   // 15 % de 10.000
        assertEquals(0, calcularTotales(productos, FECHA_FUERA_PROMOCION).descuentoPromocion)
    }

    @Test
    fun `los extremos del periodo de promocion estan incluidos`() {
        val productos = listOf(pizzaEspecial())
        assertEquals(1500, calcularTotales(productos, LocalDate.of(2026, 11, 1)).descuentoPromocion)
        assertEquals(1500, calcularTotales(productos, LocalDate.of(2026, 11, 30)).descuentoPromocion)
        assertEquals(0, calcularTotales(productos, LocalDate.of(2026, 10, 31)).descuentoPromocion)
        assertEquals(0, calcularTotales(productos, LocalDate.of(2026, 12, 1)).descuentoPromocion)
    }

    @Test
    fun `un combo aporta sus pizzas al conteo del descuento por volumen`() {
        val combo = Combo("Combo", 20000, listOf(pizza(), pizza(), Bebida("Bebida", 2000)))
        // 2 pizzas del combo + 2 sueltas = 4 → aplica volumen
        val totales = calcularTotales(listOf(combo, pizza(), pizza()), FECHA_FUERA_PROMOCION)
        assertEquals(4000, totales.descuentoVolumen)
    }

    @Test
    fun `un pedido sin productos es un costo no valido`() {
        assertFailsWith<IllegalArgumentException> { calcularTotales(emptyList(), FECHA_EN_PROMOCION) }
    }
}
