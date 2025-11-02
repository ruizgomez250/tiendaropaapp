package com.tienda.tiendaropaapp

data class Cliente(
    val id: Int,
    val razonsocial: String,
    val ruc: String? = null,
    val direccion: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val celular: String? = null,
    val observacion: String? = null,
    val estado: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)