package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * آدرس سرور (مثلاً آدرس Cloudflare Quick Tunnel) و توکن ورود اینجا نگه‌داری می‌شن.
 * عمداً بدون SharedPreferences/Context طراحی شده تا ساده و بدون نیاز به تغییر
 * AndroidManifest/Application class باشه — برای مرحله‌ی تست کافیه (با هر بار
 * باز کردن اپ، آدرس سرور دوباره وارد می‌شه).
 */
object NetworkConfig {
    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    fun setServerUrl(url: String) {
        _serverUrl.value = url.trim().removeSuffix("/")
        ApiClient.reset()
    }

    fun setAuthToken(token: String?) {
        _authToken.value = token
    }

    fun baseUrlOrNull(): String? = _serverUrl.value.ifBlank { null }
}
