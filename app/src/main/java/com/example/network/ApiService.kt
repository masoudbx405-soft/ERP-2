package com.example.network

import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/request-otp")
    suspend fun requestOtp(@Body body: RequestOtpBody): Response<RequestOtpResponse>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpBody): Response<AuthResponse>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<NotificationDto>>

    @POST("api/notifications")
    suspend fun createNotification(@Body body: CreateNotificationBody): Response<NotificationDto>

    @GET("api/invoices")
    suspend fun getInvoices(@Query("driverId") driverId: String? = null): Response<List<InvoiceDto>>

    @PATCH("api/invoices/{id}/status")
    suspend fun updateInvoiceStatus(@Path("id") id: String, @Body body: UpdateStatusBody): Response<InvoiceDto>

    @GET("api/missions")
    suspend fun getMissions(@Query("driverId") driverId: String? = null): Response<List<MissionDto>>

    @PATCH("api/missions/{id}/status")
    suspend fun updateMissionStatus(@Path("id") id: String, @Body body: UpdateStatusBody): Response<MissionDto>

    @POST("api/drivers/{id}/location")
    suspend fun updateDriverLocation(@Path("id") driverId: String, @Body body: DriverLocationBody): Response<Unit>

    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>
}
