package com.tienda.tiendaropaapp

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val total: Int? = null, // opcional, porque en "crear cliente" no viene
    val data: T
)