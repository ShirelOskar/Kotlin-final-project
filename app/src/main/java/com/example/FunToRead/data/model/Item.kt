// data/model/Item
package com.example.FunToRead.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

    @Parcelize
@Entity(tableName = "items")
    data class Item(

        @PrimaryKey(autoGenerate = true) var id: Int = 0,

        @ColumnInfo(name = "title")
        val title:String, // Title of the item

        @ColumnInfo(name = "content_desc")
        val description:String? = null, // Description of the item (optional)

        @ColumnInfo(name = "status")
        val status: String, // Status of the item (e.g., "to read" or "already read")

        @ColumnInfo(name = "rating")
        val rating: Float? = null, // Rating of the item (optional)

        @ColumnInfo(name = "review")
        val review: String? = null, // Review of the item (optional)

        @ColumnInfo(name = "image")
        val photo: String?, // URI of the item's photo

        @ColumnInfo(name = "playlist")
        val playlist: List<String>? = null // List of URLs associated with the item (optional)

    ) : Parcelable { // Parcelable implementation to allow passing Item instances between components

        // Function to add a URL to the playlist and return a new Item instance with the updated playlist
        fun addToPlaylist(url: String): Item {
            val updatedPlaylist = playlist?.toMutableList()
            updatedPlaylist?.add(url)
            return copy(playlist = updatedPlaylist)
        }
        
    }
