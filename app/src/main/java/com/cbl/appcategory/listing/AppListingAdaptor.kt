package com.cbl.appcategory.listing

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbl.appcategory.R
import com.cbl.appcategory.common.safeLet
import com.cbl.appcategory.data.AppDetailInfo
import com.cbl.appcategory.recommend.RecommendRecyclerAdaptor
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder


class AppListingAdaptor : RecyclerView.Adapter<AppListingAdaptor.BaseAppListingViewHolder>() {

    private val recommendModels = mutableListOf<AppDetailInfo>()
    private val appTopModels = mutableListOf<AppDetailInfo>()
    private var footerMsg: CharSequence? = null

    private companion object {
        const val VT_TITLE = 0x000
        const val VT_RECOMMEND_LIST = 0x001
        const val VT_ODD_ITEM = 0x002
        const val VT_EVEN_ITEM = 0x003

    }

    fun setFooterMsg(label: CharSequence?) {
        this.footerMsg = label
        this.notifyItemChanged(itemCount - 1)
    }

    fun setRecommendListData(list: List<AppDetailInfo>) {
        this.recommendModels.clear()
        this.recommendModels.addAll(list)
        this.notifyItemChanged(0)
    }

    fun setAppTopListData(list: List<AppDetailInfo>) {
        this.appTopModels.clear()
        this.appTopModels.addAll(list)
        this.appTopModels.size.let {
            if (it > 0) {
                this.notifyItemRangeChanged(1, it + 1)
            } else {
                this.notifyDataSetChanged()
            }
        }
    }

