package com.example.collegeproject
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class orderDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "orders.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "orders"
        const val COLUMN_ID = "id"
        const val COLUMN_SERVICE = "service"
        const val COLUMN_VEHICLE_NAME = "vehicle_name"
        const val COLUMN_QUANTITY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SERVICE TEXT,
                $COLUMN_VEHICLE_NAME TEXT,
                $COLUMN_QUANTITY TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert an order into the database
    fun insertOrder(service: String, vehicleName: String, quantity: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_SERVICE, service)
        values.put(COLUMN_VEHICLE_NAME, vehicleName)
        values.put(COLUMN_QUANTITY, quantity)

        return db.insert(TABLE_NAME, null, values)
    }
}