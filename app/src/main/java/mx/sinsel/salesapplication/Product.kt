package mx.sinsel.salesapplication

data class Product(
    var id : Int = 0,
    var upc : String = "",
    var description : String = "",
    var price : String = "0",
    var quantity : String = "0",
    var image : String = "",
    var category : Category = Category()
)
