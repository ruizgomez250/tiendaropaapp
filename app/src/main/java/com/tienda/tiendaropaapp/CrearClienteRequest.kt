package com.tienda.tiendaropaapp

data class CrearClienteRequest(
    val razonsocial: String,
    val telefono: String? = null,
    val correo: String? = null,
    val direccion: String? = null,
    val observacion: String? = null,
    val estado: Int = 1
)
