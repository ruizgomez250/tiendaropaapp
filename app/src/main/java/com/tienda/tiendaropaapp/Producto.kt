package com.tienda.tiendaropaapp

data class Producto(
    val id: Int,
    val codigo: String,
    val descripcion: String,
    val detalle: String?,
    val id_categoria: Int,
    val stock: String,        // ❗ No Double, no Int → es "-2.000" (texto)
    val id_medida: Int,
    val estado: Int,
    val pcosto: String,       // ❗ "5000" → String
    val pventa: String,       // ❗ "6000" → String
    val observacion: String?,
    val imagen: String?,
    val impuesto: Int,
    val categoriaproducto: CategoriaProducto,
    val unidaddemedida: UnidadDeMedida
)