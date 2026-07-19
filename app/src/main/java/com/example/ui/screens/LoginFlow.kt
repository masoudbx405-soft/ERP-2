package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppStateStore
import com.example.network.NetworkConfig
import com.example.network.NetworkSync
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginFlowContainer(onLoginComplete: (String) -> Unit) {
    val step by AppStateStore.loginStep.collectAsState()
    val selectedRole by AppStateStore.selectedRoleForLogin.collectAsState()
    val userPhone by AppStateStore.currentUserPhone.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0B4DA2).copy(alpha = 0.08f),
                            Color(0xFFF7F9FC)
                        )
                    )
                )
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                },
                label = "LoginTransition"
            ) { currentStep ->
                when (currentStep) {
                    "splash" -> SplashScreenView {
                        AppStateStore.loginStep.value = "welcome"
                    }
                    "welcome" -> WelcomeScreenView {
                        AppStateStore.loginStep.value = "role"
                    }
                    "role" -> RoleSelectionView(selectedRole) { role ->
                        AppStateStore.selectedRoleForLogin.value = role
                        AppStateStore.loginStep.value = "login"
                    }
                    "login" -> LoginScreenView(
                        role = selectedRole,
                        phone = userPhone,
                        onPhoneChange = { AppStateStore.currentUserPhone.value = it },
                        onBack = { AppStateStore.loginStep.value = "role" },
                        onLoginSuccess = {
                            AppStateStore.loginStep.value = "otp"
                        }
                    )
                    "otp" -> OtpVerificationView(
                        phone = userPhone,
                        role = selectedRole,
                        onBack = { AppStateStore.loginStep.value = "login" },
                        onVerified = {
                            AppStateStore.currentRole.value = selectedRole
                            onLoginComplete(selectedRole)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreenView(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "LogoScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Animated Premium Logo Emblem
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0B4DA2), Color(0xFF1E88E5))
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "قالیشویی هوشمند پاکان",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B4DA2),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "سامانه مدیریت هوشمند و جامع کارخانه (ERP)",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF2BB673),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun WelcomeScreenView(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large Premium Illustration Container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1.2f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE3E8EF), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = Color(0xFF0B4DA2),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "مدیریت هوشمند جمع‌آوری، شستشو و تحویل منظم",
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "خوش آمدید",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "به سامانه جامع و یکپارچه قالیشویی هوشمند خوش آمدید. کنترل تمامی مراحل شستشو، مسیریابی رانندگان و ردیابی آنلاین فرش‌های مشتریان در دستان شماست.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "شروع به کار",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RoleSelectionView(selectedRole: String, onRoleSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "انتخاب نقش کاربری",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B4DA2)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "لطفا بخش مربوط به کاربری خود را انتخاب نمایید",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        val roles = listOf(
            Triple("customer", "پنل مشتریان", "پیگیری سفارش، پرداخت فاکتور، درخواست جمع‌آوری"),
            Triple("driver", "راننده توزیع", "ماموریت‌های روزانه، مسیریابی هوشمند، صدور فاکتور"),
            Triple("warehouse", "کنترل کیفیت و انبار", "مدیریت شستشو، کنترل کیفیت (QC)، چیدمان قفسه‌ها"),
            Triple("manager", "مدیریت کل سیستم", "مانیتورینگ زنده کارخانه، داشبورد مالی، گزارشات هوشمند")
        )

        roles.forEach { (roleKey, title, desc) ->
            val isSelected = selectedRole == roleKey
            val borderColor = if (isSelected) Color(0xFF0B4DA2) else Color(0xFFE3E8EF)
            val bgColor = if (isSelected) Color(0xFF0B4DA2).copy(alpha = 0.05f) else Color.White
            val icon = when (roleKey) {
                "customer" -> Icons.Default.Person
                "driver" -> Icons.Default.LocalShipping
                "warehouse" -> Icons.Default.Layers
                else -> Icons.Default.Dashboard
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onRoleSelected(roleKey) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF0B4DA2) else Color(0xFFF0F4F8)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else Color(0xFF0B4DA2)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2BB673)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { AppStateStore.loginStep.value = "login" },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "تایید و ادامه",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginScreenView(
    role: String,
    phone: String,
    onPhoneChange: (String) -> Unit,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val roleTitle = when (role) {
        "customer" -> "پنل مشتریان"
        "driver" -> "پنل رانندگان"
        "warehouse" -> "پنل انبار و کارخانه"
        else -> "پنل مدیریتی"
    }

    val scope = rememberCoroutineScope()
    val serverUrl by NetworkConfig.serverUrl.collectAsState()
    var serverUrlInput by remember { mutableStateOf(serverUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ورود به $roleTitle",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // آدرس سرور (Cloudflare Tunnel) — هر دو اپ (مدیر و راننده) باید یک آدرس یکسان بزنند
        OutlinedTextField(
            value = serverUrlInput,
            onValueChange = { serverUrlInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("آدرس سرور (Cloudflare Tunnel)") },
            placeholder = { Text("https://xxxx.trycloudflare.com") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B4DA2),
                focusedLabelColor = Color(0xFF0B4DA2)
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF0B4DA2).copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFF0B4DA2),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "شماره همراه خود را وارد کنید",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "جهت دریافت کد تایید هویت یک‌بار مصرف (OTP)",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("شماره تلفن همراه") },
            placeholder = { Text("مثال: ۰۹۱۲۳۴۵۶۷۸۹") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0B4DA2),
                focusedLabelColor = Color(0xFF0B4DA2)
            ),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.Gray)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Cloudflare Secure Connection Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2BB673).copy(alpha = 0.06f))
                .border(1.dp, Color(0xFF2BB673).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF2BB673),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "اتصال ایمن برقرار است (SSL - Cloudflare Secured)",
                fontSize = 11.sp,
                color = Color(0xFF2BB673),
                fontWeight = FontWeight.SemiBold
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                fontSize = 12.sp,
                color = Color(0xFFE53935),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (phone.length >= 10 && serverUrlInput.isNotBlank() && !isLoading) {
                    errorMessage = null
                    NetworkConfig.setServerUrl(serverUrlInput)
                    isLoading = true
                    scope.launch {
                        val result = NetworkSync.requestOtp(phone)
                        isLoading = false
                        result.onSuccess { onLoginSuccess() }
                            .onFailure { errorMessage = it.message ?: "ارسال کد ناموفق بود." }
                    }
                }
            },
            enabled = phone.length >= 10 && serverUrlInput.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = "ارسال کد تایید",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtpVerificationView(
    phone: String,
    role: String,
    onBack: () -> Unit,
    onVerified: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var otpCode by remember { mutableStateOf("") }
    var countdown by remember { mutableStateOf(120) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roleTitle = when (role) {
        "customer" -> "مشتری"
        "driver" -> "راننده"
        "warehouse" -> "انباردار"
        else -> "مدیر کارخانه"
    }

    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    if (isSuccess) {
        LaunchedEffect(Unit) {
            delay(1200)
            onVerified()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تایید هویت",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (isSuccess) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2BB673).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2BB673),
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "شماره با موفقیت تایید شد",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2BB673)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "در حال ورود به داشبورد کاری قالیشویی...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "کد تایید پیامکی را وارد کنید",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "کد ۴ رقمی به شماره $phone ارسال شد",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Visual OTP Boxes Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        if (it.length <= 4) otpCode = it
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(64.dp),
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 16.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0B4DA2),
                        unfocusedBorderColor = Color(0xFFE3E8EF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (countdown > 0) {
                    val minutes = countdown / 60
                    val seconds = countdown % 60
                    Text(
                        text = "ارسال مجدد کد تا: $minutes:${String.format("%02d", seconds)}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        text = "ارسال مجدد کد",
                        fontSize = 14.sp,
                        color = Color(0xFF0B4DA2),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            countdown = 120
                            otpCode = ""
                            errorMessage = null
                            scope.launch { NetworkSync.requestOtp(phone) }
                        }
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFFE53935),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (otpCode.length == 4 && !isLoading) {
                        errorMessage = null
                        isLoading = true
                        scope.launch {
                            val result = NetworkSync.verifyOtpAndConnect(phone, otpCode, roleTitle, role)
                            isLoading = false
                            result.onSuccess { isSuccess = true }
                                .onFailure { errorMessage = it.message ?: "کد نامعتبر بود." }
                        }
                    }
                },
                enabled = otpCode.length == 4 && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B4DA2)),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "بررسی و ورود به سیستم",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
