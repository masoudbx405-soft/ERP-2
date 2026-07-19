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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun DriverDashboardScreen(onLogout: () -> Unit) {
    // Current step in the driver workflow
    var step by remember { mutableStateOf("dashboard") } // dashboard, details, navigation, verify, otp, invoice, add_carpet, qr_scan, printer, invoice_preview, delivery_confirm, payment, success
    val scope = rememberCoroutineScope()

    val missions by AppStateStore.missions.collectAsState()
    val invoices by AppStateStore.invoices.collectAsState()
    val carpets by AppStateStore.carpets.collectAsState()
    val messages by AppStateStore.messages.collectAsState()

    var activeMissionId by remember { mutableStateOf("M-301") }
    val activeMission = missions.find { it.id == activeMissionId } ?: missions[0]
    val activeInvoice = invoices.find { it.id == activeMission.invoiceId } ?: invoices[0]

    // Form states for Invoice registration
    var carpetType by remember { mutableStateOf("ماشینی ۹۰۰ شانه") }
    var carpetColor by remember { mutableStateOf("سرمه‌ای افشان") }
    var carpetWidth by remember { mutableStateOf("۲.۵") }
    var carpetLength by remember { mutableStateOf("۳.۵") }
    var carpetService by remember { mutableStateOf("شستشوی معمولی") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (step) {
                                "dashboard" -> "داشبورد توزیع راننده"
                                "details" -> "جزئیات ماموریت کارخانه"
                                "navigation" -> "مسیریاب زنده قالیشویی"
                                "verify" -> "احراز هویت مشتری"
                                "otp" -> "ورود کد احراز OTP"
                                "invoice" -> "ثبت و صدور فاکتور جدید"
                                "add_carpet" -> "افزودن مشخصات فرش"
                                "qr_scan" -> "اسکنر بارکد و QR الصاقی"
                                "printer" -> "چاپگر حرارتی بلوتوث"
                                "invoice_preview" -> "پیش‌نمایش فاکتور چاپی"
                                "delivery_confirm" -> "تایید تحویل فرش‌ها"
                                "payment" -> "دریافت وجه از مشتری"
                                "success" -> if (activeMission.type == "تحویل") "تحویل با موفقیت انجام شد" else "ثبت موفق سفارش"
                                else -> "توزیع پاکان"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B4DA2)
                        )
                    },
                    navigationIcon = {
                        if (step != "dashboard") {
                            IconButton(onClick = {
                                step = when (step) {
                                    "details" -> "dashboard"
                                    "navigation" -> "details"
                                    "verify" -> "details"
                                    "otp" -> "verify"
                                    "invoice" -> "details"
                                    "add_carpet" -> "invoice"
                                    "qr_scan" -> "invoice"
                                    "printer" -> "invoice"
                                    "invoice_preview" -> "invoice"
                                    "delivery_confirm" -> "otp"
                                    "payment" -> "delivery_confirm"
                                    "success" -> "dashboard"
                                    else -> "dashboard"
                                }
                            }) {
                                Icon(Icons.Default.ArrowBack, "Back", tint = Color(0xFF0B4DA2))
                            }
                        } else {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.LocalShipping, "Driver", tint = Color(0xFF0B4DA2))
                            }
                        }
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
                    targetState = step,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    },
                    label = "DriverStepTransition"
                ) { currentStep ->
                    when (currentStep) {
                        "dashboard" -> DriverMissionsView(
                            missions = missions,
                            onSelectMission = { id, nextStep ->
                                activeMissionId = id
                                step = nextStep
                            }
                        )
                        "details" -> DriverMissionDetailsView(
                            mission = activeMission,
                            invoice = activeInvoice,
                            onNavigate = { step = "navigation" },
                            onStartInvoice = { step = "verify" }
                        )
                        "navigation" -> DriverNavigationMapView(
                            mission = activeMission,
                            onArrived = { step = "verify" }
                        )
                        "verify" -> DriverVerificationView(
                            mission = activeMission,
                            onSendOtp = { step = "otp" }
                        )
                        "otp" -> DriverOtpView(
                            mission = activeMission,
                            onVerified = {
                                step = if (activeMission.type == "تحویل") "delivery_confirm" else "invoice"
                            }
                        )
                        "invoice" -> DriverInvoiceRegistrationView(
                            invoice = activeInvoice,
                            onAddCarpet = { step = "add_carpet" },
                            onAssignQr = { step = "qr_scan" },
                            onPrintReceipt = { step = "printer" },
                            onPreview = { step = "invoice_preview" },
                            onComplete = {
                                AppStateStore.completeMission(activeMission.id)
                                step = "success"
                            }
                        )
                        "add_carpet" -> DriverAddCarpetForm(
                            type = carpetType,
                            color = carpetColor,
                            width = carpetWidth,
                            length = carpetLength,
                            service = carpetService,
                            onTypeChange = { carpetType = it },
                            onColorChange = { carpetColor = it },
                            onWidthChange = { carpetWidth = it },
                            onLengthChange = { carpetLength = it },
                            onServiceChange = { carpetService = it },
                            onSave = {
                                AppStateStore.addCarpetToInvoice(
                                    activeInvoice.id,
                                    carpetType,
                                    carpetColor,
                                    carpetWidth.toDoubleOrNull() ?: 3.0,
                                    carpetLength.toDoubleOrNull() ?: 4.0,
                                    carpetService
                                )
                                step = "invoice"
                            }
                        )
                        "qr_scan" -> DriverQrScanView {
                            step = "invoice"
                        }
                        "printer" -> DriverPrinterView(activeInvoice) {
                            step = "invoice"
                        }
                        "invoice_preview" -> DriverInvoicePreviewView(activeInvoice) {
                            step = "invoice"
                        }
                        "delivery_confirm" -> DriverDeliveryConfirmationView(
                            invoice = activeInvoice,
                            onConfirm = { step = "payment" }
                        )
                        "payment" -> DriverCollectPaymentView(
                            invoice = activeInvoice,
                            onCollect = { amount, method ->
                                AppStateStore.completeDelivery(activeInvoice.id, activeMission.id, amount, method)
                                step = "success"
                            }
                        )
                        "success" -> DriverSuccessView(
                            isDelivery = activeMission.type == "تحویل",
                            onFinish = { step = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}

// 1. Driver Missions List Dashboard
@Composable
fun DriverMissionsView(
    missions: List<Mission>,
    onSelectMission: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Driver performance summary banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "خوش آمدید، علی کریمی عزیز",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "نیسان توزیع ۲ - کارکرد امروز شما عالی است",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text(text = "امتیاز عملکرد", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(text = "۹۸ از ۱۰۰", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(text = "مسافت امروز", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(text = "۶۸.۴ کیلومتر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalShipping, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        item {
            Text(
                text = "ماموریت‌های تخصیص یافته امروز",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        items(missions) { mission ->
            val isCompleted = mission.status == MissionStatus.COMPLETED
            val isActive = mission.status == MissionStatus.ACTIVE

            val cardBorder = if (isActive) Color(0xFF0B4DA2) else Color(0xFFE3E8EF)
            val cardBg = if (isActive) Color(0xFF0B4DA2).copy(alpha = 0.02f) else Color.White

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMission(mission.id, "details") }
                    .border(
                        width = if (isActive) 2.dp else 1.dp,
                        color = cardBorder,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (mission.type == "جمع‌آوری") Color(0xFF0B4DA2).copy(alpha = 0.1f)
                                        else Color(0xFF2BB673).copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mission.type == "جمع‌آوری") Icons.Default.Layers else Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (mission.type == "جمع‌آوری") Color(0xFF0B4DA2) else Color(0xFF2BB673),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${mission.type}: ${mission.customerName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Status badge
                        val statusBg = when (mission.status) {
                            MissionStatus.COMPLETED -> Color(0xFF2BB673).copy(alpha = 0.1f)
                            MissionStatus.ACTIVE -> Color(0xFF0B4DA2).copy(alpha = 0.1f)
                            MissionStatus.PENDING -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        }
                        val statusColor = when (mission.status) {
                            MissionStatus.COMPLETED -> Color(0xFF2BB673)
                            MissionStatus.ACTIVE -> Color(0xFF0B4DA2)
                            MissionStatus.PENDING -> Color(0xFFFF9800)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(statusBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = mission.status.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mission.address,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مسافت: ${mission.distanceKm} کیلومتر • زمان تخمینی: ${mission.estMinutes} دقیقه",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        if (!isCompleted) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        AppStateStore.startMission(mission.id)
                                        onSelectMission(mission.id, "navigation")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "مسیریابی هوشمند", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Driver Mission Detail Screen
@Composable
fun DriverMissionDetailsView(
    mission: Mission,
    invoice: Invoice,
    onNavigate: () -> Unit,
    onStartInvoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "سفارش دهنده", fontSize = 12.sp, color = Color.Gray)
                        Text(text = mission.customerName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2BB673).copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Phone, null, tint = Color(0xFF2BB673))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Map, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "آدرس تحویل/جمع‌آوری", fontSize = 12.sp, color = Color.Gray)
                        Text(text = mission.address, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map mini preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE1F5FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Map, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(24.dp))
                        Text(text = "پاسداران، بوستان چهارم - نمایش روی نقشه", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B4DA2))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "اقلام سفارش (پیش‌نویس فاکتور)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                invoice.carpets.forEach { carpet ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${carpet.type} (${carpet.color})", fontSize = 12.sp)
                        Text(text = "${carpet.width} × ${carpet.length} متر", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مجموع مبلغ فاکتور:", fontSize = 13.sp)
                    Text(
                        text = "${String.format("%,d", invoice.totalAmount)} ریال",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B4DA2)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onNavigate,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Map, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("شروع ناوبری")
            }

            Button(
                onClick = onStartInvoice,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("شروع صدور فاکتور")
            }
        }
    }
}

// 3. Driver Live Navigation View
@Composable
fun DriverNavigationMapView(
    mission: Mission,
    onArrived: () -> Unit
) {
    var speed by remember { mutableStateOf(42) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            speed = Random.nextInt(35, 55)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Google Maps Simulation Background (light green)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = Color(0xFF0B4DA2).copy(alpha = 0.2f),
                    modifier = Modifier.size(150.dp)
                )
                Text(
                    text = "نمای سه‌بعدی شبیه‌ساز نقشه تهران",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        // Top Navigation Prompt overlay (Glassmorphism effect)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B4DA2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ArrowUpward, null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = "۲۵۰ متر جلوتر، بپیچید به سمت راست", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "بوستان چهارم پاسداران", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Bottom Navigation details overlay
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "مسیر تا مقصد: ${mission.customerName}", fontSize = 12.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "۸ دقیقه", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2BB673))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "(۱.۶ کیلومتر)", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(text = "سرعت فعلی: $speed کیلومتر بر ساعت", fontSize = 11.sp, color = Color.DarkGray)
                }

                Button(
                    onClick = onArrived,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(Icons.Default.Check, null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("من رسیده‌ام")
                }
            }
        }
    }
}

// 4. Driver Verification Screen
@Composable
fun DriverVerificationView(
    mission: Mission,
    onSendOtp: () -> Unit
) {
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
                .background(Color(0xFF0B4DA2).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, null, tint = Color(0xFF0B4DA2), modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "احراز هویت مشتری پیش از صدور فاکتور",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "جهت ثبت نهایی و تایید فاکتور در سامانه مرکزی ERP، رمز یک‌بار مصرف OTP به شماره همراه مشتری (${mission.phone}) پیامک خواهد شد.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                AppStateStore.addNotification("کد احراز هویت پیامک شد", "رمز تایید هویت برای مشتری ${mission.customerName} ارسال شد.", "سیستم")
                onSendOtp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "ارسال پیامک تایید هویت (OTP)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 5. Driver OTP Verification input View
@Composable
fun DriverOtpView(
    mission: Mission,
    onVerified: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(120) }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "رمز احراز هویت پیامکی را وارد کنید",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "کد ۵ رقمی به شماره همراه مشتری ${mission.customerName} ارسال گردید",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 5) code = it },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(64.dp),
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 12.sp
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B4DA2)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ارسال مجدد کد تا: ${countdown / 60}:${String.format("%02d", countdown % 60)}",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (code.isNotEmpty()) {
                    onVerified()
                }
            },
            enabled = code.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "بررسی و تایید شماره مشتری", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 6. Driver Invoice Form Registration View
