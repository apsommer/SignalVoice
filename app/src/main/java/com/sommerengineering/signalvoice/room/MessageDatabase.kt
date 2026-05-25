package com.sommerengineering.signalvoice.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}