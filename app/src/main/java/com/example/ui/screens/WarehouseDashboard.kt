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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseDashboardScreen(onLogout: () -> Unit) {
    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, qc, washing, shelves, tracking, emergency
    val carpets by AppStateStore.carpets.collectAsState()
    val notifications by AppStateStore.notifications.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCarpetId by remember { mutableStateOf<String?>("C-103") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val tabs = listOf(
                        Triple("dashboard", "انبار زنده", Icons.Default.Layers),
                        Triple("shelves", "قفسه‌بندی", Icons.Default.GridOn),
                        Triple("washing", "خط شستشو", Icons.Default.LocalLaundryService),
                        Triple("qc", "کنترل کیفیت", Icons.Default.AssignmentTurnedIn),
                        Triple("tracking", "ردیابی QR", Icons.Default.QrCodeScanner)
                    )
                    tabs.forEach { (tabId, label, icon) ->
                        val isSelected = activeTab == tabId
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = tabId },
                            icon = { Icon(icon, contentDescription = label, tint = if (isSelected) Color(0xFF0B4DA2) else Color.Gray) },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                                "dashboard" -> "سامانه انبار کارخانه پاکان"
                                "shelves" -> "مدیریت قفسه و چیدمان فرش"
                                "washing" -> "صف شستشو و خشک‌کن"
                                "qc" -> "کنترل کیفیت و رفوگری"
                                "tracking" -> "ردیابی کارگاهی بارکد"
                                "emergency" -> "وضعیت اضطراری و آفلاین"
                                else -> "کارگاه پاکان"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4DA2)
                        )
                    },
                    actions = {
                        IconButton(onClick = { activeTab = "emergency" }) {
                            Icon(Icons.Default.CloudOff, "Offline", tint = Color(0xFFFF9800))
                        }
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
                    label = "WarehouseTabTransition"
                ) { currentTab ->
                    when (currentTab) {
                        "dashboard" -> WarehouseOverviewView(carpets, notifications) { tab -> activeTab = tab }
                        "shelves" -> WarehouseShelvesView(carpets)
                        "washing" -> WarehouseWashingView(carpets)
                        "qc" -> WarehouseQcView(carpets)
                        "tracking" -> WarehouseTrackingView(carpets, searchQuery, onQueryChange = { searchQuery = it })
                        "emergency" -> WarehouseEmergencyView()
                    }
                }
            }
        }
    }
}

