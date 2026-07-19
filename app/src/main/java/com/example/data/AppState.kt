package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

// Data models representing real ERP entities
data class Carpet(
    val id: String,
    val trackingNumber: String,
    val type: String, // ماشینی، دستباف، ابریشم
    val color: String,
    val width: Double,
    val length: Double,
    val area: Double,
    val serviceType: String, // شستشو، رفوگری، ریشه‌بافی، لکه‌گیری
    val status: CarpetStatus,
    val shelf: String? = null,
    val photoUrl: String? = null,
    val qualityNotes: String? = null,
    val repairNotes: String? = null,
    val prevWashes: Int = 1
)

enum class CarpetStatus(val label: String, val step: Int) {
    RECEIVED("ثبت شده", 0),
    COLLECTED("جمع‌آوری شده", 1),
    WAREHOUSE("ورود به انبار", 2),
    WASHING("در حال شستشو", 3),
    DRYING("در حال خشک‌شدن", 4),
    QC("کنترل کیفیت", 5),
    READY("آماده تحویل", 6),
    DELIVERED("تحویل شده", 7)
}

data class Invoice(
    val id: String,
    val trackingNumber: String,
    val customerName: String,
    val customerPhone: String,
    val address: String,
    val date: String,
    val carpets: List<Carpet>,
    val status: InvoiceStatus,
    val paymentMethod: String? = null,
    val totalAmount: Long,
    val paidAmount: Long,
    val driverName: String = "علی کریمی",
    val latitude: Double = 35.6997,
    val longitude: Double = 51.3380
)

enum class InvoiceStatus(val label: String) {
    UNPAID("پرداخت نشده"),
    PARTIAL("پرداخت شده (علی‌الحساب)"),
    PAID("تسویه کامل")
}

data class Mission(
    val id: String,
    val customerName: String,
    val phone: String,
    val address: String,
    val type: String, // جمع‌آوری، تحویل
    var status: MissionStatus,
    val invoiceId: String,
    val distanceKm: Double,
    val estMinutes: Int,
    val carpetsCount: Int = 3
)

enum class MissionStatus(val label: String) {
    PENDING("در انتظار"),
    ACTIVE("در حال انجام"),
    COMPLETED("تکمیل شده")
}

data class Driver(
    val name: String,
    val phone: String,
    val status: String, // آنلاین، آفلاین، در ماموریت
    val rating: Double,
    val performanceScore: Int,
    val speed: Int,
    val collectedCash: Long,
    val collectedOnline: Long,
    val distanceKm: Double,
    val completedMissions: Int
)

data class Message(
    val id: String,
    val sender: String,
    val senderRole: String, // راننده، دفتر، انبار، پشتیبانی
    val text: String,
    val time: String,
    val isIncoming: Boolean,
    val isRead: Boolean = true,
    val type: MessageType = MessageType.TEXT
)

enum class MessageType { TEXT, IMAGE, VOICE, FILE }

data class SmsLog(
    val id: String,
    val recipient: String,
    val template: String,
    val text: String,
    val time: String,
    val status: String // ارسال شده، در صف
)

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val category: String, // مالی، سیستم، انبار، راننده
    val isRead: Boolean = false
)

data class Employee(
    val id: String,
    val name: String,
    val role: String, // انباردار، ناظر کیفیت، حسابدار، پشتیبانی
    val status: String, // حاضر، در مرخصی، غایب
    val tasksToday: Int,
    val completedTasks: Int,
    val performanceScore: Int,
    val phone: String
)

data class Expense(
    val id: String,
    val category: String, // سوخت، تعمیرات، حقوق و دستمزد، آب و برق، مواد شوینده، سایر
    val amount: Long,
    val date: String,
    val description: String,
    val status: String // تایید شده، در انتظار تایید
)

data class CustomerProfile(
    val name: String,
    val phone: String,
    val addresses: List<String>,
    val totalOrders: Int,
    val totalSpent: Long,
    val outstandingDebt: Long,
    val segment: String, // VIP، عادی، بدهکار، غیرفعال
    val lastOrderDate: String,
    val invoices: List<Invoice>
)

// ===== Accounting ERP models (Prompt 9) =====
data class BankAccount(
    val id: String,
    val bankName: String, // ملت، صادرات، پاسارگاد
    val accountNumber: String,
    val cardNumber: String,
    val balance: Long,
    val type: String // جاری، پس‌انداز
)

enum class CashEntryType { IN, OUT }

data class CashRegisterEntry(
    val id: String,
    val type: CashEntryType,
    val amount: Long,
    val date: String,
    val time: String,
    val description: String,
    val balanceAfter: Long
)

data class SalaryRecord(
    val id: String,
    val employeeName: String,
    val role: String,
    val baseSalary: Long,
    val bonus: Long,
    val penalty: Long,
    val insurance: Long,
    val status: String, // پرداخت شده، در انتظار پرداخت
    val month: String
) {
    val netPay: Long get() = baseSalary + bonus - penalty - insurance
}

data class DriverCommissionRecord(
    val driverName: String,
    val completedOrders: Int,
    val collectedCash: Long,
    val collectedOnline: Long,
    val commissionPercent: Int,
    val month: String
) {
    val totalCollected: Long get() = collectedCash + collectedOnline
    val commissionAmount: Long get() = (totalCollected * commissionPercent) / 100
}

data class PurchaseOrder(
    val id: String,
    val supplier: String,
    val product: String,
    val quantity: Int,
    val unitPrice: Long,
    val status: String, // در انتظار تایید، تایید شده، تحویل شده
    val date: String
) {
    val totalPrice: Long get() = quantity.toLong() * unitPrice
}

