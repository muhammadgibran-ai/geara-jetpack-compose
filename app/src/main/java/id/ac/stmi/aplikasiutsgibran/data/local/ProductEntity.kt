package id.ac.stmi.aplikasiutsgibran.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room SQLite untuk penyimpanan cache lokal katalog suku cadang.
 * Memastikan aplikasi tetap menampilkan data produk secara instan dan andal
 * saat perangkat berada di area bengkel atau jalanan dengan sinyal seluler offline.
 */
@Entity(tableName = "products_cache")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String,
    val category: String,
    val price: Int,
    val originalPrice: Int,
    val discountPercent: Int,
    val rating: Float,
    val soldCount: Int,
    val stock: Int,
    val inStock: Boolean,
    val imageRes: Int,
    val imageUrl: String,
    val description: String,
    val motorCompatibility: String, // format CSV
    val partNumber: String
)
