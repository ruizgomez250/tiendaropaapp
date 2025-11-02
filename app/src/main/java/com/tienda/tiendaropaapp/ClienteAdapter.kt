package com.tienda.tiendaropaapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tienda.tiendaropaapp.Cliente

class ClienteAdapter(
    private var clientes: List<Cliente>,
    private val onClienteSeleccionado: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {
    fun actualizarLista(nuevaLista: List<Cliente>) {
        this.clientes = nuevaLista
        notifyDataSetChanged()
    }
    class ClienteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvContacto: TextView = view.findViewById(R.id.tvContacto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val cliente = clientes[position]
        holder.tvNombre.text = cliente.razonsocial
        holder.tvContacto.text = cliente.telefono ?: cliente.celular ?: "Sin contacto"

        holder.itemView.setOnClickListener {
            onClienteSeleccionado(cliente)
        }
    }

    override fun getItemCount() = clientes.size
}
