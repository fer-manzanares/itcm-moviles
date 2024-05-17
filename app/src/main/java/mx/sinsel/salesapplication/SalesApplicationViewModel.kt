package mx.sinsel.salesapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Base64

class SalesApplicationViewModel : ViewModel() {
    private val _productState = mutableStateOf(ProductState())
    var productState: State<ProductState> = _productState
    var bitmap : Bitmap? = null
    private val _categoriesState = mutableStateOf(CategoryState())
    val categoriesState: State<CategoryState> = _categoriesState
    val productView = Product()
    val category = Category()
    var productDescription = mutableStateOf("")
    var productPrice = mutableStateOf("")
    var productQuantity = mutableStateOf("")
    var productUpc = mutableStateOf("")
    var productImage = mutableStateOf(bitmap)
    var productCategory = mutableStateOf(Category())
    var messageProductDescError = mutableStateOf(false)

    init {
        fetchCategories()
        println("fetching categories")
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = categoryService.getCategories()
                _categoriesState.value = _categoriesState.value.copy(
                    categories = response,
                    loading = false,
                    error = null
                )
                println("categories fetched")
            } catch (e: Exception) {
                _categoriesState.value = _categoriesState.value.copy(
                    loading = false,
                    error = "Error fetching Categories ${e.message}"
                )
                println("categories fetch failed: ${e.message}")
            }
        }
    }

    data class ProductState(
        val loading: Boolean = false,
        val product: Product? = null,
        val error: String? = null
    )

    data class CategoryState(
        val loading: Boolean = true,
        val categories: List<Category> = emptyList(),
        val error: String? = null
    )

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun consultProduct() {
        if (productUpc.value != "") {
            try {
                val response = productService.getProduct(productUpc.value)
                _productState.value = _productState.value.copy(
                    product = response,
                    loading = false,
                    error = null
                )
                println("Product fetched: ${response.description}")
                productDescription.value = response.description
                productPrice.value = "$ ${response.price}"
                productQuantity.value = response.quantity
                productCategory.value = response.category
                productImage.value = convertImageByteArrayToBitmap(Base64.getDecoder().decode(response.image))
            } catch (e: Exception) {
                _productState.value = _productState.value.copy(
                    loading = false,
                    error = e.message
                )
                println("Product fetched failed: ${e.message}")
            }

        } else {
            messageProductDescError.value = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createProduct() {
        productView.upc= productUpc.value
        productView.description = productDescription.value
        productView.price = productPrice.value
        productView.quantity = productQuantity.value
        productView.category = productCategory.value
        when {
            productImage.value != null -> {
                val byteArrayOutputStream = ByteArrayOutputStream()
                productImage.value!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val encoded: String = Base64.getEncoder().encodeToString(byteArray)
                productView.image = encoded
                println("Image: with image")
            }
            else -> {
                productView.image = ""
                println("Image: without image")
            }
        }
        try {
            println("Trying product creation: ${productView.toString()}")
            productService.postProduct(productView)
        } catch (e: Exception) {
            println("Product creation failed: ${e.message}")
        }

    }
}



fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
}

fun convertBitmapToByteArray(bitmap: Bitmap) : ByteArray {
    val stream = ByteArrayOutputStream()
    return stream.toByteArray()
}