// ===== CRM + AI + Smart Services models (Prompt 6) =====
data class Complaint(
    val id: String,
    val customerName: String,
    val subject: String,
    val description: String,
    val priority: String, // بالا، متوسط، پایین
    val status: String, // باز، در حال بررسی، حل شده
    val assignedTo: String,
    val date: String
)

data class MarketingCampaign(
    val id: String,
    val title: String,
    val targetSegment: String, // مشتریان VIP، مشتریان غیرفعال، همه مشتریان
    val channel: String, // پیامک، اعلان درون‌برنامه‌ای
    val recipientsCount: Int,
    val sentCount: Int,
    val conversionRate: Int, // درصد
    val status: String, // زمان‌بندی شده، در حال ارسال، تکمیل شده
    val scheduledDate: String
)

data class CustomerFeedback(
    val id: String,
    val customerName: String,
    val rating: Int, // 1..5
    val comment: String,
    val date: String,
    val resolved: Boolean
)

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val audience: String, // همه پرسنل، رانندگان، انبار، مدیریت
    val date: String
)

data class AiRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // بازاریابی، تحویل، انبار، پیش‌بینی
    val impact: String // بالا، متوسط
)

// ===== Live Operations Center models (Prompt 8) =====
data class WashingMachine(
    val id: String,
    val name: String, // ماشین شستشوی ۱
    val status: String, // در حال کار، آزاد، تعمیرات
    val operator: String,
    val remainingMinutes: Int,
    val loadPercent: Int
)

data class WarehouseZone(
    val id: String,
    val name: String, // انبار اصلی، بخش خشک‌کن، بخش QC
    val capacity: Int,
    val occupied: Int
) {
    val occupiedPercent: Int get() = if (capacity == 0) 0 else ((occupied * 100) / capacity)
}

data class InventoryItem(
    val id: String,
    val name: String, // مواد شوینده، برچسب حرارتی، نایلون بسته‌بندی
    val unit: String,
    val currentStock: Int,
    val minThreshold: Int
) {
    val isLow: Boolean get() = currentStock <= minThreshold
}

data class DeliveryZoneHeat(
    val zoneName: String, // پاسداران، شهرک غرب، سعادت‌آباد
    val orderCount: Int,
    val intensity: Int // 0..100
)

// ===== System Administration models (Prompt 10) =====
data class TrustedDevice(
    val id: String,
    val deviceName: String, // Samsung Galaxy A54، iPhone 13
    val owner: String,
    val lastLogin: String,
    val location: String,
    val isCurrent: Boolean
)

data class Branch(
    val id: String,
    val name: String, // شعبه مرکزی، شعبه شرق تهران
    val address: String,
    val manager: String,
    val capacityPercent: Int
)

data class PrinterDevice(
    val id: String,
    val name: String, // پرینتر حرارتی صندوق
    val connectionType: String, // بلوتوث، وای‌فای
    val status: String, // متصل، آفلاین
    val paperStatus: String // کافی، رو به اتمام
)

data class SystemLog(
    val id: String,
    val actor: String,
    val action: String,
    val target: String,
    val time: String,
    val severity: String // عادی، مهم، بحرانی
)

object AppStateStore {
    // Current Active Role Selection
    val currentRole = MutableStateFlow<String?>(null) // "manager", "driver", "warehouse", "customer", null (splash/login)
    val loginStep = MutableStateFlow<String>("splash") // "splash", "welcome", "role", "login", "otp", "dashboard"
    val selectedRoleForLogin = MutableStateFlow<String>("customer") // manager, driver, warehouse, customer
    val currentUserPhone = MutableStateFlow("")

    // Real-time collections
    private val _carpets = MutableStateFlow<List<Carpet>>(emptyList())
    val carpets: StateFlow<List<Carpet>> = _carpets.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()

    private val _drivers = MutableStateFlow<List<Driver>>(emptyList())
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _smsLogs = MutableStateFlow<List<SmsLog>>(emptyList())
    val smsLogs: StateFlow<List<SmsLog>> = _smsLogs.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _bankAccounts = MutableStateFlow<List<BankAccount>>(emptyList())
    val bankAccounts: StateFlow<List<BankAccount>> = _bankAccounts.asStateFlow()

    private val _cashEntries = MutableStateFlow<List<CashRegisterEntry>>(emptyList())
    val cashEntries: StateFlow<List<CashRegisterEntry>> = _cashEntries.asStateFlow()

    private val _salaryRecords = MutableStateFlow<List<SalaryRecord>>(emptyList())
    val salaryRecords: StateFlow<List<SalaryRecord>> = _salaryRecords.asStateFlow()

    private val _commissionRecords = MutableStateFlow<List<DriverCommissionRecord>>(emptyList())
    val commissionRecords: StateFlow<List<DriverCommissionRecord>> = _commissionRecords.asStateFlow()

    private val _purchaseOrders = MutableStateFlow<List<PurchaseOrder>>(emptyList())
    val purchaseOrders: StateFlow<List<PurchaseOrder>> = _purchaseOrders.asStateFlow()

    private val _complaints = MutableStateFlow<List<Complaint>>(emptyList())
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private val _campaigns = MutableStateFlow<List<MarketingCampaign>>(emptyList())
    val campaigns: StateFlow<List<MarketingCampaign>> = _campaigns.asStateFlow()

