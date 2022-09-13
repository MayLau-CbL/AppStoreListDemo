package com.cbl.appcategory

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.cbl.appcategory.data.AppDetailInfo
import com.cbl.appcategory.data.AppDetailInfoRes
import com.cbl.appcategory.data.AppInfo
import com.cbl.appcategory.data.AppInfoRes
import com.cbl.appcategory.data.local.LocalRoomDatabase
import com.cbl.appcategory.data.network.AppStoreServerInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface IListRepoListener {
    fun getTopAppList(list: List<AppInfo>)
    fun getRecommendAppList(list: List<AppInfo>)

    fun getTopAppInfoDetailList(list: List<AppDetailInfo>)
    fun getRecommendAppInfoDetailList(list: List<AppDetailInfo>)

    fun networkFail()
    fun error()
}

enum class ListType {
    TOP_LIST,
    RECOMMEND_LIST
}

open class ListRepo(
    private val service: AppStoreServerInterface,
    private val db: LocalRoomDatabase,
    private val connectivityManager: ConnectivityManager
) {

    var listRepoListener: IListRepoListener? = null

    fun getTopAppInfoDetail(idList: List<String>?) = getAppInfoDetail(idList, ListType.TOP_LIST)
    fun getRecommendAppInfoDetail(idList: List<String>?) =
        getAppInfoDetail(idList, ListType.RECOMMEND_LIST)


    private fun getAppInfoDetail(idList: List<String>?, listType: ListType) {
        if (isNetworkAvailable() && idList != null) {
            service.getAppDetail(idList.joinToString(separator = ","))
                .enqueue(object : Callback<AppDetailInfoRes> {
                    override fun onFailure(call: Call<AppDetailInfoRes>, t: Throwable) {
                        listRepoListener?.error()
                    }

                    override fun onResponse(
                        call: Call<AppDetailInfoRes>,
                        response: Response<AppDetailInfoRes>
                    ) {
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
                            when (listType) {
                                ListType.TOP_LIST -> {
                                    cacheAppDetailInfo(resultList)
                                    listRepoListener?.getTopAppInfoDetailList(resultList)
                                }
                                ListType.RECOMMEND_LIST -> {
                                    listRepoListener?.getRecommendAppInfoDetailList(resultList)
                                }

                            }

                        } ?: run {
                            listRepoListener?.error()
                        }
                    }
                })
        } else {
            if (idList.isNullOrEmpty()) {
                val thread = Thread {
                    when (listType) {
                        ListType.TOP_LIST -> {
                            db.appDetailInfoResDao().getAll().apply {
                                if (isNotEmpty()) {
                                    listRepoListener?.getTopAppInfoDetailList(this)
                                } else {
                                    listRepoListener?.error()
                                }
                            }

                        }
                        ListType.RECOMMEND_LIST -> {
                            db.appDetailInfoResDao().getAll().apply {
                                if (isNotEmpty()) {
                                    listRepoListener?.getRecommendAppInfoDetailList(this)
                                } else {
                                    listRepoListener?.error()
                                }
                            }
                        }

                    }
                }
                thread.start()
            } else {
                listRepoListener?.error()
            }
        }
    }

    fun getAppTopList() {
        if (isNetworkAvailable()) {
            service.getAppTop100List()
                .enqueue(object : Callback<AppInfoRes> {
                    override fun onFailure(call: Call<AppInfoRes>, t: Throwable) {
                        listRepoListener?.error()
                    }

                    override fun onResponse(
                        call: Call<AppInfoRes>,
                        response: Response<AppInfoRes>
                    ) {
                        response.body()?.feed?.entry?.let {
                            listRepoListener?.getTopAppList(it)
                        }
                    }
                })
        } else {
            listRepoListener?.networkFail()
        }
    }

    fun getAppRecommendList() {
        if (isNetworkAvailable()) {
            service.getAppRecommendList()
                .enqueue(object : Callback<AppInfoRes> {
                    override fun onFailure(call: Call<AppInfoRes>, t: Throwable) {
                        listRepoListener?.error()
                    }

                    override fun onResponse(
                        call: Call<AppInfoRes>,
                        response: Response<AppInfoRes>
                    ) {
                        response.body()?.feed?.entry?.let {
                            listRepoListener?.getRecommendAppList(it)
                        }
                    }
                })
        }
    }

    fun cacheAppDetailInfo(list: List<AppDetailInfo>) {
        val thread = Thread {
            db.appDetailInfoResDao().deleteAll()
            db.appDetailInfoResDao().insertAll(list)
        }
        thread.start()
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