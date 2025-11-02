package com.tienda.tiendaropaapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body

interface ApiService {
    @GET("api/productos")
    suspend fun getProductos(): Response<List<Producto>>
    @GET("api/clientesa")
    suspend fun getClientes(): Response<ApiResponse<List<Cliente>>>

    @POST("api/clientesa")
    suspend fun crearCliente(@Body cliente: CrearClienteRequest): Response<ApiResponse<Cliente>>

    @POST("api/ventasa")
    suspend fun crearVenta(@Body venta: VentaRequest): Response<ApiResponse<Unit>>
}

object ApiClient {
    // ⚠️ CAMBIA ESTA IP POR LA DE TU COMPUTADORA 10.11.0.130
    private const val BASE_URL ="http://192.168.1.10:8000/" //"http://10.11.0.130:8000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}