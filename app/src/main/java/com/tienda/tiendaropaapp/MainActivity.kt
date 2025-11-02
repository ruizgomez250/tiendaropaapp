package com.tienda.tiendaropaapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

// ✅ Extensión segura para convertir String a Double
fun String?.toSafeDouble(): Double {
    return this?.replace(",", ".")?.toDoubleOrNull() ?: -1.0
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: ProductoViewModel
    private var productoSeleccionado: Producto? = null
    private var adapter: ProductoAdapter? = null // 👈 Variable para el adaptador

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.recyclerView)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[ProductoViewModel::class.java]

        viewModel.productos.observe(this) { productos ->
            swipeRefreshLayout.isRefreshing = false
            if (productos.isEmpty()) {
                mostrarMensajeSinProductos()
            } else {
                ocultarMensajeSinProductos()
                adapter = ProductoAdapter(productos) { producto ->
                    val stock = producto.stock.toSafeDouble()
                    if (stock <= 0) {
                        Toast.makeText(this@MainActivity, "Producto sin stock disponible", Toast.LENGTH_SHORT).show()
                        return@ProductoAdapter
                    }
                    productoSeleccionado = producto
                    iniciarSeleccionCliente(producto)
                }
                recyclerView.adapter = adapter
            }
        }

        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.cargarProductos(ApiClient.apiService)
        }

        // 🔍 Configurar buscador
        val editTextBuscar = findViewById<EditText>(R.id.editTextBuscar)
        editTextBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adapter?.filter?.filter(s.toString())
            }
        })

        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish()
            }
        }

        viewModel.cargarProductos(ApiClient.apiService)
    }

    private fun mostrarMensajeSinProductos() {
        recyclerView.visibility = View.GONE
        findViewById<TextView>(R.id.tvSinProductos).visibility = View.VISIBLE
    }

    private fun ocultarMensajeSinProductos() {
        recyclerView.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvSinProductos).visibility = View.GONE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_productos -> {
                viewModel.cargarProductos(ApiClient.apiService)
            }
            R.id.nav_clientes -> {
                val intent = Intent(this, SeleccionClienteActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_ventas -> {
                Toast.makeText(this, "Próximamente: Ventas", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun iniciarSeleccionCliente(producto: Producto) {
        val intent = Intent(this, SeleccionClienteActivity::class.java)
        startActivityForResult(intent, REQUEST_SELECCION_CLIENTE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECCION_CLIENTE && resultCode == Activity.RESULT_OK) {
            val clienteId = data?.getIntExtra("cliente_id", -1) ?: return
            val clienteNombre = data.getStringExtra("cliente_nombre") ?: "Desconocido"

            val producto = productoSeleccionado ?: return

            AlertDialog.Builder(this)
                .setTitle("Confirmar venta")
                .setMessage("¿Vender ${producto.descripcion} a $clienteNombre?")
                .setPositiveButton("Aceptar") { _, _ ->
                    enviarVenta(clienteId, producto)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    productoSeleccionado = null
                }
                .show()
        }
    }

    private fun enviarVenta(clienteId: Int, producto: Producto) {
        val cantidad = 1
        val precioUnitario = producto.pventa.toSafeDouble()
        if (precioUnitario <= 0) {
            Toast.makeText(this, "Precio de venta inválido", Toast.LENGTH_SHORT).show()
            return
        }

        val totalConIva = precioUnitario * cantidad

        val detalle = listOf(
            DetalleVenta(
                codigo = producto.id,
                descripcion = producto.descripcion,
                cantidad = cantidad,
                precio = precioUnitario,
                iva = 10,
                diez = totalConIva
            )
        )

        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val calendar = Calendar.getInstance()
        val fechasPago = mutableListOf<String>()
        fechasPago.add(fechaActual)
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val fechaSiguiente = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        fechasPago.add(fechaSiguiente)

        val venta = VentaRequest(
            fechaemision = fechaActual,
            nrofactura = "F001-${(10000..99999).random()}",
            id_cliente = clienteId,
            condicion = "CREDITO",
            timbrado = "12345678",
            detalle = detalle,
            cantpago = 2,
            fechP = fechasPago
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.crearVenta(venta)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@MainActivity, "¡Venta registrada con éxito!", Toast.LENGTH_LONG).show()
                        viewModel.cargarProductos(ApiClient.apiService)
                        productoSeleccionado = null
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMsg = errorBody ?: "Error desconocido (${response.code()})"
                        Log.e("VENTA_ERROR", "Respuesta fallida: $errorMsg")
                        Toast.makeText(this@MainActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("VENTA_ERROR", "Excepción al enviar venta", e)
                    Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_SELECCION_CLIENTE = 100
    }
}