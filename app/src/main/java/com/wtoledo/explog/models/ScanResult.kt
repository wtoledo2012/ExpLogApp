package com.wtoledo.explog.models

import com.google.gson.annotations.SerializedName

data class ScanResult(
    @SerializedName("scannedAmount")
    val scannedAmount: Double?,
    @SerializedName("scannedDate")
    val scannedDate: String?,
    @SerializedName("scannedName")
    val scannedName: String?
)