//data/localdb/ItemDao
package com.example.FunToRead.data.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.FunToRead.data.model.Item


  //Data Access Object (DAO) for performing operations on the Item entity in the Room database.

@Dao
interface ItemDao {
    /**
      Inserts a new item into the database. If the item already exists, it replaces the existing entry.
      @param item The item to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item:Item)

    /**
      Deletes one or more items from the database.
      @param items The items to be deleted.
     */
    @Delete
    suspend fun deleteItem(vararg items: Item)

    /**
      Updates an existing item in the database.
      @param item The item to be updated.
     */
    @Update
     suspend fun updateItem(item: Item)

    /**
      Retrieves all items from the database as a LiveData list.
      LiveData ensures that the UI is updated automatically when data changes.
      @return A LiveData list of all items.
     */
    @Query("SELECT * FROM ITEMS ")
    fun getItems(): LiveData<List<Item>>

    /**
      Retrieves a specific item by its ID.
      @param id The ID of the item to be retrieved.
      @return The item with the specified ID.
     */
    @Query("SELECT * FROM ITEMS WHERE id LIKE :id")
    fun getItem(id:Int):Item


    //Deletes all items from the database.

    @Query("DELETE FROM ITEMS")
    suspend fun deleteAll()

    /**
      Retrieves items that have a status of 'To Read' or 'לקריאה', ordered by title in ascending order.
      @return A LiveData list of items to read.
     */
    @Query("SELECT * FROM ITEMS WHERE status = 'To Read' OR status = 'לקריאה'  ORDER BY title ASC")
    fun getToReadBooks(): LiveData<List<Item>>

    /**
      Retrieves items that have a status of 'Already Read' or 'כבר נקרא', ordered by title in ascending order.
      @return A LiveData list of items that have been read.
     */
    @Query("SELECT * FROM ITEMS WHERE status = 'Already Read' OR status = 'כבר נקרא' ORDER BY title ASC")
    fun getAlreadyReadBooks(): LiveData<List<Item>>
}
