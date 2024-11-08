package mx.sinsel.salesapplication

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private val retrofitProduct = Retrofit.Builder().baseUrl("http://192.168.231.14:8080/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val productService = retrofitProduct.create(ApiServiceProduct::class.java)

interface ApiServiceProduct {
    @GET("products/productUpc")
    suspend fun getProduct(@Query("upc") upc : String): Product

    @POST("products")
    // on below line we are creating a method to post our data.
    suspend fun postProduct(@Body product: Product?): Call<Product?>?
}
