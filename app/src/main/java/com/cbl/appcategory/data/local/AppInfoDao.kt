package com.cbl.appcategory.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.cbl.appcategory.data.AppDetailInfo

@Dao
interface AppInfoDao {

    @Query("SELECT * FROM AppDetailInfo")
    fun getAll(): List<AppDetailInfo>

    @Insert
    fun insertAll(list: List<AppDetailInfo>)

    @Query("DELETE FROM AppDetailInfo")
    fun deleteAll()
}