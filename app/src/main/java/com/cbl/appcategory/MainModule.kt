package com.cbl.appcategory

import android.content.Context
import androidx.room.Room
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.data.local.LocalRoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun providerLocalRoomDatabase(
        @ApplicationContext context: Context
    ): LocalRoomDatabase{
        return Room.databaseBuilder(
            context,
            LocalRoomDatabase::class.java, Constants.DB_NAME
        ).build()
    }
}