package com.cbl.appcategory

import android.accounts.NetworkErrorException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.cbl.appcategory.data.AppDetailInfo
import com.cbl.appcategory.data.AppDetailInfoRes
import com.cbl.appcategory.data.AppInfo
import com.cbl.appcategory.data.AppInfoRes
import com.cbl.appcategory.data.local.LocalRoomDatabase
import com.cbl.appcategory.data.network.AppStoreServerInterface
import retrofit2.Response
import retrofit2.awaitResponse

enum class ListType {
    TOP_LIST,
    RECOMMEND_LIST
}

data class AppInfoDetails(val listType: ListType, val list: List<AppDetailInfo>)

open class ListRepo(
    private val service: AppStoreServerInterface,
    private val db: LocalRoomDatabase,
    private val connectivityManager: ConnectivityManager
) {


    suspend fun getTopAppInfoDetail(idList: List<String>?): Result<AppInfoDetails> =
        getAppInfoDetail(idList, ListType.TOP_LIST)

    suspend fun getRecommendAppInfoDetail(idList: List<String>?): Result<AppInfoDetails> =
        getAppInfoDetail(idList, ListType.RECOMMEND_LIST)


    private suspend fun getAppInfoDetail(
        idList: List<String>?,
        listType: ListType
    ): Result<AppInfoDetails> {
        if (idList.isNullOrEmpty()) {
            return Result.failure(Exception())
        }

        if (isNetworkAvailable()) {
            val response: Response<AppDetailInfoRes> =
                service.getAppDetail(idList.joinToString(separator = ",")).awaitResponse()
            response.body()?.results?.let { appDetailInfoList ->
                val resultList = mutableListOf<AppDetailInfo>()
                idList.forEach { id ->
                    val item =
                        appDetailInfoList.mapNotNull { if (it.trackId == id) it else null }
                            .getOrNull(0)
                    if (item != null) {
                        resultList.add(item)
                    } else {
                        resultList.add(
                            AppDetailInfo(
                                trackId = id,
                                artworkUrl60 = null,
                                artworkUrl512 = null,
                                trackCensoredName = null,
                                averageUserRating = null,
                                userRatingCount = null,
                                description = null,
                                artistName = null,
                                genres = listOf()
                            )
                        )
                    }
                }
                if (listType == ListType.TOP_LIST) {
                    cacheAppDetailInfo(resultList)
                }
                return Result.success(
                    AppInfoDetails(
                        listType = listType,
                        list = resultList
                    )
                )
            } ?: run {
                return Result.failure(Exception())
            }

        } else {
            db.appDetailInfoResDao().getAll().apply {
                return if (isNotEmpty()) {
                    Result.success(AppInfoDetails(listType = listType, list = this))
                } else {
                    Result.failure(Exception())
                }
            }
        }
    }

    suspend fun getAppTopList(): Result<List<AppInfo>> {
        if (isNetworkAvailable()) {
            val response: Response<AppInfoRes> = service.getAppTop100List()
                .awaitResponse()
            if (response.isSuccessful) {
                response.body()?.feed?.entry?.let {
                    return Result.success(it)
                }
            } else {
                return Result.failure(Exception())
            }
        }
        return Result.failure(NetworkErrorException())
    }

    suspend fun getAppRecommendList(): Result<List<AppInfo>> {
        if (isNetworkAvailable()) {
            val response = service.getAppRecommendList()
                .awaitResponse()
            if (response.isSuccessful) {
                response.body()?.feed?.entry?.let {
                    return Result.success(it)
                }
            } else {
                return Result.failure(Exception())
            }
        }
        return Result.failure(NetworkErrorException())
    }

    private fun cacheAppDetailInfo(list: List<AppDetailInfo>) {
        db.queryExecutor.execute {
            db.appDetailInfoResDao().deleteAll()
            db.appDetailInfoResDao().insertAll(list)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: run {
                return false
            }
        val result = when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }

}