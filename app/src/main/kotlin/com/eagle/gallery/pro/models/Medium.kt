package com.eagle.gallery.pro.models

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eagle.commons.extensions.formatDate
import com.eagle.commons.extensions.formatSize
import com.eagle.commons.extensions.getFilenameExtension
import com.eagle.commons.helpers.SORT_BY_DATE_MODIFIED
import com.eagle.commons.helpers.SORT_BY_NAME
import com.eagle.commons.helpers.SORT_BY_PATH
import com.eagle.commons.helpers.SORT_BY_SIZE
import com.eagle.gallery.pro.helpers.*
import java.io.Serializable
import java.util.*

@Entity(tableName = "media", indices = [(Index(value = ["full_path"], unique = true))])
data class Medium(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "filename") var name: String,
        @ColumnInfo(name = "full_path") var path: String,
        @ColumnInfo(name = "parent_path") var parentPath: String,
        @ColumnInfo(name = "last_modified") val modified: Long,
        @ColumnInfo(name = "date_taken") var taken: Long,
        @ColumnInfo(name = "size") val size: Long,
        @ColumnInfo(name = "type") val type: Int,
        @ColumnInfo(name = "video_duration") val videoDuration: Int,
        @ColumnInfo(name = "is_favorite") var isFavorite: Boolean,
        @ColumnInfo(name = "deleted_ts") var deletedTS: Long) : Serializable, ThumbnailItem() {

    companion object {
        private const val serialVersionUID = -6553149366975655L
    }

    fun isGIF() = type == TYPE_GIFS

    fun isImage() = type == TYPE_IMAGES

    fun isVideo() = type == TYPE_VIDEOS

    fun isRaw() = type == TYPE_RAWS

    fun isSVG() = type == TYPE_SVGS

    fun isHidden() = name.startsWith('.')

    fun getBubbleText(sorting: Int, context: Context) = when {
        sorting and SORT_BY_NAME != 0 -> name
        sorting and SORT_BY_PATH != 0 -> path
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(context)
        else -> taken.formatDate(context)
    }

    fun getGroupingKey(groupBy: Int): String {
        return when {
            groupBy and GROUP_BY_LAST_MODIFIED != 0 -> getDayStartTS(modified)
            groupBy and GROUP_BY_DATE_TAKEN != 0 -> getDayStartTS(taken)
            groupBy and GROUP_BY_FILE_TYPE != 0 -> type.toString()
            groupBy and GROUP_BY_EXTENSION != 0 -> name.getFilenameExtension().toLowerCase()
            groupBy and GROUP_BY_FOLDER != 0 -> parentPath
            else -> ""
        }
    }

    fun getIsInRecycleBin() = deletedTS != 0L

    private fun getDayStartTS(ts: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = ts
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis.toString()
    }
}
