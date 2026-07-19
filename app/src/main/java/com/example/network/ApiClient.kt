package com.example.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    @Volatile private var retrofit: Retrofit? = null

    /** وقتی آدرس سرور عوض می‌شه (مثلاً تونل جدید)، این باید صدا زده بشه تا نمونه‌ی قبلی دور ریخته بشه. */
    fun reset() {
        retrofit = null
    }

    private fun buildOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = NetworkConfig.authToken.value
                val request = if (token != null) {
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()
    }

    private fun instance(): Retrofit {
        val base = NetworkConfig.baseUrlOrNull()
            ?: throw IllegalStateException("آدرس سرور تنظیم نشده است. ابتدا آدرس Cloudflare Tunnel را وارد کنید.")
        return retrofit ?: Retrofit.Builder()
            .baseUrl("$base/")
            .client(buildOkHttp())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .also { retrofit = it }
    }

    val api: ApiService get() = instance().create(ApiService::class.java)
}
