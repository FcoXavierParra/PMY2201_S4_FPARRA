package com.example.pizzeriaapp.modelo

// Clase base de Cliente y Repartidor: datos de contacto comunes.
// nombre y correo identifican a la persona; el teléfono se puede actualizar desde las subclases.
open class Usuario(val nombre: String, telefono: String, val correo: String) {
    var telefono: String = telefono
        protected set
}
