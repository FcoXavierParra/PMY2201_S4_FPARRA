package com.example.pizzeriaapp

import com.example.pizzeriaapp.utils.convertirEntero
import com.example.pizzeriaapp.utils.formatearNombre
import com.example.pizzeriaapp.utils.formatearTelefono
import com.example.pizzeriaapp.utils.parsearFecha
import com.example.pizzeriaapp.utils.validarCorreo
import com.example.pizzeriaapp.utils.validarZona
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class ValidacionesTest {

    @Test
    fun `correo valido se normaliza en minusculas`() {
        assertEquals("ana@mail.com", validarCorreo("  Ana@Mail.com "))
    }

    @Test
    fun `correos sin formato nombre@dominio son invalidos`() {
        listOf("ana@mail", "ana.mail.com", "@mail.com", "ana@.com", "", null).forEach { assertNull(validarCorreo(it), "$it") }
    }

    @Test
    fun `telefonos se formatean como +XX (XXX) XXX-XXXX`() {
        assertEquals("+56 (912) 345-678", formatearTelefono("912345678"))            // 9 dígitos: se asume +56
        assertEquals("+56 (912) 345-678", formatearTelefono("+56 9 1234 5678"))
        assertEquals("+56 (221) 234-5678", formatearTelefono("2212345678"))          // 10 dígitos: formato completo
        assertEquals("+54 (911) 234-5678", formatearTelefono("+54 (911) 234-5678"))
    }

    @Test
    fun `telefonos con letras o largo incorrecto son invalidos`() {
        listOf("12ab", "1234567", "+56 9 1234 5678 999", "", null).forEach { assertNull(formatearTelefono(it), "$it") }
    }

    @Test
    fun `fechas inexistentes o con otro formato son invalidas`() {
        assertEquals(LocalDate.of(2026, 11, 15), parsearFecha("15-11-2026"))
        listOf("31-02-2026", "2026-11-15", "15/11/2026", "1-11-2026").forEach { assertNull(parsearFecha(it), it) }
    }

    @Test
    fun `entradas numericas fuera de rango negativas o con texto se rechazan`() {
        assertEquals(100, convertirEntero(" 100 ", 1..100))
        assertFailsWith<NumberFormatException> { convertirEntero("x", 1..100) }
        assertFailsWith<IllegalArgumentException> { convertirEntero("-1", 1..100) }
        assertFailsWith<IllegalArgumentException> { convertirEntero("0", 1..100) }
        assertFailsWith<IllegalArgumentException> { convertirEntero("101", 1..100) }
    }

    @Test
    fun `nombre y zona con valores por defecto`() {
        assertEquals("Ana Pérez", formatearNombre("  ana   PÉREZ "))
        assertEquals("Cliente sin nombre", formatearNombre(null))
        assertEquals("Sur", validarZona("sur"))
        assertEquals("Centro", validarZona("Marte"))
        assertEquals("Centro", validarZona(null))
    }
}
