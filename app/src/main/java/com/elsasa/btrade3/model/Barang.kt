package com.elsasa.btrade3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Barang(
    val brgId: String,
    val brgCode: String,
    val brgName: String,
    val kategoriName: String,
    val satBesar: String,
    val satKecil: String,
    val konversi: Int,
    val hrgSat: Double,
    val stok: Int
) : Parcelable