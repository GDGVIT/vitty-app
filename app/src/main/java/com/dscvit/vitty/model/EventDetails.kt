package com.dscvit.vitty.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EventDetails(
    @SerializedName("EventDate")
    @Expose
    val EventDate: String,
    @SerializedName("Events")
    @Expose
    val Events: List<Event>
)
