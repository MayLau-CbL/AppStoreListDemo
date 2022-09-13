package com.cbl.appcategory

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.data.AppDetailInfo
import com.cbl.appcategory.data.AppInfo
import com.cbl.appcategory.data.network.AppStoreServerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
object ListViewModelModule {
    @Provides
    fun providerListRepo(
        @ApplicationContext context: Context
    ): ListRepo {
        val app = context as Main
        return ListRepo(
            service = app.retrofit.create(AppStoreServerInterface::class.java),
            db = app.db,
            connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        )
    }
}

@HiltViewModel
class ListViewModel @Inject constructor(private val repo: ListRepo) : ViewModel() {
    private companion object {
        const val PAGE_SIZE = 10
        const val TTL_PAGE = 100
    }

    private val appInfoListData = MutableLiveData<MutableList<AppInfo>>()
    private val appRecommendInfoListData = MutableLiveData<MutableList<AppInfo>>()

    val appInfoDetailListData = MutableLiveData<MutableList<AppDetailInfo>>()
    val appRecommendInfoDetailListData = MutableLiveData<MutableList<AppDetailInfo>>()

    val isLoadingLiveData = MutableLiveData<Boolean>()
    val isErrorLiveData = MutableLiveData<Boolean>()

    val searchKeyword = MutableLiveData<CharSequence>()

    init {
        appInfoListData.value = mutableListOf()
        appRecommendInfoListData.value = mutableListOf()
        appInfoDetailListData.value = mutableListOf()
        appRecommendInfoDetailListData.value = mutableListOf()
        isLoadingLiveData.value = false
        isErrorLiveData.value = false
        searchKeyword.value = Constants.EMPTY
        // refresh once when init finished
        refresh()
    }

    fun refresh() {
        appInfoDetailListData.value = appInfoDetailListData.value?.apply {
            clear()
        }
        appRecommendInfoDetailListData.value = appRecommendInfoDetailListData.value?.apply {
            clear()
        }
        fetchAppInfoList()
        fetchAppRecommendInfoList()
    }


    fun getNextTopAppListDetail() {
        val size = appInfoDetailListData.value?.size ?: 0
        if (size < TTL_PAGE) {
            appInfoListData.value?.apply {
                if (this.size >= size + PAGE_SIZE) {
                    fetchAppTopInfoDetail(subList(size, size + PAGE_SIZE))
                } else {
                    //if smaller, then it must be either using cache or list returned not correct
                    //ask user refresh whole list
                    error()
                }
            }
        }
    }

    fun updateSearch(keyword: CharSequence?) {
        searchKeyword.value = keyword
    }

    /**
     * fetch Data
     */
    private fun fetchAppInfoList() {
        isLoadingLiveData.value = true
        viewModelScope.launch {
            val result = repo.getAppTopList()
            if (result.isSuccess) {
                result.getOrNull()?.apply {
                    getTopAppList(this)
                }
            } else {
                if (result.exceptionOrNull() is NetworkErrorException) {
                    networkFail()
                } else {
                    error()
                }
            }
        }
    }

    private fun fetchAppRecommendInfoList() {
        viewModelScope.launch {
            val result = repo.getAppRecommendList()
            if (result.isSuccess) {
                result.getOrNull()?.apply {
                    getRecommendAppList(this)
                }
            } else {
                if (result.exceptionOrNull() is NetworkErrorException) {
                    networkFail()
                } else {
                    error()
                }
            }
        }
    }

    private fun fetchAppTopInfoDetail(list: List<AppInfo>?) {
        if (isLoadingLiveData.value != true) {
            isLoadingLiveData.value = true
            viewModelScope.launch {
                val result = repo.getTopAppInfoDetail(list?.map { it.id.attributes.id })
                if (result.isSuccess) {
                    result.getOrNull()?.apply {
                        getTopAppInfoDetailList(this.list)
                    }
                } else {
                    error()
                }
            }
        }
    }

    private fun fetchAppRecommendInfoDetail(list: List<AppInfo>) {
        viewModelScope.launch {
            val result = repo.getRecommendAppInfoDetail(list.map { it.id.attributes.id })
            if (result.isSuccess) {
                result.getOrNull()?.apply {
                    getRecommendAppInfoDetailList(this.list)
                }
            } else {
                error()
            }
        }
    }

    /**
     * Update UI Data
     */
    private fun getTopAppList(list: List<AppInfo>) {
        appInfoListData.value = appInfoListData.value?.apply {
            clear()
            addAll(list)
        }
        isLoadingLiveData.value = false
        isErrorLiveData.value = false
        fetchAppTopInfoDetail(list.subList(0, PAGE_SIZE))
    }

    private fun getRecommendAppList(list: List<AppInfo>) {
        appRecommendInfoListData.value = appRecommendInfoListData.value?.apply {
            clear()
            addAll(list)
        }
        fetchAppRecommendInfoDetail(list)
    }

    private fun getTopAppInfoDetailList(list: List<AppDetailInfo>) {
        appInfoDetailListData.postValue(appInfoDetailListData.value?.apply {
            addAll(list)
        })
        isLoadingLiveData.postValue(false)
        isErrorLiveData.postValue(false)
    }

    private fun getRecommendAppInfoDetailList(list: List<AppDetailInfo>) {
        appRecommendInfoDetailListData.value = appRecommendInfoDetailListData.value?.apply {
            addAll(list)
        }
    }

    private fun networkFail() {
        isLoadingLiveData.value = false
        fetchAppTopInfoDetail(null)
    }

    private fun error() {
        isLoadingLiveData.postValue(false)
        isErrorLiveData.postValue(true)
    }
}