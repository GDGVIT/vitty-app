package com.dscvit.vitty.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("EventName")
    @Expose
    val EventName: String,
    @SerializedName("Time")
    @Expose
    val Time: String
)
