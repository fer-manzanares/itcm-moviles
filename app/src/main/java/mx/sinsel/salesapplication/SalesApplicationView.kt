package mx.sinsel.salesapplication

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ProductScreen(modifier: Modifier = Modifier) {
    val salesApplicationViewModel: SalesApplicationViewModel = viewModel()
    var description by remember { salesApplicationViewModel.productDescription }
    var upc by remember { salesApplicationViewModel.productUpc }
    var price by remember { salesApplicationViewModel.productPrice }
    var quantity by remember { salesApplicationViewModel.productQuantity }
    var category by remember { salesApplicationViewModel.productCategory }
    val categoriesState by salesApplicationViewModel.categoriesState

    when {
        !categoriesState.loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            )  {
                Text("Creación/Consulta de producto", style = MaterialTheme.typography.headlineLarge)
                Box {
                    OutlinedTextField(
                        value = upc,
                        onValueChange = { upc = it },
                        label = { Text(text = "Código de barras") }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Box {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { salesApplicationViewModel.productDescription.value = it },
                        label = { Text(text = "Descripción") }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Box {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text(text = "Precio") }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Box {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text(text = "Existencia") }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                createCategoriesDropDown(categories = categoriesState.categories, salesApplicationViewModel)
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            salesApplicationViewModel.createProduct()
                        }
                    })
                    {
                       Text(text = "Crear")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            salesApplicationViewModel.consultProduct()
                        }
                    }) {
                        Text(text = "Consultar")
                    }
                }
            }
        } categoriesState.error != null -> {
        Text("ERROR OCCURRED: $categoriesState.error")
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
    var categoryText by remember { mutableStateOf("Categoría") }
    Box {
        // Input Button
        Button(onClick = { isExpanded = true }) {
            Text(text = categoryText)
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
                        categoryText = category.name
                    }
                )
            }
            println("${categories.size} categorías")
            Log.i("INFO_DEBUG", "${categories.size} categorías")
        }

    }


}