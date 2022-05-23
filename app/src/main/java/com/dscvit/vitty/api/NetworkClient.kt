package com.dscvit.vitty.api

import com.dscvit.vitty.util.APIConstants.BASE_URL
import com.dscvit.vitty.util.APIConstants.TIMEOUT
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    var retrofit: Retrofit? = null

    val retrofitClient: Retrofit
        get() {
            if (retrofit == null) {
                val okHttpClientBuilder = OkHttpClient.Builder()
                okHttpClientBuilder.connectTimeout(TIMEOUT.toLong(), TimeUnit.SECONDS)
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClientBuilder.build())
                    .build()
            }
            return retrofit!!
        }
}
