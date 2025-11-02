package com.tienda.tiendaropaapp

data class VentaRequest(
    val fechaemision: String,
    val nrofactura: String,
    val id_cliente: Int,
    val condicion: String,
    val timbrado: String,
    val detalle: List<DetalleVenta>,
    val cantpago: Int? = null,
    val fechP: List<String>? = null
)