    private val _feedbacks = MutableStateFlow<List<CustomerFeedback>>(emptyList())
    val feedbacks: StateFlow<List<CustomerFeedback>> = _feedbacks.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _aiRecommendations = MutableStateFlow<List<AiRecommendation>>(emptyList())
    val aiRecommendations: StateFlow<List<AiRecommendation>> = _aiRecommendations.asStateFlow()

    private val _machines = MutableStateFlow<List<WashingMachine>>(emptyList())
    val machines: StateFlow<List<WashingMachine>> = _machines.asStateFlow()

    private val _warehouseZones = MutableStateFlow<List<WarehouseZone>>(emptyList())
    val warehouseZones: StateFlow<List<WarehouseZone>> = _warehouseZones.asStateFlow()

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _deliveryHeat = MutableStateFlow<List<DeliveryZoneHeat>>(emptyList())
    val deliveryHeat: StateFlow<List<DeliveryZoneHeat>> = _deliveryHeat.asStateFlow()

    private val _trustedDevices = MutableStateFlow<List<TrustedDevice>>(emptyList())
    val trustedDevices: StateFlow<List<TrustedDevice>> = _trustedDevices.asStateFlow()

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _printers = MutableStateFlow<List<PrinterDevice>>(emptyList())
    val printers: StateFlow<List<PrinterDevice>> = _printers.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<SystemLog>>(emptyList())
    val systemLogs: StateFlow<List<SystemLog>> = _systemLogs.asStateFlow()

    init {
        resetState()
    }

