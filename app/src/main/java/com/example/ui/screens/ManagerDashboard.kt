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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf("command") } // command, crm, orders, twin, finance, team, tracking, system
    val invoices by AppStateStore.invoices.collectAsState()
    val carpets by AppStateStore.carpets.collectAsState()
    val drivers by AppStateStore.drivers.collectAsState()
    val smsLogs by AppStateStore.smsLogs.collectAsState()
    val notifications by AppStateStore.notifications.collectAsState()
    val employees by AppStateStore.employees.collectAsState()
    val expenses by AppStateStore.expenses.collectAsState()
    val bankAccounts by AppStateStore.bankAccounts.collectAsState()
    val cashEntries by AppStateStore.cashEntries.collectAsState()
    val salaryRecords by AppStateStore.salaryRecords.collectAsState()
    val commissionRecords by AppStateStore.commissionRecords.collectAsState()
    val purchaseOrders by AppStateStore.purchaseOrders.collectAsState()
    val complaints by AppStateStore.complaints.collectAsState()
    val campaigns by AppStateStore.campaigns.collectAsState()
    val feedbacks by AppStateStore.feedbacks.collectAsState()
    val announcements by AppStateStore.announcements.collectAsState()
    val aiRecommendations by AppStateStore.aiRecommendations.collectAsState()
    val machines by AppStateStore.machines.collectAsState()
    val warehouseZones by AppStateStore.warehouseZones.collectAsState()
    val inventoryItems by AppStateStore.inventoryItems.collectAsState()
    val deliveryHeat by AppStateStore.deliveryHeat.collectAsState()
    val trustedDevices by AppStateStore.trustedDevices.collectAsState()
    val branches by AppStateStore.branches.collectAsState()
    val printers by AppStateStore.printers.collectAsState()
    val systemLogs by AppStateStore.systemLogs.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple("command", "اتاق فرمان", Icons.Default.Dashboard),
                        Triple("crm", "مشتریان", Icons.Default.People),
                        Triple("smart", "هوش مصنوعی", Icons.Default.AutoAwesome),
                        Triple("ops", "اتاق عملیات", Icons.Default.Factory),
                        Triple("orders", "سفارشات", Icons.Default.ListAlt),
                        Triple("finance", "مالی", Icons.Default.Payments),
                        Triple("team", "تیم", Icons.Default.Badge),
                        Triple("system", "سیستم", Icons.Default.Settings)
                    )
                    tabs.forEach { (tabId, label, icon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            icon = { Icon(icon, contentDescription = label, tint = if (isSelected) Color(0xFF0B4DA2) else Color.Gray) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF0B4DA2).copy(alpha = 0.1f))
                        )
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (activeTab) {
                                "command" -> "مرکز مانیتورینگ کارخانه پاکان"
                                "crm" -> "مدیریت ارتباط با مشتریان (CRM)"
                                "smart" -> "هوش مصنوعی، بازاریابی و پشتیبانی"
                                "ops" -> "اتاق عملیات زنده کارخانه"
                                "orders" -> "مدیریت سفارشات و تحویل هوشمند"
                                "twin" -> "همزاد دیجیتال کارخانه (Twin)"
                                "finance" -> "داشبورد حسابداری و مالی"
                                "team" -> "مدیریت تیم و پرسنل"
                                "tracking" -> "ردیابی زنده ناوگان رانندگان"
                                "system" -> "مدیریت سیستم و امنیت"
                                else -> "کنترل پنل مدیر"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4DA2)
                        )
                    },
                    actions = {
                        IconButton(onClick = { onLogout() }) {
                            Icon(Icons.Default.ExitToApp, "Logout", tint = Color(0xFFE53935))
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
                        fadeIn(animationSpec = tween(250)) with fadeOut(animationSpec = tween(250))
                    },
                    label = "ManagerTabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        "command" -> ManagerCommandCenterView(
                            invoices, carpets, drivers, notifications,
                            onOpenTwin = { activeTab = "twin" },
                            onOpenTracking = { activeTab = "tracking" }
                        )
                        "crm" -> ManagerCrmView(invoices)
                        "smart" -> ManagerCrmMarketingScreen(
                            complaints, campaigns, feedbacks, announcements, aiRecommendations, employees,
                            onLaunchCampaign = { AppStateStore.launchCampaign(it) },
                            onResolveComplaint = { id, status -> AppStateStore.updateComplaintStatus(id, status) }
                        )
                        "ops" -> ManagerOperationsCenterScreen(
                            machines, warehouseZones, inventoryItems, deliveryHeat, carpets, notifications
                        )
                        "orders" -> ManagerOrdersView(invoices, drivers, onOpenTracking = { activeTab = "tracking" })
                        "twin" -> ManagerDigitalTwinView(carpets)
                        "finance" -> ManagerAccountingScreen(
                            invoices, expenses, bankAccounts, cashEntries,
                            salaryRecords, commissionRecords, purchaseOrders
                        )
                        "team" -> ManagerTeamView(drivers, employees)
                        "tracking" -> ManagerDriversMapView(drivers)
                        "system" -> ManagerSystemAdminScreen(
                            trustedDevices, branches, printers, systemLogs, smsLogs, notifications,
                            onRevokeDevice = { AppStateStore.revokeDevice(it) },
                            onReconnectPrinter = { AppStateStore.reconnectPrinter(it) }
                        )
                    }
                }
            }
        }
    }
}

