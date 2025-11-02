package com.tienda.tiendaropaapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeleccionClienteActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabNuevo: FloatingActionButton
    private lateinit var etBusqueda: TextInputEditText
    private lateinit var adapter: ClienteAdapter
    private var listaCompleta: List<Cliente> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_cliente)

        recyclerView = findViewById(R.id.recyclerViewClientes)
        fabNuevo = findViewById(R.id.fabNuevoCliente)
        etBusqueda = findViewById(R.id.etBusqueda)

        recyclerView.layoutManager = LinearLayoutManager(this)
        cargarClientes()

        fabNuevo.setOnClickListener {
            mostrarFormularioNuevoCliente()
        }

        // 🔍 Búsqueda en tiempo real
        etBusqueda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarClientes(s.toString())
            }
        })
    }

    private fun cargarClientes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getClientes()
                if (response.isSuccessful && response.body()?.success == true) {
                    listaCompleta = response.body()!!.data
                    withContext(Dispatchers.Main) {
                        adapter = ClienteAdapter(listaCompleta) { cliente ->
                            val intent = Intent().apply {
                                putExtra("cliente_id", cliente.id)
                                putExtra("cliente_nombre", cliente.razonsocial)
                            }
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                        recyclerView.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeleccionClienteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filtrarClientes(query: String) {
        val listaFiltrada = if (query.isEmpty()) {
            listaCompleta
        } else {
            listaCompleta.filter { cliente ->
                cliente.razonsocial.contains(query, ignoreCase = true) ||
                        cliente.ruc?.contains(query, ignoreCase = true) == true ||
                        cliente.telefono?.contains(query, ignoreCase = true) == true ||
                        cliente.celular?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.actualizarLista(listaFiltrada)
    }

    private fun mostrarFormularioNuevoCliente() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nuevo_cliente, null)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Nuevo Cliente")

        val etRazonSocial = dialogView.findViewById<EditText>(R.id.etRazonSocial)
        val etTelefono = dialogView.findViewById<EditText>(R.id.etTelefono)
        val etCorreo = dialogView.findViewById<EditText>(R.id.etCorreo)
        val etDireccion = dialogView.findViewById<EditText>(R.id.etDireccion)

        builder.setPositiveButton("Crear") { _, _ ->
            val razon = etRazonSocial.text.toString().trim()
            if (razon.isEmpty()) {
                Toast.makeText(this, "La razón social es obligatoria", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val nuevoCliente = CrearClienteRequest(
                razonsocial = razon,
                telefono = etTelefono.text.toString().takeIf { it.isNotEmpty() },
                correo = etCorreo.text.toString().takeIf { it.isNotEmpty() },
                direccion = etDireccion.text.toString().takeIf { it.isNotEmpty() },
                estado = 1
            )

            crearClienteYActualizar(nuevoCliente)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun crearClienteYActualizar(cliente: CrearClienteRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.crearCliente(cliente)
                if (response.isSuccessful && response.body()?.success == true) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SeleccionClienteActivity, "Cliente creado", Toast.LENGTH_SHORT).show()
                        cargarClientes() // Recarga y restablece la búsqueda
                        etBusqueda.setText("") // Limpia la búsqueda
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SeleccionClienteActivity, "Error al crear cliente", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SeleccionClienteActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}