    fun resetState() {
        // Mocking high-quality realistic Persian data
        val initialCarpets = listOf(
            Carpet("C-101", "TRK-981240", "دستباف تبریز (طرح هریس)", "لاکی سرخ", 3.0, 4.0, 12.0, "شستشو و لکه‌گیری", CarpetStatus.RECEIVED, null, null, null, null, 2),
            Carpet("C-102", "TRK-981241", "ماشینی نگین مشهد (۱۲۰۰ شانه)", "سرمه‌ای", 2.5, 3.5, 8.75, "شستشوی ویژه اعلا", CarpetStatus.WASHING, null, null, null, null, 4),
            Carpet("C-103", "TRK-981242", "دستباف ابریشم قم", "کرم گل‌بهی", 2.0, 3.0, 6.0, "شستشوی دستی و رفوگری", CarpetStatus.WAREHOUSE, "قفسه A-12", null, null, "نیاز به دقت بالا - بافت ابریشم حساس", 1),
            Carpet("C-104", "TRK-981243", "گلیم دستباف قشقایی", "رنگارنگ طبیعی", 1.5, 2.5, 3.75, "ریشه‌بافی و شستشو", CarpetStatus.QC, "قفسه B-05", null, "ریشه کشی دستی انجام شد", null, 3),
            Carpet("C-105", "TRK-981244", "ماشینی ستاره کویر یزد", "گردویی", 3.0, 3.0, 9.0, "شستشوی معمولی", CarpetStatus.READY, "قفسه C-02", null, null, null, 5),
            Carpet("C-106", "TRK-981245", "دستباف نایین", "کرم سرمه‌ای", 3.0, 4.0, 12.0, "شستشو و قلم‌زنی رنگ", CarpetStatus.DELIVERED, null, null, null, null, 1)
        )
        _carpets.value = initialCarpets

        _invoices.value = listOf(
            Invoice("INV-40210", "TRK-981240", "جناب آقای مرتضی رضایی", "09121234567", "تهران، پاسداران، بوستان چهارم، پلاک ۱۲، واحد ۳", "۱۴۰۵/۰۴/۲۶", listOf(initialCarpets[0]), InvoiceStatus.UNPAID, null, 12000000, 0),
            Invoice("INV-40211", "TRK-981241", "خانم دکتر مریم سماواتی", "09129876543", "تهران، شهرک غرب، خیابان مهستان، کوچه هشتم، پلاک ۵", "۱۴۰۵/۰۴/۲۶", listOf(initialCarpets[1]), InvoiceStatus.PARTIAL, "کارتخوان سیار", 18500000, 5000000),
            Invoice("INV-40212", "TRK-981242", "مهندس فرامرز امینی", "09127654321", "تهران، نیاوران، خیابان یاسر، کوچه لادن، پلاک ۲۴", "۱۴۰۵/۰۴/۲۵", listOf(initialCarpets[2], initialCarpets[3]), InvoiceStatus.UNPAID, null, 35000000, 0),
            Invoice("INV-40213", "TRK-981244", "خانم سارا کریمی", "09355432109", "تهران، سعادت‌آباد، سرو غربی، کوچه لاله، پلاک ۴۲", "۱۴۰۵/۰۴/۲۴", listOf(initialCarpets[4]), InvoiceStatus.PAID, "درگاه آنلاین", 9500000, 9500000),
            Invoice("INV-40214", "TRK-981245", "جناب حاج محمد کاظمی", "09123456789", "تهران، شریعتی، بالاتر از ظفر، کوچه همایون، پلاک ۱۰", "۱۴۰۵/۰۴/۲۳", listOf(initialCarpets[5]), InvoiceStatus.PAID, "نقدی", 14000000, 14000000)
        )

        _missions.value = listOf(
            Mission("M-301", "مرتضی رضایی", "09121234567", "تهران، پاسداران، بوستان چهارم، پلاک ۱۲", "جمع‌آوری", MissionStatus.ACTIVE, "INV-40210", 3.2, 18),
            Mission("M-302", "مریم سماواتی", "09129876543", "تهران، شهرک غرب، خیابان مهستان، پلاک ۵", "جمع‌آوری", MissionStatus.PENDING, "INV-40211", 8.5, 32),
            Mission("M-303", "سارا کریمی", "09355432109", "تهران، سعادت‌آباد، سرو غربی، پلاک ۴۲", "تحویل", MissionStatus.PENDING, "INV-40213", 6.1, 25),
            Mission("M-304", "حاج محمد کاظمی", "09123456789", "تهران، شریعتی، بالاتر از ظفر، پلاک ۱۰", "تحویل", MissionStatus.COMPLETED, "INV-40214", 4.8, 22)
        )

        _drivers.value = listOf(
            Driver("علی کریمی", "09121111111", "آنلاین - در حال ماموریت", 4.8, 96, 42, 14000000, 9500000, 68.4, 12),
            Driver("حمید صادقی", "09122222222", "آنلاین - آزاد", 4.9, 98, 0, 8500000, 17000000, 85.0, 15),
            Driver("رضا محمدی", "09123333333", "آفلاین", 4.5, 88, 0, 0, 12000000, 45.2, 8)
        )

        _messages.value = listOf(
            Message("MSG-1", "دفتر مرکزی", "دفتر", "سلام علی جان، ماموریت جدید پاسداران آماده است. لطفا هماهنگ کن.", "۱۰:۰۵", true),
            Message("MSG-2", "علی کریمی", "راننده", "سلام. بله فاکتور رضایی رو دیدم. الان دارم میرم سمت لوکیشن.", "۱۰:۰۷", false),
            Message("MSG-3", "بخش انبار", "انبار", "فرش ابریشم فاکتور ۴۰۲۱۲ شستشوش تموم شد. بره رفوگری یا آماده‌سازی؟", "۰۹:۳۰", true),
            Message("MSG-4", "پشتیبانی مشتریان", "پشتیبانی", "مشتری کریمی تماس گرفته و می‌پرسه تحویل امروزش ساعت چند انجام میشه؟", "۰۸:۴۵", true)
        )

        _smsLogs.value = listOf(
            SmsLog("SMS-101", "09121234567", "تایید جمع‌آوری", "قالیشویی هوشمند: راننده ما (علی کریمی) در حال حرکت به سمت شما برای جمع‌آوری فرش است.", "۱۰:۰۶", "ارسال شده"),
            SmsLog("SMS-102", "09355432109", "کد احراز OTP", "کد احراز هویت قالیشویی شما: ۵۸۲۴\nمعتبر برای ۵ دقیقه", "۱۰:۲۲", "ارسال شده")
        )

        _notifications.value = listOf(
            NotificationItem("NTF-1", "سفارش جدید ثبت شد", "مشتری مریم سماواتی درخواست جمع‌آوری فرش ثبت نمود.", "۱۰:۰۱", "سیستم", false),
            NotificationItem("NTF-2", "ورود وجه نقد", "راننده علی کریمی مبلغ ۱۴,۰۰۰,۰۰۰ ریال وجه نقد بابت فاکتور حاج محمد کاظمی ثبت نمود.", "۰۹:۴۵", "مالی", false),
            NotificationItem("NTF-3", "هشدار ظرفیت انبار", "ظرفیت بخش قفسه‌های خشک‌کن به ۸۴٪ رسیده است.", "۰۹:۱۵", "انبار", true),
            NotificationItem("NTF-4", "خرابی پرینتر حرارتی", "پرینتر بلوتوث راننده صادقی قطع ارتباط شد.", "۰۸:۳۰", "سیستم", true)
        )

        _employees.value = listOf(
            Employee("EMP-01", "رضا احمدی", "انباردار", "حاضر", 12, 8, 91, "09131111111"),
            Employee("EMP-02", "فاطمه نوری", "ناظر کیفیت", "حاضر", 9, 9, 97, "09132222222"),
            Employee("EMP-03", "امیر حسینی", "حسابدار", "حاضر", 6, 4, 88, "09133333333"),
            Employee("EMP-04", "زهرا موسوی", "پشتیبانی مشتریان", "در مرخصی", 0, 0, 85, "09134444444"),
            Employee("EMP-05", "بهنام رستمی", "انباردار", "حاضر", 10, 7, 79, "09135555555")
        )

        _expenses.value = listOf(
            Expense("EXP-901", "سوخت", 3200000, "۱۴۰۵/۰۴/۲۶", "سوخت‌گیری خودروی راننده کریمی", "تایید شده"),
            Expense("EXP-902", "مواد شوینده", 8500000, "۱۴۰۵/۰۴/۲۵", "خرید ماهانه مواد شوینده و نرم‌کننده", "تایید شده"),
            Expense("EXP-903", "تعمیرات", 4100000, "۱۴۰۵/۰۴/۲۴", "تعمیر ماشین شستشوی شماره ۲", "در انتظار تایید"),
            Expense("EXP-904", "حقوق و دستمزد", 210000000, "۱۴۰۵/۰۴/۲۰", "حقوق پرسنل تیر ماه", "تایید شده"),
            Expense("EXP-905", "آب و برق", 6300000, "۱۴۰۵/۰۴/۱۸", "قبض آب و برق کارگاه", "تایید شده")
        )

        _bankAccounts.value = listOf(
            BankAccount("BNK-01", "بانک ملت", "۰۱۰۲۳۴۵۶۷۸۹۰", "۶۱۰۴-....-....-۳۳۲۱", 842000000, "جاری"),
            BankAccount("BNK-02", "بانک صادرات", "۰۲۰۹۸۷۶۵۴۳۲۱", "۶۰۳۷-....-....-۷۷۱۲", 215000000, "جاری"),
            BankAccount("BNK-03", "بانک پاسارگاد", "۰۳۰۵۵۵۱۲۳۴۵۶", "۵۰۲۲-....-....-۹۹۰۵", 96000000, "پس‌انداز")
        )

        _cashEntries.value = listOf(
            CashRegisterEntry("CSH-501", CashEntryType.IN, 14000000, "۱۴۰۵/۰۴/۲۶", "۰۹:۴۵", "دریافت نقدی فاکتور INV-40214", 62400000),
            CashRegisterEntry("CSH-502", CashEntryType.OUT, 3200000, "۱۴۰۵/۰۴/۲۶", "۰۸:۱۰", "سوخت خودروی راننده کریمی", 48400000),
            CashRegisterEntry("CSH-503", CashEntryType.IN, 9500000, "۱۴۰۵/۰۴/۲۴", "۱۱:۲۰", "دریافت نقدی فاکتور INV-40213", 51600000),
            CashRegisterEntry("CSH-504", CashEntryType.OUT, 4100000, "۱۴۰۵/۰۴/۲۴", "۱۴:۰۰", "تعمیر ماشین شستشوی شماره ۲", 42100000)
        )

        _salaryRecords.value = listOf(
            SalaryRecord("SAL-01", "علی کریمی", "راننده", 45000000, 5000000, 0, 3200000, "پرداخت شده", "تیر ۱۴۰۵"),
            SalaryRecord("SAL-02", "حمید صادقی", "راننده", 45000000, 8000000, 0, 3200000, "پرداخت شده", "تیر ۱۴۰۵"),
            SalaryRecord("SAL-03", "رضا احمدی", "انباردار", 38000000, 2000000, 500000, 2800000, "پرداخت شده", "تیر ۱۴۰۵"),
            SalaryRecord("SAL-04", "فاطمه نوری", "ناظر کیفیت", 40000000, 3000000, 0, 2900000, "در انتظار پرداخت", "تیر ۱۴۰۵"),
            SalaryRecord("SAL-05", "امیر حسینی", "حسابدار", 42000000, 1500000, 0, 3000000, "در انتظار پرداخت", "تیر ۱۴۰۵")
        )

        _commissionRecords.value = listOf(
            DriverCommissionRecord("علی کریمی", 12, 14000000, 9500000, 12, "تیر ۱۴۰۵"),
            DriverCommissionRecord("حمید صادقی", 15, 8500000, 17000000, 12, "تیر ۱۴۰۵"),
            DriverCommissionRecord("رضا محمدی", 8, 0, 12000000, 10, "تیر ۱۴۰۵")
        )

        _purchaseOrders.value = listOf(
            PurchaseOrder("PO-701", "شرکت مواد شوینده آریا", "شامپو فرش صنعتی (۲۰ لیتری)", 15, 1800000, "تایید شده", "۱۴۰۵/۰۴/۲۰"),
            PurchaseOrder("PO-702", "بازرگانی نوین بسته‌بندی", "نایلون بسته‌بندی فرش", 500, 45000, "در انتظار تایید", "۱۴۰۵/۰۴/۲۴"),
            PurchaseOrder("PO-703", "چاپ و برچسب پارسیان", "برچسب حرارتی QR", 2000, 3200, "تحویل شده", "۱۴۰۵/۰۴/۱۵")
        )

        _complaints.value = listOf(
            Complaint("CMP-201", "مهندس فرامرز امینی", "تاخیر در تحویل", "سفارش دو روز دیرتر از موعد تحویل داده شد.", "بالا", "در حال بررسی", "زهرا موسوی", "۱۴۰۵/۰۴/۲۵"),
            Complaint("CMP-202", "خانم دکتر مریم سماواتی", "آسیب جزئی به ریشه فرش", "متوجه پارگی کوچک در ریشه فرش بعد از شستشو شدیم.", "متوسط", "باز", "فاطمه نوری", "۱۴۰۵/۰۴/۲۶"),
            Complaint("CMP-203", "خانم سارا کریمی", "بوی نامطبوع باقی‌مانده", "بعد از شستشو بوی مواد شوینده از بین نرفته بود.", "پایین", "حل شده", "رضا احمدی", "۱۴۰۵/۰۴/۲۲")
        )

        _campaigns.value = listOf(
            MarketingCampaign("CMPN-01", "تخفیف تابستانه شستشوی فرش", "همه مشتریان", "پیامک", 1240, 1240, 18, "تکمیل شده", "۱۴۰۵/۰۴/۱۰"),
            MarketingCampaign("CMPN-02", "کمپین بازگشت مشتریان غیرفعال", "مشتریان غیرفعال", "پیامک", 340, 210, 7, "در حال ارسال", "۱۴۰۵/۰۴/۲۶"),
            MarketingCampaign("CMPN-03", "پیشنهاد ویژه اعضای VIP", "مشتریان VIP", "اعلان درون‌برنامه‌ای", 85, 0, 0, "زمان‌بندی شده", "۱۴۰۵/۰۵/۰۱")
        )

        _feedbacks.value = listOf(
            CustomerFeedback("FB-301", "حاج محمد کاظمی", 5, "کیفیت شستشو عالی بود، بسیار راضی هستم.", "۱۴۰۵/۰۴/۲۳", true),
            CustomerFeedback("FB-302", "خانم سارا کریمی", 4, "کار خوب بود ولی زمان تحویل کمی طول کشید.", "۱۴۰۵/۰۴/۲۴", true),
            CustomerFeedback("FB-303", "مهندس فرامرز امینی", 2, "از تاخیر در تحویل ناراضی بودم.", "۱۴۰۵/۰۴/۲۵", false)
        )

        _announcements.value = listOf(
            Announcement("ANN-01", "تعطیلی نیمه‌روز جمعه", "کارگاه روز جمعه تا ساعت ۱۳ فعال خواهد بود.", "همه پرسنل", "۱۴۰۵/۰۴/۲۶"),
            Announcement("ANN-02", "آموزش ایمنی ماشین شستشو", "کلاس آموزشی ایمنی برای اپراتورهای ماشین‌آلات برگزار می‌شود.", "انبار", "۱۴۰۵/۰۴/۲۴"),
            Announcement("ANN-03", "به‌روزرسانی مسیرهای تحویل", "مسیرهای پیشنهادی تحویل هفتگی به‌روزرسانی شد.", "رانندگان", "۱۴۰۵/۰۴/۲۲")
        )

        _aiRecommendations.value = listOf(
            AiRecommendation("AI-01", "زمان‌بندی مجدد کمپین پیامکی", "ارسال کمپین بازگشت مشتریان در ساعات عصر نرخ تبدیل بالاتری خواهد داشت.", "بازاریابی", "متوسط"),
            AiRecommendation("AI-02", "گروه‌بندی تحویل منطقه پاسداران", "۳ سفارش تحویل در منطقه پاسداران قابل گروه‌بندی برای صرفه‌جویی در مسیر هستند.", "تحویل", "بالا"),
            AiRecommendation("AI-03", "پیش‌بینی افزایش تقاضا آخر هفته", "بر اساس روند سفارشات، پیش‌بینی می‌شود تقاضای پنجشنبه ۲۰٪ افزایش یابد.", "پیش‌بینی", "بالا")
        )

        _machines.value = listOf(
            WashingMachine("MCH-01", "ماشین شستشوی ۱ (صنعتی سنگین)", "در حال کار", "بهنام رستمی", 24, 68),
            WashingMachine("MCH-02", "ماشین شستشوی ۲ (دستباف ظریف)", "در حال کار", "رضا احمدی", 40, 45),
            WashingMachine("MCH-03", "ماشین شستشوی ۳ (استاندارد)", "آزاد", "-", 0, 0),
            WashingMachine("MCH-04", "ماشین شستشوی ۴ (استاندارد)", "تعمیرات", "تیم فنی", 0, 0)
        )

        _warehouseZones.value = listOf(
            WarehouseZone("ZN-01", "انبار اصلی (قفسه‌بندی)", 200, 148),
            WarehouseZone("ZN-02", "بخش خشک‌کن", 80, 67),
            WarehouseZone("ZN-03", "بخش کنترل کیفیت", 50, 21),
            WarehouseZone("ZN-04", "بخش بسته‌بندی و تحویل", 60, 18)
        )

        _inventoryItems.value = listOf(
            InventoryItem("INV-01", "شامپو فرش صنعتی", "لیتر", 120, 50),
            InventoryItem("INV-02", "برچسب حرارتی QR", "عدد", 340, 500),
            InventoryItem("INV-03", "نایلون بسته‌بندی", "رول", 28, 20),
            InventoryItem("INV-04", "رول کاغذ پرینتر حرارتی", "عدد", 6, 10)
        )

        _deliveryHeat.value = listOf(
            DeliveryZoneHeat("پاسداران", 14, 85),
            DeliveryZoneHeat("شهرک غرب", 9, 55),
            DeliveryZoneHeat("سعادت‌آباد", 11, 68),
            DeliveryZoneHeat("نیاوران", 6, 38),
            DeliveryZoneHeat("شریعتی", 5, 30)
        )

        _trustedDevices.value = listOf(
            TrustedDevice("DEV-01", "Samsung Galaxy S23", "مدیر کارخانه", "امروز ۰۹:۱۰", "تهران، ایران", true),
            TrustedDevice("DEV-02", "iPhone 14 Pro", "امیر حسینی (حسابدار)", "دیروز ۱۸:۴۰", "تهران، ایران", false),
            TrustedDevice("DEV-03", "Xiaomi Redmi Note 12", "علی کریمی (راننده)", "امروز ۰۷:۲۰", "تهران، ایران", false)
        )

        _branches.value = listOf(
            Branch("BR-01", "شعبه مرکزی (کارخانه اصلی)", "تهران، جاده مخصوص کرج، کیلومتر ۱۰", "حاج محمد کاظمی", 74),
            Branch("BR-02", "شعبه شرق تهران (پذیرش)", "تهران، تهرانپارس، خیابان ۱۹۶ شرقی", "زهرا موسوی", 52),
            Branch("BR-03", "انبار پشتیبان غرب", "تهران، شهرک صنعتی چهاردانگه", "رضا احمدی", 38)
        )

        _printers.value = listOf(
            PrinterDevice("PRT-01", "پرینتر حرارتی صندوق مرکزی", "وای‌فای", "متصل", "کافی"),
            PrinterDevice("PRT-02", "پرینتر برچسب QR انبار", "بلوتوث", "متصل", "رو به اتمام"),
            PrinterDevice("PRT-03", "پرینتر حرارتی راننده کریمی", "بلوتوث", "آفلاین", "کافی")
        )

        _systemLogs.value = listOf(
            SystemLog("LOG-901", "زهرا موسوی", "ویرایش فاکتور", "INV-40211", "امروز ۱۰:۱۵", "عادی"),
            SystemLog("LOG-902", "سیستم", "ورود ناموفق (رمز اشتباه)", "کاربر ناشناس", "امروز ۰۸:۵۰", "مهم"),
            SystemLog("LOG-903", "امیر حسینی", "حذف رکورد هزینه", "EXP-902", "دیروز ۱۶:۳۰", "بحرانی"),
            SystemLog("LOG-904", "مدیر کارخانه", "تغییر سطح دسترسی کاربر", "رضا احمدی", "دیروز ۱۱:۰۰", "عادی")
        )
    }

