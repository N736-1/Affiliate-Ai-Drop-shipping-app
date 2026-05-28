package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.random.Random

class CommerceRepository(private val dao: CommerceDao) {

    // Exposure of core database flows
    val stores: Flow<List<StoreEntity>> = dao.getAllStoresFlow()
    val products: Flow<List<ProductEntity>> = dao.getAllProductsFlow()
    val logs: Flow<List<AgentLogEntity>> = dao.getRecentLogsFlow()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactionsFlow()

    suspend fun addLog(agent: String, subAgent: String, message: String, type: String) {
        dao.insertLog(AgentLogEntity(agentName = agent, subAgentName = subAgent, message = message, type = type))
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateStoreStatus(storeId: String, status: String) {
        dao.updateStoreStatus(storeId, status)
    }

    // Populate the database with initial elements if empty
    suspend fun populateInitialDataIfEmpty() {
        val currentStores = stores.first()
        if (currentStores.isNotEmpty()) {
            return
        }

        // Define initial e-commerce and affiliate stores requested by user
        val initialStores = listOf(
            StoreEntity("wed2c_theamericane", "Wed2C (The Americane)", "https://httpstheamericane.wed2c.com", "Wed2C", "Connected", 2, 450.00),
            StoreEntity("wed2c_americanemporiu", "Wed2C (The American Emporiu)", "https://theamericanemporiu.wed2c.com", "Wed2C", "Connected", 2, 720.00),
            StoreEntity("wed2c_sellonlinestore1", "Wed2C (SellOnlineStore1)", "https://sellonlinestore1.wed2c.com", "Wed2C", "Connected", 1, 140.00),
            StoreEntity("wed2c_sellbazzarshop", "Wed2C (SellBazzarShop)", "https://sellbazzarshop.wed2c.com", "Wed2C", "Connected", 1, 350.00),
            StoreEntity("wed2c_dropshippingshop", "Wed2C (DropShippingShop)", "https://dropshippingshop.wed2c.com", "Wed2C", "Connected", 1, 1200.00),
            StoreEntity("wed2c_bazzarstore", "Wed2C (BazzarStore)", "https://bazzarstore.wed2c.com", "Wed2C", "Connected", 1, 199.90),
            StoreEntity("wed2c_bazaarshop", "Wed2C (BazaarShop)", "https://bazaarshop.wed2c.com", "Wed2C", "Connected", 1, 280.00),
            StoreEntity("wed2c_onlinedropstore", "Wed2C (OnlineDropStore)", "https://onlinedropstore.wed2c.com", "Wed2C", "Connected", 1, 150.00),
            StoreEntity("wed2c_sellonlinestore", "Wed2C (SellOnlineStore)", "https://sellonlinestore.wed2c.com", "Wed2C", "Connected", 1, 1899.90),
            
            StoreEntity("aliexpress", "AliExpress Main", "https://www.aliexpress.com", "AliExpress", "Connected", 2, 3210.50),
            StoreEntity("ebay", "eBay Store Hub", "https://www.ebay.com", "eBay", "Connected", 1, 980.00),
            StoreEntity("daraz_pk", "Daraz.pk Punjab Outlet", "https://www.daraz.pk", "Daraz PK", "Connected", 1, 180.00),
            StoreEntity("cjdropshipping", "CJ Dropshipping Fulfill", "https://www.cjdropshipping.com/contactus#online", "CJ Dropship", "Connected", 1, 590.00),
            StoreEntity("digistore24_promo", "DigiStore24 (GlobalWarming)", "https://www.digistore24.com/redir/431152/globalwarming/", "DigiStore24", "Connected", 1, 470.00),
            StoreEntity("digistore24_portal", "DigiStore24 Control Panel", "https://www.digistore24.com/login", "DigiStore24", "Connected", 0, 0.0),
            StoreEntity("mercedes_affiliate", "Mercedes-Benz Birmingham Affiliates", "https://www.mbbhm.com/finance/affiliates/", "Mercedes Benz", "Connected", 1, 1350.00)
        )

        dao.insertStores(initialStores)

        // Products Catalog mapping to each store
        val initialProducts = listOf(
            // Wed2C
            ProductEntity("p_ambient_sunset", "wed2c_theamericane", "Ambient Smart Sunset Projector", 24.99, 9.50, "", "https://httpstheamericane.wed2c.com", "Home Decor", 150, 18),
            ProductEntity("p_aurora_light", "wed2c_theamericane", "Northern Lights Crystal Globe", 34.00, 14.00, "", "https://httpstheamericane.wed2c.com", "Home Decor", 80, 5),
            
            ProductEntity("p_mini_projector", "wed2c_americanemporiu", "High-Definition Portable Mini Projector 4K", 89.00, 34.00, "", "https://theamericanemporiu.wed2c.com", "Electronics", 45, 8),
            ProductEntity("p_hum_diffuser", "wed2c_americanemporiu", "Anti-Gravity Flame Air Humidifier", 32.50, 11.00, "", "https://theamericanemporiu.wed2c.com", "Home Decor", 120, 14),

            ProductEntity("p_gym_bands", "wed2c_sellonlinestore1", "Pro Gym Heavy Resistance Band Set", 14.99, 4.00, "", "https://sellonlinestore1.wed2c.com", "Fitness", 250, 10),
            ProductEntity("p_ar_humidifier", "wed2c_sellbazzarshop", "Modern Ultrasonic Humidifier Diffuser", 35.00, 12.50, "", "https://sellbazzarshop.wed2c.com", "Electronics", 90, 10),
            ProductEntity("p_cat_litter", "wed2c_dropshippingshop", "Automatic Self-Cleaning Cat Litter Box Wifi", 299.00, 125.00, "", "https://dropshippingshop.wed2c.com", "Pet Supplies", 12, 4),
            ProductEntity("p_ergo_keypad", "wed2c_bazzarstore", "Wireless Ergonomic Mechanical Keypad", 19.99, 6.00, "", "https://bazzarstore.wed2c.com", "Tech Hardware", 180, 10),
            ProductEntity("p_sonic_toothbrush", "wed2c_bazaarshop", "Rechargeable Multi-Sonic Toothbrush", 28.00, 9.50, "", "https://bazaarshop.wed2c.com", "Dental Care", 300, 10),
            ProductEntity("p_glass_planter", "wed2c_onlinedropstore", "Nordic Glass Desktop Planter Wood Stand", 15.00, 4.50, "", "https://onlinedropstore.wed2c.com", "Home Decor", 400, 10),
            ProductEntity("p_foldable_drone", "wed2c_sellonlinestore", "Compact Foldable Drone with UHD 4K Camera", 189.99, 65.00, "", "https://sellonlinestore.wed2c.com", "Tech Hardware", 30, 10),

            // AliExpress
            ProductEntity("p_smart_watch", "aliexpress", "Luxury Bluetooth Smart Watch v4", 39.99, 12.00, "", "https://www.aliexpress.com", "Electronics", 500, 80),
            ProductEntity("p_ergo_chair", "aliexpress", "Ergonomic Lumbar Office Gaming Chair", 149.99, 45.00, "", "https://www.aliexpress.com", "Furniture", 60, 21),

            // eBay
            ProductEntity("p_coffee_grinder", "ebay", "Premium Stainless Coffee Grinder Handcrank", 19.50, 5.00, "", "https://www.ebay.com", "Kitchenware", 320, 50),

            // Daraz.pk
            ProductEntity("p_peshawari_chappal", "daraz_pk", "Daraz PK Genuine Peshawari Sandal", 18.00, 7.50, "", "https://www.daraz.pk", "Footwear", 150, 10),

            // CJ Dropshipping
            ProductEntity("p_leather_bag", "cjdropshipping", "CJ Dropship Vintage Leather Messenger Bag", 59.00, 18.00, "", "https://www.cjdropshipping.com/contactus#online", "Accessories", 110, 10),

            // DigiStore24
            ProductEntity("p_keto_system", "digistore24_promo", "Metabolic Keto System Lifestyle Ecode", 47.00, 14.10, "", "https://www.digistore24.com/redir/431152/globalwarming/", "Digital Programs", 9999, 10),

            // Mercedes Benz Affiliate
            ProductEntity("p_benz_keychain", "merced_affiliate", "Mercedes-Benz Birmingham Carbon Fiber Keychain", 45.00, 38.25, "", "https://www.mbbhm.com/finance/affiliates/", "Autocare & Gifts", 100, 30)
        )

        dao.insertProducts(initialProducts)

        // Seed some historic simulated transactions
        val customers = listOf("Zara Ali", "Michael Vance", "Haris Khan", "Sofia Geller", "Dimitri Volk", "Amara Okafor", "Kenji Sato", "Elena Rostova")
        val billingMethods = listOf("Credit Card", "Apple Pay", "PayPal", "USDT")
        
        for (i in 1..6) {
            val product = initialProducts[Random.nextInt(initialProducts.size)]
            val transaction = TransactionEntity(
                id = "TXN-${100000 + i}",
                customerName = customers[Random.nextInt(customers.size)],
                productName = product.name,
                amount = product.price,
                cost = product.supplierCost,
                paymentMethod = billingMethods[Random.nextInt(billingMethods.size)],
                gatewayName = "OmniPay Hub",
                status = if (Random.nextBoolean()) "Completed" else "Routed",
                routingDestination = initialStores.find { it.id == product.storeId }?.name ?: "Supplier Sync",
                timestamp = System.currentTimeMillis() - (i * 3600000 * 4) // spaced apart by hours
            )
            dao.insertTransaction(transaction)
        }

        // Add startup agent logs
        addLog(
            agent = "Inventory Agent",
            subAgent = "Spec Scraper",
            message = "Initialized web scraping connections for 9 Wed2C stores and AliExpress API.",
            type = "success"
        )
        addLog(
            agent = "Marketing Agent",
            subAgent = "Visual Generator",
            message = "Simulated Social media ad pixels attached for Facebook Pixel and TikTok Ads manager.",
            type = "info"
        )
        addLog(
            agent = "Fulfillment Agent",
            subAgent = "Risk Checker",
            message = "Auto-checkout gateway mapped securely. 16 products successfully matched with suppliers.",
            type = "success"
        )
        addLog(
            agent = "Finance Agent",
            subAgent = "Cost Calculator",
            message = "Awaiting live orders to evaluate SaaS cloud ROI. Standard hosting maintenance budget set to $110.00/mo.",
            type = "info"
        )
    }

    // Export complete data to standard text format / AppSheet CSV simulation format
    suspend fun generateExportData(format: String): String {
        val currentStores = stores.first()
        val currentProducts = products.first()
        val currentTransactions = transactions.first()

        val sb = StringBuilder()
        if (format == "CSV") {
            // AppSheet-friendly CSV content
            sb.append("--- OMNIGATE DATA EXPORT (APP SHEET CSV COMPATIBLE) ---\n\n")
            
            sb.append("[STORES]\n")
            sb.append("StoreId,Name,Link,Platform,Status,Products,Revenue USD\n")
            currentStores.forEach {
                sb.append("${it.id},\"${it.name}\",\"${it.link}\",${it.platform},${it.status},${it.productCount},${it.totalRevenue}\n")
            }
            
            sb.append("\n[PRODUCTS]\n")
            sb.append("ProductId,StoreId,Name,Price,SupplierCost,AffiliateLink,Stock,Sales\n")
            currentProducts.forEach {
                sb.append("${it.id},${it.storeId},\"${it.name}\",${it.price},${it.supplierCost},\"${it.affiliateLink}\",${it.stock},${it.sales}\n")
            }
            
            sb.append("\n[TRANSACTIONS]\n")
            sb.append("TxnId,Customer,Product,AmountPaid,SupplierCost,PaymentMethod,Status,RoutingTarget,Timestamp\n")
            currentTransactions.forEach {
                sb.append("${it.id},\"${it.customerName}\",\"${it.productName}\",${it.amount},${it.cost},${it.paymentMethod},${it.status},\"${it.routingDestination}\",${it.timestamp}\n")
            }

        } else {
            // Text Document Format
            sb.append("========================================================\n")
            sb.append("           OMNIGATE COMMERCE MANAGER - REPORT           \n")
            sb.append("========================================================\n")
            sb.append("Report Type: Live Server Database Sync\n")
            sb.append("Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}\n")
            sb.append("--------------------------------------------------------\n\n")

            sb.append("I. INTEGRATED STORES (${currentStores.size} Total outlets)\n")
            currentStores.forEach {
                sb.append(" - Name: ${it.name}\n   Platform: ${it.platform} | Status: ${it.status}\n   Link: ${it.link}\n   Revenue generated: $${it.totalRevenue}\n\n")
            }

            sb.append("II. PRODUCT DOCK (${currentProducts.size} Items Online)\n")
            currentProducts.forEach {
                sb.append(" + SKU [${it.id}] ${it.name}\n   Store Target ID: ${it.storeId}\n   Sale Price: $${it.price} (Margin: $${String.format("%.2f", it.price - it.supplierCost)})\n   In Stock: ${it.stock} | Total Sales: ${it.sales}\n\n")
            }

            sb.append("III. HISTORIC PAYMENT TRANSACTION SWARM (${currentTransactions.size} Records)\n")
            currentTransactions.forEach {
                sb.append(" * TXN CODE: ${it.id} | Client: ${it.customerName}\n   Product: ${it.productName}\n   Charged: $${it.amount} via ${it.paymentMethod}\n   Status: ${it.status} -> routed to [${it.routingDestination}]\n\n")
            }
            sb.append("==================== END OF REPORT ====================\n")
        }

        return sb.toString()
    }
}
