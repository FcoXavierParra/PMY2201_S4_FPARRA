package com.example.pizzeriaapp.utils

// 38513 -> "$38.513"
fun formatearPesos(monto: Int): String =
    "$" + monto.toString().reversed().chunked(3).joinToString(".").reversed()

// Descuento con signo negativo, o $0 si no aplica
fun formatearDescuento(monto: Int): String = if (monto > 0) "-${formatearPesos(monto)}" else formatearPesos(0)
