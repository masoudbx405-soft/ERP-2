@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.*
import com.example.network.NetworkSync
import kotlinx.coroutines.launch

private val AdmPrimary = Color(0xFF0B4DA2)
private val AdmSecondary = Color(0xFF2BB673)
private val AdmDanger = Color(0xFFE53935)
private val AdmWarning = Color(0xFFFF9800)
private val AdmBorder = Color(0xFFE3E8EF)

// Entry point: System Administration module (Prompt 10)
@Composable
fun ManagerSystemAdminScreen(
    trustedDevices: List<TrustedDevice>,
    branches: List<Branch>,
    printers: List<PrinterDevice>,
    systemLogs: List<SystemLog>,
    smsLogs: List<SmsLog>,
    notifications: List<NotificationItem>,
    onRevokeDevice: (String) -> Unit,
    onReconnectPrinter: (String) -> Unit
) {
    var subTab by remember { mutableStateOf("cloud") }
    val tabs = listOf(
        "cloud" to "زیرساخت ابری",
        "security" to "امنیت و دستگاه‌ها",
        "roles" to "نقش‌ها و دسترسی",
        "printers" to "پرینتر و QR",
        "branches" to "شعب و مکان‌ها",
        "logs" to "لاگ‌های سیستم",
        "update" to "بروزرسانی و پشتیبانی"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == subTab }.coerceAtLeast(0),
            containerColor = Color.White,
            edgePadding = 12.dp,
            divider = {}
        ) {
            tabs.forEach { (id, label) ->
                Tab(
                    selected = subTab == id,
                    onClick = { subTab = id },
                    text = { Text(label, fontSize = 12.sp, fontWeight = if (subTab == id) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = AdmPrimary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = subTab,
                transitionSpec = { fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200)) },
                label = "AdminSubTab"
            ) { tab ->
                when (tab) {
                    "cloud" -> CloudInfraView(smsLogs, notifications)
                    "security" -> SecurityCenterView(trustedDevices, onRevokeDevice)
                    "roles" -> RolesPermissionsView()
                    "printers" -> PrinterQrView(printers, onReconnectPrinter)
                    "branches" -> BranchesView(branches)
                    "logs" -> SystemLogsView(systemLogs)
                    "update" -> UpdateSupportView()
                }
            }
        }
    }
}

