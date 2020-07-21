package com.cbl.appcategory.data.network

import com.cbl.appcategory.data.AppDetailInfoRes
import com.cbl.appcategory.data.AppInfoRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AppStoreServerInterface {

    @GET("/hk/rss/topfreeapplications/limit=100/json")
    fun getAppTop100List(): Call<AppInfoRes>

    @GET("/hk/lookup")
    fun getAppDetail(@Query("id") id: String): Call<AppDetailInfoRes>

    @GET("/hk/rss/topgrossingapplications/limit=10/json")
    fun getAppRecommendList(): Call<AppInfoRes>
}