    fun setListData(topList: List<AppDetailInfo>, recommendList: List<AppDetailInfo>) {
        this.appTopModels.clear()
        this.appTopModels.addAll(topList)
        this.recommendModels.clear()
        this.recommendModels.addAll(recommendList)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAppListingViewHolder {
        return when (viewType) {
            VT_TITLE -> {
                TitleViewHolder(parent)
            }
            VT_RECOMMEND_LIST -> {
                HorizontalRecyclerViewHolder(parent)
            }
            VT_ODD_ITEM -> {
                AppOddDetailItemViewHolder(parent)
            }
            VT_EVEN_ITEM -> {
                AppEvenDetailItemViewHolder(parent)
            }
            else -> throw Exception("Invalid View Type in ${this.javaClass.simpleName}")
        }
    }

    override fun getItemCount(): Int {
        return this.appTopModels.size + 2
    }

    override fun onBindViewHolder(holder: BaseAppListingViewHolder, position: Int) {
        when (holder) {
            is HorizontalRecyclerViewHolder -> {
                holder.setDate(this.recommendModels)
            }
            is AppOddDetailItemViewHolder -> {
                val index = position - 1
                holder.setData(this.appTopModels.getOrNull(index), index + 1)
            }
            is AppEvenDetailItemViewHolder -> {
                val index = position - 1
                holder.setData(this.appTopModels.getOrNull(index), index + 1)
            }
            is TitleViewHolder -> {
                if (position > 100) {
                    //return all done
                    holder.setData(R.string.loaded_all)
                } else {
                    //show loading or error msg
                    holder.setData(footerMsg)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> VT_RECOMMEND_LIST
            position == (itemCount - 1) -> VT_TITLE
            (position + 1) % 2 == 0 -> VT_EVEN_ITEM
            else -> VT_ODD_ITEM
        }
    }

    class TitleViewHolder(parent: ViewGroup) : BaseAppListingViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_app_list_title_item, parent, false
        )
    ) {
        private val tvLabel = itemView.findViewById<TextView>(R.id.tv_label)

        fun setData(label: CharSequence?) {
            tvLabel.text = label
        }

        fun setData(labelResId: Int) {
            tvLabel.setText(labelResId)
        }
    }

    class HorizontalRecyclerViewHolder(parent: ViewGroup) : BaseAppListingViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_app_list_horizontal_rv_item, parent, false
        )
    ) {
        private val rv = itemView.findViewById<RecyclerView>(R.id.rv_recommend)
        private val tvLabel = itemView.findViewById<TextView>(R.id.tv_label)

        init {
            tvLabel.text = ""
            rv.layoutManager =
                LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
            rv.adapter = RecommendRecyclerAdaptor()
        }

        fun setDate(list: List<AppDetailInfo>) {
            (rv.adapter as? RecommendRecyclerAdaptor)?.setDate(list)
            if (list.isEmpty()) {
                tvLabel.text = ""
            } else {
                tvLabel.setText(R.string.recommend)
            }
        }
    }

    class AppEvenDetailItemViewHolder(parent: ViewGroup) : BaseAppListingViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_app_list_even_item, parent, false
        )
    ) {
        private val sdIcon: SimpleDraweeView = itemView.findViewById(R.id.sd_icon)
        private val tvSeq: TextView = itemView.findViewById(R.id.tv_seq)
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
        private val tvSubLabel: TextView = itemView.findViewById(R.id.tv_sub_label)

        //        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvReviewUser: TextView = itemView.findViewById(R.id.tv_review_user)
        private val ivStar0: ImageView = itemView.findViewById(R.id.iv_star_0)
        private val ivStar1: ImageView = itemView.findViewById(R.id.iv_star_1)
        private val ivStar2: ImageView = itemView.findViewById(R.id.iv_star_2)
        private val ivStar3: ImageView = itemView.findViewById(R.id.iv_star_3)
        private val ivStar4: ImageView = itemView.findViewById(R.id.iv_star_4)

        fun setData(appDetailInfo: AppDetailInfo?, seq: Int) {
            tvSeq.text = seq.toString()
            appDetailInfo?.let {
                tvLabel.text = it.trackCensoredName
                tvSubLabel.text = it.genres.getOrNull(0)

                it.averageUserRating?.subSequence(0, 4)?.toString()?.toFloatOrNull()?.let {
                    updateRating(ivStar0, it, 0f)
                    updateRating(ivStar1, it, 1f)
                    updateRating(ivStar2, it, 2f)
                    updateRating(ivStar3, it, 3f)
                    updateRating(ivStar4, it, 4f)
                }

                tvReviewUser.text = "(${getKMT(it.userRatingCount)})"
                safeLet(it.artworkUrl60, it.artworkUrl512) { safe60, safe521 ->
                    val lowResRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(safe60))
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(safe521))
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    sdIcon.controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(sdIcon.controller)
                        .setImageRequest(request)
                        .setLowResImageRequest(lowResRequest)
                        .setImageOriginListener { controllerId, imageOrigin, successful, ultimateProducerName ->
                            sdIcon.post {
                                if (successful) {
                                    sdIcon.contentDescription = safe521
                                } else {
                                    sdIcon.contentDescription = "error"
                                }
                            }
                        }
                        .build()
                }
            }
        }
    }

    class AppOddDetailItemViewHolder(parent: ViewGroup) : BaseAppListingViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_app_list_odd_item, parent, false
        )
    ) {
        private val sdIcon: SimpleDraweeView = itemView.findViewById(R.id.sd_icon)
        private val tvSeq: TextView = itemView.findViewById(R.id.tv_seq)
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
        private val tvSubLabel: TextView = itemView.findViewById(R.id.tv_sub_label)

        //        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvReviewUser: TextView = itemView.findViewById(R.id.tv_review_user)
        private val ivStar0: ImageView = itemView.findViewById(R.id.iv_star_0)
        private val ivStar1: ImageView = itemView.findViewById(R.id.iv_star_1)
        private val ivStar2: ImageView = itemView.findViewById(R.id.iv_star_2)
        private val ivStar3: ImageView = itemView.findViewById(R.id.iv_star_3)
        private val ivStar4: ImageView = itemView.findViewById(R.id.iv_star_4)

        fun setData(appDetailInfo: AppDetailInfo?, seq: Int) {
            tvSeq.text = seq.toString()
            appDetailInfo?.let {
                tvLabel.text = it.trackCensoredName
                tvSubLabel.text = it.genres.getOrNull(0)
                it.averageUserRating?.subSequence(0, 4)?.toString()?.toFloatOrNull()?.let {
                    updateRating(ivStar0, it, 0f)
                    updateRating(ivStar1, it, 1f)
                    updateRating(ivStar2, it, 2f)
                    updateRating(ivStar3, it, 3f)
                    updateRating(ivStar4, it, 4f)
                }
                tvReviewUser.text = "(${getKMT(it.userRatingCount)})"
                safeLet(it.artworkUrl60, it.artworkUrl512) { safe60, safe521 ->
                    val lowResRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(safe60))
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(safe521))
                        .setProgressiveRenderingEnabled(true)
                        .build()
                    sdIcon.controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(sdIcon.controller)
                        .setImageRequest(request)
                        .setLowResImageRequest(lowResRequest)
                        .setImageOriginListener { controllerId, imageOrigin, successful, ultimateProducerName ->
                            sdIcon.post {
                                if (successful) {
                                    sdIcon.contentDescription = safe521
                                } else {
                                    sdIcon.contentDescription = "error"
                                }
                            }
                        }
                        .build()
                }
            }
        }

    }

    abstract class BaseAppListingViewHolder(parent: View) : RecyclerView.ViewHolder(parent) {

        protected fun updateRating(iv: ImageView, score: Float, div: Float) {
            when {
                score > (1 + div) -> {
                    iv.setImageResource(R.drawable.ic_full_star);
                }
                score >= (0.5f + div) -> {
                    iv.setImageResource(R.drawable.ic_half_star);
                }
                else -> {
                    iv.setImageResource(R.drawable.ic_holo_star);
                }
            }
        }

        protected fun getKMT(value: String?): String? {
            value?.toIntOrNull()?.let {
                return when {
                    it >= 100000000 -> {
                        "${it / 1000000000}T"
                    }
                    it >= 1000000 -> {
                        "${it / 1000000}M"
                    }
                    it >= 1000 -> {
                        "${it / 1000}k"
                    }
                    else -> {
                        value
                    }
                }
            } ?: run {
                return value
            }
        }
    }

}