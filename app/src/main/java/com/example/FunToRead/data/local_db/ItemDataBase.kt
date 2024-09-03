//data/localdb/ItemDataBase
package com.example.FunToRead.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.FunToRead.data.model.Item
import androidx.room.TypeConverters


  //The Room database for the application, which contains the Item table.

@TypeConverters(Converters::class)
@Database(entities = [Item::class], version = 2, exportSchema = false)
abstract class ItemDataBase : RoomDatabase() {


      //Provides access to the DAO (Data Access Object) for the Item entity.

    abstract fun itemDao(): ItemDao

    companion object{

        // Volatile keyword ensures that changes to this variable are visible to all threads.
        @Volatile
        private var instance:ItemDataBase?=null

        /**
          Returns a singleton instance of the database. If it doesn't exist, it creates one.
          @param context The application context.
          @return An instance of ItemDataBase.
         */
        fun getDataBase(context:Context)= instance?: synchronized(this){
            Room.databaseBuilder(context.applicationContext,ItemDataBase::class.java,"items_db")
                .build()
        }
    }
}