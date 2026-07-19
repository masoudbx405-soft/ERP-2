package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestOtpBody(val phone: String)

@JsonClass(generateAdapter = true)
data class RequestOtpResponse(val sent: Boolean)

@JsonClass(generateAdapter = true)
data class VerifyOtpBody(
    val phone: String,
    val code: String,
    val name: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: String,
    val phone: String,
    val name: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    val id: String,
    val title: String,
    val body: String?,
    val category: String?,
    @Json(name = "target_role") val targetRole: String?,
    @Json(name = "target_user_id") val targetUserId: String?,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class CreateNotificationBody(
    val title: String,
    val body: String? = null,
    val category: String? = null,
    val targetRole: String? = null,
    val targetUserId: String? = null
)

@JsonClass(generateAdapter = true)
data class DriverLocationBody(val latitude: Double, val longitude: Double)

@JsonClass(generateAdapter = true)
data class UpdateStatusBody(val status: String)

@JsonClass(generateAdapter = true)
data class InvoiceDto(
    val id: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "driver_id") val driverId: String?,
    val status: String,
    @Json(name = "total_amount") val totalAmount: Long,
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class MissionDto(
    val id: String,
    @Json(name = "invoice_id") val invoiceId: String?,
    @Json(name = "driver_id") val driverId: String,
    val type: String,
    val status: String,
    @Json(name = "created_at") val createdAt: String?
)
