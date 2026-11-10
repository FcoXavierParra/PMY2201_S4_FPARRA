package com.example.pizzeriaapp

import com.example.pizzeriaapp.utils.formatearDescuento
import com.example.pizzeriaapp.utils.formatearPesos
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatoTest {

    @Test
    fun `montos con separador de miles`() {
        assertEquals("$990", formatearPesos(990))
        assertEquals("$59.391", formatearPesos(59391))
        assertEquals("$1.234.567", formatearPesos(1234567))
    }

    @Test
    fun `descuentos con signo negativo o cero`() {
        assertEquals("-$5.497", formatearDescuento(5497))
        assertEquals("$0", formatearDescuento(0))
    }
}
