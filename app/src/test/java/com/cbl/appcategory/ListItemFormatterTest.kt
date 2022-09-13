package com.cbl.appcategory

import android.view.View
import com.cbl.appcategory.listing.AppListingAdaptor
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class ListItemFormatterTest {

    private lateinit var baseAppListingViewHolder: MockBaseAppListingViewHolder

    @Before
    fun init() {
        val view = mock(View::class.java)
        baseAppListingViewHolder = MockBaseAppListingViewHolder(view)
    }

    @Test
    fun test_review_kMT_formatter_null() {
        MatcherAssert.assertThat(
            baseAppListingViewHolder.openGetKMB(null),
            CoreMatchers.nullValue()
        )
    }

    @Test
    fun test_review_kMT_formatter_empty() {
        val value = ""
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo(value))
    }

    @Test
    fun test_review_kMT_formatter_zero() {
        val value = "0"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo(value))
    }

    @Test
    fun test_review_kMT_formatter_k() {
        val value = "1000"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1k"))
    }

    @Test
    fun test_review_kMT_formatter_k_round_down() {
        val value = "1999"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1k"))
    }

    @Test
    fun test_review_kMT_formatter_M() {
        val value = "1000000"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1M"))
    }

    @Test
    fun test_review_kMT_formatter_M_round_down() {
        val value = "1999999"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1M"))
    }

    @Test
    fun test_review_kMT_formatter_B() {
        val value = "1000000000"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1B"))
    }

    @Test
    fun test_review_kMT_formatter_B_round_down() {
        val value = "1999999999"
        MatcherAssert.assertThat(baseAppListingViewHolder.openGetKMB(value), equalTo("1B"))
    }

    class MockBaseAppListingViewHolder(view: View) :
        AppListingAdaptor.BaseAppListingViewHolder(view) {
        fun openGetKMB(value: String?): String? {
            return getKMB(value)
        }
    }

}