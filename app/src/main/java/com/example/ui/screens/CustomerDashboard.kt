@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf("home") } // home, tracking, pickup, loyalty, support, profile, passport, payments, tracking_map, chat, stats, ratings
    val scope = rememberCoroutineScope()
    val invoices by AppStateStore.invoices.collectAsState()
    val carpets by AppStateStore.carpets.collectAsState()
    val messages by AppStateStore.messages.collectAsState()

    var selectedCarpetId by remember { mutableStateOf<String?>("C-103") }
    var selectedInvoiceId by remember { mutableStateOf<String?>("INV-40212") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Quadruple("home", "خانه", Icons.Default.Home, Icons.Outlined.Home),
                        Quadruple("tracking", "پیگیری", Icons.Default.Timeline, Icons.Outlined.Timeline),
                        Quadruple("pickup", "درخواست جمع‌آوری", Icons.Default.Add, Icons.Outlined.AddCircle),
                        Quadruple("loyalty", "کلوپ پاکان", Icons.Default.Star, Icons.Outlined.StarBorder),
                        Quadruple("support", "پشتیبانی", Icons.Default.Chat, Icons.Outlined.Chat)
                    )
                    tabs.forEach { (tabId, label, activeIcon, inactiveIcon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) activeIcon else inactiveIcon,
                                    contentDescription = label,
                                    tint = if (isSelected) Color(0xFF0B4DA2) else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF0B4DA2) else Color.Gray
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0xFF0B4DA2).copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (activeTab) {
                                "home" -> "قالیشویی هوشمند پاکان"
                                "tracking" -> "پیگیری هوشمند فرش"
                                "pickup" -> "سفارش جمع‌آوری"
                                "loyalty" -> "کلوپ مشتریان پاکان"
                                "support" -> "پشتیبانی و گفتگوی زنده"
                                "profile" -> "پروفایل کاربری"
                                "passport" -> "پاسپورت فرش"
                                "payments" -> "پرداخت و فاکتورها"
                                "tracking_map" -> "ردیابی زنده راننده"
                                "chat" -> "گفتگو با پشتیبانی"
                                "stats" -> "گزارشات و عملکرد"
                                "ratings" -> "ثبت امتیاز سفارش"
                                else -> "قالیشویی پاکان"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4DA2)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeTab = "profile" }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFF0B4DA2)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onLogout() }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color(0xFFE53935)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFF7F9FC)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        slideInHorizontally { width -> -width } + fadeIn() with slideOutHorizontally { width -> width } + fadeOut()
                    },
                    label = "TabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        "home" -> CustomerHomeView(
                            invoices = invoices,
                            carpets = carpets,
                            onNavigateTab = { activeTab = it },
                            onSelectCarpet = {
                                selectedCarpetId = it
                                activeTab = "passport"
                            },
                            onSelectInvoice = {
                                selectedInvoiceId = it
                                activeTab = "payments"
                            }
                        )
                        "tracking" -> CustomerTrackingView(carpets) { carpetId ->
                            selectedCarpetId = carpetId
                            activeTab = "passport"
                        }
                        "pickup" -> CustomerRequestPickupView {
                            activeTab = "tracking"
                        }
                        "loyalty" -> CustomerLoyaltyView()
                        "support" -> CustomerSupportCenterView { view -> activeTab = view }
                        "profile" -> CustomerProfileView()
                        "passport" -> CustomerPassportView(
                            carpetId = selectedCarpetId,
                            carpets = carpets,
                            onBack = { activeTab = "home" }
                        )
                        "payments" -> CustomerPaymentsView(
                            invoiceId = selectedInvoiceId,
                            invoices = invoices,
                            onBack = { activeTab = "home" }
                        )
                        "tracking_map" -> LiveDriverTrackingView(onBack = { activeTab = "home" })
                        "chat" -> CustomerSupportChatView(messages)
                        "stats" -> CustomerStatsView(invoices, carpets, onBack = { activeTab = "home" })
                        "ratings" -> CustomerRatingsView(onBack = { activeTab = "home" })
                    }
                }
            }
        }
    }
}

