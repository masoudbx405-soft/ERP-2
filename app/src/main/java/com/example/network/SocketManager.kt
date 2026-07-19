package com.example.network

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * پوششی روی Socket.IO برای دریافت رویدادهای زنده از سرور (نوتیفیکیشن، تغییر فاکتور/ماموریت،
 * موقعیت راننده و...). این همون کانالیه که باعث میشه اپ مدیر و اپ راننده «همزمان» بمونن —
 * وقتی یکی چیزی رو عوض می‌کنه، بقیه فوراً باخبر می‌شن، بدون نیاز به رفرش دستی.
 */
object SocketManager {
    private var socket: Socket? = null

    val isConnected: Boolean get() = socket?.connected() == true

    fun connect(
        token: String,
        onConnected: () -> Unit = {},
        onDisconnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        disconnect() // اگر اتصال قبلی باز مانده، اول ببندیم

        val base = NetworkConfig.baseUrlOrNull()
        if (base == null) {
            onError("آدرس سرور تنظیم نشده است.")
            return
        }

        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionDelay = 2000
            }
            val s = IO.socket(base, options)

            s.on(Socket.EVENT_CONNECT) { onConnected() }
            s.on(Socket.EVENT_DISCONNECT) { onDisconnected() }
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val msg = args.firstOrNull()?.toString() ?: "خطای نامشخص اتصال"
                onError(msg)
            }

            socket = s
            s.connect()
        } catch (e: URISyntaxException) {
            onError("آدرس سرور نامعتبر است: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    /** گوش‌دادن به یک رویداد دلخواه (مثلاً "notification:new", "invoice:update", "driver:location") */
    fun on(event: String, callback: (JSONObject) -> Unit) {
        socket?.on(event) { args ->
            val data = args.firstOrNull()
            if (data is JSONObject) callback(data)
        }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun joinRoom(room: String) {
        socket?.emit("chat:join", room)
    }

    fun leaveRoom(room: String) {
        socket?.emit("chat:leave", room)
    }
}