@Composable
fun DriverInvoiceRegistrationView(
    invoice: Invoice,
    onAddCarpet: () -> Unit,
    onAssignQr: () -> Unit,
    onPrintReceipt: () -> Unit,
    onPreview: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "جزئیات فاکتور صادر شده", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "شماره فاکتور سیستم: ${invoice.id}", fontSize = 11.sp, color = Color.Gray)
                Text(text = "مشتری: ${invoice.customerName} • آدرس: ${invoice.address}", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Text(
            text = "لیست فرش‌های تحویل گرفته شده:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        invoice.carpets.forEach { carpet ->
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
                        Text(
                            text = "ابعاد: ${carpet.width} × ${carpet.length} متر (${carpet.area} متر مربع)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "خدمت: ${carpet.serviceType} • رنگ: ${carpet.color}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2BB673).copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "بارکد الصاق شد", fontSize = 10.sp, color = Color(0xFF2BB673), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Add Carpet and Assign QR actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddCarpet,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = Color(0xFF0B4DA2))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن فرش جدید", fontSize = 12.sp, color = Color(0xFF0B4DA2), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAssignQr,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null, tint = Color(0xFFFF9800))
                Spacer(modifier = Modifier.width(4.dp))
                Text("اسکن و الصاق بارکد", fontSize = 12.sp, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مجموع مبلغ فاکتور:", fontSize = 13.sp)
                    Text(
                        text = "${String.format("%,d", invoice.totalAmount)} ریال",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0B4DA2)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onPrintReceipt,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Print, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("چاپ فاکتور", fontSize = 12.sp)
            }

            Button(
                onClick = onPreview,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("پیش‌نمایش", fontSize = 12.sp)
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "ثبت نهایی و اتمام ماموریت", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 7. Add Carpet Form View
@Composable
fun DriverAddCarpetForm(
    type: String,
    color: String,
    width: String,
    length: String,
    service: String,
    onTypeChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onLengthChange: (String) -> Unit,
    onServiceChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ورود جزئیات فرش جدید",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B4DA2)
        )

        OutlinedTextField(
            value = type,
            onValueChange = onTypeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("نوع فرش (مثال: دستباف تبریز، ماشینی ۷۰۰ شانه)") },
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = color,
            onValueChange = onColorChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("زمینه رنگی فرش") },
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = width,
                onValueChange = onWidthChange,
                modifier = Modifier.weight(1f),
                label = { Text("عرض (متر)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = length,
                onValueChange = onLengthChange,
                modifier = Modifier.weight(1f),
                label = { Text("طول (متر)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        OutlinedTextField(
            value = service,
            onValueChange = onServiceChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("نوع خدمت مورد نظر (مثال: شستشو اعلا، رفو)") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "ذخیره و الصاق به فاکتور", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 8. Driver barcode scanner View simulator
@Composable
fun DriverQrScanView(onScanComplete: () -> Unit) {
    var isScanning by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        isScanning = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .border(3.dp, Color(0xFF2BB673), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = Color(0xFF2BB673),
                        modifier = Modifier.size(96.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "در حال شبیه‌سازی اسکن فیزیکی برچسب قالیشویی...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2BB673).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2BB673), modifier = Modifier.size(64.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "بارکد با موفقیت اسکن شد",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2BB673)
            )

            Text(
                text = "شناسه اسکن شده: QR-PAKAN-981240",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onScanComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بازگشت به فاکتور")
            }
        }
    }
}

// 9. Printer view
@Composable
fun DriverPrinterView(invoice: Invoice, onBack: () -> Unit) {
    var isSearching by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        isSearching = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSearching) {
            CircularProgressIndicator(color = Color(0xFF0B4DA2))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "در حال اتصال به چاپگر کمری حرارتی (Bluetooth)...", fontSize = 13.sp, color = Color.Gray)
        } else {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2BB673), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "چاپگر حرارتی Sewoo متصل شد", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2BB673))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    AppStateStore.addNotification("فاکتور چاپ شد", "فاکتور شماره ${invoice.id} با موفقیت توسط پرینتر حرارتی چاپ شد.", "راننده")
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("شروع چاپ فاکتور (تک نسخه مشتری)")
            }
        }
    }
}

