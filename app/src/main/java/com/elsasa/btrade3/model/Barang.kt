package com.elsasa.btrade3.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
@Entity(tableName = "barang_table")
@Parcelize
data class Barang(
    @PrimaryKey
    val brgId: String,
    val brgCode: String,
    val brgName: String,
    val kategoriName: String,
    val satBesar: String,
    val satKecil: String,
    val konversi: Int,
    val hrgSat: Double,
    val stok: Int
): Parcelable