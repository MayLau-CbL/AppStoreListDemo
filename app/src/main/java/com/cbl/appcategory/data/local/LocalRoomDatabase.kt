package com.cbl.appcategory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cbl.appcategory.data.AppDetailInfo

@Database(entities = [AppDetailInfo::class], version = 2)
@TypeConverters(Converters::class)
abstract class LocalRoomDatabase : RoomDatabase() {
    abstract fun appDetailInfoResDao(): AppInfoDao
}