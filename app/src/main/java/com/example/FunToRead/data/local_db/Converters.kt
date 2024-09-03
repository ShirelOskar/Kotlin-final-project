//data/localdb/Converters
package com.example.FunToRead.data.local_db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


  //Provides TypeConverter methods for converting custom types to and from their database representation.

object Converters {
    // An empty list to be used when a nullable list is provided.
    private val emptyList: List<String> = emptyList()

    /**
      Converts a JSON string into a List of Strings.
      This method is used to read data from the database and convert it to a usable List.
      @param value The JSON string representing a List of Strings.
      @return The List of Strings, or null if the input string is null.
     */
    @TypeConverter
    @JvmStatic
    fun fromStringList(value: String?): List<String>? {
        if (value == null) {
            return null
        }
        // Define the type of the list for Gson to deserialize
        val listType = object : TypeToken<List<String>>() {}.type
        // Use Gson to convert the JSON string to a List of Strings
        return Gson().fromJson(value, listType)
    }

    /**
      Converts a List of Strings into a JSON string.
      This method is used to write data to the database and store it as a JSON string.
      @param list The List of Strings to be converted to JSON.
      @return The JSON string representing the List of Strings.
     */
    @TypeConverter
    @JvmStatic
    fun toStringList(list: List<String>?): String {
        // Use Gson to convert the List of Strings to a JSON string
        return Gson().toJson(list ?: emptyList)
    }
}
