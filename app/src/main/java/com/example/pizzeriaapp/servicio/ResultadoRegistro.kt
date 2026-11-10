package com.example.pizzeriaapp.servicio

import com.example.pizzeriaapp.modelo.Cliente

// cliente: el registrado (nuevo o el que ya existía); esNuevo: false si se reutilizó uno existente
data class ResultadoRegistro(val cliente: Cliente, val esNuevo: Boolean)
