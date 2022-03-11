package com.beat.core.data.storage

import android.content.Context
import androidx.room.Room
import com.beat.core.data.storage.database.AppDatabase

class DatabaseClient constructor(context: Context) {

    private val appDatabase: AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "BeatMusic").build()

    fun getAppDatabase() = appDatabase

}