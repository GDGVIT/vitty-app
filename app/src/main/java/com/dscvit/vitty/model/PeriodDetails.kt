package com.dscvit.vitty.model

import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID

data class PeriodDetails(
//    var courseType: String = "",
    val id: String = UUID.randomUUID().toString(),
    var courseCode: String = "",
    var courseName: String = "",
    var startTime: Timestamp = Timestamp(Date()),
    var endTime: Timestamp = Timestamp(Date()),
    var slot: String = "",
    var roomNo: String = "",
)