// Screen 1 + 9 + 12: Cloud Infrastructure + SMS Gateway + Backup Center
@Composable
private fun CloudInfraView(smsLogs: List<SmsLog>, notifications: List<NotificationItem>) {
    val scope = rememberCoroutineScope()
    val connectionStatus by NetworkSync.status.collectAsState()
    val connectionError by NetworkSync.lastError.collectAsState()
    var testMessage by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    AdmLazyColumn {
        item { Text("زیرساخت ابری قالیشویی (Cloud Server)", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            val (statusLabel, statusColor) = when (connectionStatus) {
                "connected" -> "متصل و زنده" to AdmSecondary
                "connecting" -> "در حال اتصال..." to AdmWarning
                "error" -> "خطا در اتصال" to AdmDanger
                else -> "قطع (وارد نشده‌اید)" to Color.Gray
            }
            AdmCard(container = statusColor.copy(alpha = 0.06f), border = statusColor.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("وضعیت اتصال زنده به سرور اختصاصی: $statusLabel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (connectionError != null) {
                            Text(connectionError ?: "", fontSize = 10.sp, color = AdmDanger)
                        }
                    }
                }
            }
        }
        item {
            AdmCard {
                Text("تست ارتباط زنده بین گوشی‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "با زدن این دکمه یک نوتیفیکیشن واقعی به سرور فرستاده می‌شود؛ اگر گوشی راننده هم با همین سرور وارد شده باشد، بلافاصله این پیام را می‌بیند.",
                    fontSize = 10.sp, color = Color.Gray, lineHeight = 15.sp
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        isSending = true
                        testMessage = null
                        scope.launch {
                            val result = NetworkSync.broadcastNotification(
                                title = "پیام تستی از مدیر",
                                body = "این یک پیام آزمایشی برای بررسی ارتباط زنده است.",
                                category = "سیستم",
                                targetRole = "driver"
                            )
                            isSending = false
                            testMessage = result.fold(
                                onSuccess = { "✅ پیام با موفقیت به سرور ارسال شد." },
                                onFailure = { "❌ ${it.message}" }
                            )
                        }
                    },
                    enabled = !isSending,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AdmPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("ارسال پیام تستی به راننده‌ها", fontSize = 12.sp)
                    }
                }
                if (testMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(testMessage ?: "", fontSize = 11.sp, color = Color.DarkGray)
                }
            }
        }
        item {
            AdmCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تونل دیتابیس ابری (Cloudflare Tunnel):", fontSize = 11.sp, color = Color.Gray)
                        Text("متصل (SSL) • ۲۴ میلی‌ثانیه", fontSize = 11.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("سامانه پیامک ملی (KavehNegar):", fontSize = 11.sp, color = Color.Gray)
                        Text("متصل • اعتبار: ۴۲۰,۰۰۰ ریال", fontSize = 11.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("سرویس نقشه گوگل (Maps API Key):", fontSize = 11.sp, color = Color.Gray)
                        Text("فعال", fontSize = 11.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Text("آخرین پیامک‌های سیستمی و OTP", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
        items(smsLogs) { log ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("گیرنده: ${log.recipient} • ${log.time}", fontSize = 11.sp, color = Color.Gray)
                    Text(log.status, fontSize = 10.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(log.text, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
            }
        }
        item {
            AdmCard(container = AdmSecondary.copy(alpha = 0.06f), border = AdmSecondary.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDone, null, tint = AdmSecondary)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("آخرین پشتیبان‌گیری ابری: امروز ساعت ۰۴:۰۰", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("حجم بکاپ: ۱۲۸ مگابایت • وضعیت: موفق", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
        item {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AdmPrimary),
                shape = RoundedCornerShape(12.dp)
            ) { Text("شروع پشتیبان‌گیری دستی", fontSize = 12.sp) }
        }
    }
}

// Screen 5: Roles & Permissions
@Composable
private fun RolesPermissionsView() {
    val roles = listOf(
        Triple("مدیر کارخانه", "دسترسی کامل به تمام بخش‌ها", AdmPrimary),
        Triple("راننده", "ماموریت، فاکتور، چت", AdmSecondary),
        Triple("انباردار", "انبار، قفسه‌بندی، QC", AdmWarning),
        Triple("حسابدار", "مالی، هزینه، گزارش", AdmDanger),
        Triple("ناظر کیفیت", "کنترل کیفیت، تایید/رد فرش", Color(0xFF9C27B0)),
        Triple("پشتیبانی مشتری", "چت، شکایات، CRM", Color(0xFF00838F))
    )
    AdmLazyColumn {
        item { Text("ماتریس نقش‌ها و سطوح دسترسی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(roles) { (role, perms, color) ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(8.dp))
                        Text(role, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(perms, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// Screen 6: Security Center
@Composable
private fun SecurityCenterView(devices: List<TrustedDevice>, onRevoke: (String) -> Unit) {
    AdmLazyColumn {
        item {
            AdmCard(container = AdmSecondary.copy(alpha = 0.06f), border = AdmSecondary.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = AdmSecondary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("امتیاز امنیتی سیستم: ۸۷ از ۱۰۰", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("احراز هویت دو مرحله‌ای فعال است", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
        item {
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("احراز هویت دو مرحله‌ای (2FA)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = AdmSecondary))
                }
            }
        }
        item {
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("ورود با اثر انگشت / بیومتریک", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = AdmSecondary))
                }
            }
        }
        item { Text("دستگاه‌های مورد اعتماد (نشست‌های فعال)", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(devices) { d ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PhoneAndroid, null, tint = if (d.isCurrent) AdmSecondary else AdmPrimary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(d.deviceName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${d.owner} • ${d.lastLogin} • ${d.location}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    if (d.isCurrent) {
                        Text("این دستگاه", fontSize = 10.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                    } else {
                        TextButton(onClick = { onRevoke(d.id) }, contentPadding = PaddingValues(0.dp)) {
                            Text("لغو دسترسی", fontSize = 10.sp, color = AdmDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Screen 10 + 11: Thermal Printer + QR Label Management
@Composable
private fun PrinterQrView(printers: List<PrinterDevice>, onReconnect: (String) -> Unit) {
    AdmLazyColumn {
        item { Text("پرینترهای متصل به سیستم", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(printers) { p ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, null, tint = if (p.status == "متصل") AdmSecondary else AdmDanger, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(p.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${p.connectionType} • کاغذ: ${p.paperStatus}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    if (p.status == "متصل") {
                        Text("متصل", fontSize = 10.sp, color = AdmSecondary, fontWeight = FontWeight.Bold)
                    } else {
                        TextButton(onClick = { onReconnect(p.id) }, contentPadding = PaddingValues(0.dp)) {
                            Text("اتصال مجدد", fontSize = 10.sp, color = AdmPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        item { Text("مدیریت برچسب‌های QR", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            AdmCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("برچسب‌های چاپ‌شده امروز:", fontSize = 12.sp, color = Color.Gray)
                        Text("۴۸ برچسب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdmPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صف چاپ در انتظار:", fontSize = 12.sp, color = Color.Gray)
                        Text("۶ برچسب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdmWarning)
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AdmPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("چاپ دسته‌ای برچسب‌های در صف", fontSize = 12.sp) }
                }
            }
        }
    }
}

// Screen 3: Branches
@Composable
private fun BranchesView(branches: List<Branch>) {
    AdmLazyColumn {
        item { Text("شعب و مکان‌های کارخانه", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(branches) { b ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(b.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(b.address, fontSize = 10.sp, color = Color.Gray, lineHeight = 14.sp)
                        Text("مدیر شعبه: ${b.manager}", fontSize = 10.sp, color = AdmPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${b.capacityPercent}٪", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AdmSecondary)
                        Text("ظرفیت", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Screen 13 + 14: System Logs + Audit Dashboard
@Composable
private fun SystemLogsView(logs: List<SystemLog>) {
    AdmLazyColumn {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AdmKpi("عادی", "${logs.count { it.severity == "عادی" }}", AdmSecondary, Modifier.weight(1f))
                AdmKpi("مهم", "${logs.count { it.severity == "مهم" }}", AdmWarning, Modifier.weight(1f))
                AdmKpi("بحرانی", "${logs.count { it.severity == "بحرانی" }}", AdmDanger, Modifier.weight(1f))
            }
        }
        items(logs) { l ->
            AdmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(l.action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${l.actor} → ${l.target}", fontSize = 10.sp, color = Color.Gray)
                        Text(l.time, fontSize = 10.sp, color = Color.Gray)
                    }
                    val color = when (l.severity) {
                        "بحرانی" -> AdmDanger
                        "مهم" -> AdmWarning
                        else -> AdmSecondary
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text(l.severity, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color) }
                }
            }
        }
    }
}

// Screen 17 + 18 + 19: Update Center + Help Center + About System
@Composable
private fun UpdateSupportView() {
    AdmLazyColumn {
        item {
            AdmCard(container = AdmPrimary.copy(alpha = 0.05f), border = AdmPrimary.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SystemUpdate, null, tint = AdmPrimary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("نسخه فعلی سیستم: ۲.۴.۱", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("سیستم به‌روز است — آخرین بررسی: امروز", fontSize = 10.sp, color = AdmSecondary)
                    }
                }
            }
        }
        item { Text("مرکز راهنما و پشتیبانی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            AdmCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HelpRow(Icons.Default.MenuBook, "راهنمای کاربری کامل سیستم")
                    HelpRow(Icons.Default.PlayCircle, "ویدیوهای آموزشی")
                    HelpRow(Icons.Default.SupportAgent, "پشتیبانی آنلاین و تیکت")
                    HelpRow(Icons.Default.Call, "تماس مستقیم با تیم فنی")
                }
            }
        }
        item { Text("درباره سیستم", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            AdmCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AboutRow("نسخه اپلیکیشن", "۲.۴.۱")
                    AboutRow("نسخه پایگاه داده", "PostgreSQL 15")
                    AboutRow("توسعه‌دهنده", "تیم فنی رزشاپ")
                    AboutRow("مجوز بهره‌برداری", "لایسنس تجاری فعال")
                }
            }
        }
    }
}

@Composable
private fun HelpRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null, tint = AdmPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AdmKpi(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AdmBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun AdmCard(
    container: Color = Color.White,
    border: Color = AdmBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, border)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun AdmLazyColumn(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
