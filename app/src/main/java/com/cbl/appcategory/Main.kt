package com.cbl.appcategory

import android.app.Application
import androidx.room.Room
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.data.local.LocalRoomDatabase
import com.facebook.drawee.backends.pipeline.Fresco
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Main : Application() {
    lateinit var retrofit: Retrofit
    lateinit var db: LocalRoomDatabase


    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        db = Room.databaseBuilder(
            applicationContext,
            LocalRoomDatabase::class.java, Constants.DB_NAME
        ).build()
    }
}