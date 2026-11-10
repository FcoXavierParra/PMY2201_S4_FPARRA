package com.example.pizzeriaapp.modelo

import com.example.pizzeriaapp.utils.calcularIngredientesLote

@Promocionable(descripcion = "Combo familiar", porcentaje = 10)
class Combo(nombre: String, precio: Int, val componentes: List<Producto>) :
    Producto(nombre, precio, Categoria.COMBO, calcularIngredientesLote(componentes)) {

    override fun unidadesPizza(): Int = componentes.sumOf { it.unidadesPizza() }

    override fun composicion(): List<String> = componentes.map { it.nombre }
}
