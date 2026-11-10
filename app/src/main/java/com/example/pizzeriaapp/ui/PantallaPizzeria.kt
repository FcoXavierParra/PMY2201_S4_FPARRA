package com.example.pizzeriaapp.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pizzeriaapp.datos.DatosIniciales
import com.example.pizzeriaapp.modelo.Cliente
import com.example.pizzeriaapp.modelo.EstadoPedido
import com.example.pizzeriaapp.modelo.Producto
import com.example.pizzeriaapp.servicio.ResultadoPedido
import com.example.pizzeriaapp.utils.FORMATO_FECHA
import com.example.pizzeriaapp.utils.NOMBRE_PROMOCION
import com.example.pizzeriaapp.utils.PERIODO_PROMOCION
import com.example.pizzeriaapp.utils.RANGO_CANTIDAD
import com.example.pizzeriaapp.utils.ZONAS_CON_COBERTURA
import com.example.pizzeriaapp.utils.calcularTotales
import com.example.pizzeriaapp.utils.formatearDescuento
import com.example.pizzeriaapp.utils.formatearNombre
import com.example.pizzeriaapp.utils.formatearPesos
import com.example.pizzeriaapp.utils.formatearTelefono
import com.example.pizzeriaapp.utils.promocionDe
import com.example.pizzeriaapp.utils.validarCorreo
import java.time.LocalDate

