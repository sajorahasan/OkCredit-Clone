package com.sajorahasan.okcredit.api.callback

import com.google.gson.annotations.SerializedName

data class OtpResponse(
    @SerializedName("UserId")
    val userId: String,
    @SerializedName("ValidOTP")
    val validOTP: Boolean
)