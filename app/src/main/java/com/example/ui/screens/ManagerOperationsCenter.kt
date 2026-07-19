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

private val OpsPrimary = Color(0xFF0B4DA2)
private val OpsSecondary = Color(0xFF2BB673)
private val OpsDanger = Color(0xFFE53935)
private val OpsWarning = Color(0xFFFF9800)
private val OpsBorder = Color(0xFFE3E8EF)

// Entry point: Live Operations Center (Prompt 8)
@Composable
fun ManagerOperationsCenterScreen(
    machines: List<WashingMachine>,
    warehouseZones: List<WarehouseZone>,
    inventoryItems: List<InventoryItem>,
    deliveryHeat: List<DeliveryZoneHeat>,
    carpets: List<Carpet>,
    notifications: List<NotificationItem>
) {
    var subTab by remember { mutableStateOf("machines") }
    val tabs = listOf(
        "machines" to "مانیتورینگ ماشین‌آلات",
        "warehouse" to "انبار زنده",
        "queue" to "صف تولید",
        "heatmap" to "نقشه حرارتی تحویل",
        "inventory" to "موجودی مصرفی",
        "alerts" to "دیوار اعلان‌ها"
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
                    selectedContentColor = OpsPrimary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = subTab,
                transitionSpec = { fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200)) },
                label = "OpsSubTab"
            ) { tab ->
                when (tab) {
                    "machines" -> MachineMonitoringView(machines)
                    "warehouse" -> WarehouseLiveView(warehouseZones)
                    "queue" -> ProductionQueueView(carpets)
                    "heatmap" -> DeliveryHeatMapView(deliveryHeat)
                    "inventory" -> InventoryDashboardView(inventoryItems)
                    "alerts" -> NotificationWallView(notifications)
                }
            }
        }
    }
}

// Screen 4: Machine Monitoring
@Composable
private fun MachineMonitoringView(machines: List<WashingMachine>) {
    OpsLazyColumn {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OpsKpi("در حال کار", "${machines.count { it.status == "در حال کار" }}", OpsSecondary, Modifier.weight(1f))
                OpsKpi("آزاد", "${machines.count { it.status == "آزاد" }}", OpsPrimary, Modifier.weight(1f))
                OpsKpi("تعمیرات", "${machines.count { it.status == "تعمیرات" }}", OpsDanger, Modifier.weight(1f))
            }
        }
        items(machines) { m ->
            OpsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("اپراتور: ${m.operator}", fontSize = 10.sp, color = Color.Gray)
                        if (m.status == "در حال کار") {
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = m.loadPercent / 100f,
                                modifier = Modifier.fillMaxWidth(0.8f).height(6.dp).clip(RoundedCornerShape(4.dp)),
                                color = OpsPrimary,
                                trackColor = OpsPrimary.copy(alpha = 0.1f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("زمان باقی‌مانده: ${m.remainingMinutes} دقیقه", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    val color = when (m.status) {
                        "در حال کار" -> OpsSecondary
                        "تعمیرات" -> OpsDanger
                        else -> Color.Gray
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text(m.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color) }
                }
            }
        }
    }
}

// Screen 3: Warehouse Live View
@Composable
private fun WarehouseLiveView(zones: List<WarehouseZone>) {
    OpsLazyColumn {
        item { Text("نقشه اشغال ظرفیت مناطق کارگاه", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(zones) { z ->
            OpsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(z.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${z.occupied}/${z.capacity}", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(8.dp))
                val color = when {
                    z.occupiedPercent >= 85 -> OpsDanger
                    z.occupiedPercent >= 60 -> OpsWarning
                    else -> OpsSecondary
                }
                LinearProgressIndicator(
                    progress = z.occupiedPercent / 100f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.1f)
                )
                Spacer(Modifier.height(4.dp))
                Text("اشغال شده: ${z.occupiedPercent}٪", fontSize = 10.sp, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Screen 8: Queue Monitor (derived from real carpet statuses)
@Composable
private fun ProductionQueueView(carpets: List<Carpet>) {
    val stages = listOf(
        CarpetStatus.RECEIVED, CarpetStatus.WAREHOUSE, CarpetStatus.WASHING,
        CarpetStatus.DRYING, CarpetStatus.QC, CarpetStatus.READY
    )
    OpsLazyColumn {
        item { Text("وضعیت صف تولید بر اساس مرحله", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(stages) { stage ->
            val count = carpets.count { it.status == stage }
            OpsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stage.label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier.clip(CircleShape).background(OpsPrimary.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("$count فرش", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OpsPrimary) }
                }
            }
        }
        val delayed = carpets.count { it.status == CarpetStatus.WASHING || it.status == CarpetStatus.QC }
        item {
            OpsCard(container = OpsWarning.copy(alpha = 0.06f), border = OpsWarning.copy(alpha = 0.2f)) {
                Text("سفارشات در معرض تاخیر احتمالی", fontSize = 12.sp, color = Color.DarkGray)
                Text("$delayed سفارش", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = OpsWarning)
            }
        }
    }
}

// Screen 7: Heat Map (Orders by delivery zone)
@Composable
private fun DeliveryHeatMapView(zones: List<DeliveryZoneHeat>) {
    OpsLazyColumn {
        item { Text("تراکم سفارشات تحویل بر اساس منطقه", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(zones.sortedByDescending { it.intensity }) { z ->
            OpsCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(OpsDanger.copy(alpha = z.intensity / 100f))
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(z.zoneName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${z.orderCount} سفارش فعال", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Text("${z.intensity}٪", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OpsDanger)
                }
            }
        }
    }
}

// Screen 15: Inventory Dashboard
@Composable
private fun InventoryDashboardView(items: List<InventoryItem>) {
    OpsLazyColumn {
        item { Text("موجودی مواد مصرفی و بسته‌بندی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(items) { i ->
            OpsCard(
                container = if (i.isLow) OpsDanger.copy(alpha = 0.04f) else Color.White,
                border = if (i.isLow) OpsDanger.copy(alpha = 0.25f) else OpsBorder
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(i.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("موجودی: ${i.currentStock} ${i.unit} • حداقل: ${i.minThreshold} ${i.unit}", fontSize = 10.sp, color = Color.Gray)
                    }
                    if (i.isLow) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(OpsDanger.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("هشدار کمبود", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OpsDanger) }
                    } else {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(OpsSecondary.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("کافی", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OpsSecondary) }
                    }
                }
            }
        }
    }
}

// Screen 12: Notification Wall (system-wide alert aggregation)
@Composable
private fun NotificationWallView(notifications: List<NotificationItem>) {
    OpsLazyColumn {
        item { Text("دیوار اعلانات و رخدادهای سیستمی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(notifications) { n ->
            OpsCard {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val color = when (n.category) {
                        "مالی" -> OpsSecondary
                        "انبار" -> OpsWarning
                        "راننده" -> OpsPrimary
                        else -> OpsDanger
                    }
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(n.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(n.body, fontSize = 10.sp, color = Color.Gray, lineHeight = 14.sp)
                    }
                    Text(n.time, fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun OpsKpi(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, OpsBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun OpsCard(
    container: Color = Color.White,
    border: Color = OpsBorder,
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
private fun OpsLazyColumn(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
