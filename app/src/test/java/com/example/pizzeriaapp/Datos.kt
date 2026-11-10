package com.example.pizzeriaapp

import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Pizza
import com.example.pizzeriaapp.modelo.PizzaEspecial
import java.time.LocalDate

// Datos de prueba con montos redondos para calcular a mano los resultados esperados
val FECHA_EN_PROMOCION: LocalDate = LocalDate.of(2026, 11, 15)
val FECHA_FUERA_PROMOCION: LocalDate = LocalDate.of(2026, 12, 15)

fun pizza(nombre: String = "Napolitana", precio: Int = 10000) =
    Pizza(nombre, precio, mapOf("Masa" to 1, "Queso" to 1))

fun pizzaEspecial(nombre: String = "Especial", precio: Int = 10000) =
    PizzaEspecial(nombre, precio, mapOf("Masa" to 1, "Queso" to 2))

fun cliente(nombre: String = "Ana Pérez", correo: String = "ana@mail.com", zona: String = "Sur") =
    Cliente(nombre, "+56 (912) 345-678", correo, "Calle 1", zona)
