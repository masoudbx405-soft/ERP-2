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

private val AccPrimary = Color(0xFF0B4DA2)
private val AccSecondary = Color(0xFF2BB673)
private val AccDanger = Color(0xFFE53935)
private val AccWarning = Color(0xFFFF9800)
private val AccBorder = Color(0xFFE3E8EF)

private fun rial(v: Long): String = "${String.format("%,d", v)} ریال"

// Entry point: Full Accounting ERP module (Prompt 9)
@Composable
fun ManagerAccountingScreen(
    invoices: List<Invoice>,
    expenses: List<Expense>,
    bankAccounts: List<BankAccount>,
    cashEntries: List<CashRegisterEntry>,
    salaryRecords: List<SalaryRecord>,
    commissionRecords: List<DriverCommissionRecord>,
    purchaseOrders: List<PurchaseOrder>
) {
    var subTab by remember { mutableStateOf("dashboard") }
    val tabs = listOf(
        "dashboard" to "داشبورد مالی",
        "cash" to "صندوق",
        "bank" to "بانک‌ها",
        "salary" to "حقوق و دستمزد",
        "commission" to "کمیسیون رانندگان",
        "purchase" to "سفارشات خرید",
        "tax" to "مالیات و گزارش"
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
                    selectedContentColor = AccPrimary,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = subTab,
                transitionSpec = { fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200)) },
                label = "AccountingSubTab"
            ) { tab ->
                when (tab) {
                    "dashboard" -> AccountingDashboardView(invoices, expenses, bankAccounts)
                    "cash" -> CashRegisterView(cashEntries)
                    "bank" -> BankAccountsView(bankAccounts)
                    "salary" -> SalaryManagementView(salaryRecords)
                    "commission" -> DriverCommissionView(commissionRecords)
                    "purchase" -> PurchaseOrdersView(purchaseOrders)
                    "tax" -> TaxAndReportsView(invoices, expenses)
                }
            }
        }
    }
}

