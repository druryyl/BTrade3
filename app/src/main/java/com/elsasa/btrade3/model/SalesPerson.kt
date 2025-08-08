package com.elsasa.btrade3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salesperson_table")
data class SalesPerson(
    @PrimaryKey
    val salesPersonId: String,
    val salesPersonCode: String,
    val salesPersonName: String
)