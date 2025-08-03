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
                Customer("C001", "PT Maju Jaya", "Jl. Merdeka No. 123"),
                Customer("C002", "CV Berkah Abadi", "Jl. Sudirman No. 456"),
                Customer("C003", "Toko Sejahtera", "Jl. Gatot Subroto No. 789"),
                Customer("C004", "UD Harmoni", "Jl. Diponegoro No. 321"),
                Customer("C005", "PT Sumber Rejeki", "Jl. Thamrin No. 654")
            )
        )
    }

    fun getSalesPersons(): Flow<List<SalesPerson>> = flow {
        emit(
            listOf(
                SalesPerson("S001", "Budi Santoso"),
                SalesPerson("S002", "Ani Wijaya"),
                SalesPerson("S003", "Joko Susilo"),
                SalesPerson("S004", "Dewi Lestari"),
                SalesPerson("S005", "Agus Prabowo")
            )
        )
    }

    fun getBarangs(): Flow<List<Barang>> = flow {
        emit(
            listOf(
                Barang("B001", "Laptop ASUS", "Elektronik", 50, "Unit", 8500000.0),
                Barang("B002", "Mouse Wireless", "Aksesoris", 200, "Unit", 150000.0),
                Barang("B003", "Keyboard Mechanical", "Aksesoris", 150, "Unit", 450000.0),
                Barang("B004", "Monitor 24\"", "Elektronik", 75, "Unit", 2200000.0),
                Barang("B005", "Printer Epson", "Elektronik", 30, "Unit", 3200000.0),
                Barang("B006", "Flashdisk 32GB", "Aksesoris", 300, "Unit", 85000.0),
                Barang("B007", "Webcam HD", "Aksesoris", 80, "Unit", 650000.0),
                Barang("B008", "Speaker Bluetooth", "Elektronik", 60, "Unit", 750000.0)
            )
        )
    }
}