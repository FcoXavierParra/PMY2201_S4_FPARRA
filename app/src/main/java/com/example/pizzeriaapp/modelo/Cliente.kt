package com.example.pizzeriaapp.modelo

class Cliente(
    nombre: String,
    telefono: String,
    correo: String,
    direccion: String,
    zona: String
) : Usuario(nombre, telefono, correo) {

    var direccion: String = direccion
        private set

    var zona: String = zona
        private set

    // Criterio de actualización: nombre y correo identifican al cliente y no cambian; teléfono,
    // dirección y zona se reemplazan por los del pedido más reciente, que es donde quiere recibirlo.
    // Devuelve los campos que cambiaron (vacío si eran iguales).
    fun actualizarContacto(nuevo: Cliente): List<String> {
        require(this == nuevo) { "solo se actualizan los datos del mismo cliente ($this y $nuevo)" }
        val cambios = mutableListOf<String>()
        if (telefono != nuevo.telefono) {
            telefono = nuevo.telefono
            cambios += "teléfono"
        }
        if (direccion != nuevo.direccion) {
            direccion = nuevo.direccion
            cambios += "dirección"
        }
        if (zona != nuevo.zona) {
            zona = nuevo.zona
            cambios += "zona"
        }
        return cambios
    }

    // Desestructuración: val (nombre, correo, telefono) = cliente
    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono

    // Mismo cliente = mismo nombre y mismo correo, sin importar mayúsculas
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return nombre.equals(other.nombre, ignoreCase = true) && correo.equals(other.correo, ignoreCase = true)
    }

    override fun hashCode(): Int = 31 * nombre.lowercase().hashCode() + correo.lowercase().hashCode()

    override fun toString(): String = "$nombre <$correo>"
}
