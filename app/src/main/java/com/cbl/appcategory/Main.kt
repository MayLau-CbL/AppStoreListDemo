package com.cbl.appcategory

import android.app.Application
import androidx.room.Room
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.data.local.LocalRoomDatabase
import com.facebook.drawee.backends.pipeline.Fresco
import dagger.hilt.android.HiltAndroidApp
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

@HiltAndroidApp
class Main : Application() {
    @Inject
    lateinit var retrofit: Retrofit
    @Inject
    lateinit var db: LocalRoomDatabase


    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
    }
}