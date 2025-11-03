package com.tienda.tiendaropaapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation

class ProductoAdapter(
    private var productos: List<Producto>,
    private val onProductoClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>(), Filterable {

    private var productosFiltrados = productos.toMutableList()

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val ivImagen: ImageView = view.findViewById(R.id.ivImagen)
        val containerDatos: View = view.findViewById(R.id.container_datos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productosFiltrados[position]
        val context = holder.itemView.context

        // ✅ Textos con strings localizables
        holder.tvDescripcion.text = producto.descripcion
        holder.tvCodigo.text = context.getString(R.string.format_codigo, producto.codigo)
        holder.tvPrecio.text = context.getString(R.string.format_precio_venta, producto.pventa)
        holder.tvCategoria.text = context.getString(R.string.format_categoria, producto.categoriaproducto.descripcion)
        holder.tvStock.text = context.getString(R.string.format_stock, producto.stock)

        // ✅ Cargar imagen con Coil
        val imageUrl: String? = producto.imagen_url
            ?.trim()
            ?.takeIf { it.isNotEmpty() && it.startsWith("http") }

        holder.ivImagen.load(imageUrl) {

            placeholder(R.drawable.image_placeholder_background)
            error(R.drawable.image_placeholder_background)
            transformations(RoundedCornersTransformation(8f))
        }

        // ✅ Estilos según stock
        val stock = producto.stock.toSafeDouble()
        val tieneStock = stock > 0

        val backgroundColor = if (tieneStock) {
            ContextCompat.getColor(context, R.color.fondo_stock_disponible)
        } else {
            ContextCompat.getColor(context, R.color.fondo_stock_agotado)
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        holder.tvDescripcion.setTextColor(
            ContextCompat.getColor(context, R.color.texto_titulo)
        )
        holder.tvStock.setTextColor(
            if (tieneStock) {
                ContextCompat.getColor(context, R.color.texto_stock_bueno)
            } else {
                ContextCompat.getColor(context, R.color.texto_stock_agotado)
            }
        )

        holder.itemView.alpha = if (tieneStock) 1.0f else 0.6f

        // 🔥 Clic en ZONA DE DATOS → vender
        holder.containerDatos.setOnClickListener {
            try {
                onProductoClick(producto)
            } catch (e: Exception) {
                Log.e("CLICK_ERROR", "Error al hacer clic en producto", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔥 Clic en IMAGEN → agrandar
        holder.ivImagen.setOnClickListener {
            val urlParaAgrandar = producto.imagen_url
                ?.trim()
                ?.takeIf { it.isNotEmpty() && it.startsWith("http") }

            if (!urlParaAgrandar.isNullOrEmpty()) {
                agrandarImagen(context, urlParaAgrandar)
            } else {
                Toast.makeText(context, "No hay imagen disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = productosFiltrados.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim() ?: ""
                val filteredList = if (query.isEmpty()) {
                    productos.toMutableList()
                } else {
                    productos.filter { producto ->
                        producto.descripcion.lowercase().contains(query) ||
                                producto.codigo.lowercase().contains(query) ||
                                producto.categoriaproducto.descripcion.lowercase().contains(query)
                    }.toMutableList()
                }

                return FilterResults().apply {
                    values = filteredList
                    count = filteredList.size
                }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productosFiltrados.clear()
                productosFiltrados.addAll(results?.values as MutableList<Producto>)
                notifyDataSetChanged()
            }
        }
    }

    // ✅ Mostrar imagen en grande
    private fun agrandarImagen(context: Context, imageUrl: String) {
        Log.d("ImagenAgrandar", "URL al hacer clic: $imageUrl")
        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true // ✅ Importante: ajusta proporción
            setMaxWidth((context.resources.displayMetrics.widthPixels * 0.9f).toInt())
            setMaxHeight((context.resources.displayMetrics.heightPixels * 0.8f).toInt())
            load(imageUrl) {
                placeholder(R.drawable.image_placeholder_background)
                error(R.drawable.image_placeholder_background)
            }
            // Agregar márgenes (opcional pero mejora la UX)
            val margin = (24 * context.resources.displayMetrics.density).toInt()
            setPadding(margin, margin, margin, margin)
        }

        AlertDialog.Builder(context)
            .setView(imageView)
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }
}