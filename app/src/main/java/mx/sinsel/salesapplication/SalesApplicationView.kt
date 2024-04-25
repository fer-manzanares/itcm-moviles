package mx.sinsel.salesapplication

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductScreen(modifier: Modifier = Modifier, activity : ComponentActivity) {
    val salesApplicationViewModel: SalesApplicationViewModel = viewModel()
    var description by remember { salesApplicationViewModel.productDescription }
    var upc by remember { salesApplicationViewModel.productUpc }
    var price by remember { salesApplicationViewModel.productPrice }
    var quantity by remember { salesApplicationViewModel.productQuantity }
    var image by remember {salesApplicationViewModel.productImage}
    val categoriesState by salesApplicationViewModel.categoriesState

    when {
        !categoriesState.loading && categoriesState.error == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )  {
                Text("Creación/Consulta de producto", style = MaterialTheme.typography.headlineSmall)
                Box {
                    OutlinedTextField(
                        value = upc,
                        onValueChange = { upc = it },
                        label = { Text(text = "Código de barras") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { salesApplicationViewModel.productDescription.value = it },
                        label = { Text(text = "Descripción") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(text = "Precio") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(text = "Existencia") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                createCategoriesDropDown(categories = categoriesState.categories, salesApplicationViewModel)

                Spacer(modifier = Modifier.height(8.dp))
                image?.let {
                    Image(
                        bitmap =  it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .wrapContentSize()
                            .aspectRatio(.5f)
                            .size(75.dp)
                            .border(BorderStroke(1.dp, Color.Black))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Button(onClick = {
                        if (!isValidUpc(upc)){
                            val toast = Toast.makeText(activity, "El UPC del producto es obligatorio y debe estar formado por 12 dígitos", Toast.LENGTH_LONG)
                            toast.show()
                        }else if (!isValidDescription(description)){
                            val toast = Toast.makeText(activity, "La descripción del producto es obligatoria", Toast.LENGTH_LONG)
                            toast.show()
                        } else if (!isValidPrice(price)){
                            val toast = Toast.makeText(activity, "El precio del producto es obligatorio y solo se aceptan números positivos", Toast.LENGTH_LONG)
                            toast.show()
                        } else if (!isValidQuantity(quantity)){
                            val toast = Toast.makeText(activity, "La existencia/cantidad del producto es obligatorio y solo se aceptan números enteros positivos", Toast.LENGTH_LONG)
                            toast.show()
                        } else CoroutineScope(Dispatchers.IO).launch {
                            salesApplicationViewModel.createProduct()
                        }
                    })
                    {
                       Text(text = "Crear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if(!isValidUpc(upc)){
                            val toast = Toast.makeText(activity, "El UPC del producto es obligatorio y debe contener 12 dígitos", Toast.LENGTH_LONG)
                            toast.show()
                        }else CoroutineScope(Dispatchers.IO).launch {
                            salesApplicationViewModel.consultProduct()
                        }
                    }) {
                        Text(text = "Consultar")
                    }
                }
            }
        } categoriesState.error != null ->{
        Text("Fallo de comunicación con el servicio, intente más tarde")
        val toast = Toast.makeText(activity, "ERROR OCCURRED: $categoriesState.error", Toast.LENGTH_LONG)
        toast.show()
        }else -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator(modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun createCategoriesDropDown(categories: List<Category>, salesApplicationViewModel: SalesApplicationViewModel) {
    var isExpanded by remember { mutableStateOf(false) }
    var categorySelected by remember { salesApplicationViewModel.productCategory }
    Box {
        // Input Button
        Button(onClick = { isExpanded = true }) {
            Text(text = categorySelected.name)
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Arrow Down"
            )
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }, modifier = Modifier
            .fillMaxWidth()
            .padding(all = 2.dp)) {
            for (category in categories) {
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        isExpanded = false
                        categorySelected = category
                    }
                )
            }
            println("${categories.size} categorías")
            Log.i("INFO_DEBUG", "${categories.size} categorías")
        }

    }


}


fun isValidUpc(upc : String): Boolean {
    var pattern = Pattern.compile("\\d{12}")
    var mattcher = pattern.matcher(upc)
    return mattcher.matches()
}

fun isValidDescription(description : String): Boolean {
    if( description != "" && description != null)
        return true
    else
        return false
}

fun isValidPrice(price : String): Boolean {
    var pattern = Pattern.compile("^([+]?\\d*\\.?\\d*)")
    var mattcher = pattern.matcher(price)
    return mattcher.matches()
}


fun isValidQuantity(quantity : String): Boolean {
    var pattern = Pattern.compile("[+]?\\d*")
    var mattcher = pattern.matcher(quantity)
    return mattcher.matches()
}
