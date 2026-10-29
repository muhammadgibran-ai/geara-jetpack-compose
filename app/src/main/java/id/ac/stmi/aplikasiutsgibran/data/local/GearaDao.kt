package id.ac.stmi.aplikasiutsgibran.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GearaDao {

    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity): Long

    @Update
    suspend fun updateCartItem(item: CartEntity): Int

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItemById(productId: String): Int

    @Query("DELETE FROM cart_items WHERE isSelected = 1")
    suspend fun clearSelectedCartItems(): Int

    // --- Product Catalog Offline Caching ---
    @Query("SELECT * FROM products_cache")
    suspend fun getAllCachedProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedProducts(products: List<ProductEntity>): List<Long>
}

