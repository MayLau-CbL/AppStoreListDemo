package com.cbl.appcategory.recommend

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cbl.appcategory.R
import com.cbl.appcategory.common.safeLet
import com.cbl.appcategory.data.AppDetailInfo
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.ImageRequestBuilder

class RecommendRecyclerAdaptor :
    RecyclerView.Adapter<RecommendRecyclerAdaptor.RecommendAppItemViewHolder>() {
    private val models = mutableListOf<AppDetailInfo>()

    fun setData(list: List<AppDetailInfo>) {
        this.models.clear()
        this.models.addAll(list)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendAppItemViewHolder {
        return RecommendAppItemViewHolder(parent)
    }

    override fun getItemCount(): Int {
        return models.size
    }

    override fun onBindViewHolder(holder: RecommendAppItemViewHolder, position: Int) {
        holder.setData(this.models.getOrNull(position))
    }

    class RecommendAppItemViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.layout_recommend_list_item, parent, false
        )
    ) {
        private val sdIcon: SimpleDraweeView = itemView.findViewById(R.id.sd_icon)
        private val tvLabel: TextView = itemView.findViewById(R.id.tv_label)
        private val tvSubLabel: TextView = itemView.findViewById(R.id.tv_sub_label)
        fun setData(appDetailInfo: AppDetailInfo?) {
            appDetailInfo?.let {
                tvLabel.text = it.trackCensoredName
                tvSubLabel.text = it.genres.getOrNull(0)
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
}