package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AgentLogEntity
import com.example.data.ProductEntity
import com.example.data.StoreEntity
import com.example.data.TransactionEntity
import kotlinx.coroutines.delay
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ActiveScreen
import com.example.viewmodel.ECommerceViewModel
import com.example.viewmodel.SocialAdCampaign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: ECommerceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OmniGateAppContent(viewModel)
            }
        }
    }
}

// Custom Premium Dark Color Tokens
val DarkBg = Color(0xFF0F111A)
val CardBg = Color(0xFF161924)
val SurfaceBg = Color(0xFF1F2332)
val NeonPurple = Color(0xFF9E00FF)
val NeonCyan = Color(0xFF00FFC2)
val AccentOrange = Color(0xFFFF8A00)
val SoftGray = Color(0xFF94A3B8)
val BorderGray = Color(0xFF2E354F)

@Composable
fun OmniGateAppContent(viewModel: ECommerceViewModel) {
    val activeScreen by viewModel.selectedScreen
    val context = LocalContext.current

    // Observe StateFlows safely using lifecycle aware collectors
    val stores by viewModel.storesState.collectAsStateWithLifecycle()
    val products by viewModel.productsState.collectAsStateWithLifecycle()
    val logs by viewModel.logsState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val campaigns by viewModel.campaigns.collectAsStateWithLifecycle()

    // Alert indicator state for fresh customer simulated orders
    var showNotification by remember { mutableStateOf(false) }
    var notificationMessage by remember { mutableStateOf("") }
    val lastOrderTime by viewModel.lastSimulatedOrderTime

    LaunchedEffect(lastOrderTime) {
        val msg = viewModel.lastSimulatedOrderMsg.value
        if (lastOrderTime != null && msg != null) {
            notificationMessage = msg
            showNotification = true
            delay(5000)
            showNotification = false
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("root_scaffold"),
        containerColor = DarkBg,
        bottomBar = {
            OmniGateBottomNav(
                currentScreen = activeScreen,
                onScreenSelected = { viewModel.selectedScreen.value = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Routing
            Column(modifier = Modifier.fillMaxSize()) {
                // Shared Status Header
                HeaderWidget(
                    isSimActive = viewModel.isSimulationRunning.value,
                    onToggleSim = { viewModel.isSimulationRunning.value = !viewModel.isSimulationRunning.value }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeScreen) {
                        ActiveScreen.Dashboard -> DashboardScreen(
                            viewModel = viewModel,
                            stores = stores,
                            products = products,
                            transactions = transactions,
                            logs = logs
                        )
                        ActiveScreen.AIAgents -> AgentsScreen(
                            viewModel = viewModel,
                            logs = logs
                        )
                        ActiveScreen.Storefronts -> StoresScreen(
                            viewModel = viewModel,
                            stores = stores,
                            products = products
                        )
                        ActiveScreen.MarketingSocial -> MarketingScreen(
                            viewModel = viewModel,
                            campaigns = campaigns
                        )
                        ActiveScreen.FinanceCosts -> FinanceScreen(
                            viewModel = viewModel,
                            transactions = transactions
                        )
                        ActiveScreen.DataExporter -> ExporterScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Real-Time HUD Alerts Banner overlay
            AnimatedVisibility(
                visible = showNotification,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .testTag("alert_banner")
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                    border = BorderStroke(1.5.dp, NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Order Sale",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OMNIPAY AUTO-GATEWAY HIT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = notificationMessage,
                                fontSize = 13.sp,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showNotification = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Alert", tint = SoftGray)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NAVIGATION COMPONENTS
// -------------------------------------------------------------
@Composable
fun OmniGateBottomNav(
    currentScreen: ActiveScreen,
    onScreenSelected: (ActiveScreen) -> Unit
) {
    // Elegant standard Material 3 NavigationBar configured with custom dark palette
    NavigationBar(
        containerColor = CardBg,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars, // Proper bottom padding offset
        modifier = Modifier
            .border(width = (0.5).dp, color = BorderGray, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple(ActiveScreen.Dashboard, Icons.Default.GridView, "Dashboard"),
            Triple(ActiveScreen.AIAgents, Icons.Default.Memory, "AI Swarm"),
            Triple(ActiveScreen.Storefronts, Icons.Default.Storefront, "Our Stores"),
            Triple(ActiveScreen.MarketingSocial, Icons.Default.Campaign, "Marketing Ads"),
            Triple(ActiveScreen.FinanceCosts, Icons.Default.AccountBalanceWallet, "Costs & ROI"),
            Triple(ActiveScreen.DataExporter, Icons.Default.DataObject, "Export csv")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) NeonCyan else SoftGray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) Color.White else SoftGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = NeonPurple.copy(alpha = 0.25f)
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase(Locale.ROOT)}")
            )
        }
    }
}

// -------------------------------------------------------------
// HEADER WIDGET
// -------------------------------------------------------------
@Composable
fun HeaderWidget(
    isSimActive: Boolean,
    onToggleSim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = (0.5).dp, color = BorderGray),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "OMNIGATE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SWARM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple,
                        modifier = Modifier
                            .background(NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "9 Active Stores • 4/4 AI Agents Online",
                    fontSize = 11.sp,
                    color = SoftGray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pulse Status Mode Bullet
            val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Pulse"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onToggleSim() }
                    .background(SurfaceBg, RoundedCornerShape(8.dp))
                    .border(1.dp, if (isSimActive) NeonCyan.copy(alpha = 0.5f) else SoftGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (isSimActive) NeonCyan else Color.Red,
                            shape = CircleShape
                        )
                        .drawBehind {
                            if (isSimActive) {
                                drawCircle(
                                    color = NeonCyan,
                                    radius = size.width * 1.5f,
                                    alpha = pulseAlpha * 0.4f
                                )
                            }
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSimActive) "AUTO STREAM" else "SIM PAUSED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSimActive) Color.White else SoftGray
                )
            }
        }
    }
}

// =============================================================
// I. DASHBOARD SCREEN
// =============================================================
@Composable
fun DashboardScreen(
    viewModel: ECommerceViewModel,
    stores: List<StoreEntity>,
    products: List<ProductEntity>,
    transactions: List<TransactionEntity>,
    logs: List<AgentLogEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Telemetry Scorecards Grid
        item {
            val totalRevenue = viewModel.calculateTotalRevenue()
            val totalMargin = viewModel.calculateTotalNetProfit()
            val activeStoresCount = stores.filter { it.status == "Connected" }.size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "REVENUE FLOW",
                    value = "$${String.format("%.2f", totalRevenue)}",
                    subtitle = "15 Transactions",
                    accentColor = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "NET MARKUP",
                    value = "$${String.format("%.2f", totalMargin)}",
                    subtitle = "Payout Profit ROI",
                    accentColor = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "STORES CONNECTED",
                    value = "${stores.size}/15",
                    subtitle = "9 Wed2C, AliExpress...",
                    accentColor = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "AGENT PULSE",
                    value = "98%",
                    subtitle = "4 Main + 4 Sub Swarms",
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Graphic Trend Chart Custom Canvas
        item {
            LiveSalesChartWidget(transactions = transactions)
        }

        // Autonomous Swarm Status Bar
        item {
            SwarmTrackerRow(viewModel = viewModel)
        }

        // Real-Time Agent Logs Feed Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Logs",
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "REAL-TIME AGENT TELEMETRY FEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Clear Database Logs",
                    fontSize = 10.sp,
                    color = SoftGray,
                    modifier = Modifier
                        .clickable {
                            viewModel.clearLogs()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Stream terminal
        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(0.5.dp, BorderGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs received yet. Toggle AUTO STREAM above to prompt simulated activities.", color = SoftGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(logs.take(15)) { log ->
                LogItemWidget(log = log)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SoftGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = SoftGray
            )
        }
    }
}

// Custom line chart of transaction amounts utilizing Android Compose Canvas APIs
@Composable
fun LiveSalesChartWidget(transactions: List<TransactionEntity>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "REAL-TIME SALES VELOCITY",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aggregated hourly order volume trends (USD)",
                        color = SoftGray,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "AUTO LIVE",
                    fontSize = 9.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(NeonCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))

            // Graph representation using custom canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw horizontal baseline grid
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = (height / gridLines) * i
                        drawLine(
                            color = BorderGray.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    // Map real transaction trends or fallbacks
                    val points = if (transactions.size >= 4) {
                        transactions.reversed().map { it.amount.toFloat() }
                    } else {
                        listOf(25f, 89f, 47f, 189f, 45f, 299f) // dummy chart visualizer curve
                    }

                    val maxVal = (points.maxOrNull() ?: 100f).coerceAtLeast(100f)
                    val minVal = 0f
                    val yRange = maxVal - minVal

                    val stepX = width / (points.size - 1).coerceAtLeast(1)
                    val path = Path()
                    val connections = mutableListOf<Offset>()

                    points.forEachIndexed { index, valAmount ->
                        val x = stepX * index
                        // Flip y-coordinate for Canvas standard coordinates (0 is at top)
                        val relativeY = 1f - ((valAmount - minVal) / yRange)
                        val y = relativeY * height * 0.85f + 10f
                        connections.add(Offset(x, y))

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw the core line graph
                    drawPath(
                        path = path,
                        color = NeonCyan,
                        style = Stroke(width = 4f)
                    )

                    // Fill gradient underneath the path
                    val filledPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = filledPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw circles at key nodes
                    connections.forEachIndexed { i, offset ->
                        drawCircle(
                            color = if (i == connections.lastIndex) AccentOrange else NeonCyan,
                            radius = 6f,
                            center = offset
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("T-12 Hrs", fontSize = 9.sp, color = SoftGray)
                Text("T-8 Hrs", fontSize = 9.sp, color = SoftGray)
                Text("T-4 Hrs", fontSize = 9.sp, color = SoftGray)
                Text("Active Now", fontSize = 9.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SwarmTrackerRow(viewModel: ECommerceViewModel) {
    val invState by viewModel.inventoryAgentState.collectAsStateWithLifecycle()
    val mktState by viewModel.marketingAgentState.collectAsStateWithLifecycle()
    val fulState by viewModel.fulfillmentAgentState.collectAsStateWithLifecycle()
    val finState by viewModel.financeAgentState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "AI SWARM SWEEP LOAD",
                fontSize = 10.sp,
                color = SoftGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircularAgentLoad(name = "Inventory", state = invState, color = NeonCyan)
                CircularAgentLoad(name = "Marketing", state = mktState, color = NeonPurple)
                CircularAgentLoad(name = "Routing", state = fulState, color = AccentOrange)
                CircularAgentLoad(name = "Finance", state = finState, color = Color.White)
            }
        }
    }
}

@Composable
fun CircularAgentLoad(name: String, state: String, color: Color) {
    val isActive = state != "IDLE"
    val animateAngle by animateFloatAsState(
        targetValue = if (isActive) 360f else 0f,
        animationSpec = if (isActive) infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ) else tween(0),
        label = "spin"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(SurfaceBg, CircleShape)
                .border(
                    width = 2.dp,
                    color = if (isActive) color else BorderGray,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when(name) {
                    "Inventory" -> Icons.Default.CloudSync
                    "Marketing" -> Icons.Default.Share
                    "Routing" -> Icons.Default.CurrencyExchange
                    else -> Icons.Default.Calculate
                },
                contentDescription = name,
                tint = if (isActive) color else SoftGray,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            text = state,
            fontSize = 7.sp,
            color = if (isActive) color else SoftGray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LogItemWidget(log: AgentLogEntity) {
    val logColor = when(log.type) {
        "success" -> NeonCyan
        "warning" -> AccentOrange
        "error" -> Color.Red
        else -> NeonPurple
    }

    val systemTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "[$systemTime]",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = SoftGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.agentName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = logColor
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "sub",
                        tint = SoftGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = log.subAgentName,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = SoftGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(SurfaceBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.message,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}

// =============================================================
// II. AGENTS DEEP INTERFACE
// =============================================================
@Composable
fun AgentsScreen(
    viewModel: ECommerceViewModel,
    logs: List<AgentLogEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("agents_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AGENT INTELLIGENCE SWARM NETWORK",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Below are the 4 main parent agents operating autonomously. Each works symbiotically with a sub-agent to scrape, promote, route, and audit operations.",
            fontSize = 11.sp,
            color = SoftGray
        )

        // Agent Cards Visualizing connections
        AgentSwarmNode(
            parentName = "INVENTORY AUTO-SYNC",
            subName = "SPEC SCRAPER",
            desc = "Scrapes AliExpress, CJ Dropshipper & 9 Wed2C listings to maintain catalog pricing structures & update products.",
            workState = viewModel.inventoryAgentState.collectAsStateWithLifecycle().value,
            color = NeonCyan,
            icon = Icons.Default.CloudSync
        )

        AgentSwarmNode(
            parentName = "MARKETING WRITER",
            subName = "VISUAL GENERATOR",
            desc = "Formats social feeds with copy. Drives leads of stores through FB Pixels, Google Keywords, TikTok & IG ads.",
            workState = viewModel.marketingAgentState.collectAsStateWithLifecycle().value,
            color = NeonPurple,
            icon = Icons.Default.Campaign
        )

        AgentSwarmNode(
            parentName = "ORDER ROUTER GATEWAY",
            subName = "RISK CHECKER",
            desc = "Pipes transactions into supplier vaults. Handles payment channels (Stripe, Paypal, Crypto API) safely.",
            workState = viewModel.fulfillmentAgentState.collectAsStateWithLifecycle().value,
            color = AccentOrange,
            icon = Icons.Default.Shuffle
        )

        AgentSwarmNode(
            parentName = "FINANCIAL AUDITOR",
            subName = "COST CALCULATOR",
            desc = "Monitors system server hosting, API quotas, dynamic processor fees, and logs final monthly net ROI.",
            workState = viewModel.financeAgentState.collectAsStateWithLifecycle().value,
            color = Color.White,
            icon = Icons.Default.AccountBalance
        )
    }
}

@Composable
fun AgentSwarmNode(
    parentName: String,
    subName: String,
    desc: String,
    workState: String,
    color: Color,
    icon: ImageVector
) {
    val active = workState != "IDLE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (active) color else BorderGray,
                shape = RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = parentName, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(parentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("🤖 SUB-AGENT: $subName", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = workState,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White else SoftGray,
                    modifier = Modifier
                        .background(if (active) color else SurfaceBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(desc, fontSize = 11.sp, color = SoftGray)
        }
    }
}

// =============================================================
// III. STOREFRONT & PRODUCTS MANAGER
// =============================================================
@Composable
fun StoresScreen(
    viewModel: ECommerceViewModel,
    stores: List<StoreEntity>,
    products: List<ProductEntity>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Stores/Links, 1 = Synced Products

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("stores_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "E-COMMERCE INTEGRATION MODULE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )

        // Custom M3 Tab Arrangement
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CardBg,
            contentColor = NeonCyan,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, BorderGray, RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Integrated Storefronts (${stores.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Cached Products (${products.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        text = "9 USER-SUPPLIED WED2C LINKS & AFFILIATE OUTLETS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray,
                        letterSpacing = 0.5.sp
                    )
                }
                items(stores) { store ->
                    StoreOutletRow(
                        store = store,
                        onSyncClick = { viewModel.manualSyncStore(store) }
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Text(
                        "DISPATCH DOCK (STOCK QUANTITIES & SUPPLIER MARGINS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGray
                    )
                }
                items(products) { product ->
                    ProductDockItem(product = product)
                }
            }
        }
    }
}

@Composable
fun StoreOutletRow(
    store: StoreEntity,
    onSyncClick: () -> Unit
) {
    var expandLink by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        store.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("PLATFORM: ${store.platform}", fontSize = 9.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                }
                
                // Status tag
                Text(
                    text = store.status,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (store.status == "Connected") NeonCyan else AccentOrange,
                    modifier = Modifier
                        .background(
                            (if (store.status == "Connected") NeonCyan else AccentOrange).copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                IconButton(
                    onClick = { onSyncClick() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Manual Scraping Sync", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    store.link,
                    fontSize = 10.sp,
                    color = SoftGray,
                    maxLines = if (expandLink) 3 else 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expandLink = !expandLink }
                )
                Text(
                    "Sales: $${String.format("%.1f", store.totalRevenue)}",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProductDockItem(product: ProductEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.GridOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$${product.price}", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cost: $${product.supplierCost}", fontSize = 10.sp, color = SoftGray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Markup profit: +$${String.format("%.2f", product.price - product.supplierCost)}", fontSize = 10.sp, color = NeonPurple)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Stock: ${product.stock}", fontSize = 10.sp, color = if (product.stock < 20) AccentOrange else SoftGray)
                Text("Sales: ${product.sales}", fontSize = 10.sp, color = Color.White)
            }
        }
    }
}

// =============================================================
// IV. MARKETING & ADS MANAGEMENT
// =============================================================
@Composable
fun MarketingScreen(
    viewModel: ECommerceViewModel,
    campaigns: List<SocialAdCampaign>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("marketing_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "SOCIAL MEDIA ADS COORDINATOR",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "The Marketing Agent targets Facebook Pixel, Google Keywords, TikTok video hooks, and Instagram tags automatically. Tap BOOST to scale individual network bidding.",
            fontSize = 11.sp,
            color = SoftGray
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(campaigns) { campaign ->
                SocialCampaignRow(
                    campaign = campaign,
                    onBoostClick = { viewModel.manualOptimizeCampaign(campaign.platform) }
                )
            }
        }
    }
}

@Composable
fun SocialCampaignRow(
    campaign: SocialAdCampaign,
    onBoostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderGray, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    campaign.platform,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = campaign.status,
                    fontSize = 8.sp,
                    color = if (campaign.status == "Running") NeonCyan else NeonPurple,
                    modifier = Modifier
                        .background(
                            (if (campaign.status == "Running") NeonCyan else NeonPurple).copy(alpha = 0.15f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "Budget: $${String.format("%.2f", campaign.budget)}/mo",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"${campaign.promoText}\"",
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif,
                color = SoftGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Impressions: ${campaign.impressions}", fontSize = 10.sp, color = SoftGray)
                    Text("Clicks: ${campaign.clicks} (Avg CPC: $${String.format("%.2f", campaign.currentCpc)})", fontSize = 10.sp, color = SoftGray)
                    Text("Conversions: ${campaign.conversions}", fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onBoostClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = "Boost", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BOOST ROI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// =============================================================
// V. PAYMENT GATEWAY & FINANCIALS
// =============================================================
@Composable
fun FinanceScreen(
    viewModel: ECommerceViewModel,
    transactions: List<TransactionEntity>
) {
    var testAmount by remember { mutableStateOf("45.00") }
    var chosenStoreName by remember { mutableStateOf("Wed2C (The Americane)") }
    var cardNumber by remember { mutableStateOf("4532 9901 8847 2201") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("finance_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "UNIFIED OMNIPAY INTEGRATED GATEWAY",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Handles consumer checkout charges from Facebook/TikTok. Slices total proceeds and routes supplier cost to Wed2C instantly, capturing net markups.",
            fontSize = 11.sp,
            color = SoftGray
        )

        var showSandbox by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(0.5.dp, BorderGray)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "SANDBOX GATEWAY TRANSACT SIMULATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Test card gateway purchase flows online", fontSize = 10.sp, color = SoftGray)
                    TextButton(onClick = { showSandbox = !showSandbox }) {
                        Text(if (showSandbox) "Hide Sandbox" else "Show Sandbox Client", fontSize = 11.sp, color = NeonCyan)
                    }
                }

                if (showSandbox) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        label = { Text("Simulated Card Number") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = BorderGray,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = SoftGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = testAmount,
                            onValueChange = { testAmount = it },
                            label = { Text("Amount ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderGray,
                                focusedLabelColor = NeonCyan,
                                unfocusedLabelColor = SoftGray
                            )
                        )
                        OutlinedTextField(
                            value = chosenStoreName,
                            onValueChange = { chosenStoreName = it },
                            label = { Text("Target Supplier Store") },
                            modifier = Modifier.weight(1.5f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = BorderGray,
                                focusedLabelColor = NeonCyan,
                                unfocusedLabelColor = SoftGray
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val amt = testAmount.toDoubleOrNull() ?: 20.0
                            viewModel.runTestPaymentGateway(cardNumber, amt, chosenStoreName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SUBMIT SIMULATED SALE CHARGE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    viewModel.testPaymentOutput.value?.let { output ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = output,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(SurfaceBg, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Monthly Operational Systems Costs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
            border = BorderStroke(1.dp, NeonPurple)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "TOTAL MAINTENANCE COST BREAKDOWN (1 MONTH)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                CostLine("SaaS Server & CDN Cloud Deploy", "$${String.format("%.2f", viewModel.serverBaseCost)}")
                CostLine("LLM Agents Smart API Tokens (Gemini/OpenAI)", "$${String.format("%.2f", viewModel.apiTokensCost)}")
                CostLine("API Store Crawler Proxies (Wed2C / Ali)", "$${String.format("%.2f", viewModel.proxyScraperCost)}")
                
                // Active Ad Slider Dynamic representation
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Social Ads Budget Allocation", fontSize = 11.sp, color = SoftGray)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("$${viewModel.adBudgetSlider.value.toInt()}/mo", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = viewModel.adBudgetSlider.value,
                    onValueChange = { viewModel.adBudgetSlider.value = it },
                    valueRange = 0f..2000f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = BorderGray,
                        thumbColor = NeonCyan
                    )
                )

                val transactionsProcessorFees = transactions.sumOf { it.amount * 0.029 + 0.30 }
                CostLine("Payment Processor Gateway Slices (2.9% + $0.30)", "$${String.format("%.2f", transactionsProcessorFees)}")
                
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))
                CostLine("COMBINED MAINTENANCE TOTAL OUTFLOW", "$${String.format("%.2f", viewModel.calculateTotalMonthlyCost())}", isBold = true)
            }
        }

        // Ledger of recent routed payments
        Text(
            "RECENT PAYMENT TRANSACTIONS LEDGER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SoftGray
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions) { txn ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Row(modifier = Modifier.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.productName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Customer: ${txn.customerName} | Method: ${txn.paymentMethod}", fontSize = 9.sp, color = SoftGray)
                            Text("Routed To: ${txn.routingDestination}", fontSize = 9.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("+$${String.format("%.2f", txn.amount)}", fontSize = 12.sp, color = NeonCyan, fontWeight = FontWeight.Black)
                            Text("Net markup: +$${String.format("%.2f", txn.amount - txn.cost)}", fontSize = 9.sp, color = SoftGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CostLine(label: String, valStr: String, isBold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            fontSize = if (isBold) 12.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) Color.White else SoftGray
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            valStr,
            fontSize = if (isBold) 12.sp else 11.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = if (isBold) NeonCyan else Color.White
        )
    }
}

// =============================================================
// VI. LOCAL EXPORTER (CSV / TEXT FORMATS)
// =============================================================
@Composable
fun ExporterScreen(
    viewModel: ECommerceViewModel
) {
    val exportText = viewModel.exportResult.value
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("exporter_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "AGENT RAW DATA INTEGRATION PORTERS",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Text(
            text = "Generate structured documents locally containing the synced stores, cached product SKUs, and transaction histories. Export is fully compatible with Google AppSheet and standard CSV imports.",
            fontSize = 11.sp,
            color = SoftGray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { 
                    viewModel.currentExportFormat.value = "TXT"
                    viewModel.loadFileExportText()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentExportFormat.value == "TXT") NeonPurple else SurfaceBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("TEXT DOCUMENT REPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { 
                    viewModel.currentExportFormat.value = "CSV"
                    viewModel.loadFileExportText()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.currentExportFormat.value == "CSV") NeonPurple else SurfaceBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("APPSHEET CSV TABLE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(width = 0.5.dp, color = BorderGray, shape = RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                if (exportText.isEmpty()) {
                    Text(
                        text = "Click one of the raw data compile formats above to aggregate local database tables inside the export container.",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = SoftGray
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("COMPILED DATA STREAM:", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(exportText))
                                    Toast.makeText(context, "Data Stream successfully copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 4.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = exportText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