// 10. Thermal Receipt Preview
@Composable
fun DriverInvoicePreviewView(invoice: Invoice, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "پیش‌نمایش فاکتور چاپی مشتری", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "قالیشویی مکانیزه پاکان تهران", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "سامانه سراسری قالیشویی هوشمند کشور", fontSize = 10.sp, color = Color.Gray)
                Text(text = "تلفن پشتیبانی: ۱۵۴۲ (بدون پیش‌شماره)", fontSize = 10.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "شماره فاکتور: ${invoice.id}", fontSize = 10.sp)
                    Text(text = "تاریخ: ${invoice.date}", fontSize = 10.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "مشتری: ${invoice.customerName}", fontSize = 10.sp)
                    Text(text = "کد اشتراک: ${invoice.trackingNumber}", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                invoice.carpets.forEach { carpet ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "• ${carpet.type}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${carpet.width} × ${carpet.length} (${carpet.area} متر)", fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "جمع کل فاکتور:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${String.format("%,d", invoice.totalAmount)} ریال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Icon(Icons.Default.QrCode, null, modifier = Modifier.size(72.dp))
                Text(text = "بارکد اعتبار الکترونیکی قالیشویی", fontSize = 9.sp, color = Color.Gray)
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("بازگشت")
        }
    }
}

