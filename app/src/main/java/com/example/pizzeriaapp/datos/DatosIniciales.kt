package com.example.pizzeriaapp.datos

import com.example.pizzeriaapp.modelo.Bebida
import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.Combo
import com.example.pizzeriaapp.modelo.IngredienteExtra
import com.example.pizzeriaapp.modelo.Inventario
import com.example.pizzeriaapp.modelo.Pedido
import com.example.pizzeriaapp.modelo.Pizza
import com.example.pizzeriaapp.modelo.PizzaEspecial
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.modelo.Registro
import com.example.pizzeriaapp.modelo.Repartidor
import com.example.pizzeriaapp.servicio.Pizzeria
import java.time.LocalDate

// Catálogo, inventario, repartidores, clientes y pedidos con que parte la jornada
class DatosIniciales {
    val margarita = Pizza("Margarita", 7990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 2))
    val pepperoni = Pizza("Pepperoni", 8990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Pepperoni" to 2))
    val hawaiana = Pizza("Hawaiana", 9490, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Jamón" to 1, "Piña" to 1))
    val vegetariana = Pizza("Vegetariana", 9990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 1, "Champiñones" to 2, "Pimentón" to 1))
    val cuatroQuesos = PizzaEspecial("Cuatro Quesos", 11990, mapOf("Masa" to 1, "Salsa de tomate" to 1, "Queso" to 3))
    val bebida = Bebida("Bebida 1.5L", 2490)
    val extraQueso = IngredienteExtra("Extra queso", 1490, insumo = "Queso")
    val comboFamiliar = Combo("Combo Familiar", 18990, listOf(pepperoni, margarita, bebida))

    val juan = Cliente("Juan Pérez", "+56 (912) 345-678", "juan.perez@mail.com", "Calle Moneda 1020", "Centro")
    val maria = Cliente("María López", "+56 (922) 334-455", "maria@mail.com", "Av. Independencia 500", "Norte")

    // Pedidos que ya estaban en cocina al iniciar la jornada
    private val fechaPedidosPrevios: LocalDate = LocalDate.of(2026, 11, 3)
    val pedidoJuanA = Pedido(juan, listOf(pepperoni, pepperoni), fechaPedidosPrevios)
    val pedidoJuanB = Pedido(juan, listOf(margarita, cuatroQuesos), fechaPedidosPrevios)
    val pedidoMaria = Pedido(maria, listOf(hawaiana), fechaPedidosPrevios)
    val pedidosPrevios: List<Pedido> = listOf(pedidoJuanA, pedidoJuanB, pedidoMaria)

    val productosDuplicados: List<Producto>
    val pizzeria: Pizzeria

    init {
        // La última "PEPPERONI" simula una carga repetida: el registro la descarta por equals()/hashCode()
        val catalogo = Registro<Producto>()
        productosDuplicados = listOf(
            margarita, pepperoni, hawaiana, vegetariana, cuatroQuesos, bebida, extraQueso, comboFamiliar,
            Pizza("PEPPERONI", 9990, pepperoni.insumos)
        ).filterNot { catalogo.agregar(it) }

        val clientes = Registro<Cliente>()
        clientes.agregar(juan)
        clientes.agregar(maria)

        val repartidores = listOf(
            Repartidor("Carlos Muñoz", "+56 (911) 112-222", "carlos@pizzeria.cl", "Norte", 1.2),
            Repartidor("Andrea Rojas", "+56 (933) 334-444", "andrea@pizzeria.cl", "Sur", 0.8),
            Repartidor("Valentina Soto", "+56 (955) 556-666", "valentina@pizzeria.cl", "Centro", 0.5),
            Repartidor("Mario Díaz", "+56 (977) 778-888", "mario@pizzeria.cl", "Oriente", 2.5)
        )

        val inventario = Inventario(
            mapOf(
                "Masa" to 25, "Salsa de tomate" to 25, "Queso" to 23, "Pepperoni" to 12, "Jamón" to 5,
                "Piña" to 5, "Champiñones" to 0, "Pimentón" to 5, "Bebida 1.5L" to 10
            )
        )

        pizzeria = Pizzeria(catalogo.todos(), inventario, repartidores, clientes)
        // Los pedidos previos consumen stock: las existencias finales reflejan todos los pedidos del día
        pedidosPrevios.forEach { pizzeria.registrarPedidoPrevio(it) }
    }
}