// 1. Warehouse Overview / Dashboard View
@Composable
fun WarehouseOverviewView(
    carpets: List<Carpet>,
    notifications: List<NotificationItem>,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warehouse Stats Cards Group
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE3E8EF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "ظرفیت فعلی قفسه‌های انبار", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "قفسه‌های اشغال شده:", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "۱۴۲ از ۲۰۰ قفسه (۷۱٪)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B4DA2))
                        }

                        Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = 0.71f,
                                color = Color(0xFF0B4DA2),
                                trackColor = Color(0xFFF0F4F8),
                                strokeWidth = 6.dp
                            )
                            Text(text = "۷۱٪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
                        }
                    }
                }
            }
        }

        // Sub-sections Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val stats = listOf(
                    Triple("کل فرش‌های امروز", "${carpets.size} تخته", Color(0xFF0B4DA2)),
                    Triple("در صف شستشو", "${carpets.count { it.status == CarpetStatus.WASHING }} تخته", Color(0xFFFF9800)),
                    Triple("آماده تحویل", "${carpets.count { it.status == CarpetStatus.READY }} تخته", Color(0xFF2BB673))
                )

                stats.forEach { (label, value, color) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = label, fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
                        }
                    }
                }
            }
        }

        // Recent Notifications section
        item {
            Text(
                text = "هشدارهای زنده کارگاهی",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        items(notifications.take(3)) { ntf ->
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (ntf.category) {
                                    "انبار" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                    "مالی" -> Color(0xFF2BB673).copy(alpha = 0.1f)
                                    else -> Color(0xFF0B4DA2).copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (ntf.category) {
                                "انبار" -> Icons.Default.Layers
                                "مالی" -> Icons.Default.Payments
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = when (ntf.category) {
                                "انبار" -> Color(0xFFFF9800)
                                "مالی" -> Color(0xFF2BB673)
                                else -> Color(0xFF0B4DA2)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = ntf.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = ntf.body, fontSize = 11.sp, color = Color.Gray)
                    }

                    Text(text = ntf.time, fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}

// 2. Storage Shelves View
@Composable
fun WarehouseShelvesView(carpets: List<Carpet>) {
    var selectedShelf by remember { mutableStateOf("قفسه A-12") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "طرح جانمایی قفسه‌های فیزیکی کارخانه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                // Grid mapping the rack shelves layout
                val shelvesGrid = listOf(
                    listOf("A-11", "A-12", "A-13", "A-14"),
                    listOf("B-01", "B-02", "B-03", "B-05"),
                    listOf("C-01", "C-02", "C-03", "C-04")
                )

                shelvesGrid.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { shelf ->
                            val fullLabel = "قفسه $shelf"
                            val isSelected = selectedShelf == fullLabel
                            val hasCarpet = carpets.any { it.shelf == fullLabel }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) Color(0xFF0B4DA2)
                                        else if (hasCarpet) Color(0xFFF0F4F8)
                                        else Color.White
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Color(0xFF0B4DA2) else Color(0xFFE3E8EF),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedShelf = fullLabel }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = shelf,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black
                                    )
                                    if (hasCarpet && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF2BB673))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(text = "فرش‌های مستقر در $selectedShelf:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        val carpetsInShelf = carpets.filter { it.shelf == selectedShelf }

        if (carpetsInShelf.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E8EF), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "این قفسه در حال حاضر خالی است", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(carpetsInShelf) { carpet ->
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
                                Text(text = carpet.type, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "کد رهگیری: ${carpet.trackingNumber} • رنگ: ${carpet.color}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = { AppStateStore.updateCarpetStatus(carpet.id, CarpetStatus.WASHING) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "انتقال به شستشو", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Washing Queue Department View
@Composable
fun WarehouseWashingView(carpets: List<Carpet>) {
    val washingQueue = carpets.filter { it.status == CarpetStatus.WASHING || it.status == CarpetStatus.WAREHOUSE }

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
                Text(text = "وضعیت دستگاه‌های شستشوی صنعتی", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "دستگاه اتوماتیک روتاری شماره ۱:", fontSize = 12.sp)
                    Text(text = "در حال شستشو (۴۲ دقیقه باقیمانده)", fontSize = 12.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "دستگاه ریلی شماره ۲:", fontSize = 12.sp)
                    Text(text = "آماده به کار (Idle)", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Text(text = "صف ورودی و خط شستشوی کارگاه", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(washingQueue) { carpet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = carpet.type, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "وضعیت: ${carpet.status.label} • خدمت: ${carpet.serviceType}", fontSize = 11.sp, color = Color.Gray)
                            }

                            if (carpet.status == CarpetStatus.WAREHOUSE) {
                                Button(
                                    onClick = { AppStateStore.updateCarpetStatus(carpet.id, CarpetStatus.WASHING) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "شروع شستشو", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { AppStateStore.updateCarpetStatus(carpet.id, CarpetStatus.QC) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "اتمام شستشو و خشک‌کن (QC)", fontSize = 11.sp)
                                }
                            }
                        }

                        if (carpet.status == CarpetStatus.WASHING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = 0.45f,
                                color = Color(0xFF0B4DA2),
                                trackColor = Color(0xFFF0F4F8),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Quality Control Dashboard View
@Composable
fun WarehouseQcView(carpets: List<Carpet>) {
    val qcQueue = carpets.filter { it.status == CarpetStatus.QC }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "ایستگاه بازرسی کنترل کیفیت نهایی (QC)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        if (qcQueue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE3E8EF), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "فرشی در صف بازرسی کیفیت نیست", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(qcQueue) { carpet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = carpet.type, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "کد بارکد: ${carpet.trackingNumber} • زمینه رنگی: ${carpet.color}", fontSize = 11.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(12.dp))

                            // Side-by-side Photo comparison before / after washing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEBEE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "تصویر قبل از شستشو\n(چرک و غبار مفرط)",
                                        fontSize = 10.sp,
                                        color = Color(0xFFC62828),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "تصویر پس از شستشو\n(سفید شفاف اعلا)",
                                        fontSize = 10.sp,
                                        color = Color(0xFF2E7D32),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { AppStateStore.updateCarpetStatus(carpet.id, CarpetStatus.READY) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = "تایید کیفیت (Pass)", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { AppStateStore.updateCarpetStatus(carpet.id, CarpetStatus.WASHING) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = "نیاز به شستشوی مجدد", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Carpet Tracking QR/Search View
@Composable
fun WarehouseTrackingView(
    carpets: List<Carpet>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    val filtered = carpets.filter { it.id.contains(query, true) || it.trackingNumber.contains(query, true) || it.type.contains(query, true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("جستجوی سریع بارکد / شناسه فرش...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { carpet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3E8EF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = carpet.type, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(text = "شناسه بارکد: ${carpet.id} • قفسه: ${carpet.shelf ?: "نامشخص"}", fontSize = 11.sp, color = Color.Gray)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFF0B4DA2).copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = carpet.status.label, fontSize = 10.sp, color = Color(0xFF0B4DA2), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 6. Emergency & Simulated Offline View
@Composable
fun WarehouseEmergencyView() {
    var isSyncing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CloudOff, null, tint = Color(0xFFFF9800), modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "شبیه‌ساز وضعیت قطعی شبکه کارگاه",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "در صورت بروز قطعی اینترنت در سالن شستشوی کارخانه، برنامه فاقد توقف و به صورت کاملاً آفلاین عملیات ثبت بارکد و اسکن قفسه‌ها را در دیتابیس محلی ذخیره می‌کند.",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "صف آپلود دیتابیس محلی (Sync Queue)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "سوابق اسکن شده در صف انتظار:", fontSize = 12.sp)
                    Text(text = "۱۲ عدد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { isSyncing = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Sync, null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isSyncing) "در حال همگام‌سازی..." else "همگام‌سازی دستی با سرور مرکزی ERP")
        }
    }
}