    // Interactive state modifications
    fun updateCarpetStatus(carpetId: String, newStatus: CarpetStatus, shelf: String? = null) {
        val current = _carpets.value.toMutableList()
        val index = current.indexOfFirst { it.id == carpetId }
        if (index != -1) {
            val updated = current[index].copy(status = newStatus, shelf = shelf ?: current[index].shelf)
            current[index] = updated
            _carpets.value = current

            // Add notification
            addNotification("تغییر وضعیت فرش", "فرش کد ${carpetId} به وضعیت «${newStatus.label}» تغییر یافت.", "انبار")

            // Propagate to Invoice
            updateInvoiceStatusForCarpetChange()
        }
    }

    private fun updateInvoiceStatusForCarpetChange() {
        // Recalculate invoice statuses based on their carpets if needed, simple logic for simulation
    }

    fun addNotification(title: String, body: String, category: String) {
        val newNtf = NotificationItem(
            id = "NTF-${Random.nextInt(1000, 9999)}",
            title = title,
            body = body,
            time = "۱۰:۳۰",
            category = category,
            isRead = false
        )
        _notifications.value = listOf(newNtf) + _notifications.value
    }

    fun completeMission(missionId: String) {
        val current = _missions.value.toMutableList()
        val index = current.indexOfFirst { it.id == missionId }
        if (index != -1) {
            current[index] = current[index].copy(status = MissionStatus.COMPLETED)
            _missions.value = current

            // If it was a pickup, we change carpet status to WAREHOUSE
            val invId = current[index].invoiceId
            val carpetsList = _carpets.value.toMutableList()
            // Find carpets under this invoice and set to WAREHOUSE
            val inv = _invoices.value.find { it.id == invId }
            inv?.carpets?.forEach { carpet ->
                val cIdx = carpetsList.indexOfFirst { it.id == carpet.id }
                if (cIdx != -1) {
                    carpetsList[cIdx] = carpetsList[cIdx].copy(status = CarpetStatus.WAREHOUSE)
                }
            }
            _carpets.value = carpetsList

            addNotification("ماموریت تکمیل شد", "ماموریت ${current[index].customerName} توسط راننده با موفقیت انجام شد.", "راننده")
        }
    }

