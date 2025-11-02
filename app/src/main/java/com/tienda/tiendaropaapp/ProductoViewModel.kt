package com.tienda.tiendaropaapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    fun cargarProductos(apiService: ApiService) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getProductos()
                if (response.isSuccessful) {
                    val lista = response.body()?.filter { it.estado == 1 }?.sortedWith(
                        compareByDescending<Producto> {
                            val stock = it.stock.toDoubleOrNull() ?: 0.0
                            stock > 0
                        }
                            .thenByDescending {
                                it.stock.toDoubleOrNull() ?: 0.0
                            }
                    ) ?: emptyList()

                    _productos.postValue(lista)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}