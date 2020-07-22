package com.cbl.appcategory.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * App List Res
 */
data class AppInfoRes(
    val feed:AppInfoEntry
)

data class AppInfoEntry(
    val entry: List<AppInfo>
)

data class AppInfo(
    @SerializedName("id")
    val id: LabelRes
)

data class LabelRes(
    val label: String,
    val attributes: AttributeRes
)

data class AttributeRes(
    @SerializedName("im:id")
    val id: String
)

/**
 * App Detail Res
 */
data class AppDetailInfoRes(
    val results:List<AppDetailInfo>
)

@Entity
data class AppDetailInfo(
    @PrimaryKey val trackId: String,
    @ColumnInfo(name = "artworkUrl60") val artworkUrl60: String?,
    @ColumnInfo(name = "artworkUrl512") val artworkUrl512: String?,
    @ColumnInfo(name = "trackCensoredName") val trackCensoredName: String?,
    @ColumnInfo(name = "averageUserRating") val averageUserRating: String?,
    @ColumnInfo(name = "userRatingCount") val userRatingCount: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "artistName") val artistName: String?,
    @ColumnInfo(name = "genres") val genres: List<String>
)