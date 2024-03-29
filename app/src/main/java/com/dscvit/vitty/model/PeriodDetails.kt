package com.dscvit.vitty.model

import com.google.firebase.Timestamp
import java.util.Date

data class PeriodDetails(
//    var courseType: String = "",
    var courseCode: String = "",
    var courseName: String = "",
    var startTime: Timestamp = Timestamp(Date()),
    var endTime: Timestamp = Timestamp(Date()),
    var slot: String = "",
    var roomNo: String = "",
)
