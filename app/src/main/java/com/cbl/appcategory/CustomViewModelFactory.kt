package com.cbl.appcategory

import android.net.ConnectivityManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cbl.appcategory.data.local.LocalRoomDatabase
import com.cbl.appcategory.data.network.AppStoreServerInterface
import retrofit2.Retrofit

class CustomViewModelFactory(
    private val retrofit: Retrofit,
    private val db: LocalRoomDatabase,
    private val connectivityManager: ConnectivityManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ListViewModel::class.java) -> {
                return ListViewModel(
                    repo = ListRepo(
                        service = this.retrofit.create<AppStoreServerInterface>(AppStoreServerInterface::class.java),
                        db = this.db,
                        connectivityManager = this.connectivityManager
                    )
                ) as T
            }
            else -> {
                throw Exception("Invalid ViewModel")
            }
        }
    }
}