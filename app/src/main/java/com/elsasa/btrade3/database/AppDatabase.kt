package com.elsasa.btrade3.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.elsasa.btrade3.dao.BarangDao
import com.elsasa.btrade3.dao.CustomerDao
import com.elsasa.btrade3.dao.FakturDao
import com.elsasa.btrade3.dao.FakturItemDao
import com.elsasa.btrade3.dao.SalesPersonDao
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.model.Faktur
import com.elsasa.btrade3.model.FakturItem
import com.elsasa.btrade3.model.SalesPerson

@Database(
    entities = [Faktur::class, FakturItem::class, Barang::class, Customer::class, SalesPerson::class],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fakturDao(): FakturDao
    abstract fun fakturItemDao(): FakturItemDao
    abstract fun barangDao(): BarangDao
    abstract fun customerDao(): CustomerDao
    abstract fun salesPersonDao(): SalesPersonDao // Add this

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sales_order_database"
                )
                .fallbackToDestructiveMigration(false) // Add this for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}