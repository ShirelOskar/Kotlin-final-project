// data/repository/ItemRepository
package com.example.FunToRead.data.repository

import android.app.Application
import com.example.FunToRead.data.local_db.ItemDao
import com.example.FunToRead.data.local_db.ItemDataBase
import com.example.FunToRead.data.model.Item

class ItemRepository(application: Application) {

    private var itemDao: ItemDao?

    init {
        // Initialize the database and item DAO
        val db=ItemDataBase.getDataBase(application.applicationContext)
        itemDao= db.itemDao()
    }

    // Retrieve all items
    fun getItems()=itemDao?.getItems()
    // Retrieve items marked as "to read"
    fun getToReadBooks() = itemDao?.getToReadBooks()
    // Retrieve items marked as "already read"
    fun getAlreadyReadBooks() = itemDao?.getAlreadyReadBooks()

    // Update an existing item in the database
    suspend fun updateItem(item: Item) {
        itemDao?.updateItem(item)
    }

    // Add a new item to the database
    suspend fun addItem(item: Item){
            itemDao?.addItem(item)
    }

    // Delete an existing item from the database
     suspend fun deleteItem(item: Item){
            itemDao?.deleteItem(item)
    }

    // Retrieve a specific item by its ID
    fun getItem(id:Int) = itemDao?.getItem(id)

    // Delete all items from the database
    suspend fun deleteAll(){
        itemDao?.deleteAll()
    }
}