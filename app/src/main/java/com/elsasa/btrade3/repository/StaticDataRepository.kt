package com.elsasa.btrade3.repository

import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.model.SalesPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StaticDataRepository {
    fun getCustomers(): Flow<List<Customer>> = flow {
        emit(
            listOf(
                Customer("", "C001", "PT Maju Jaya", "Jl. Merdeka No. 123", ""),
                Customer("", "C002", "CV Berkah Abadi", "Jl. Sudirman No. 456", ""),
                Customer("", "C003", "Toko Sejahtera", "Jl. Gatot Subroto No. 789", ""),
                Customer("", "C004", "UD Harmoni", "Jl. Diponegoro No. 321", ""),
                Customer("", "C005", "PT Sumber Rejeki", "Jl. Thamrin No. 654", "")
            )
        )
    }

    fun getSalesPersons(): Flow<List<SalesPerson>> = flow {
        emit(
            listOf(
                SalesPerson("", "S001", "Budi Santoso"),
                SalesPerson("", "S002", "Ani Wijaya"),
                SalesPerson("", "S003", "Joko Susilo"),
                SalesPerson("", "S004", "Dewi Lestari"),
                SalesPerson("", "S005", "Agus Prabowo")
            )
        )
    }

    fun getBarangs(): Flow<List<Barang>> = flow {
        emit(
            listOf(
                Barang("", "B001", "Laptop ASUS", "Elektronik", "", "Unit", 1, 8500000.0, 50),
                Barang("", "B002", "Mouse Wireless", "Aksesoris", "", "Unit", 1, 150000.0, 200),
                Barang("", "B003", "Keyboard Mechanical", "Aksesoris", "", "Unit", 1, 450000.0, 150),
                Barang("", "B004", "Monitor 24\"", "Elektronik", "", "Unit", 1, 2200000.0, 75),
                Barang("", "B005", "Printer Epson", "Elektronik", "", "Unit", 1, 3200000.0, 30),
                Barang("", "B006", "Flashdisk 32GB", "Aksesoris", "", "Unit", 1, 85000.0, 300),
                Barang("", "B007", "Webcam HD", "Aksesoris", "", "Unit", 1, 650000.0, 80),
                Barang("", "B008", "Speaker Bluetooth", "Elektronik", "", "Unit", 1, 750000.0, 60)
            )
        )
    }
}