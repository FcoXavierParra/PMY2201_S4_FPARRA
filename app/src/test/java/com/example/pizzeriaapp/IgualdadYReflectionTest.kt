package com.example.pizzeriaapp

import com.example.pizzeriaapp.modelo.Combo
import com.example.pizzeriaapp.modelo.IngredienteExtra
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.modelo.Registro
import com.example.pizzeriaapp.utils.describirClase
import com.example.pizzeriaapp.utils.promocionDe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IgualdadYReflectionTest {

    @Test
    fun `productos con el mismo nombre y categoria son iguales`() {
        val a = pizza("Pepperoni", 8990)
        val b = pizza("PEPPERONI", 9990)
        assertEquals(a, b)
        assertNotSame(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals<Producto>(a, IngredienteExtra("Pepperoni", 1290, "Pepperoni"))
    }

    @Test
    fun `clientes con el mismo nombre y correo son iguales`() {
        assertEquals(cliente("Ana Pérez", "ana@mail.com"), cliente("ANA PÉREZ", "Ana@Mail.com"))
        assertNotEquals(cliente("Ana Pérez", "ana@mail.com"), cliente("Ana Pérez", "otra@mail.com"))
    }

    @Test
    fun `el registro no admite duplicados y agrupa productos iguales`() {
        val catalogo = Registro<Producto>()
        assertTrue(catalogo.agregar(pizza("Pepperoni")))
        assertFalse(catalogo.agregar(pizza("pepperoni")))
        assertEquals(1, catalogo.cantidad)

        val pedido = Pedido(cliente(), listOf(pizza("Pepperoni"), pizza("PEPPERONI")), FECHA_EN_PROMOCION)
        assertEquals(2, pedido.detalleProductos().values.single())
    }

    @Test
    fun `la anotacion se lee con reflection en las clases marcadas`() {
        assertEquals(15, promocionDe(pizzaEspecial())?.porcentaje)
        assertEquals(10, promocionDe(Combo("Combo", 15000, listOf(pizza())))?.porcentaje)
        assertNull(promocionDe(pizza()))
    }

    @Test
    fun `reflection lista propiedades y metodos de Pedido`() {
        val descripcion = describirClase(Pedido::class).joinToString("\n")
        assertTrue("productos: List<Producto>" in descripcion)
        assertTrue("plus(otro: Pedido): Pedido [operator]" in descripcion)
        assertTrue("component3(): Int [operator]" in descripcion)
    }
}
