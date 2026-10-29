package id.ac.stmi.aplikasiutsgibran.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val productId: String,
    val name: String,
    val brand: String,
    val price: Int,
    val imageRes: Int,
    val imageUrl: String = "",
    val category: String,
    val quantity: Int,
    val isSelected: Boolean
)
