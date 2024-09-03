// ui/itemsViewModel
package com.example.FunToRead.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.example.FunToRead.data.model.Item
import com.example.FunToRead.data.repository.ItemRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import android.content.SharedPreferences
import android.content.Context

class ItemsViewModel(application: Application): AndroidViewModel(application) {

    private val _totalReadingTime = MutableLiveData<Int>()
    val totalReadingTime: LiveData<Int> get() = _totalReadingTime

    // Repository to handle data operations
    private val repository = ItemRepository(application)
    // LiveData to hold the list of items
    val items: LiveData<List<Item>>? = repository.getItems()
    // LiveData to hold the list of items marked as "To Read"
    val toReadItems: LiveData<List<Item>>? = repository.getToReadBooks()
    // LiveData to hold the list of items marked as "Already Read"
    val alreadyReadItems: LiveData<List<Item>>? = repository.getAlreadyReadBooks()

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("reading_prefs", Context.MODE_PRIVATE)

    // MediatorLiveData to hold the currently chosen item
    private val _chosenItem = MediatorLiveData<Item>()
    val chosenItem: LiveData<Item> get() = _chosenItem

    init {
        // Initialize the total reading time
        _totalReadingTime.value = sharedPreferences.getInt("total_reading_time", 0)

        // Set up a listener for SharedPreferences changes
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "total_reading_time") {
                _totalReadingTime.value = sharedPreferences.getInt("total_reading_time", 0)
            }
        }
    }


    // Method to set the chosen item
    fun setItem(item: Item) {
        _chosenItem.value = item
    }

    // Method to add a new item to the repository
    fun addItem(item: Item) {
        viewModelScope.launch {
            repository.addItem(item)
            // Update the chosen item to reflect the newly added item
            _chosenItem.value = item
        }
    }

    // Method to update an existing item in the repository
    fun updateItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
            // Update the chosen item to reflect the newly updated item
            _chosenItem.value = item
        }
    }

    // Method to delete an item from the repository
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    // Method to delete all items from the repository
    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }


    fun setTotalReadingTime(time: Int) {
        _totalReadingTime.value = time
    }

    fun addReadingTime(time: Int) {
        val currentTime = _totalReadingTime.value ?: 0
        _totalReadingTime.value = currentTime + time
    }
}