// Reemplaza la capa de consola de las semanas anteriores: la lógica sigue en servicio/modelo/utils
// y esta pantalla solo toma los datos del usuario y muestra los resultados.
@Composable
fun PantallaPizzeria(encabezado: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val datos = remember { DatosIniciales() }
    val pizzeria = datos.pizzeria
    val fecha = remember { LocalDate.now() }

    val cantidades = remember { mutableStateMapOf<Producto, Int>() }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var zona by rememberSaveable { mutableStateOf("Centro") }
    var resultado by remember { mutableStateOf<ResultadoPedido?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    // Los objetos del dominio no son observables: este contador avisa a Compose que cambiaron
    var cambios by remember { mutableIntStateOf(0) }

    val productos = pizzeria.menu.flatMap { producto -> List(cantidades[producto] ?: 0) { producto } }
    val correoValido = validarCorreo(correo)
    val telefonoValido = formatearTelefono(telefono)
    val datosCompletos = nombre.isNotBlank() && correoValido != null && telefonoValido != null && direccion.isNotBlank()
    val existencias = remember(resultado, cambios) { pizzeria.inventario.existencias() }
    val historial = remember(resultado, cambios) { resultado?.pedido?.historial().orEmpty() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { encabezado() }
        item { TarjetaPromocion(fecha) }
        item { Titulo("Menú") }
        items(pizzeria.menu) { producto ->
            FilaProducto(
                producto = producto,
                cantidad = cantidades[producto] ?: 0,
                habilitado = resultado == null,
                alCambiar = { nueva -> if (nueva == 0) cantidades.remove(producto) else cantidades[producto] = nueva }
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Titulo("Tu pedido")
                ResumenPedido(productos, fecha)
            }
        }

        val actual = resultado
        if (actual == null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Titulo("Datos de entrega")
                    FormularioCliente(
                        nombre = nombre, alCambiarNombre = { nombre = it },
                        correo = correo, correoValido = correoValido != null, alCambiarCorreo = { correo = it },
                        telefono = telefono, telefonoFormateado = telefonoValido, alCambiarTelefono = { telefono = it },
                        direccion = direccion, alCambiarDireccion = { direccion = it },
                        zona = zona, alCambiarZona = { zona = it }
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        val cliente = Cliente(formatearNombre(nombre), telefonoValido!!, correoValido!!, direccion.trim(), zona)
                        resultado = pizzeria.confirmarPedido(cliente, productos, fecha)
                        aviso = null
                    },
                    enabled = productos.isNotEmpty() && datosCompletos,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Confirmar pedido") }
            }
        } else {
            item {
                TarjetaResultado(
                    resultado = actual,
                    estado = historial.lastOrNull() ?: actual.pedido.estado,
                    historial = historial,
                    aviso = aviso,
                    alReponer = {
                        actual.faltantes.forEach { pizzeria.reponer(it.ingrediente, it.faltante) }
                        resultado = pizzeria.procesarPedido(actual.pedido, actual.clienteNuevo)
                    },
                    alEntregar = {
                        aviso = runCatching { actual.repartidor?.entregarPedido(actual.pedido) }
                            .exceptionOrNull()?.message
                        cambios++
                    },
                    alNuevoPedido = {
                        cantidades.clear()
                        resultado = null
                        aviso = null
                    }
                )
            }
        }
        item { Existencias(existencias) }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun TarjetaPromocion(fecha: LocalDate) {
    val periodo = "${PERIODO_PROMOCION.start.format(FORMATO_FECHA)} al ${PERIODO_PROMOCION.endInclusive.format(FORMATO_FECHA)}"
    val texto = if (fecha in PERIODO_PROMOCION) {
        "★ Hoy ${fecha.format(FORMATO_FECHA)} rige $NOMBRE_PROMOCION ($periodo): los productos ★ tienen descuento."
    } else {
        "Hoy ${fecha.format(FORMATO_FECHA)} está fuera de $NOMBRE_PROMOCION ($periodo)."
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(texto, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FilaProducto(producto: Producto, cantidad: Int, habilitado: Boolean, alCambiar: (Int) -> Unit) {
    val promocion = promocionDe(producto)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (promocion != null) "★ ${producto.nombre}  -${promocion.porcentaje}%" else producto.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                val detalle = listOf(producto.categoria.etiqueta, producto.composicion().joinToString(", "))
                Text(detalle.filter { it.isNotEmpty() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                Text(formatearPesos(producto.precio), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { alCambiar(cantidad - 1) }, enabled = habilitado && cantidad > 0) { Text("−") }
            Text("$cantidad", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { alCambiar(cantidad + 1) }, enabled = habilitado && cantidad < RANGO_CANTIDAD.last) { Text("+") }
        }
    }
}

@Composable
private fun ResumenPedido(productos: List<Producto>, fecha: LocalDate) {
    if (productos.isEmpty()) {
        Text("Agrega productos del menú con el botón +.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    val totales = calcularTotales(productos, fecha)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            productos.groupingBy { it }.eachCount().forEach { (producto, cantidad) ->
                FilaMonto("$cantidad × ${producto.nombre}", formatearPesos(producto.precio * cantidad))
            }
            HorizontalDivider()
            FilaMonto("Subtotal", formatearPesos(totales.subtotal))
            FilaMonto("Promoción $NOMBRE_PROMOCION", formatearDescuento(totales.descuentoPromocion))
            FilaMonto("Descuento 10% (más de 3 pizzas)", formatearDescuento(totales.descuentoVolumen))
            FilaMonto("IVA (19%)", formatearPesos(totales.impuesto))
            FilaMonto("TOTAL", formatearPesos(totales.total), destacado = true)
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, monto: String, destacado: Boolean = false) {
    val peso = if (destacado) FontWeight.Bold else FontWeight.Normal
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, fontWeight = peso, modifier = Modifier.weight(1f))
        Text(monto, fontWeight = peso)
    }
}

@Composable
private fun FormularioCliente(
    nombre: String, alCambiarNombre: (String) -> Unit,
    correo: String, correoValido: Boolean, alCambiarCorreo: (String) -> Unit,
    telefono: String, telefonoFormateado: String?, alCambiarTelefono: (String) -> Unit,
    direccion: String, alCambiarDireccion: (String) -> Unit,
    zona: String, alCambiarZona: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(nombre, alCambiarNombre, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = correo, onValueChange = alCambiarCorreo, label = { Text("Correo electrónico") }, singleLine = true,
            isError = correo.isNotBlank() && !correoValido,
            supportingText = { if (correo.isNotBlank() && !correoValido) Text("Use el formato nombre@dominio.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefono, onValueChange = alCambiarTelefono, label = { Text("Teléfono") }, singleLine = true,
            isError = telefono.isNotBlank() && telefonoFormateado == null,
            supportingText = {
                when {
                    telefono.isBlank() -> Text("Ej.: +56 9 1234 5678")
                    telefonoFormateado == null -> Text("Ingrese de 9 a 12 dígitos; puede usar +, espacios, guiones o paréntesis")
                    else -> Text("Se registrará como $telefonoFormateado")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(direccion, alCambiarDireccion, label = { Text("Dirección de entrega") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("Zona", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ZONAS_CON_COBERTURA.forEach { opcion ->
                FilterChip(selected = zona == opcion, onClick = { alCambiarZona(opcion) }, label = { Text(opcion) })
            }
        }
    }
}

@Composable
private fun TarjetaResultado(
    resultado: ResultadoPedido,
    estado: EstadoPedido,
    historial: List<EstadoPedido>,
    aviso: String?,
    alReponer: () -> Unit,
    alEntregar: () -> Unit,
    alNuevoPedido: () -> Unit
) {
    val pedido = resultado.pedido
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Pedido #${pedido.id} · ${estado.etiqueta}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val registro = if (resultado.clienteNuevo) "cliente nuevo" else "ya estaba registrado, no se duplica"
            Text("Cliente: ${pedido.cliente.nombre} ($registro)")
            Text("Teléfono: ${pedido.cliente.telefono} · ${pedido.cliente.correo}")
            Text("Entrega: ${pedido.cliente.direccion} (zona ${pedido.cliente.zona})")
            FilaMonto("TOTAL", formatearPesos(pedido.calcularTotal().total), destacado = true)

            HorizontalDivider()
            Text("Verificación de ingredientes", fontWeight = FontWeight.SemiBold)
            resultado.verificacion.forEach {
                Text("${it.ingrediente}: necesita ${it.necesario} · hay ${it.disponible} → ${it.disponibilidad.etiqueta}")
            }

            if (resultado.faltantes.isNotEmpty()) {
                Text(
                    "Stock insuficiente: no se descontó ningún ingrediente.",
                    color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold
                )
                Button(onClick = alReponer, modifier = Modifier.fillMaxWidth()) { Text("Reponer faltantes y reintentar") }
            } else {
                HorizontalDivider()
                Text("Seguimiento: ${historial.joinToString(" → ") { it.etiqueta }}")
                val repartidor = resultado.repartidor
                Text(
                    if (repartidor != null) "Repartidor: ${repartidor.nombre} (zona ${repartidor.zona}, ${repartidor.distanciaKm} km)"
                    else "Sin repartidores disponibles: el pedido queda Listo en el local."
                )
                if (estado == EstadoPedido.EN_CAMINO) {
                    Button(onClick = alEntregar, modifier = Modifier.fillMaxWidth()) { Text("Registrar entrega") }
                }
            }
            aviso?.let { Text("Validación: $it", color = MaterialTheme.colorScheme.error) }
            OutlinedButton(onClick = alNuevoPedido, modifier = Modifier.fillMaxWidth()) { Text("Nuevo pedido") }
        }
    }
}

@Composable
private fun Existencias(existencias: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Titulo("Existencias")
        Text(
            existencias.entries.joinToString(" · ") { (ingrediente, cantidad) -> "$ingrediente: $cantidad" },
            style = MaterialTheme.typography.bodySmall
        )
    }
}
