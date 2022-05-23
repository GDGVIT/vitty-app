package com.dscvit.vitty.model

import android.net.Uri

data class UserDetails(
    var name: String? = "",
    var email: String? = "",
    var photoUrl: Uri? = null,
    var token: String? = "",
    var method: String? = "Google",
)
