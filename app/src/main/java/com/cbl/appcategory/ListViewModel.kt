package com.cbl.appcategory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.data.AppDetailInfo
import com.cbl.appcategory.data.AppInfo

class ListViewModel(private val repo: ListRepo) : ViewModel(), IListRepoListener {
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
        repo.listRepoListener = this
        appInfoListData.value = mutableListOf<AppInfo>()
        appRecommendInfoListData.value = mutableListOf<AppInfo>()
        appInfoDetailListData.value = mutableListOf<AppDetailInfo>()
        appRecommendInfoDetailListData.value = mutableListOf<AppDetailInfo>()
        isLoadingLiveData.value = false
        isErrorLiveData.value = false
        searchKeyword.value = Constants.EMPTY
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
                }else{
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
        repo.getAppTopList()
    }

    private fun fetchAppRecommendInfoList() {
        repo.getAppRecommendList()
    }

    private fun fetchAppTopInfoDetail(list: List<AppInfo>?) {
        if (isLoadingLiveData.value != true) {
            isLoadingLiveData.value = true
            repo.getTopAppInfoDetail(list?.map { it.id.attributes.id })
        }
    }

    private fun fetchAppRecommendInfoDetail(list: List<AppInfo>) {
        repo.getRecommendAppInfoDetail(list.map { it.id.attributes.id })
    }

    /**
     * Listener
     */
    override fun getTopAppList(list: List<AppInfo>) {
        appInfoListData.value = appInfoListData.value?.apply {
            clear()
            addAll(list)
        }
        isLoadingLiveData.value = false
        isErrorLiveData.value = false
        fetchAppTopInfoDetail(list.subList(0, PAGE_SIZE))
    }

    override fun getRecommendAppList(list: List<AppInfo>) {
        appRecommendInfoListData.value = appRecommendInfoListData.value?.apply {
            clear()
            addAll(list)
        }
        fetchAppRecommendInfoDetail(list)
    }

    override fun getTopAppInfoDetailList(list: List<AppDetailInfo>) {
        appInfoDetailListData.postValue(appInfoDetailListData.value?.apply {
            addAll(list)
        })
        isLoadingLiveData.postValue(false)
        isErrorLiveData.postValue(false)
    }

    override fun getRecommendAppInfoDetailList(list: List<AppDetailInfo>) {
        appRecommendInfoDetailListData.value = appRecommendInfoDetailListData.value?.apply {
            addAll(list)
        }
    }

    override fun networkFail() {
        isLoadingLiveData.value = false
        fetchAppTopInfoDetail(null)
    }

    override fun error() {
        isLoadingLiveData.postValue(false)
        isErrorLiveData.postValue(true)
    }
}