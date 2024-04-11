package mx.sinsel.salesapplication

import android.icu.math.BigDecimal

data class Category(
    var id : Int= 0,
    var name : String = "",
    var iva : String = "",
    var ieps : String = "",
    var discount : String = "")