    fun startMission(missionId: String) {
        val current = _missions.value.toMutableList()
        val index = current.indexOfFirst { it.id == missionId }
        if (index != -1) {
            current[index] = current[index].copy(status = MissionStatus.ACTIVE)
            _missions.value = current
            addNotification("شروع ماموریت", "ماموریت جمع‌آوری/تحویل برای ${current[index].customerName} شروع شد.", "راننده")
        }
    }

    fun addCarpetToInvoice(invoiceId: String, type: String, color: String, w: Double, l: Double, service: String) {
        val area = w * l
        val carpetId = "C-${Random.nextInt(107, 200)}"
        val trk = "TRK-${Random.nextInt(981246, 981300)}"
        val newCarpet = Carpet(
            id = carpetId,
            trackingNumber = trk,
            type = type,
            color = color,
            width = w,
            length = l,
            area = area,
            serviceType = service,
            status = CarpetStatus.RECEIVED
        )

        // Add to global carpets
        _carpets.value = _carpets.value + newCarpet

        // Add to Invoice
        val updatedInvoices = _invoices.value.toMutableList()
        val invIdx = updatedInvoices.indexOfFirst { it.id == invoiceId }
        if (invIdx != -1) {
            val inv = updatedInvoices[invIdx]
            val updatedCarpets = inv.carpets + newCarpet
            val calculatedPrice = (area * if (type.contains("دستباف")) 2000000 else 1000000).toLong()
            val total = inv.totalAmount + calculatedPrice
            updatedInvoices[invIdx] = inv.copy(carpets = updatedCarpets, totalAmount = total)
            _invoices.value = updatedInvoices
        }
    }

