package mx.sinsel.salesapplication
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private val retrofitCategory = Retrofit.Builder().baseUrl("http://192.168.43.241:8443/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val categoryService = retrofitCategory.create(ApiServiceCategory::class.java)
interface ApiServiceCategory {
    @GET("categories")
    suspend fun getCategories(): List<Category>
}