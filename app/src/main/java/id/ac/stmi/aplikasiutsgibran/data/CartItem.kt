package id.ac.stmi.aplikasiutsgibran.data

data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selected: Boolean = true
) {
    val totalPrice: Int
        get() = product.price * quantity

    val formattedTotalPrice: String
        get() = "Rp " + "%,d".format(totalPrice).replace(',', '.')
}
