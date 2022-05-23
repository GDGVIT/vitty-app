package com.dscvit.vitty.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dscvit.vitty.api.ApiEventRestClient
import com.dscvit.vitty.api.RetrofitEventListener
import com.dscvit.vitty.model.EventDetails
import retrofit2.Call

class VITEventsViewModel : ViewModel() {

    private val _events = MutableLiveData<EventDetails>().apply {
        fetchEvents()
    }

    val events: LiveData<EventDetails> = _events

    private fun fetchEvents() {
        ApiEventRestClient.instance.getEvents(object : RetrofitEventListener {
            override fun onSuccess(call: Call<EventDetails>?, response: EventDetails?) {
                _events.postValue(response)
            }
            override fun onError(call: Call<EventDetails>?, t: Throwable?) {
                _events.postValue(null)
            }
        })
    }
}