// 11. Complete Success Screen
@Composable
fun DriverSuccessView(isDelivery: Boolean = false, onFinish: () -> Unit) {
    // Simple celebratory scale-in animation, standing in for a confetti effect
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(targetValue = if (visible) 1f else 0.6f, label = "successScale")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Color(0xFF2BB673).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2BB673), modifier = Modifier.size(72.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isDelivery) "فرش‌ها با موفقیت تحویل داده شد!" else "فاکتور با موفقیت ثبت شد!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2BB673)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isDelivery)
                "پرداخت مشتری ثبت شد، وضعیت فرش‌ها «تحویل شده» گردید و ماموریت در سامانه مرکزی ERP بسته شد."
            else
                "اطلاعات فاکتور در سرور مرکزی ERP کارخانه با موفقیت به‌روزرسانی شد. سفارش به بخش انبار و دریافت انتقال یافت.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("بازگشت به ماموریت‌ها")
        }
    }
}

// 13. Delivery Confirmation View (Prompt 5 - Screen 6: Arrival / Delivery Confirmation)
@Composable
fun DriverDeliveryConfirmationView(invoice: Invoice, onConfirm: () -> Unit) {
    val checkedState = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(invoice.id) {
        invoice.carpets.forEach { checkedState[it.id] = true }
    }
    val allChecked = invoice.carpets.isNotEmpty() && invoice.carpets.all { checkedState[it.id] == true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B4DA2).copy(alpha = 0.06f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "تحویل به: ${invoice.customerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = invoice.address, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "شماره پیگیری: ${invoice.trackingNumber}", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Text(text = "بررسی و تایید هر فرش هنگام تحویل:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        invoice.carpets.forEach { carpet ->
            val checked = checkedState[carpet.id] ?: true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (checked) Color(0xFF2BB673) else Color(0xFFE3E8EF))
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = carpet.type, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "کد QR: ${carpet.trackingNumber} • وضعیت: سالم و بدون آسیب", fontSize = 11.sp, color = Color.Gray)
                    }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checkedState[carpet.id] = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2BB673))
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.PhotoCamera, null, tint = Color(0xFF0B4DA2))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "ثبت عکس قبل/بعد تحویل (اختیاری)", fontSize = 12.sp, color = Color.DarkGray)
            }
        }

        Button(
            onClick = onConfirm,
            enabled = allChecked,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2BB673)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "تایید صحت تحویل و ادامه به پرداخت", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 14. Payment Collection View (Prompt 5 - Screen 9: Payment)
