package com.elsasa.btrade3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.elsasa.btrade3.dao.BarangDao
import com.elsasa.btrade3.dao.CustomerDao
import com.elsasa.btrade3.dao.OrderDao
import com.elsasa.btrade3.dao.OrderItemDao
import com.elsasa.btrade3.dao.SalesPersonDao
import com.elsasa.btrade3.model.Barang
import com.elsasa.btrade3.model.Customer
import com.elsasa.btrade3.model.Order
import com.elsasa.btrade3.model.OrderItem
import com.elsasa.btrade3.model.SalesPerson

@Database(
    entities = [Order::class, OrderItem::class, Barang::class, Customer::class, SalesPerson::class],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun barangDao(): BarangDao
    abstract fun customerDao(): CustomerDao
    abstract fun salesPersonDao(): SalesPersonDao

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
                .addMigrations(MIGRATION_15_16)
                .build()
                INSTANCE = instance
                instance
            }
        }


        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE order_table RENAME COLUMN orderLocalCode TO orderLocalId")
            }
        }
    }
}