    fun addMessage(text: String, isIncoming: Boolean = false, role: String = "راننده") {
        val newMsg = Message(
            id = "MSG-${Random.nextInt(100, 999)}",
            sender = if (isIncoming) "دفتر مرکزی" else "شما",
            senderRole = role,
            text = text,
            time = "۱۰:۳۰",
            isIncoming = isIncoming
        )
        _messages.value = _messages.value + newMsg
    }

    // Full delivery completion: collect payment, mark carpets delivered, close mission
    fun completeDelivery(invoiceId: String, missionId: String, amount: Long, method: String) {
        recordPayment(invoiceId, amount, method)

        val carpetsList = _carpets.value.toMutableList()
        val inv = _invoices.value.find { it.id == invoiceId }
        inv?.carpets?.forEach { carpet ->
            val cIdx = carpetsList.indexOfFirst { it.id == carpet.id }
            if (cIdx != -1) {
                carpetsList[cIdx] = carpetsList[cIdx].copy(status = CarpetStatus.DELIVERED)
            }
        }
        _carpets.value = carpetsList

        val missions = _missions.value.toMutableList()
        val mIdx = missions.indexOfFirst { it.id == missionId }
        if (mIdx != -1) {
            missions[mIdx] = missions[mIdx].copy(status = MissionStatus.COMPLETED)
            _missions.value = missions
        }

        addNotification("تحویل با موفقیت انجام شد", "سفارش ${invoiceId} به مشتری ${inv?.customerName ?: ""} تحویل و تسویه گردید.", "راننده")
    }

