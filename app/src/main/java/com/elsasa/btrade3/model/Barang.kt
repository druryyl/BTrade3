package com.elsasa.btrade3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Barang(
    val brgCode: String,
    val brgName: String,
    val kategoriName: String,
    val availableStock: Int,
    val unitName: String,
    val unitPrice: Double
) : Parcelable