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
    version = 17,
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
                .addMigrations(MIGRATION_15_16, MIGRATION_16_17)
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

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Rename the old table
                db.execSQL("ALTER TABLE order_item_table RENAME TO order_item_table_old")

                // Step 2: Recreate the table with ON DELETE NO_ACTION
                db.execSQL("""
            CREATE TABLE order_item_table (
                orderId TEXT NOT NULL,
                noUrut INTEGER NOT NULL,
                brgId TEXT NOT NULL,
                brgCode TEXT NOT NULL,
                brgName TEXT NOT NULL,
                kategoriName TEXT NOT NULL,
                qtyBesar INTEGER NOT NULL,
                satBesar TEXT NOT NULL,
                qtyKecil INTEGER NOT NULL,
                satKecil TEXT NOT NULL,
                konversi INTEGER NOT NULL,
                unitPrice REAL NOT NULL,
                lineTotal REAL NOT NULL,
                PRIMARY KEY (orderId, noUrut),
                FOREIGN KEY (orderId) REFERENCES order_table(orderId) ON DELETE NO ACTION
            )
        """.trimIndent())

                // Step 3: Copy data back
                db.execSQL("""
            INSERT INTO order_item_table 
            SELECT * FROM order_item_table_old
        """)

                // Step 4: Drop old table
                db.execSQL("DROP TABLE order_item_table_old")
            }
        }
    }
}