// 1. Home View
@Composable
fun CustomerHomeView(
    invoices: List<Invoice>,
    carpets: List<Carpet>,
    onNavigateTab: (String) -> Unit,
    onSelectCarpet: (String) -> Unit,
    onSelectInvoice: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Points Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سلام، جناب مرتضی رضایی عزیز",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "به کارواش و قالیشویی هوشمند پاکان خوش آمدید.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "کلوپ مشتریان: ۲,۴۵۰ امتیاز (طلایی)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Quick Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val actions = listOf(
                    Triple("tracking_map", "ردیابی زنده راننده", Icons.Default.Map),
                    Triple("chat", "گفتگو با پشتیبانی", Icons.Default.Chat),
                    Triple("stats", "عملکرد و آمار", Icons.Default.Analytics),
                    Triple("ratings", "امتیازدهی", Icons.Default.Star)
                )

                actions.forEach { (tabId, label, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateTab(tabId) }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE3E8EF), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = Color(0xFF0B4DA2),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Outstanding Balance Card
        val unpaidInvoices = invoices.filter { it.status != InvoiceStatus.PAID }
        if (unpaidInvoices.isNotEmpty()) {
            item {
                val totalUnpaid = unpaidInvoices.sumOf { it.totalAmount - it.paidAmount }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935).copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "بدهی معوقه پرداخت نشده",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE53935)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${String.format("%,d", totalUnpaid)} ریال",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigateTab("payments") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "پرداخت آنلاین", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Active Orders Section Title
        item {
            Text(
                text = "سفارشات فعال و در حال پردازش شما",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        }

        items(carpets.filter { it.status != CarpetStatus.DELIVERED }) { carpet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCarpet(carpet.id) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F4F8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = Color(0xFF0B4DA2),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = carpet.type,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "کد ردیابی: ${carpet.trackingNumber}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Status pill
                    val statusBg = when (carpet.status) {
                        CarpetStatus.RECEIVED, CarpetStatus.COLLECTED -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        CarpetStatus.WASHING, CarpetStatus.DRYING -> Color(0xFF0B4DA2).copy(alpha = 0.1f)
                        CarpetStatus.QC -> Color(0xFFFFC107).copy(alpha = 0.15f)
                        CarpetStatus.READY -> Color(0xFF2BB673).copy(alpha = 0.1f)
                        else -> Color.Gray.copy(alpha = 0.1f)
                    }
                    val statusColor = when (carpet.status) {
                        CarpetStatus.RECEIVED, CarpetStatus.COLLECTED -> Color(0xFFFF9800)
                        CarpetStatus.WASHING, CarpetStatus.DRYING -> Color(0xFF0B4DA2)
                        CarpetStatus.QC -> Color(0xFFD4AF37)
                        CarpetStatus.READY -> Color(0xFF2BB673)
                        else -> Color.Gray
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(statusBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = carpet.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }

        // Previous Orders Section Title
        item {
            Text(
                text = "فاکتورهای اخیر شما",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        }

        items(invoices) { invoice ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectInvoice(invoice.id) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "فاکتور شماره ${invoice.id}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تاریخ صدور: ${invoice.date} • تعداد فرش: ${invoice.carpets.size} تخته",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format("%,d", invoice.totalAmount)} ریال",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0B4DA2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val badgeColor = when (invoice.status) {
                            InvoiceStatus.PAID -> Color(0xFF2BB673)
                            InvoiceStatus.PARTIAL -> Color(0xFFFF9800)
                            InvoiceStatus.UNPAID -> Color(0xFFE53935)
                        }
                        Text(
                            text = invoice.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

// 2. Tracking View with Timeline
@Composable
fun CustomerTrackingView(carpets: List<Carpet>, onCarpetSelect: (String) -> Unit) {
    var selectedCarpet by remember { mutableStateOf<Carpet?>(if (carpets.isNotEmpty()) carpets[0] else null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "یک فرش را جهت مشاهده روند شستشو انتخاب کنید:",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal list of carpets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            carpets.forEach { carpet ->
                val isSelected = selectedCarpet?.id == carpet.id
                val borderCol = if (isSelected) Color(0xFF0B4DA2) else Color(0xFFE3E8EF)
                val bgCol = if (isSelected) Color(0xFF0B4DA2).copy(alpha = 0.05f) else Color.White

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgCol)
                        .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                        .clickable { selectedCarpet = carpet }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = carpet.type,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF0B4DA2) else Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedCarpet?.let { carpet ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCarpetSelect(carpet.id) }
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color(0xFF0B4DA2),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = carpet.type,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "شناسه فاکتور: ${carpet.trackingNumber} • رنگ: ${carpet.color}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Timeline steps layout
            val steps = listOf(
                Triple(CarpetStatus.RECEIVED, "ثبت اولیه سفارش", "درخواست قالیشویی ثبت و فاکتور در سیستم صادر شد"),
                Triple(CarpetStatus.COLLECTED, "جمع‌آوری شده توسط راننده", "فرش‌ها توسط علی کریمی تحویل و به خودرو منتقل شد"),
                Triple(CarpetStatus.WAREHOUSE, "ورود به انبار کارخانه", "فرش بررسی اولیه شد و در قفسه انبار چیده شد"),
                Triple(CarpetStatus.WASHING, "شستشو با ماشین‌آلات مدرن", "مرحله شستشوی تخصصی اتوماتیک با مواد شوینده ارگانیک"),
                Triple(CarpetStatus.DRYING, "گرم‌خانه و خشک‌سازی", "خشک شدن کامل فرش در گرم‌خانه هوشمند گازی"),
                Triple(CarpetStatus.QC, "کنترل کیفیت نهایی (QC)", "بررسی نهایی عدم وجود لکه و اصلاح ریشه‌ها"),
                Triple(CarpetStatus.READY, "آماده توزیع و تحویل", "فرش کاور شده و در نوبت تحویل راننده قرار دارد"),
                Triple(CarpetStatus.DELIVERED, "تحویل نهایی مشتری", "فرش به صورت معطر و کاور شده تحویل مشتری گردید")
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(steps) { (status, title, desc) ->
                    val hasPassed = carpet.status.step >= status.step
                    val isActive = carpet.status == status

                    val iconBg = if (isActive) Color(0xFF2BB673) else if (hasPassed) Color(0xFF0B4DA2) else Color(0xFFE3E8EF)
                    val iconTint = if (hasPassed || isActive) Color.White else Color.Gray
                    val titleColor = if (isActive) Color(0xFF2BB673) else if (hasPassed) Color.Black else Color.Gray
                    val descColor = if (hasPassed) Color.DarkGray else Color.Gray

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(iconBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else if (hasPassed) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = iconTint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Vertical Line
                            if (status != CarpetStatus.DELIVERED) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(48.dp)
                                        .background(if (hasPassed) Color(0xFF0B4DA2) else Color(0xFFE3E8EF))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                color = titleColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = descColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. Customer Pickup Request Form
@Composable
fun CustomerRequestPickupView(onSubmit: () -> Unit) {
    var selectedDate by remember { mutableStateOf("فردا (شنبه ۲۷ تیر)") }
    var selectedTime by remember { mutableStateOf("ساعت ۹ الی ۱۳ (صبح)") }
    var address by remember { mutableStateOf("پاسداران، بوستان چهارم، پلاک ۱۲") }
    var notes by remember { mutableStateOf("فرش ابریشم نفیس است، لطفا کاور حمل ویژه آورده شود.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "ثبت درخواست جمع‌آوری فرش",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4DA2)
            )
            Text(
                text = "لطفا جزئیات زمان و مکان مورد نظر جهت حضور خودرو را مشخص کنید",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "آدرس و مکان جمع‌آوری",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Map, null, tint = Color(0xFF0B4DA2)) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Map Picker Window
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE1F5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = Color(0xFF0B4DA2),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "موقعیت نقشه پین شده است (منطقه پاسداران)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B4DA2)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "زمان حضور خودروی کارخانه",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "انتخاب تاریخ حضور:", fontSize = 12.sp, color = Color.Gray)
                    val dates = listOf("فردا (شنبه ۲۷ تیر)", "یک‌شنبه ۲۸ تیر", "دوشنبه ۲۹ تیر")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dates.forEach { date ->
                            val isSelected = selectedDate == date
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0xFF0B4DA2) else Color(0xFFF0F4F8))
                                    .clickable { selectedDate = date }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "انتخاب بازه ساعتی:", fontSize = 12.sp, color = Color.Gray)
                    val times = listOf("ساعت ۹ الی ۱۳ (صبح)", "ساعت ۱۳ الی ۱۷ (عصر)", "ساعت ۱۷ الی ۲۱ (شب)")
                    times.forEach { time ->
                        val isSelected = selectedTime == time
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2BB673).copy(alpha = 0.1f) else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF2BB673) else Color(0xFFE3E8EF),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTime = time }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedTime = time })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = time, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "توضیحات و یادداشت برای راننده",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    AppStateStore.addNotification(
                        "درخواست جمع‌آوری ثبت شد",
                        "درخواست جمع‌آوری آدرس $address برای تاریخ $selectedDate ثبت شد.",
                        "سیستم"
                    )
                    onSubmit()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "ثبت نهایی درخواست حضور راننده",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 4. Loyalty Club View
@Composable
fun CustomerLoyaltyView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "کلوپ وفاداری طلایی",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = "VIP LEVEL", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(text = "مجموع امتیاز وفاداری شما:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    Text(
                        text = "۲,۴۵۰ امتیاز",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = 0.75f,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "تنها ۵۵۰ امتیاز تا سطح ویژه مشتریان الماس فاصله دارید",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                text = "جوایز و تخفیف‌های ویژه فعال برای شما",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        val coupons = listOf(
            Triple("تخفیف ۲۰ درصدی شستشو ویژه اعلا", "کد تخفیف: PAKAN20 • مصرف تا پایان مردادماه", "۵۰۰ امتیاز"),
            Triple("یک تخته شستشوی رایگان ماشینی", "هدیه وفاداری طلایی کارخانه • معتبر ۳ ماه", "۱۲۰۰ امتیاز"),
            Triple("رفوگری و ریشه‌کشی ۵۰٪ تخفیف", "ویژه خدمات تعمیراتی دستی و ابریشم", "۸۰۰ امتیاز")
        )

        items(coupons) { (title, subtitle, cost) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "دریافت ($cost)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// 5. Support Center View
@Composable
fun CustomerSupportCenterView(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ارتباط مستقیم با کارخانه قالیشویی",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate("chat") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("گفتگوی متنی زنده", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تماس تلفنی سریع", fontSize = 12.sp)
                    }
                }
            }
        }

        Text(
            text = "پرسش‌های متداول (FAQ)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        val faqs = listOf(
            "زمان تحویل معمول فرش‌ها چند روز کاری است؟" to "زمان تحویل استاندارد برای فرش‌های ماشینی ۴۸ ساعت کاری و برای فرش‌های گران‌قیمت دستباف و ابریشم بین ۴ الی ۷ روز کاری جهت رفوگری و شستشوی دستی نفیس است.",
            "آیا مواد شوینده باعث رنگ‌دهی فرش‌های دستباف می‌شود؟" to "خیر، کارخانه قالیشویی پاکان مجهز به دستگاه قلم‌زنی و فیکساتور رنگ است که از مواد آلی گیاهی بدون کلر استفاده کرده و رنگ فرش‌های دستی نفیس را تثبیت می‌کند.",
            "نحوه پرداخت فاکتور به چه صورت است؟" to "شما می‌توانید فاکتور صادر شده را با درگاه بانکی درون برنامه، کیف پول کاربری، یا پرداخت حضوری توسط دستگاه کارتخوان همراه راننده (POS) تسویه کامل نمایید."
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(faqs) { (q, a) ->
                var expanded by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = a,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. Carpet Passport View
@Composable
fun CustomerPassportView(carpetId: String?, carpets: List<Carpet>, onBack: () -> Unit) {
    val carpet = carpets.find { it.id == carpetId } ?: carpets[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "پاسپورت هوشمند فرش",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4DA2)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Carpet Representation / QR Header
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F4F8)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = carpet.id,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = carpet.type,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFE3E8EF))

                Spacer(modifier = Modifier.height(16.dp))

                val details = listOf(
                    "شناسه رهگیری" to carpet.trackingNumber,
                    "طرح و رنگ فرش" to carpet.color,
                    "ابعاد و مساحت" to "${carpet.width} × ${carpet.length} متر (${carpet.area} متر مربع)",
                    "نوع خدمات دریافتی" to carpet.serviceType,
                    "تعداد دفعات شستشو" to "${carpet.prevWashes} بار در کارخانه پاکان",
                    "آخرین وضعیت فیزیکی" to (carpet.qualityNotes ?: "سالم و بدون آسیب"),
                    "عملیات رفوگری / تعمیرات" to (carpet.repairNotes ?: "عدم نیاز")
                )

                details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, fontSize = 13.sp, color = Color.Gray)
                        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

// 7. Customer Payments and Invoices Details Screen
@Composable
fun CustomerPaymentsView(invoiceId: String?, invoices: List<Invoice>, onBack: () -> Unit) {
    val invoice = invoices.find { it.id == invoiceId } ?: invoices[0]
    var showReceipt by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "جزئیات فاکتور شماره ${invoice.id}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4DA2)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "مشتری: ${invoice.customerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "تلفن همراه: ${invoice.customerPhone}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (invoice.status == InvoiceStatus.PAID) Color(0xFF2BB673).copy(alpha = 0.1f)
                                else Color(0xFFE53935).copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = invoice.status.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (invoice.status == InvoiceStatus.PAID) Color(0xFF2BB673) else Color(0xFFE53935)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "لیست فرش‌های فاکتور شده:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                invoice.carpets.forEach { carpet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• ${carpet.type} (${carpet.color})", fontSize = 12.sp)
                        Text(text = "${carpet.width}×${carpet.length} متر", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مبلغ کل فاکتور:", fontSize = 13.sp)
                    Text(
                        text = "${String.format("%,d", invoice.totalAmount)} ریال",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مبلغ پرداخت شده:", fontSize = 13.sp)
                    Text(
                        text = "${String.format("%,d", invoice.paidAmount)} ریال",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2BB673)
                    )
                }

                val remaining = invoice.totalAmount - invoice.paidAmount
                if (remaining > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "باقیمانده بدهی:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                        Text(
                            text = "${String.format("%,d", remaining)} ریال",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE53935)
                        )
                    }
                }
            }
        }

        if (invoice.status != InvoiceStatus.PAID) {
            Button(
                onClick = {
                    AppStateStore.recordPayment(invoice.id, invoice.totalAmount - invoice.paidAmount, "درگاه بانکی سداد")
                    showReceipt = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Payment, "Pay")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "پرداخت آنلاین کل وجه فاکتور", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = { showReceipt = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, "Receipt")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "مشاهده رسید پرداخت حرارتی", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showReceipt) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF0B4DA2), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "--- رسید پرداخت تراکنش قالیشویی هوشمند پاکان ---",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "نام کارخانه: قالیشویی مکانیزه پاکان تهران",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "کد فاکتور مرجع: ${invoice.id}", fontSize = 11.sp)
                    Text(text = "شناسه رهگیری پیگیری: ${invoice.trackingNumber}", fontSize = 11.sp)
                    Text(text = "مبلغ تراکنش: ${String.format("%,d", invoice.totalAmount)} ریال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "درگاه تسویه: ${invoice.paymentMethod ?: "پورتال کاربری بانک سامان"}", fontSize = 11.sp)
                    Text(text = "وضعیت: تسویه کامل و تایید شده توسط شاپرک", fontSize = 11.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Validation",
                        modifier = Modifier.size(64.dp)
                    )
                    Text(text = "رسید الکترونیکی معتبر شاپرک", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// 8. Live Driver Tracking View
@Composable
fun LiveDriverTrackingView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ردیابی زنده خودروی توزیع فرش",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4DA2)
            )
        }

        // Mock Live Google Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFF0B4DA2),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "راننده (علی کریمی) ۳.۲ کیلومتر تا شما فاصله دارد",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = "زمان تقریبی حضور: ۱۲ دقیقه آینده (ترافیک روان)",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        // Driver details overlay card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B4DA2).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFF0B4DA2))
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "علی کریمی (توزیع‌کننده پاسداران)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "کامیونت ایسوزو سفید - ۴۲ ب ۸۱۲ ایران ۳۳", fontSize = 11.sp, color = Color.Gray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2BB673).copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Phone, null, tint = Color(0xFF2BB673))
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF0B4DA2).copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Chat, null, tint = Color(0xFF0B4DA2))
                    }
                }
            }
        }
    }
}

