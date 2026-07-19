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
import com.example.data.*

private val CrmPrimary = Color(0xFF0B4DA2)
private val CrmSecondary = Color(0xFF2BB673)
private val CrmDanger = Color(0xFFE53935)
private val CrmWarning = Color(0xFFFF9800)
private val CrmBorder = Color(0xFFE3E8EF)
private val CrmVip = Color(0xFFD4AF37)

// Entry point: CRM + AI + Smart Services module (Prompt 6)
@Composable
fun ManagerCrmMarketingScreen(
    complaints: List<Complaint>,
    campaigns: List<MarketingCampaign>,
    feedbacks: List<CustomerFeedback>,
    announcements: List<Announcement>,
    aiRecommendations: List<AiRecommendation>,
    employees: List<Employee>,
    onLaunchCampaign: (String) -> Unit,
    onResolveComplaint: (String, String) -> Unit
) {
    var subTab by remember { mutableStateOf("ai") }
    val tabs = listOf(
        "ai" to "مرکز هوش مصنوعی",
        "marketing" to "بازاریابی",
        "complaints" to "شکایات",
        "feedback" to "بازخورد مشتریان",
        "performance" to "عملکرد کارکنان",
        "announcements" to "اعلانات داخلی"
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
                    selectedContentColor = CrmPrimary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = subTab,
                transitionSpec = { fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200)) },
                label = "CrmSubTab"
            ) { tab ->
                when (tab) {
                    "ai" -> AiCommandCenterView(aiRecommendations)
                    "marketing" -> MarketingCenterView(campaigns, onLaunchCampaign)
                    "complaints" -> ComplaintManagementView(complaints, onResolveComplaint)
                    "feedback" -> CustomerFeedbackView(feedbacks)
                    "performance" -> EmployeePerformanceView(employees)
                    "announcements" -> AnnouncementCenterView(announcements)
                }
            }
        }
    }
}

// Screen 1: AI Command Center
@Composable
private fun AiCommandCenterView(recommendations: List<AiRecommendation>) {
    CrmLazyColumn {
        item {
            CrmCard(container = CrmPrimary.copy(alpha = 0.05f), border = CrmPrimary.copy(alpha = 0.2f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = CrmPrimary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("موتور پیشنهادات هوشمند کارخانه", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("پیشنهاداتی بر اساس تحلیل رفتار مشتریان و روند سفارشات", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
        item { Text("پیشنهادات فعال", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(recommendations) { r ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(r.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(r.description, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("دسته‌بندی: ${r.category}", fontSize = 10.sp, color = CrmPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    ImpactPill(r.impact)
                }
            }
        }
    }
}

@Composable
private fun ImpactPill(impact: String) {
    val color = if (impact == "بالا") CrmDanger else CrmWarning
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("تاثیر $impact", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// Screen 9: Marketing Center
@Composable
private fun MarketingCenterView(campaigns: List<MarketingCampaign>, onLaunch: (String) -> Unit) {
    CrmLazyColumn {
        item { Text("کمپین‌های بازاریابی پیامکی و درون‌برنامه‌ای", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(campaigns) { c ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(c.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("هدف: ${c.targetSegment} • کانال: ${c.channel}", fontSize = 10.sp, color = Color.Gray)
                        Text("گیرندگان: ${c.recipientsCount} • نرخ تبدیل: ${c.conversionRate}٪", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        CrmStatusPill(c.status, c.status == "تکمیل شده")
                        if (c.status == "زمان‌بندی شده") {
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { onLaunch(c.id) }, contentPadding = PaddingValues(0.dp)) {
                                Text("ارسال اکنون", fontSize = 10.sp, color = CrmPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Screen 10 + 12: Complaint Management
@Composable
private fun ComplaintManagementView(complaints: List<Complaint>, onResolve: (String, String) -> Unit) {
    CrmLazyColumn {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CrmKpi("باز", "${complaints.count { it.status == "باز" }}", CrmDanger, Modifier.weight(1f))
                CrmKpi("در حال بررسی", "${complaints.count { it.status == "در حال بررسی" }}", CrmWarning, Modifier.weight(1f))
                CrmKpi("حل شده", "${complaints.count { it.status == "حل شده" }}", CrmSecondary, Modifier.weight(1f))
            }
        }
        items(complaints) { c ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(c.subject, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(c.customerName, fontSize = 11.sp, color = CrmPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(c.description, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("مسئول پیگیری: ${c.assignedTo} • ${c.date}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        val prColor = if (c.priority == "بالا") CrmDanger else if (c.priority == "متوسط") CrmWarning else Color.Gray
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(prColor.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text("اولویت ${c.priority}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = prColor) }
                        Spacer(Modifier.height(6.dp))
                        CrmStatusPill(c.status, c.status == "حل شده")
                        if (c.status != "حل شده") {
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { onResolve(c.id, "حل شده") }, contentPadding = PaddingValues(0.dp)) {
                                Text("علامت‌گذاری حل‌شده", fontSize = 10.sp, color = CrmSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Screen 10 + 11 (feedback/star rating)
@Composable
private fun CustomerFeedbackView(feedbacks: List<CustomerFeedback>) {
    val avg = if (feedbacks.isNotEmpty()) feedbacks.map { it.rating }.average() else 0.0
    CrmLazyColumn {
        item {
            CrmCard(container = CrmSecondary.copy(alpha = 0.06f), border = CrmSecondary.copy(alpha = 0.2f)) {
                Text("میانگین رضایت مشتریان", fontSize = 12.sp, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(String.format("%.1f", avg), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = CrmSecondary)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                }
            }
        }
        item { Text("نظرات اخیر مشتریان", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(feedbacks) { f ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(f.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            repeat(5) { i ->
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = if (i < f.rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(f.comment, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                    }
                    CrmStatusPill(if (f.resolved) "پیگیری شده" else "نیاز به پیگیری", f.resolved)
                }
            }
        }
    }
}

// Screen 15 + 16: Employee Profile + Performance Dashboard
@Composable
private fun EmployeePerformanceView(employees: List<Employee>) {
    CrmLazyColumn {
        item { Text("عملکرد پرسنل کارخانه", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(employees.sortedByDescending { it.performanceScore }) { e ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(CircleShape).background(CrmPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Badge, null, tint = CrmPrimary, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(e.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${e.role} • ${e.status}", fontSize = 10.sp, color = Color.Gray)
                            Text("وظایف: ${e.completedTasks}/${e.tasksToday}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${e.performanceScore}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CrmSecondary)
                        Text("امتیاز عملکرد", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

// Screen 13: Announcement Center
@Composable
private fun AnnouncementCenterView(announcements: List<Announcement>) {
    CrmLazyColumn {
        item { Text("اعلانات و اطلاعیه‌های داخلی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(announcements) { a ->
            CrmCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(a.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(a.body, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("مخاطب: ${a.audience} • ${a.date}", fontSize = 10.sp, color = CrmPrimary)
                    }
                    Icon(Icons.Default.Campaign, null, tint = CrmWarning, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CrmStatusPill(text: String, positive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background((if (positive) CrmSecondary else CrmWarning).copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (positive) CrmSecondary else CrmWarning)
    }
}

@Composable
private fun CrmKpi(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CrmBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun CrmCard(
    container: Color = Color.White,
    border: Color = CrmBorder,
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
private fun CrmLazyColumn(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
