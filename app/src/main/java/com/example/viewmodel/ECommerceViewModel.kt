package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgentLogEntity
import com.example.data.CommerceDatabase
import com.example.data.CommerceRepository
import com.example.data.ProductEntity
import com.example.data.StoreEntity
import com.example.data.TransactionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class ActiveScreen {
    Dashboard,
    AIAgents,
    Storefronts,
    MarketingSocial,
    FinanceCosts,
    DataExporter
}

data class SocialAdCampaign(
    val platform: String, // "Facebook", "Google Ads", "YouTube", "TikTok", "WhatsApp", "Instagram"
    val budget: Double,
    val status: String, // "Running", "Paused", "Optimizing"
    val impressions: Int,
    val clicks: Int,
    val conversions: Int,
    val currentCpc: Double,
    val promoText: String
)

class ECommerceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CommerceDatabase.getDatabase(application)
    private val repository = CommerceRepository(db.dao())

    // UI screen state
    var selectedScreen = mutableStateOf(ActiveScreen.Dashboard)
    
    // UI detail bottom sheets or dialog states
    var selectedStore = mutableStateOf<StoreEntity?>(null)
    var selectedProduct = mutableStateOf<ProductEntity?>(null)
    var testPaymentOutput = mutableStateOf<String?>(null)
    var currentExportFormat = mutableStateOf("TXT") // "TXT" or "CSV"
    var exportResult = mutableStateOf("")

    // Agent animation status loaders
    val inventoryAgentState = MutableStateFlow("IDLE") // "IDLE", "SCANNING", "SYNCING"
    val marketingAgentState = MutableStateFlow("IDLE") // "IDLE", "GENERATING", "POSTING"
    val fulfillmentAgentState = MutableStateFlow("IDLE") // "IDLE", "VERIFYING", "ROUTING"
    val financeAgentState = MutableStateFlow("IDLE") // "IDLE", "CALCULATING", "AUDITING"

    // Simulation activity setting
    var isSimulationRunning = mutableStateOf(true)
    var lastSimulatedOrderTime = mutableStateOf<Long?>(null)
    var lastSimulatedOrderMsg = mutableStateOf<String?>(null)

    // User sliding budgets for maintenance costs
    var adBudgetSlider = mutableStateOf(500f) // default $500/month
    var serverBaseCost = 80.00
    var apiTokensCost = 120.00
    var proxyScraperCost = 45.00

    // Database flows exposed reactively
    val storesState: StateFlow<List<StoreEntity>> = repository.stores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productsState: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logsState: StateFlow<List<AgentLogEntity>> = repository.logs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsState: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Social Media Campaigns State
    private val _campaigns = MutableStateFlow<List<SocialAdCampaign>>(emptyList())
    val campaigns = _campaigns.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.populateInitialDataIfEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                initializeSocialCampaigns()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            startAutonomousAgentSimulation()
        }
    }

    private fun initializeSocialCampaigns() {
        _campaigns.value = listOf(
            SocialAdCampaign(
                "Facebook", 120.00, "Running", 15400, 680, 24, 0.18,
                "🔥 Best-Selling Sunset Glow Projector! 50% OFF Today with free global dropshipping support. Route items automatically via Wed2C!"
            ),
            SocialAdCampaign(
                "Google Ads", 150.00, "Running", 8900, 940, 48, 0.16,
                "Buy Automatic Cat Litter Cleaner online. Fast delivery, 1-year warranty. Sourced transparently & securely from dropship suppliers."
            ),
            SocialAdCampaign(
                "YouTube Ads", 100.00, "Running", 22000, 450, 15, 0.22,
                "Watch the cinematic review of the UHD 4K Foldable Drone. Click here to purchase on our direct store!"
            ),
            SocialAdCampaign(
                "TikTok", 80.00, "Running", 45000, 1890, 72, 0.04,
                "Wait until the end... 😍 Get this magic aesthetic flame humidifier diffuser from Wed2C link in bio! ✨"
            ),
            SocialAdCampaign(
                "WhatsApp", 30.00, "Running", 5200, 310, 19, 0.09,
                "Direct catalog offer: Elegant genuine Peshawari Sandals for your Eid outfit! Buy now on Daraz.pk PK store link."
            ),
            SocialAdCampaign(
                "Instagram", 70.00, "Running", 18200, 810, 31, 0.08,
                "Level up your desk vibes with this minimal Nordic glass desktop planter! Perfect gift 🌿"
            )
        )
    }

    // Continuous autonomous simulation loop mimicking a fully automatic e-commerce business
    private fun startAutonomousAgentSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(12000) // Execute cycle every 12 seconds
                if (!isSimulationRunning.value) continue

                try {
                    val roll = Random.nextInt(100)
                    if (roll < 45) {
                        // 45% chance: Autonomous Customer Order simulation (The supreme payment gateway/routing showcase!)
                        triggerSimulatedCustomerCheckout()
                    } else {
                        // 55% chance: Idle autonomous agent task
                        triggerSimulatedAgentMaintenanceTask()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    try {
                        repository.addLog(
                            "System", "Autonomous Engine",
                            "Simulation run error: ${e.localizedMessage ?: e.message}",
                            "error"
                        )
                    } catch (dbEx: Exception) {
                        dbEx.printStackTrace()
                    }
                }
            }
        }
    }

    // Simulate an Incoming Consumer Buy Checkout
    private suspend fun triggerSimulatedCustomerCheckout() {
        val allProducts = productsState.value
        val allStores = storesState.value
        if (allProducts.isEmpty() || allStores.isEmpty()) return

        // Pick a random product for checkout
        val product = allProducts[Random.nextInt(allProducts.size)]
        val store = allStores.find { it.id == product.storeId } ?: return

        val quantity = 1
        val revenue = product.price * quantity
        val cost = product.supplierCost * quantity
        val netCommission = revenue - cost

        val buyers = listOf("Oliver Twist", "Anas Iqbal", "Jessica Miller", "Chen Wei", "Fatima Al-Sudairy", "Liam Murphy", "Siddharth Sen", "Camila Silva")
        val billingOptions = listOf("Credit Card", "Apple Pay", "PayPal", "USDT")
        val buyer = buyers[Random.nextInt(buyers.size)]
        val payMethod = billingOptions[Random.nextInt(billingOptions.size)]
        val txnId = "TXN-${Random.nextInt(110000, 999999)}"

        // Step 1: Inventory Agent scans stock count
        inventoryAgentState.value = "SCANNING"
        delay(1500)
        repository.addLog(
            "Inventory Agent", "Spec Scraper",
            "Auto-Verified catalog stock for '${product.name}' in remote store [${store.name}]. Available stock: ${product.stock} units. Safe for order routing.",
            "success"
        )
        inventoryAgentState.value = "IDLE"

        // Step 2: Checkout processor verifies order risk
        fulfillmentAgentState.value = "VERIFYING"
        delay(1500)
        repository.addLog(
            "Fulfillment Agent", "Risk Checker",
            "Customer '$buyer' checked out from automated funnel page. Fraud risk rating: LOW (0.02). Gateway payment authorized successfully.",
            "success"
        )

        // Step 3: Payment Hub handles payment routing
        fulfillmentAgentState.value = "ROUTING"
        val newTxn = TransactionEntity(
            id = txnId,
            customerName = buyer,
            productName = product.name,
            amount = revenue,
            cost = cost,
            paymentMethod = payMethod,
            gatewayName = "OmniPay Hub",
            status = "Routed",
            routingDestination = store.name,
            timestamp = System.currentTimeMillis()
        )
        repository.addTransaction(newTxn)
        
        // Update store total revenue in local database and update product sales
        val updatedStore = store.copy(
            productCount = store.productCount,
            totalRevenue = store.totalRevenue + revenue
        )
        db.dao().insertStore(updatedStore)

        // Update product statistics
        val updatedProduct = product.copy(
            sales = product.sales + 1,
            stock = if (product.stock > 0) product.stock - 1 else 100
        )
        db.dao().insertProduct(updatedProduct)

        delay(1000)
        repository.addLog(
            "Fulfillment Agent", "Auto-Checkout Router",
            "SUCCESS: Payment of $${String.format("%.2f", revenue)} processed. Dispatched wholesale cost of $${String.format("%.2f", cost)} to dropship partner [${store.platform}]. Net payout profit: +$${String.format("%.2f", netCommission)} routed back.",
            "success"
        )
        fulfillmentAgentState.value = "IDLE"

        // Step 4: Finance Agent updates financial metrics
        financeAgentState.value = "CALCULATING"
        delay(1000)
        repository.addLog(
            "Finance Agent", "Cost Calculator",
            "Updated SaaS dynamic monthly ledger. Added transaction processing fee of $${String.format("%.2f", revenue * 0.029 + 0.30)} to maintenance costs. Business healthy.",
            "info"
        )
        financeAgentState.value = "IDLE"

        // Raise floating state trigger for dashboard notifications
        lastSimulatedOrderTime.value = System.currentTimeMillis()
        lastSimulatedOrderMsg.value = "🎉 Sale of $${String.format("%.2f", revenue)}! ${product.name} routed to ${store.name}."
    }

    // Simulate standard autonomous agent activities
    private suspend fun triggerSimulatedAgentMaintenanceTask() {
        val agentRoll = Random.nextInt(4)
        when (agentRoll) {
            0 -> {
                // Inventory Agent Scrapes remote listing
                inventoryAgentState.value = "SCANNING"
                delay(2000)
                val statusRoll = Random.nextInt(3)
                if (statusRoll == 0) {
                    repository.addLog(
                        "Inventory Agent", "Prices Monitor",
                        "Scraped 9 Wed2C links and compared margins. Competitor Ali Baba product price matched. List pricing aligned.",
                        "info"
                    )
                } else if (statusRoll == 1) {
                    repository.addLog(
                        "Inventory Agent", "Spec Scraper",
                        "Crawled eBay & Daraz.pk dropship catalog. Sku list is fully up-to-date. No stock shortages reported.",
                        "success"
                    )
                } else {
                    repository.addLog(
                        "Inventory Agent", "Auto-DeDuplicator",
                        "Double-checked AliExpress direct URLs. Validated 16 high-margin items. Clean caching applied.",
                        "success"
                    )
                }
                inventoryAgentState.value = "IDLE"
            }
            1 -> {
                // Marketing Agent compiles social material
                marketingAgentState.value = "GENERATING"
                delay(2000)
                marketingAgentState.value = "POSTING"
                val networks = listOf("Facebook", "Google Ads", "YouTube", "TikTok", "Instagram", "WhatsApp")
                val platform = networks[Random.nextInt(networks.size)]
                
                // Slightly adjust impressions & clicks as if ad is getting hits
                _campaigns.value = _campaigns.value.map { ad ->
                    if (ad.platform == platform) {
                        val extraImp = Random.nextInt(20, 150)
                        val extraClicks = Random.nextInt(1, 8)
                        val extraConv = if (Random.nextInt(30) == 0) 1 else 0
                        ad.copy(
                            impressions = ad.impressions + extraImp,
                            clicks = ad.clicks + extraClicks,
                            conversions = ad.conversions + extraConv
                        )
                    } else ad
                }

                repository.addLog(
                    "Marketing Agent", "Visual Generator",
                    "Optimized bid limits on $platform campaign. Added dynamic copywriting targeting new audience keywords.",
                    "success"
                )
                delay(1000)
                marketingAgentState.value = "IDLE"
            }
            2 -> {
                // Fulfillment checking links
                fulfillmentAgentState.value = "VERIFYING"
                delay(2000)
                repository.addLog(
                    "Fulfillment Agent", "Checkout Router",
                    "Pinging DigiStore24 secure portal gateway. Response latency: 142ms. Security encryption SSL verified.",
                    "success"
                )
                fulfillmentAgentState.value = "IDLE"
            }
            3 -> {
                // Finance Agent running ROI audits
                financeAgentState.value = "AUDITING"
                delay(2000)
                repository.addLog(
                    "Finance Agent", "Cost Calculator",
                    "Running daily accounting check. Server CPU: 12% usage. API cost remains well within monthly allocation safety.",
                    "info"
                )
                financeAgentState.value = "IDLE"
            }
        }
    }

    // Trigger individual manual store sync
    fun manualSyncStore(store: StoreEntity) {
        viewModelScope.launch {
            try {
                repository.addLog(
                    "Inventory Agent", "Manual Overrider",
                    "Triggering force full scraped sync for store '${store.name}' [Link: ${store.link}].",
                    "warning"
                )
                repository.updateStoreStatus(store.id, "Syncing")
                inventoryAgentState.value = "SCANNING"
                delay(3500)
                
                // Increment products/update to Connected
                repository.updateStoreStatus(store.id, "Connected")
                repository.addLog(
                    "Inventory Agent", "Spec Scraper",
                    "Finished scraping '${store.name}' catalog. Connected. Catalog is 100% synced with merchant dashboard.",
                    "success"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inventoryAgentState.value = "IDLE"
            }
        }
    }

    // Trigger manual ad boosting optimization in the Marketing section
    fun manualOptimizeCampaign(campaignPlatform: String) {
        viewModelScope.launch {
            try {
                marketingAgentState.value = "GENERATING"
                repository.addLog(
                    "Marketing Agent", "Visual Generator",
                    "Force-running optimization algorithms for '$campaignPlatform' social budget. Generating fresh ad templates.",
                    "warning"
                )
                delay(2000)
                
                _campaigns.value = _campaigns.value.map { ad ->
                    if (ad.platform == campaignPlatform) {
                        val budgetIncr = ad.budget + 20.00
                        try {
                            repository.addLog(
                                "Marketing Agent", "Promo Writer",
                                "New optimized budget set: $${String.format("%.2f", budgetIncr)} (+ $20.00 added). Slogan adjusted to convert higher CPC.",
                                "success"
                            )
                        } catch (e: Exception) {}
                        ad.copy(
                            budget = budgetIncr,
                            status = "Optimizing",
                            currentCpc = (ad.currentCpc - 0.02).coerceAtLeast(0.02)
                        )
                    } else ad
                }
                
                delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                marketingAgentState.value = "IDLE"
            }
        }
    }

    // Run custom payment simulator from UI
    fun runTestPaymentGateway(cardNumber: String, amount: Double, destinationStore: String) {
        viewModelScope.launch {
            try {
                testPaymentOutput.value = "Verifying card digits..."
                delay(1500)
                testPaymentOutput.value = "Card approved (SSL v3 Secure). Routing cost..."
                delay(1200)
                
                // Calculate simulated cost & royalty
                val processCost = amount * 0.90 // simulated supplier wholesale
                val profitGained = amount - processCost
                
                val txnId = "TXN-MAN-${Random.nextInt(10000, 99999)}"
                val dummyTxn = TransactionEntity(
                    id = txnId,
                    customerName = "Gateway Simulator Tester",
                    productName = "Custom Simulated Checkout Product",
                    amount = amount,
                    cost = processCost,
                    paymentMethod = "Sandbox Unified API",
                    gatewayName = "OmniPay Hub",
                    status = "Completed",
                    routingDestination = destinationStore,
                    timestamp = System.currentTimeMillis()
                )
                repository.addTransaction(dummyTxn)
                
                repository.addLog(
                    "Fulfillment Agent", "Manual Gateway",
                    "MANUAL CHECKOUT SUCCESS: Route total $${String.format("%.2f", amount)} -> Supplier paid $${String.format("%.2f", processCost)} to '$destinationStore'. Net profit +$${String.format("%.2f", profitGained)} logged locally.",
                    "success"
                )
                
                testPaymentOutput.value = "Payment Captured! ID: $txnId. Dispatched $${String.format("%.2f", processCost)} to $destinationStore. Profit +$${String.format("%.2f", profitGained)} recorded."
            } catch (e: Exception) {
                e.printStackTrace()
                testPaymentOutput.value = "Gateway Error: ${e.localizedMessage ?: e.message}"
            }
        }
    }

    // Export formatting utility
    fun loadFileExportText() {
        viewModelScope.launch {
            val res = repository.generateExportData(currentExportFormat.value)
            exportResult.value = res
        }
    }

    // Calculations of monthly operational maintenance costs
    fun calculateTotalMonthlyCost(): Double {
        // Base system costs + dynamic payment gateways processing fees
        val txns = transactionsState.value
        val processingFees = txns.sumOf { it.amount * 0.029 + 0.30 }
        return serverBaseCost + apiTokensCost + proxyScraperCost + adBudgetSlider.value.toDouble() + processingFees
    }

    // Calculations of financial revenues & margins
    fun calculateTotalRevenue(): Double {
        val txns = transactionsState.value
        return txns.sumOf { it.amount }
    }

    fun calculateTotalNetProfit(): Double {
        val txns = transactionsState.value
        val totalRevenue = txns.sumOf { it.amount }
        val totalSupplierCost = txns.sumOf { it.cost }
        // Net profit is the markup
        return totalRevenue - totalSupplierCost
    }

    fun clearLogs() {
        viewModelScope.launch {
            try {
                repository.clearLogs()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