@Composable
fun DriverCollectPaymentView(invoice: Invoice, onCollect: (Long, String) -> Unit) {
    val remaining = invoice.totalAmount - invoice.paidAmount
    var selectedMethod by remember { mutableStateOf("نقدی") }
    var isPartial by remember { mutableStateOf(false) }
    var partialAmountText by remember { mutableStateOf(remaining.toString()) }

    val methods = listOf(
        Triple("نقدی", Icons.Default.Payments, Color(0xFF2BB673)),
        Triple("کارتخوان سیار", Icons.Default.CreditCard, Color(0xFF0B4DA2)),
        Triple("درگاه آنلاین", Icons.Default.AccountBalanceWallet, Color(0xFFFF9800))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFE3E8EF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "مبلغ کل فاکتور:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format("%,d", invoice.totalAmount)} ریال", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "مبلغ باقیمانده جهت دریافت:", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${String.format("%,d", remaining)} ریال", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0B4DA2))
                }
            }
        }

        Text(text = "روش پرداخت را انتخاب کنید:", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            methods.forEach { (label, icon, color) ->
                val selected = selectedMethod == label
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedMethod = label },
                    colors = CardDefaults.cardColors(containerColor = if (selected) color.copy(alpha = 0.1f) else Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (selected) color else Color(0xFFE3E8EF))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, null, tint = color)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
                    }
                }
            }
        }

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
                    Text(text = "پرداخت جزئی (علی‌الحساب)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isPartial,
                        onCheckedChange = { isPartial = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
                    )
                }
                if (isPartial) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = partialAmountText,
                        onValueChange = { partialAmountText = it.filter { c -> c.isDigit() } },
                        label = { Text("مبلغ دریافتی (ریال)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0B4DA2))
                    )
                }
            }
        }

        Button(
            onClick = {
                val amount = if (isPartial) (partialAmountText.toLongOrNull() ?: 0L) else remaining
                onCollect(amount, selectedMethod)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "ثبت دریافت و اتمام تحویل", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
