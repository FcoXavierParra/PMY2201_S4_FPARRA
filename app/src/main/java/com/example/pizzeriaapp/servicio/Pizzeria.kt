package com.example.pizzeriaapp.servicio

import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Disponibilidad
import com.example.pizzeriaapp.modelo.EstadoIngrediente
import com.example.pizzeriaapp.modelo.EstadoPedido
import com.example.pizzeriaapp.modelo.Inventario
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.modelo.Registro
import com.example.pizzeriaapp.modelo.Repartidor
import com.example.pizzeriaapp.utils.calcularIngredientesLote
import java.time.LocalDate

// Reglas del negocio sin entrada ni salida de pantalla: clientes, stock, preparación y despacho
class Pizzeria(
    val menu: List<Producto>,
    val inventario: Inventario,
    val repartidores: List<Repartidor>,
    private val clientes: Registro<Cliente>
) {
    val cantidadClientes: Int
        get() = clientes.cantidad

    // Si ya existe un cliente con el mismo nombre y correo, se reutiliza en vez de duplicarlo
    fun registrarCliente(cliente: Cliente): ResultadoRegistro {
        clientes.buscar(cliente)?.let { return ResultadoRegistro(it, esNuevo = false) }
        clientes.agregar(cliente)
        return ResultadoRegistro(cliente, esNuevo = true)
    }

    fun verificarStock(pedido: Pedido): List<EstadoIngrediente> =
        inventario.verificar(calcularIngredientesLote(pedido.productos))

    // Pedido que ya estaba en cocina al iniciar la jornada: su consumo se descuenta del stock inicial
    fun registrarPedidoPrevio(pedido: Pedido) {
        inventario.descontar(calcularIngredientesLote(pedido.productos))
    }

    // Si alcanzan todos los ingredientes, los descuenta y el pedido pasa a Listo.
    // Si falta alguno, el inventario no cambia. Devuelve el detalle de la verificación.
    fun prepararPedido(pedido: Pedido): List<EstadoIngrediente> {
        val detalle = verificarStock(pedido)
        if (detalle.all { it.disponibilidad == Disponibilidad.SUFICIENTE }) {
            inventario.descontar(calcularIngredientesLote(pedido.productos))
            pedido.cambiarEstado(EstadoPedido.LISTO)
        }
        return detalle
    }

    fun reponer(ingrediente: String, cantidad: Int) = inventario.reponer(ingrediente, cantidad)

    // Regla de asignación (prioridad explícita por zona):
    //   1. Repartidor disponible de la misma zona del cliente; si hay varios, el más cercano.
    //   2. Si nadie de esa zona está disponible, el disponible más cercano al local, aunque sea de otra zona.
    // La zona va antes que la distancia porque el repartidor conoce el sector y vuelve a su zona.
    fun buscarRepartidor(zona: String): Repartidor? {
        val disponibles = repartidores.filter { it.disponible }.sortedBy { it.distanciaKm }
        return disponibles.find { it.zona == zona } ?: disponibles.firstOrNull()
    }

    fun despachar(pedido: Pedido): Repartidor? =
        buscarRepartidor(pedido.cliente.zona)?.also { it.tomarPedido(pedido) }

    // Flujo completo de un pedido nuevo: registra (o reutiliza) al cliente, prepara y despacha
    fun confirmarPedido(cliente: Cliente, productos: List<Producto>, fecha: LocalDate, notas: String? = null): ResultadoPedido {
        val registro = registrarCliente(cliente)
        return procesarPedido(Pedido(registro.cliente, productos, fecha, notas), registro.esNuevo)
    }

    // Prepara el pedido (stock todo o nada) y, solo si quedó Listo, le asigna repartidor.
    // Si faltó stock, el pedido sigue En preparación y se puede volver a procesar tras reponer.
    fun procesarPedido(pedido: Pedido, clienteNuevo: Boolean = false): ResultadoPedido {
        val detalle = prepararPedido(pedido)
        val repartidor = if (pedido.estado == EstadoPedido.LISTO) despachar(pedido) else null
        return ResultadoPedido(pedido, detalle, repartidor, clienteNuevo)
    }
}