// Screen 1 + 14 + 15: Financial Dashboard / Executive Financial Overview
@Composable
private fun AccountingDashboardView(invoices: List<Invoice>, expenses: List<Expense>, banks: List<BankAccount>) {
    val revenue = invoices.sumOf { it.paidAmount }
    val expenseTotal = expenses.sumOf { it.amount }
    val profit = revenue - expenseTotal
    val bankTotal = banks.sumOf { it.balance }
    val debts = invoices.sumOf { it.totalAmount - it.paidAmount }

    LazyColumnAcc {
        item {
            Text("داشبورد مالی اجرایی (CEO Overview)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AccKpi("درآمد وصول‌شده امروز", rial(revenue), AccSecondary, Modifier.weight(1f))
                AccKpi("هزینه‌های ثبت‌شده", rial(expenseTotal), AccDanger, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AccKpi("سود خالص ماه جاری", rial(profit), if (profit >= 0) AccSecondary else AccDanger, Modifier.weight(1f))
                AccKpi("موجودی کل بانک‌ها", rial(bankTotal), AccPrimary, Modifier.weight(1f))
            }
        }
        item {
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مطالبات معوق مشتریان (بدهی‌های باز):", fontSize = 12.sp, color = Color.Gray)
                    Text(rial(debts), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccWarning)
                }
            }
        }
        item { Text("برترین مشتریان از نظر گردش مالی", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(invoices.sortedByDescending { it.totalAmount }.take(4)) { inv ->
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(inv.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("فاکتور ${inv.id}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(rial(inv.totalAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccPrimary)
                }
            }
        }
    }
}

// Screen 2: Cash Register
@Composable
private fun CashRegisterView(entries: List<CashRegisterEntry>) {
    val balance = entries.firstOrNull()?.balanceAfter ?: 0L
    LazyColumnAcc {
        item {
            AccCard(container = AccSecondary.copy(alpha = 0.06f), border = AccSecondary.copy(alpha = 0.2f)) {
                Text("موجودی فعلی صندوق", fontSize = 12.sp, color = Color.DarkGray)
                Text(rial(balance), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AccSecondary)
            }
        }
        item { Text("تاریخچه گردش صندوق (Cash History)", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        items(entries) { e ->
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (e.type == CashEntryType.IN) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            null,
                            tint = if (e.type == CashEntryType.IN) AccSecondary else AccDanger,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(e.description, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${e.date} • ${e.time}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Text(
                        (if (e.type == CashEntryType.IN) "+ " else "- ") + rial(e.amount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (e.type == CashEntryType.IN) AccSecondary else AccDanger
                    )
                }
            }
        }
    }
}

// Screen 3: Bank Accounts
@Composable
private fun BankAccountsView(banks: List<BankAccount>) {
    LazyColumnAcc {
        item { Text("حساب‌های بانکی کارخانه", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        items(banks) { b ->
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(b.bankName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(b.cardNumber, fontSize = 11.sp, color = Color.Gray)
                        Text("نوع حساب: ${b.type}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(rial(b.balance), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccPrimary)
                }
            }
        }
        item {
            AccCard(container = AccPrimary.copy(alpha = 0.05f), border = AccPrimary.copy(alpha = 0.2f)) {
                Text("مجموع موجودی همه حساب‌ها", fontSize = 12.sp, color = Color.DarkGray)
                Text(rial(banks.sumOf { it.balance }), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AccPrimary)
            }
        }
    }
}

// Screen 6: Salary Management
@Composable
private fun SalaryManagementView(records: List<SalaryRecord>) {
    LazyColumnAcc {
        item { Text("حقوق و دستمزد پرسنل — ${records.firstOrNull()?.month ?: ""}", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        items(records) { s ->
            AccCard {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(s.employeeName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(s.role, fontSize = 11.sp, color = Color.Gray)
                        }
                        StatusPill(s.status, s.status == "پرداخت شده")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("حقوق پایه: ${rial(s.baseSalary)}", fontSize = 10.sp, color = Color.Gray)
                        Text("پاداش: ${rial(s.bonus)}", fontSize = 10.sp, color = AccSecondary)
                        Text("جریمه/بیمه: ${rial(s.penalty + s.insurance)}", fontSize = 10.sp, color = AccDanger)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("خالص پرداختی: ${rial(s.netPay)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccPrimary)
                    }
                }
            }
        }
    }
}

// Screen 7: Driver Commission
@Composable
private fun DriverCommissionView(records: List<DriverCommissionRecord>) {
    LazyColumnAcc {
        item { Text("کمیسیون رانندگان — ${records.firstOrNull()?.month ?: ""}", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        items(records) { c ->
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(c.driverName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${c.completedOrders} ماموریت تکمیل شده • درصد کمیسیون: ${c.commissionPercent}٪", fontSize = 10.sp, color = Color.Gray)
                        Text("جمع وصولی: ${rial(c.totalCollected)}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(rial(c.commissionAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccSecondary)
                }
            }
        }
    }
}

// Screen 8: Purchase Orders
@Composable
private fun PurchaseOrdersView(orders: List<PurchaseOrder>) {
    LazyColumnAcc {
        item { Text("سفارشات خرید تامین‌کنندگان", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        items(orders) { po ->
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(po.product, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("تامین‌کننده: ${po.supplier}", fontSize = 11.sp, color = Color.Gray)
                        Text("تعداد: ${po.quantity} • تاریخ: ${po.date}", fontSize = 10.sp, color = Color.Gray)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(rial(po.totalPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccPrimary)
                        Spacer(Modifier.height(4.dp))
                        StatusPill(po.status, po.status == "تحویل شده")
                    }
                }
            }
        }
    }
}

// Screen 12 + 13: Tax Dashboard + Financial Reports
@Composable
private fun TaxAndReportsView(invoices: List<Invoice>, expenses: List<Expense>) {
    val revenue = invoices.sumOf { it.totalAmount }
    val vat = (revenue * 9) / 100 // 9% نمونه مالیات بر ارزش افزوده
    val expenseTotal = expenses.sumOf { it.amount }

    LazyColumnAcc {
        item { Text("داشبورد مالیاتی (VAT)", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
        item {
            AccCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("مالیات بر ارزش افزوده (۹٪ فروش):", fontSize = 12.sp, color = Color.Gray)
                    Text(rial(vat), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccWarning)
                }
            }
        }
        item { Text("صورت سود و زیان (Income Statement)", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            AccCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportRow("جمع درآمد فروش", rial(revenue), AccSecondary)
                    ReportRow("جمع هزینه‌های عملیاتی", "- ${rial(expenseTotal)}", AccDanger)
                    Divider()
                    ReportRow("سود خالص قبل از مالیات", rial(revenue - expenseTotal), AccPrimary)
                }
            }
        }
        item { Text("خروجی گزارش", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ExportButton("خروجی PDF", Icons.Default.PictureAsPdf, Modifier.weight(1f))
                ExportButton("خروجی Excel", Icons.Default.TableChart, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun ExportButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = {},
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccPrimary)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
private fun StatusPill(text: String, positive: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background((if (positive) AccSecondary else AccWarning).copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (positive) AccSecondary else AccWarning)
    }
}

@Composable
private fun AccKpi(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AccBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun AccCard(
    container: Color = Color.White,
    border: Color = AccBorder,
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
private fun LazyColumnAcc(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
