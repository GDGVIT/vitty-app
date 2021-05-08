package com.dscvit.vitty.model

import com.google.firebase.Timestamp
import java.util.*

data class PeriodDetails(
    var courseName: String = "",
    var startTime: Timestamp = Timestamp(Date()),
    var endTime: Timestamp = Timestamp(Date()),
    var slot: String = "",
    var roomNo: String = ":)",
)
