package id.ac.stmi.aplikasiutsgibran.data.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import id.ac.stmi.aplikasiutsgibran.data.CartItem
import id.ac.stmi.aplikasiutsgibran.data.Order
import id.ac.stmi.aplikasiutsgibran.data.OrderStatus
import id.ac.stmi.aplikasiutsgibran.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreSyncService"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_PRODUCTS = "products"
        const val COLLECTION_USERS = "users"
    }

    /**
     * Menyimpan atau menyinkronkan data pesanan (Order) ke Cloud Firestore:
     * 1. /orders/{orderId} (Dapat dilihat admin/dashboard di Firebase Console)
     * 2. /users/{userId}/orders/{orderId} (Pesanan milik akun user)
     */
    suspend fun syncOrder(
        order: Order,
        userId: String = "user_default",
        userEmail: String = "",
        userName: String = ""
    ) = withContext(Dispatchers.IO) {
        try {
            val orderData = hashMapOf(
                "id" to order.id,
                "userId" to userId,
                "userEmail" to userEmail,
                "userName" to userName,
                "date" to order.date,
                "status" to order.status.name,
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "discount" to order.discount,
                "totalAmount" to order.totalAmount,
                "deliveryAddress" to order.deliveryAddress,
                "courierName" to order.courierName,
                "courierPhone" to order.courierPhone,
                "courierDriverName" to (order.courierDriverName ?: ""),
                "courierVehicle" to (order.courierVehicle ?: ""),
                "resiNumber" to order.resiNumber,
                "estimatedArrival" to order.estimatedArrival,
                "paymentMethod" to order.paymentMethod,
                "vaNumber" to (order.vaNumber ?: ""),
                "items" to order.items.map { item ->
                    hashMapOf(
                        "productId" to item.product.id,
                        "name" to item.product.name,
                        "brand" to item.product.brand,
                        "category" to item.product.category,
                        "price" to item.product.price,
                        "quantity" to item.quantity,
                        "imageUrl" to item.product.imageUrl
                    )
                },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            // Simpan di root koleksi 'orders'
            firestore.collection(COLLECTION_ORDERS)
                .document(order.id)
                .set(orderData, SetOptions.merge())
                .await()

            // Simpan juga di sub-koleksi user jika userId valid
            if (userId.isNotBlank() && userId != "user_default") {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection("orders")
                    .document(order.id)
                    .set(orderData, SetOptions.merge())
                    .await()
            }

            Log.d(TAG, "Order ${order.id} berhasil disinkronkan ke Cloud Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal sinkronisasi order ${order.id} ke Firestore: ${e.message}", e)
        }
    }

    /**
     * Memperbarui status pesanan di Firestore (misalnya saat pembayaran sukses)
     */
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        userId: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val updates = mapOf(
                "status" to newStatus.name,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(updates)
                .await()

            if (!userId.isNullOrBlank() && userId != "user_default") {
                firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection("orders")
                    .document(orderId)
                    .update(updates)
                    .await()
            }
            Log.d(TAG, "Status order $orderId berhasil diupdate ke ${newStatus.name} di Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal update status order $orderId di Firestore: ${e.message}", e)
        }
    }

    /**
     * Menyimpan profil pengguna ke Firestore /users/{userId}
     */
    suspend fun syncUserProfile(
        userId: String,
        name: String,
        email: String,
        phone: String = "",
        primaryPaymentMethod: String = "gopay"
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "user_default") return@withContext
        try {
            val userData = hashMapOf(
                "uid" to userId,
                "name" to name,
                "email" to email,
                "phone" to phone,
                "primaryPaymentMethod" to primaryPaymentMethod,
                "lastActive" to FieldValue.serverTimestamp()
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(userData, SetOptions.merge())
                .await()
            Log.d(TAG, "Profil user $userId berhasil disinkronkan ke Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal sinkronisasi profil user ke Firestore: ${e.message}", e)
        }
    }

    /**
     * Menyinkronkan item keranjang user ke /users/{userId}/cart/items
     */
    suspend fun syncCart(
        userId: String,
        items: List<CartItem>
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "user_default") return@withContext
        try {
            val cartData = hashMapOf(
                "itemCount" to items.size,
                "totalPrice" to items.filter { it.selected }.sumOf { it.product.price * it.quantity },
                "updatedAt" to FieldValue.serverTimestamp(),
                "items" to items.map { item ->
                    hashMapOf(
                        "productId" to item.product.id,
                        "name" to item.product.name,
                        "brand" to item.product.brand,
                        "price" to item.product.price,
                        "quantity" to item.quantity,
                        "selected" to item.selected,
                        "imageUrl" to item.product.imageUrl
                    )
                }
            )
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection("cart")
                .document("current_cart")
                .set(cartData)
                .await()
            Log.d(TAG, "Keranjang user $userId berhasil disinkronkan ke Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal sinkronisasi keranjang ke Firestore: ${e.message}", e)
        }
    }

    /**
     * Mengisi (seed) data katalog produk ke Cloud Firestore jika koleksi masih kosong,
     * sehingga dapat langsung dilihat di Firebase Console.
     */
    suspend fun seedInitialProducts(products: List<Product>) = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_PRODUCTS).limit(1).get().await()
            if (snapshot.isEmpty) {
                Log.d(TAG, "Koleksi products kosong. Menjalankan batch seeding produk ke Firestore...")
                val batch = firestore.batch()
                products.take(20).forEach { p ->
                    val docRef = firestore.collection(COLLECTION_PRODUCTS).document(p.id)
                    val data = hashMapOf(
                        "id" to p.id,
                        "name" to p.name,
                        "brand" to p.brand,
                        "category" to p.category,
                        "price" to p.price,
                        "originalPrice" to p.originalPrice,
                        "discountPercent" to p.discountPercent,
                        "rating" to p.rating,
                        "soldCount" to p.soldCount,
                        "stock" to p.stock,
                        "inStock" to p.inStock,
                        "partNumber" to p.partNumber,
                        "imageUrl" to p.imageUrl,
                        "description" to p.description,
                        "motorCompatibility" to p.motorCompatibility
                    )
                    batch.set(docRef, data)
                }
                batch.commit().await()
                Log.d(TAG, "Berhasil men-seed 20 produk katalog ke Cloud Firestore.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal seeding produk ke Firestore: ${e.message}", e)
        }
    }
}

