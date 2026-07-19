package com.example.network

import com.example.data.AppStateStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * پل بین سرور واقعی و AppStateStore محلی. UI با همین آبجکت کار می‌کنه؛
 * جزئیات Retrofit/Socket.IO همه پشت این پنهانه.
 */
object NetworkSync {
    // disconnected | connecting | connected | error
    private val _status = MutableStateFlow("disconnected")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    var currentUserId: String? = null
        private set

    suspend fun requestOtp(phone: String): Result<Unit> {
        return try {
            val resp = ApiClient.api.requestOtp(RequestOtpBody(phone))
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("سرور درخواست را نپذیرفت (کد ${resp.code()})."))
        } catch (e: Exception) {
            Result.failure(Exception(networkErrorMessage(e)))
        }
    }

    suspend fun verifyOtpAndConnect(phone: String, code: String, name: String, role: String): Result<Unit> {
        return try {
            val resp = ApiClient.api.verifyOtp(VerifyOtpBody(phone, code, name, role))
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                NetworkConfig.setAuthToken(body.token)
                currentUserId = body.user.id
                connectRealtime(body.token)
                Result.success(Unit)
            } else {
                Result.failure(Exception("کد تایید نادرست یا منقضی شده است."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(networkErrorMessage(e)))
        }
    }

    private fun connectRealtime(token: String) {
        _status.value = "connecting"
        SocketManager.connect(
            token = token,
            onConnected = { _status.value = "connected"; _lastError.value = null },
            onDisconnected = { _status.value = "disconnected" },
            onError = { msg -> _status.value = "error"; _lastError.value = msg }
        )

        // وقتی گوشی دیگری (مدیر/راننده/انبار) نوتیفیکیشنی بفرستد، همینجا زنده دریافت می‌شود
        SocketManager.on("notification:new") { json ->
            AppStateStore.addNotification(
                title = json.optString("title", ""),
                body = json.optString("body", ""),
                category = json.optString("category", "سیستم")
            )
        }

        // تغییر وضعیت فاکتور توسط یک نفر دیگر (مثلاً راننده) روی بقیه‌ی گوشی‌ها هم بازتاب داده می‌شود
        SocketManager.on("invoice:update") { json ->
            AppStateStore.addNotification(
                title = "به‌روزرسانی فاکتور",
                body = "فاکتور ${json.optString("id")} به وضعیت «${json.optString("status")}» تغییر کرد.",
                category = "سیستم"
            )
        }

        SocketManager.on("mission:update") { json ->
            AppStateStore.addNotification(
                title = "به‌روزرسانی ماموریت",
                body = "ماموریت ${json.optString("id")} به وضعیت «${json.optString("status")}» تغییر کرد.",
                category = "راننده"
            )
        }
    }

    fun disconnect() {
        SocketManager.disconnect()
        NetworkConfig.setAuthToken(null)
        currentUserId = null
        _status.value = "disconnected"
    }

    /** ارسال یک نوتیفیکیشن واقعی به سرور تا لحظه‌ای روی گوشی‌های دیگر هم دیده شود. */
    suspend fun broadcastNotification(title: String, body: String, category: String, targetRole: String? = null): Result<Unit> {
        return try {
            ApiClient.api.createNotification(CreateNotificationBody(title, body, category, targetRole))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(networkErrorMessage(e)))
        }
    }

    suspend fun testConnection(): Result<Unit> {
        return try {
            val resp = ApiClient.api.healthCheck()
            if (resp.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("سرور پاسخ نامعتبر داد (کد ${resp.code()})."))
        } catch (e: Exception) {
            Result.failure(Exception(networkErrorMessage(e)))
        }
    }

    private fun networkErrorMessage(e: Exception): String {
        val base = NetworkConfig.baseUrlOrNull()
        return if (base == null) "ابتدا آدرس سرور را وارد کنید."
        else "اتصال به سرور برقرار نشد. مطمئن شوید سرور و Cloudflare Tunnel روشن‌اند. (${e.message ?: "خطای شبکه"})"
    }
}