// 1. Command Center / Control Room Overview
@Composable
fun ManagerCommandCenterView(
    invoices: List<Invoice>,
    carpets: List<Carpet>,
    drivers: List<Driver>,
    notifications: List<NotificationItem>,
    onOpenTwin: () -> Unit = {},
    onOpenTracking: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Executive Summary KPI Matrices
        item {
            Text(
                text = "شاخص‌های کلیدی عملکرد امروز (KPIs)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Quick Access to Live Factory Monitor screens
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenTwin() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("همزاد دیجیتال کارخانه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenTracking() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = Color(0xFF2BB673), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نقشه زنده رانندگان", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiMiniCard(
                        title = "درآمد ناخالص امروز",
                        value = "${String.format("%,d", invoices.sumOf { it.totalAmount })} ریال",
                        color = Color(0xFF2BB673),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMiniCard(
                        title = "فرش‌های دریافتی",
                        value = "${carpets.size} تخته",
                        color = Color(0xFF0B4DA2),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KpiMiniCard(
                        title = "رانندگان آنلاین",
                        value = "${drivers.count { it.status.contains("آنلاین") }} راننده",
                        color = Color(0xFF0B4DA2),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMiniCard(
                        title = "کل فاکتورهای صادره",
                        value = "${invoices.size} فاکتور",
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // AI Predictive Recommendation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF0B4DA2).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, null, tint = Color(0xFF0B4DA2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پیشنهاد هوش مصنوعی (AI Command Center)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4DA2)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "بر اساس الگوی زمانی، میزان ترافیک پاسداران در ۲ ساعت آینده روان خواهد بود. پیشنهاد می‌شود ماموریت‌های انباشته منطقه پاسداران راننده «صادقی» زودتر تایید و فرستاده شوند. صرفه‌جویی پیش‌بینی شده سوخت: ۱۲٪.",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Live Alerts Center
        item {
            Text(text = "رخدادهای امنیتی و کاربری زنده (Audit log)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(notifications) { ntf ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (ntf.isRead) Color.Gray else Color(0xFFE53935))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ntf.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = ntf.body, fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(text = ntf.time, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// Helper Card for KPIs
@Composable
fun KpiMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// 2. Digital Twin (Floor Map representation)
@Composable
fun ManagerDigitalTwinView(carpets: List<Carpet>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "نقشه سه‌بعدی همزاد دیجیتال کارخانه قالیشویی (Twin)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "توزیع لحظه‌ای فرش‌ها در کارگاه:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                val areas = listOf(
                    Triple("انبار دریافت اصلی", carpets.count { it.status == CarpetStatus.WAREHOUSE }, Color(0xFF0B4DA2)),
                    Triple("سالن شستشو ریلی", carpets.count { it.status == CarpetStatus.WASHING }, Color(0xFFFF9800)),
                    Triple("گرم‌خانه گازی", carpets.count { it.status == CarpetStatus.DRYING }, Color(0xFFE53935)),
                    Triple("ایستگاه کنترل کیفیت (QC)", carpets.count { it.status == CarpetStatus.QC }, Color(0xFFD4AF37)),
                    Triple("بخش رفوگری و تعمیرات", carpets.count { it.qualityNotes != null }, Color(0xFF9C27B0)),
                    Triple("منطقه آماده بارگیری", carpets.count { it.status == CarpetStatus.READY }, Color(0xFF2BB673))
                )

                areas.forEach { (areaName, count, color) ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = areaName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "$count تخته فرش", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress bar reflecting relative capacity
                        val maxTarget = carpets.size.coerceAtLeast(1)
                        LinearProgressIndicator(
                            progress = count.toFloat() / maxTarget,
                            color = color,
                            trackColor = Color(0xFFF0F4F8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// 2b. CRM Dashboard (Prompt 4 Screens 3-4: Customer Segments, Customer Profile, Debt Status)
@Composable
fun ManagerCrmView(invoices: List<Invoice>) {
    // Derive customer profiles from invoice data
    val customers = remember(invoices) {
        invoices.groupBy { it.customerName to it.customerPhone }.map { (key, invs) ->
            val (name, phone) = key
            val totalSpent = invs.sumOf { it.paidAmount }
            val debt = invs.sumOf { it.totalAmount - it.paidAmount }
            val segment = when {
                debt > 10_000_000 -> "بدهکار"
                totalSpent > 15_000_000 -> "VIP"
                invs.size == 1 && invs.first().status == InvoiceStatus.PAID -> "عادی"
                else -> "عادی"
            }
            CustomerProfile(
                name = name,
                phone = phone,
                addresses = invs.map { it.address }.distinct(),
                totalOrders = invs.size,
                totalSpent = totalSpent,
                outstandingDebt = debt,
                segment = segment,
                lastOrderDate = invs.maxByOrNull { it.date }?.date ?: "-",
                invoices = invs
            )
        }
    }

    var selectedCustomer by remember { mutableStateOf<CustomerProfile?>(null) }

    if (selectedCustomer != null) {
        val c = selectedCustomer!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TextButton(onClick = { selectedCustomer = null }) {
                Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("بازگشت به لیست مشتریان", color = Color(0xFF0B4DA2), fontSize = 12.sp)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B4DA2).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF0B4DA2))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(c.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(c.phone, fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(Modifier.weight(1f))
                        val segColor = when (c.segment) {
                            "VIP" -> Color(0xFFD4AF37)
                            "بدهکار" -> Color(0xFFE53935)
                            else -> Color(0xFF2BB673)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(segColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(c.segment, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = segColor)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KpiMiniCard("تعداد سفارشات", "${c.totalOrders}", Color(0xFF0B4DA2), Modifier.weight(1f))
                        KpiMiniCard("مجموع خرید", "${String.format("%,d", c.totalSpent)} ریال", Color(0xFF2BB673), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    KpiMiniCard(
                        "بدهی باقی‌مانده",
                        if (c.outstandingDebt > 0) "${String.format("%,d", c.outstandingDebt)} ریال" else "بدون بدهی",
                        if (c.outstandingDebt > 0) Color(0xFFE53935) else Color(0xFF2BB673),
                        Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("آدرس‌های ثبت‌شده:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    c.addresses.forEach { addr ->
                        Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(addr, fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            Text("تاریخچه سفارشات و فاکتورها:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            c.invoices.forEach { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(inv.id, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(inv.date, fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(
                            inv.status.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (inv.status) {
                                InvoiceStatus.PAID -> Color(0xFF2BB673)
                                InvoiceStatus.PARTIAL -> Color(0xFFFF9800)
                                InvoiceStatus.UNPAID -> Color(0xFFE53935)
                            }
                        )
                    }
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // CRM Segment summary strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMiniCard("مشتریان VIP", "${customers.count { it.segment == "VIP" }}", Color(0xFFD4AF37), Modifier.weight(1f))
            KpiMiniCard("مشتریان بدهکار", "${customers.count { it.segment == "بدهکار" }}", Color(0xFFE53935), Modifier.weight(1f))
            KpiMiniCard("کل مشتریان", "${customers.size}", Color(0xFF0B4DA2), Modifier.weight(1f))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(customers) { c ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCustomer = c },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0B4DA2).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${c.totalOrders} سفارش • آخرین: ${c.lastOrderDate}", fontSize = 10.sp, color = Color.Gray)
                        }
                        val segColor = when (c.segment) {
                            "VIP" -> Color(0xFFD4AF37)
                            "بدهکار" -> Color(0xFFE53935)
                            else -> Color(0xFF2BB673)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(segColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(c.segment, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = segColor)
                        }
                    }
                }
            }
        }
    }
}

// 3. Finance & Revenue dashboards
@Composable
fun ManagerFinanceView(invoices: List<Invoice>, expenses: List<Expense> = emptyList()) {
    val totalRevenue = invoices.sumOf { it.totalAmount }
    val totalCash = invoices.filter { it.paymentMethod == "نقدی" }.sumOf { it.totalAmount }
    val totalOnline = invoices.filter { it.paymentMethod == "درگاه آنلاین" || it.paymentMethod == "کارتخوان سیار" }.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenses
    val outstandingDebt = invoices.sumOf { it.totalAmount - it.paidAmount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2BB673).copy(alpha = 0.05f)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF2BB673).copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "مجموع درآمد ناخالص صندوق قالیشویی", fontSize = 13.sp, color = Color.DarkGray)
                Text(
                    text = "${String.format("%,d", totalRevenue)} ریال",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2BB673)
                )
            }
        }

        // Custom drawn Canvas Financial line-chart representing positive growth trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "روند درآمد هفتگی کارخانه", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3.dp.toPx()
                        val points = listOf(10f, 30f, 25f, 60f, 45f, 85f, 100f)
                        val widthGap = size.width / (points.size - 1)
                        val heightMultiplier = size.height / 100f

                        val path = Path().apply {
                            moveTo(0f, size.height - (points[0] * heightMultiplier))
                            for (i in 1 until points.size) {
                                lineTo(i * widthGap, size.height - (points[i] * heightMultiplier))
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF0B4DA2),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "شنبه", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "دوشنبه", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "چهارشنبه", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "جمعه", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // Breakdowns by payment method
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "تفکیک مبالغ دریافتی:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "تسویه بانکی آنلاین (شاپرک):", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format("%,d", totalOnline)} ریال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "تسویه نقدی راننده:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format("%,d", totalCash)} ریال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
        }

        // Profit & Debt summary (Prompt 9: Profit Analysis + Outstanding Debts)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiMiniCard(
                title = "سود خالص (درآمد - هزینه)",
                value = "${String.format("%,d", netProfit)} ریال",
                color = if (netProfit >= 0) Color(0xFF2BB673) else Color(0xFFE53935),
                modifier = Modifier.weight(1f)
            )
            KpiMiniCard(
                title = "مطالبات معوق مشتریان",
                value = "${String.format("%,d", outstandingDebt)} ریال",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
        }

        // Expense management (Prompt 9: Expense Management)
        Text(text = "مدیریت هزینه‌های کارخانه", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "مجموع هزینه‌های ثبت شده:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format("%,d", totalExpenses)} ریال", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE53935))
                }
            }
        }

        expenses.forEach { expense ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = expense.category, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${expense.description} • ${expense.date}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${String.format("%,d", expense.amount)} ریال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
                        val approved = expense.status == "تایید شده"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background((if (approved) Color(0xFF2BB673) else Color(0xFFFF9800)).copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = expense.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (approved) Color(0xFF2BB673) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3b. Orders Management + Smart Delivery (Prompt 4 Screen 5 + Prompt 5 Screens 2,3,17,18)
@Composable
fun ManagerOrdersView(invoices: List<Invoice>, drivers: List<Driver>, onOpenTracking: () -> Unit = {}) {
    var filter by remember { mutableStateOf("all") } // all, unpaid, partial, paid

    val deliveredToday = invoices.count { it.status == InvoiceStatus.PAID }
    val pending = invoices.count { it.status != InvoiceStatus.PAID }
    val onlineDrivers = drivers.count { it.status.contains("آنلاین") }
    val revenueToday = invoices.sumOf { it.paidAmount }

    val filtered = when (filter) {
        "unpaid" -> invoices.filter { it.status == InvoiceStatus.UNPAID }
        "partial" -> invoices.filter { it.status == InvoiceStatus.PARTIAL }
        "paid" -> invoices.filter { it.status == InvoiceStatus.PAID }
        else -> invoices
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Manager Delivery Monitor (Prompt 5 Screen 17)
        item { Text("مانیتور زنده تحویل (Delivery Monitor)", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("تحویل‌شده امروز", "$deliveredToday سفارش", Color(0xFF2BB673), Modifier.weight(1f))
                KpiMiniCard("در انتظار تحویل", "$pending سفارش", Color(0xFFFF9800), Modifier.weight(1f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("رانندگان آنلاین", "$onlineDrivers نفر", Color(0xFF0B4DA2), Modifier.weight(1f))
                KpiMiniCard("درآمد امروز", "${String.format("%,d", revenueToday)} ریال", Color(0xFF2BB673), Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpenTracking() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("مشاهده نقشه زنده ناوگان تحویل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AI Delivery Planner (Prompt 5 Screen 2 & 3: route optimization + grouped delivery)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF0B4DA2).copy(alpha = 0.15f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Route, null, tint = Color(0xFF0B4DA2))
                        Spacer(Modifier.width(8.dp))
                        Text("برنامه‌ریز هوشمند تحویل (AI Delivery Planner)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "پیشنهاد گروه‌بندی: ۳ سفارش منطقه سعادت‌آباد و شریعتی در یک مسیر ترکیبی قابل تحویل هستند. مسافت کل کاهش می‌یابد و در سوخت صرفه‌جویی می‌شود.",
                        fontSize = 11.sp, color = Color.DarkGray, lineHeight = 16.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مسافت کل بهینه‌شده: ۱۸.۴ کیلومتر", fontSize = 10.sp, color = Color.Gray)
                        Text("راننده پیشنهادی: حمید صادقی", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2BB673))
                    }
                }
            }
        }

        // Delivery Analytics (Prompt 5 Screen 18)
        item { Text("آنالیتیکس تحویل (Delivery Analytics)", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("میانگین زمان تحویل:", fontSize = 11.sp, color = Color.Gray)
                        Text("۳۴ دقیقه", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رضایت مشتری (میانگین):", fontSize = 11.sp, color = Color.Gray)
                        Text("۴.۷ از ۵", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2BB673))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("فعال‌ترین راننده:", fontSize = 11.sp, color = Color.Gray)
                        Text(drivers.maxByOrNull { it.completedMissions }?.name ?: "-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Orders list with filters (Prompt 4 Screen 5)
        item { Text("مدیریت سفارشات (فیلتر وضعیت)", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("all" to "همه", "unpaid" to "پرداخت‌نشده", "partial" to "علی‌الحساب", "paid" to "تسویه‌شده").forEach { (id, label) ->
                    val selected = filter == id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (selected) Color(0xFF0B4DA2) else Color.White)
                            .border(1.dp, if (selected) Color(0xFF0B4DA2) else Color(0xFFE3E8EF), RoundedCornerShape(20.dp))
                            .clickable { filter = id }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Color.DarkGray)
                    }
                }
            }
        }
        items(filtered) { inv ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("${inv.id} • ${inv.customerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("راننده: ${inv.driverName} • ${inv.date}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(
                        inv.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (inv.status) {
                            InvoiceStatus.PAID -> Color(0xFF2BB673)
                            InvoiceStatus.PARTIAL -> Color(0xFFFF9800)
                            InvoiceStatus.UNPAID -> Color(0xFFE53935)
                        }
                    )
                }
            }
        }
    }
}

// 3c. Team Management: Drivers + Warehouse Staff (Prompt 4 Screens 6-7)
@Composable
fun ManagerTeamView(drivers: List<Driver>, employees: List<Employee> = emptyList()) {
    var tab by remember { mutableStateOf("drivers") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("drivers" to "رانندگان", "staff" to "پرسنل انبار").forEach { (id, label) ->
                val selected = tab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF0B4DA2) else Color.White)
                        .border(1.dp, if (selected) Color(0xFF0B4DA2) else Color(0xFFE3E8EF), RoundedCornerShape(12.dp))
                        .clickable { tab = id }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else Color.DarkGray)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (tab == "drivers") {
                items(drivers) { driver ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF0B4DA2).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocalShipping, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(driver.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(driver.status, fontSize = 10.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(2.dp))
                                    Text("${driver.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("امتیاز عملکرد: ${driver.performanceScore}", fontSize = 10.sp, color = Color.Gray)
                                Text("ماموریت‌های تکمیل‌شده: ${driver.completedMissions}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                items(employees) { emp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF2BB673).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Badge, null, tint = Color(0xFF2BB673), modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(emp.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(emp.role, fontSize = 10.sp, color = Color.Gray)
                                }
                                val statusColor = when (emp.status) {
                                    "حاضر" -> Color(0xFF2BB673)
                                    "در مرخصی" -> Color(0xFFFF9800)
                                    else -> Color(0xFFE53935)
                                }
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusColor.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(emp.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("وظایف امروز: ${emp.completedTasks}/${emp.tasksToday}", fontSize = 10.sp, color = Color.Gray)
                                Text("امتیاز عملکرد: ${emp.performanceScore}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Drivers Map Tracking
@Composable
fun ManagerDriversMapView(drivers: List<Driver>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "ردیابی ماهواره‌ای رانندگان روی نقشه زنده", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        // Mock Google Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Map, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(40.dp))
                Text(text = "نقشه زنده توزیع خودروهای قالیشویی تهران", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
            }
        }

        Text(text = "وضعیت زنده ناوگان حمل و نقل:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(drivers) { driver ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = driver.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "سرعت: ${driver.speed} km/h • وضعیت: ${driver.status}", fontSize = 11.sp, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(
                                    if (driver.status.contains("آنلاین")) Color(0xFF2BB673).copy(alpha = 0.1f)
                                    else Color.Gray.copy(alpha = 0.1f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (driver.status.contains("آنلاین")) "فعال" else "آفلاین",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (driver.status.contains("آنلاین")) Color(0xFF2BB673) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// 5. Cloud Infrastructure Settings view
@Composable
fun ManagerSystemSettingsView(smsLogs: List<SmsLog>, notifications: List<NotificationItem> = emptyList()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "زیرساخت ابری قالیشویی (Cloud Server)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "پینگ سرورها و اتصالات خارجی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "تونل دیتابیس ابری (Cloudflare Tunnel):", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "متصل (SSL - Safe) • ۲۴ میلی‌ثانیه", fontSize = 11.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "سامانه پیامک ملی (KavehNegar):", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "متصل (ارسال عادی) • اعتبار: ۴۲۰,۰۰۰ ریال", fontSize = 11.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "سرویس نقشه گوگل (Maps API Key):", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "فعال و ثبت نام شده در کنسول گوگل", fontSize = 11.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(text = "آخرین پیامک‌های سیستمی و OTP ارسالی:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        smsLogs.forEach { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "گیرنده: ${log.recipient} • زمان: ${log.time}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = log.status, fontSize = 10.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = log.text, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                }
            }
        }

        // Users & Permissions (Prompt 10: Roles & Permissions)
        Text(text = "کاربران و سطوح دسترسی (Roles & Permissions)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        val roles = listOf(
            Triple("مدیر کارخانه", "دسترسی کامل", Color(0xFF0B4DA2)),
            Triple("راننده", "ماموریت، فاکتور، چت", Color(0xFF2BB673)),
            Triple("انباردار", "انبار، قفسه، QC", Color(0xFFFF9800)),
            Triple("حسابدار", "مالی، هزینه، گزارش", Color(0xFFE53935))
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                roles.forEach { (role, perms, color) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = role, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = perms, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Audit log / System log summary (Prompt 10: System Logs, Audit Dashboard)
        Text(text = "رخدادهای سیستمی اخیر (System Logs)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        notifications.take(5).forEach { ntf ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ntf.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "دسته‌بندی: ${ntf.category}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(text = ntf.time, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        // Backup Center (Prompt 10: Backup Center)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2BB673).copy(alpha = 0.06f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF2BB673).copy(alpha = 0.2f))
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDone, null, tint = Color(0xFF2BB673))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "آخرین پشتیبان‌گیری ابری: امروز ساعت ۰۴:۰۰", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "حجم بکاپ: ۱۲۸ مگابایت • وضعیت: موفق", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
