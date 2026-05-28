package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ==========================================
// ROOM ENTITIES
// ==========================================

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val link: String,
    val platform: String, // "Wed2C", "AliExpress", "eBay", "DigiStore24", etc.
    val status: String,    // "Connected", "Syncing", "Offline"
    val productCount: Int,
    val totalRevenue: Double
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val price: Double,
    val supplierCost: Double,
    val imageUrl: String,
    val affiliateLink: String,
    val category: String,
    val stock: Int,
    val sales: Int
)

@Entity(tableName = "agent_logs")
data class AgentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val agentName: String,     // "Inventory", "Marketing", "Fulfillment", "Finance"
    val subAgentName: String,  // Description of sub-agent task
    val message: String,
    val type: String           // "info", "success", "warning", "error"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val customerName: String,
    val productName: String,
    val amount: Double,
    val cost: Double,
    val paymentMethod: String, // "Credit Card", "Apple Pay", "PayPal", "USDT"
    val gatewayName: String,   // "OmniPay Hub"
    val status: String,        // "Completed", "Fulfilling", "Routed"
    val routingDestination: String, // e.g., "Wed2C Store 3" or "AliExpress"
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// ROOM DATA ACCESS OBJECTS (DAOs)
// ==========================================

@Dao
interface CommerceDao {

    // --- Stores ---
    @Query("SELECT * FROM stores")
    fun getAllStoresFlow(): Flow<List<StoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStores(stores: List<StoreEntity>)

    @Query("UPDATE stores SET status = :status WHERE id = :storeId")
    suspend fun updateStoreStatus(storeId: String, status: String)

    // --- Products ---
    @Query("SELECT * FROM products")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun getProductsByStoreFlow(storeId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // --- Agent Logs ---
    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentLogsFlow(): Flow<List<AgentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AgentLogEntity)

    @Query("DELETE FROM agent_logs")
    suspend fun clearLogs()

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
}

// ==========================================
// DATABASE CONSTRUCT
// ==========================================

@Database(
    entities = [
        StoreEntity::class,
        ProductEntity::class,
        AgentLogEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CommerceDatabase : RoomDatabase() {
    abstract fun dao(): CommerceDao

    companion object {
        @Volatile
        private var INSTANCE: CommerceDatabase? = null

        fun getDatabase(context: Context): CommerceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CommerceDatabase::class.java,
                    "omnigat_commerce_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
