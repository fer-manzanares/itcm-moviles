package mx.sinsel.salesapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects
import java.util.regex.Pattern

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProductScreen(modifier: Modifier = Modifier, activity: ComponentActivity) {
    val salesApplicationViewModel: SalesApplicationViewModel = viewModel()
    var description by remember { salesApplicationViewModel.productDescription }
    var upc by remember { salesApplicationViewModel.productUpc }
    var price by remember { salesApplicationViewModel.productPrice }
    var quantity by remember { salesApplicationViewModel.productQuantity }
    var image by remember { salesApplicationViewModel.productImage }
    val categoriesState by salesApplicationViewModel.categoriesState

    val imageModifier = Modifier
        .fillMaxWidth()
        .wrapContentSize(Alignment.Center)
        .width(150.dp)
        .height(150.dp)
        .border(BorderStroke(1.dp, Color.Black))

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        salesApplicationViewModel.productImage.value =
            result.data?.extras?.get("data") as Bitmap ?: null
    }

    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

    val cameraPermissionRequest =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                launcher.launch(intent)
            } else {
                Toast.makeText(
                    activity,
                    "Permiso de uso de cámara no otorgado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    when {
        !categoriesState.loading && categoriesState.error == null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Creación/Consulta de producto",
                    style = MaterialTheme.typography.headlineSmall
                )
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
                createCategoriesDropDown(
                    categories = categoriesState.categories,
                    salesApplicationViewModel
                )

                Spacer(modifier = Modifier.height(8.dp))
                when {
                    image != null -> {
                        Image(
                            bitmap = image!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = imageModifier
                        )
                    }

                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.nodisponible),
                            contentDescription = "Imagen no disponible",
                            modifier = imageModifier,
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    try {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                activity,
                                Manifest.permission.CAMERA
                            ) -> {
                                launcher.launch(intent)
                            }
                            else -> {
                                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
                            }
                        }
                    } catch (e: Exception) {
                        println("Camera error: ${e.message}")
                        Toast.makeText(
                            activity,
                            "Permiso de uso de cámara no otorgado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
                    Icon(
                        painter = rememberVectorPainter(image = rememberPhotoCamera()),
                        contentDescription = ""
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    Button(onClick = {
                        if (!isValidUpc(upc)) {
                            val toast = Toast.makeText(
                                activity,
                                "El UPC del producto es obligatorio y debe estar formado por 12 dígitos",
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        } else if (!isValidDescription(description)) {
                            val toast = Toast.makeText(
                                activity,
                                "La descripción del producto es obligatoria",
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        } else if (!isValidPrice(price)) {
                            val toast = Toast.makeText(
                                activity,
                                "El precio del producto es obligatorio y solo se aceptan números positivos",
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        } else if (!isValidQuantity(quantity)) {
                            val toast = Toast.makeText(
                                activity,
                                "La existencia/cantidad del producto es obligatorio y solo se aceptan números enteros positivos",
                                Toast.LENGTH_LONG
                            )
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
                        if (!isValidUpc(upc)) {
                            val toast = Toast.makeText(
                                activity,
                                "El UPC del producto es obligatorio y debe contener 12 dígitos",
                                Toast.LENGTH_LONG
                            )
                            toast.show()
                        } else CoroutineScope(Dispatchers.IO).launch {
                            salesApplicationViewModel.consultProduct()
                        }
                    }) {
                        Text(text = "Consultar")
                    }
                }
            }
        }

        categoriesState.error != null -> {
            Text("Fallo de comunicación con el servicio, intente más tarde")
            val toast = Toast.makeText(
                activity,
                "ERROR OCCURRED: $categoriesState.error",
                Toast.LENGTH_LONG
            )
            toast.show()
        }

        else -> {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator(modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun createCategoriesDropDown(
    categories: List<Category>,
    salesApplicationViewModel: SalesApplicationViewModel
) {
    var isExpanded by remember { mutableStateOf(false) }
    var categorySelected by remember { salesApplicationViewModel.productCategory }
    Box {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {       // Input Button
            Text(
                modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
                text = "Categoría",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = { isExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = categorySelected.name)
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Arrow Down"
                )

            }
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 2.dp)
            ) {
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

}


fun Context.createImageFile(): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir      /* directory */
    )
    return image
}

fun isValidUpc(upc: String): Boolean {
    var pattern = Pattern.compile("\\d{12}")
    var mattcher = pattern.matcher(upc)
    return mattcher.matches()
}

fun isValidDescription(description: String): Boolean {
    if (description != "" && description != null)
        return true
    else
        return false
}

fun isValidPrice(price: String): Boolean {
    var pattern = Pattern.compile("((\\+|-)?([0-9]+)(\\.[0-9]+)?)|((\\+|-)?\\.?[0-9]+)")
    var mattcher = pattern.matcher(price)
    return mattcher.matches()
}


fun isValidQuantity(quantity: String): Boolean {
    var pattern = Pattern.compile("[0-9]+")
    var mattcher = pattern.matcher(quantity)
    return mattcher.matches()
}

@Composable
fun rememberPhotoCamera(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "photo_camera",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20f, 28.792f)
                quadToRelative(3f, 0f, 5.021f, -2.021f)
                reflectiveQuadToRelative(2.021f, -5.021f)
                quadToRelative(0f, -3f, -2.021f, -5.021f)
                reflectiveQuadTo(20f, 14.708f)
                quadToRelative(-3f, 0f, -5.021f, 2.021f)
                reflectiveQuadToRelative(-2.021f, 5.021f)
                quadToRelative(0f, 3f, 2.021f, 5.021f)
                reflectiveQuadTo(20f, 28.792f)
                close()
                moveTo(6.25f, 34.75f)
                quadToRelative(-1.083f, 0f, -1.854f, -0.792f)
                quadToRelative(-0.771f, -0.791f, -0.771f, -1.833f)
                verticalLineToRelative(-20.75f)
                quadToRelative(0f, -1.042f, 0.771f, -1.833f)
                quadToRelative(0.771f, -0.792f, 1.854f, -0.792f)
                horizontalLineToRelative(5.833f)
                lineToRelative(2.209f, -2.625f)
                quadToRelative(0.375f, -0.417f, 0.896f, -0.646f)
                quadToRelative(0.52f, -0.229f, 1.104f, -0.229f)
                horizontalLineToRelative(7.416f)
                quadToRelative(0.584f, 0f, 1.104f, 0.229f)
                quadToRelative(0.521f, 0.229f, 0.896f, 0.646f)
                lineToRelative(2.209f, 2.625f)
                horizontalLineToRelative(5.833f)
                quadToRelative(1.042f, 0f, 1.833f, 0.792f)
                quadToRelative(0.792f, 0.791f, 0.792f, 1.833f)
                verticalLineToRelative(20.75f)
                quadToRelative(0f, 1.042f, -0.792f, 1.833f)
                quadToRelative(-0.791f, 0.792f, -1.833f, 0.792f)
                close()
                moveToRelative(27.5f, -2.625f)
                verticalLineToRelative(-20.75f)
                horizontalLineTo(6.25f)
                verticalLineToRelative(20.75f)
                close()
                moveTo(20f, 21.75f)
                close()
            }
        }.build()
    }
}