package com.dscvit.vitty.api

import com.dscvit.vitty.model.EventDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApiEventRestClient {
    companion object {
        val instance = ApiEventRestClient()
    }

    private var mApiUser: APIEvents? = null

    fun getEvents(retrofitEventListener: RetrofitEventListener) {
        val retrofit = NetworkClient.retrofitClient
        mApiUser = retrofit.create<APIEvents>(APIEvents::class.java)

        val dateFormatter = SimpleDateFormat(
            "dd-MM-yyyy", Locale.getDefault()
        )
        val data = HashMap<String, String>()
        data["date"] = dateFormatter.format(Date())

        val apiUserCall = mApiUser!!.getEvents(data)

        apiUserCall.enqueue(object : Callback<EventDetails> {
            override fun onResponse(call: Call<EventDetails>, response: Response<EventDetails>) {
                if (response.body() != null) {
                    retrofitEventListener.onSuccess(call, response.body())
                }
            }
            override fun onFailure(call: Call<EventDetails>, t: Throwable) {
                retrofitEventListener.onError(call, t)
            }
        })
    }
}