    fun addCashEntry(type: CashEntryType, amount: Long, description: String) {
        val lastBalance = _cashEntries.value.firstOrNull()?.balanceAfter ?: 0L
        val newBalance = if (type == CashEntryType.IN) lastBalance + amount else lastBalance - amount
        val entry = CashRegisterEntry(
            id = "CSH-${Random.nextInt(600, 999)}",
            type = type,
            amount = amount,
            date = "۱۴۰۵/۰۴/۲۷",
            time = "۱۲:۰۰",
            description = description,
            balanceAfter = newBalance
        )
        _cashEntries.value = listOf(entry) + _cashEntries.value
        addNotification(
            if (type == CashEntryType.IN) "ورود وجه نقد به صندوق" else "خروج وجه نقد از صندوق",
            "$description — مبلغ ${amount} ریال",
            "مالی"
        )
    }

    fun approveExpense(expenseId: String) {
        val updated = _expenses.value.toMutableList()
        val index = updated.indexOfFirst { it.id == expenseId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = "تایید شده")
            _expenses.value = updated
            addNotification("تایید هزینه", "هزینه ${updated[index].description} تایید شد.", "مالی")
        }
    }

    fun paySalary(salaryId: String) {
        val updated = _salaryRecords.value.toMutableList()
        val index = updated.indexOfFirst { it.id == salaryId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = "پرداخت شده")
            _salaryRecords.value = updated
            addNotification("پرداخت حقوق", "حقوق ${updated[index].employeeName} به مبلغ ${updated[index].netPay} ریال پرداخت شد.", "مالی")
        }
    }

    fun updatePurchaseOrderStatus(poId: String, newStatus: String) {
        val updated = _purchaseOrders.value.toMutableList()
        val index = updated.indexOfFirst { it.id == poId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = newStatus)
            _purchaseOrders.value = updated
            addNotification("به‌روزرسانی سفارش خرید", "سفارش خرید ${poId} به وضعیت «${newStatus}» تغییر یافت.", "مالی")
        }
    }

    fun updateComplaintStatus(complaintId: String, newStatus: String) {
        val updated = _complaints.value.toMutableList()
        val index = updated.indexOfFirst { it.id == complaintId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = newStatus)
            _complaints.value = updated
            addNotification("به‌روزرسانی شکایت", "شکایت ${updated[index].subject} به وضعیت «${newStatus}» تغییر یافت.", "پشتیبانی")
        }
    }

    fun launchCampaign(campaignId: String) {
        val updated = _campaigns.value.toMutableList()
        val index = updated.indexOfFirst { it.id == campaignId }
        if (index != -1) {
            val c = updated[index]
            updated[index] = c.copy(status = "در حال ارسال", sentCount = c.recipientsCount)
            _campaigns.value = updated
            addNotification("ارسال کمپین بازاریابی", "کمپین «${c.title}» برای ${c.recipientsCount} مشتری ارسال شد.", "بازاریابی")
        }
    }

    fun restockInventory(itemId: String, amount: Int) {
        val updated = _inventoryItems.value.toMutableList()
        val index = updated.indexOfFirst { it.id == itemId }
        if (index != -1) {
            updated[index] = updated[index].copy(currentStock = updated[index].currentStock + amount)
            _inventoryItems.value = updated
            addNotification("شارژ موجودی انبار", "موجودی «${updated[index].name}» به میزان ${amount} واحد افزایش یافت.", "انبار")
        }
    }

    fun revokeDevice(deviceId: String) {
        val updated = _trustedDevices.value.filterNot { it.id == deviceId && !it.isCurrent }
        _trustedDevices.value = updated
        addNotification("لغو دسترسی دستگاه", "دسترسی یک دستگاه غیرمجاز از سیستم لغو شد.", "امنیت")
    }

    fun reconnectPrinter(printerId: String) {
        val updated = _printers.value.toMutableList()
        val index = updated.indexOfFirst { it.id == printerId }
        if (index != -1) {
            updated[index] = updated[index].copy(status = "متصل")
            _printers.value = updated
            addNotification("اتصال مجدد پرینتر", "پرینتر ${updated[index].name} با موفقیت متصل شد.", "سیستم")
        }
    }

    fun recordPayment(invoiceId: String, amount: Long, method: String) {
        val updated = _invoices.value.toMutableList()
        val index = updated.indexOfFirst { it.id == invoiceId }
        if (index != -1) {
            val inv = updated[index]
            val newPaid = inv.paidAmount + amount
            val status = if (newPaid >= inv.totalAmount) InvoiceStatus.PAID else InvoiceStatus.PARTIAL
            updated[index] = inv.copy(paidAmount = newPaid, status = status, paymentMethod = method)
            _invoices.value = updated

            addNotification("ثبت تراکنش مالی", "مبلغ ${amount} ریال برای فاکتور ${invoiceId} از طریق ${method} دریافت شد.", "مالی")
        }
    }
}
