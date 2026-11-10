package com.example.pizzeriaapp.modelo

@Promocionable(descripcion = "Pizza de la casa", porcentaje = 15)
class PizzaEspecial(nombre: String, precio: Int, ingredientes: Map<String, Int>) :
    Pizza(nombre, precio, ingredientes)
