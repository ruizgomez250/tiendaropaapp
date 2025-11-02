package com.tienda.tiendaropaapp

data class DetalleVenta(
    val codigo: Int, // id del producto
    val descripcion: String,
    val cantidad: Int,
    val precio: Double, // ya incluye IVA
    val iva: Int = 10, // siempre 10%
    val diez: Double // = precio * cantidad (total con IVA)
)