// 9. Customer Support Live Chat View
@Composable
fun CustomerSupportChatView(messages: List<Message>) {
    var textMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                val alignEnd = !message.isIncoming
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (alignEnd) Color(0xFF0B4DA2) else Color.White
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (alignEnd) 16.dp else 0.dp,
                            bottomEnd = if (alignEnd) 0.dp else 16.dp
                        ),
                        border = if (alignEnd) null else BorderStroke(1.dp, Color(0xFFE3E8EF)),
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = message.text,
                                fontSize = 13.sp,
                                color = if (alignEnd) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${message.sender} • ${message.time}",
                                fontSize = 9.sp,
                                color = if (alignEnd) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        // Send Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textMessage,
                onValueChange = { textMessage = it },
                placeholder = { Text("پیام خود را بنویسید...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0B4DA2)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textMessage.isNotBlank()) {
                        AppStateStore.addMessage(textMessage, false, "مشتری")
                        textMessage = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF0B4DA2))
            ) {
                Icon(Icons.Default.Send, null, tint = Color.White)
            }
        }
    }
}

// Helper Class to support standard Tab navigation mapping
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun CustomerProfileView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B4DA2).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(48.dp))
        }

        Text(text = "جناب آقای مرتضی رضایی", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "شماره اشتراک: PAK-98124", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "اطلاعات حساب کاربری", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Divider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "تلفن همراه:", fontSize = 13.sp, color = Color.Gray)
                    Text(text = "۰۹۱۲۱۲۳۴۵۶۷", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "آدرس ثبت شده:", fontSize = 13.sp, color = Color.Gray)
                    Text(text = "پاسداران، بوستان چهارم، پلاک ۱۲", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "سطح وفاداری:", fontSize = 13.sp, color = Color.Gray)
                    Text(text = "طلایی (تخفیف همیشگی ۵٪)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                }
            }
        }
    }
}

@Composable
fun CustomerStatsView(invoices: List<Invoice>, carpets: List<Carpet>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "آمار و عملکرد قالیشویی شما", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
        }

        // Cards showing totals
        val totalSpent = invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.totalAmount }
        val totalCarpets = carpets.size

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2BB673).copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "کل مبالغ پرداخت شده شما:", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "${String.format("%,d", totalSpent)} ریال", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2BB673))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2).copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "تعداد فرش‌های سپرده شده به کارخانه:", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "$totalCarpets تخته فرش نفیس", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
            }
        }
    }
}

@Composable
fun CustomerRatingsView(onBack: () -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comments by remember { mutableStateOf("کار شستشو واقعاً عالی بود، راننده هم به موقع و با ادب مراجعه کرد. تشکر.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ثبت نظرات و امتیاز سفارش", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "میزان رضایت خود را از خدمات قالیشویی ثبت کنید:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..5) {
                        val active = i <= rating
                        Icon(
                            imageVector = if (active) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (active) Color(0xFFFF9800) else Color.Gray,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("متن دیدگاه یا شکایت شما") },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ثبت و ارسال امتیاز نهایی")
                }
            }
        }
    }
}
