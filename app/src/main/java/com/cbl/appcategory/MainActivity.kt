package com.cbl.appcategory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbl.appcategory.common.Constants
import com.cbl.appcategory.common.safeLet
import com.cbl.appcategory.listing.AppListingAdaptor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val listViewModel: ListViewModel by viewModels()

    private var visibleItemCount: Int = 0
    private var totalItemCount: Int = 0
    private var pastVisibleItems: Int = 0
    private var mLayoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUIElement()
        initObserver()
    }

    private fun initUIElement() {
        mLayoutManager = LinearLayoutManager(this)

        rv_app_info.layoutManager = mLayoutManager
        rv_app_info.adapter = AppListingAdaptor()
        rv_app_info.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { //check for scroll down
                    visibleItemCount = mLayoutManager?.childCount ?: 0
                    totalItemCount = mLayoutManager?.itemCount ?: 0
                    pastVisibleItems = mLayoutManager?.findFirstVisibleItemPosition() ?: 0

                    if (listViewModel.isLoadingLiveData.value != true && listViewModel.searchKeyword.value.isNullOrEmpty()) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            listViewModel.getNextTopAppListDetail()
                        }
                    }
                }
            }

        })

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //do nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.apply {
                    listViewModel.updateSearch(this)
                }
            }
        }
        et_search_input?.addTextChangedListener(textWatcher)

        srl_main.setOnRefreshListener {
            listViewModel.refresh()
        }
    }

    private fun initObserver() {
        listViewModel.isLoadingLiveData.observe(this) {
            if (it) {
                (rv_app_info?.adapter as? AppListingAdaptor)?.setFooterMsg(getString(R.string.loading))
            } else {
                srl_main.isRefreshing = false
            }
        }

        listViewModel.isErrorLiveData.observe(this) {
            if (it) {
                (rv_app_info?.adapter as? AppListingAdaptor)?.setFooterMsg(getString(R.string.general_error_msg))
            }
        }

        listViewModel.appRecommendInfoDetailListData.observe(this) {
            (rv_app_info?.adapter as? AppListingAdaptor)?.setRecommendListData(it)
        }

        listViewModel.appInfoDetailListData.observe(this) {
            (rv_app_info?.adapter as? AppListingAdaptor)?.setAppTopListData(it)
        }

        listViewModel.searchKeyword.observe(this) { keyword ->
            if (keyword.isNullOrEmpty()) {
                safeLet(
                    listViewModel.appInfoDetailListData.value,
                    listViewModel.appRecommendInfoDetailListData.value
                ) { topList, recommendList ->
                    (rv_app_info?.adapter as? AppListingAdaptor)?.setListData(
                        topList,
                        recommendList
                    )
                }
            } else {
                safeLet(
                    listViewModel.appInfoDetailListData.value?.filter {
                        it.description?.contains(keyword, true) == true ||
                                it.trackCensoredName?.contains(keyword, true) == true ||
                                it.artistName?.contains(
                                    keyword,
                                    true
                                ) == true || it.genres.contains(
                            keyword
                        )
                    },
                    listViewModel.appRecommendInfoDetailListData.value?.filter {
                        it.description?.contains(keyword, true) == true ||
                                it.trackCensoredName?.contains(keyword, true) == true ||
                                it.artistName?.contains(
                                    keyword,
                                    true
                                ) == true || it.genres.contains(
                            keyword
                        )
                    }
                ) { topList, recommendList ->
                    (rv_app_info?.adapter as? AppListingAdaptor)?.apply {
                        setFooterMsg(Constants.EMPTY)
                        setListData(topList, recommendList)
                    }
                }
            }
        }
    }
}