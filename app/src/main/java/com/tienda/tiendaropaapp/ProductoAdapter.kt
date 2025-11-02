package com.tienda.tiendaropaapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productosFiltrados[position]
        val context = holder.itemView.context

        holder.tvDescripcion.text = producto.descripcion
        holder.tvCodigo.text = "Código: ${producto.codigo}"
        holder.tvPrecio.text = "Venta: \$${producto.pventa}"
        holder.tvCategoria.text = "Categoría: ${producto.categoriaproducto.descripcion}"
        holder.tvStock.text = "Stock: ${producto.stock}"

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

        holder.itemView.setOnClickListener {
            try {
                onProductoClick(producto)
            } catch (e: Exception) {
                Log.e("CLICK_ERROR", "Error al hacer clic en producto", e)
                Toast.makeText(it.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemView.alpha = if (tieneStock) 1.0f else 0.6f
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
}