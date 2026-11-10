package com.example.pizzeriaapp.modelo

// Marca las clases de productos que participan en las promociones de temporada.
// RUNTIME permite leerla con reflection mientras se procesa el pedido.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(val descripcion: String, val porcentaje: Int)
