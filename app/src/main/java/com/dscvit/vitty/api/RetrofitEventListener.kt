package com.dscvit.vitty.api

import com.dscvit.vitty.model.EventDetails
import retrofit2.Call

interface RetrofitEventListener {
    fun onSuccess(call: Call<EventDetails>?, response: EventDetails?)
    fun onError(call: Call<EventDetails>?, t: Throwable?)
}
