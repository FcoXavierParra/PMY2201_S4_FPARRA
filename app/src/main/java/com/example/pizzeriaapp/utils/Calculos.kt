package com.example.pizzeriaapp.utils

import com.example.pizzeriaapp.modelo.Producto
import java.time.LocalDate
import kotlin.math.roundToInt

const val TASA_IVA = 0.19
const val TASA_DESCUENTO_VOLUMEN = 0.10
const val MINIMO_PIZZAS_DESCUENTO = 3   // el descuento por volumen aplica a pedidos de MÁS de 3 pizzas
const val NOMBRE_PROMOCION = "Noviembre Pizzero"

// Rangos: periodo de la promoción de temporada y cantidad permitida por producto
val PERIODO_PROMOCION: ClosedRange<LocalDate> = LocalDate.of(2026, 11, 1)..LocalDate.of(2026, 11, 30)
val RANGO_CANTIDAD: IntRange = 1..100

data class Totales(
    val subtotal: Int,
    val descuentoPromocion: Int,
    val descuentoVolumen: Int,
    val impuesto: Int,
    val total: Int
)

// Recibe la lista completa de productos para que el subtotal y la cantidad de pizzas salgan de los mismos datos.
// 1) Promoción: si la fecha está en PERIODO_PROMOCION, cada producto @Promocionable descuenta su porcentaje.
// 2) Volumen: 10 % sobre el monto con promoción si el pedido trae más de 3 pizzas.
// 3) IVA sobre el monto con ambos descuentos.
fun calcularTotales(productos: List<Producto>, fecha: LocalDate): Totales {
    val subtotal = productos.sumOf { it.precio }
    if (subtotal <= 0) throw IllegalArgumentException("Costo de pedido no válido ($subtotal): el pedido no tiene productos")

    val descuentoPromocion = if (fecha in PERIODO_PROMOCION) {
        productos.sumOf { producto ->
            promocionDe(producto)?.let { (producto.precio * it.porcentaje / 100.0).roundToInt() } ?: 0
        }
    } else {
        0
    }
    val conPromocion = subtotal - descuentoPromocion
    val pizzas = productos.sumOf { it.unidadesPizza() }
    val descuentoVolumen = if (pizzas > MINIMO_PIZZAS_DESCUENTO) (conPromocion * TASA_DESCUENTO_VOLUMEN).roundToInt() else 0
    val neto = conPromocion - descuentoVolumen
    val impuesto = (neto * TASA_IVA).roundToInt()
    return Totales(subtotal, descuentoPromocion, descuentoVolumen, impuesto, neto + impuesto)
}

// Suma los insumos de todos los productos del lote: ingrediente -> cantidad total necesaria
fun calcularIngredientesLote(productos: List<Producto>): Map<String, Int> {
    val necesarios = mutableMapOf<String, Int>()
    for (producto in productos) {
        for ((ingrediente, cantidad) in producto.insumos) {
            necesarios[ingrediente] = (necesarios[ingrediente] ?: 0) + cantidad
        }
    }
    return necesarios
}

