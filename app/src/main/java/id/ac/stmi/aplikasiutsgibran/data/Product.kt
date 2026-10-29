package id.ac.stmi.aplikasiutsgibran.data

import androidx.annotation.DrawableRes

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Int,
    val originalPrice: Int = 0,
    val discountPercent: Int = 0,
    val rating: Float = 4.8f,
    val soldCount: Int = 120,
    val stock: Int = 50,
    @DrawableRes val imageRes: Int = 0,
    val imageUrl: String = "",
    val description: String,
    val motorCompatibility: List<String> = emptyList(),
    val partNumber: String = "",
    val inStock: Boolean = true
) {
    val formattedPrice: String
        get() = "Rp " + "%,d".format(price).replace(',', '.')

    val formattedOriginalPrice: String
        get() = "Rp " + "%,d".format(originalPrice).replace(',', '.')
}
