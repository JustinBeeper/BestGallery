package com.eagle.gallery.pro.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.eagle.commons.activities.BaseSimpleActivity
import com.eagle.commons.extensions.beGoneIf
import com.eagle.commons.extensions.beVisibleIf
import com.eagle.commons.extensions.setupDialogStuff
import com.eagle.commons.views.MyGridLayoutManager
import com.eagle.gallery.pro.R
import com.eagle.gallery.pro.adapters.MediaAdapter
import com.eagle.gallery.pro.asynctasks.GetMediaAsynctask
import com.eagle.gallery.pro.extensions.config
import com.eagle.gallery.pro.extensions.getCachedMedia
import com.eagle.gallery.pro.helpers.SHOW_ALL
import com.eagle.gallery.pro.helpers.VIEW_TYPE_GRID
import com.eagle.gallery.pro.models.Medium
import com.eagle.gallery.pro.models.ThumbnailItem
import kotlinx.android.synthetic.main.dialog_medium_picker.view.*

class PickMediumDialog(val activity: BaseSimpleActivity, val path: String, val callback: (path: String) -> Unit) {
    var dialog: AlertDialog
    var shownMedia = ArrayList<ThumbnailItem>()
    val view = activity.layoutInflater.inflate(R.layout.dialog_medium_picker, null)
    val viewType = activity.config.getFolderViewType(if (activity.config.showAll) SHOW_ALL else path)
    var isGridViewType = viewType == VIEW_TYPE_GRID

    init {
        (view.media_grid.layoutManager as MyGridLayoutManager).apply {
            orientation = if (activity.config.scrollHorizontally && isGridViewType) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            spanCount = if (isGridViewType) activity.config.mediaColumnCnt else 1
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.other_folder) { dialogInterface, i -> showOtherFolder() }
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.select_photo)
                }

        activity.getCachedMedia(path) {
            val media = it.filter { it is Medium && !it.isVideo() } as ArrayList
            if (media.isNotEmpty()) {
                activity.runOnUiThread {
                    gotMedia(media)
                }
            }
        }

        com.eagle.gallery.pro.asynctasks.GetMediaAsynctask(activity, path, true, false, false) {
            gotMedia(it)
        }.execute()
    }

    private fun showOtherFolder() {
        PickDirectoryDialog(activity, path, true) {
            callback(it)
            dialog.dismiss()
        }
    }

    private fun gotMedia(media: ArrayList<ThumbnailItem>) {
        if (media.hashCode() == shownMedia.hashCode())
            return

        shownMedia = media
        val adapter = com.eagle.gallery.pro.adapters.MediaAdapter(activity, shownMedia.clone() as ArrayList<ThumbnailItem>, null, true, false, path, view.media_grid, null) {
            if (it is Medium) {
                callback(it.path)
                dialog.dismiss()
            }
        }

        val scrollHorizontally = activity.config.scrollHorizontally && isGridViewType
        val sorting = activity.config.getFileSorting(if (path.isEmpty()) SHOW_ALL else path)
        view.apply {
            media_grid.adapter = adapter

            media_vertical_fastscroller.isHorizontal = false
            media_vertical_fastscroller.beGoneIf(scrollHorizontally)

            media_horizontal_fastscroller.isHorizontal = true
            media_horizontal_fastscroller.beVisibleIf(scrollHorizontally)

            if (scrollHorizontally) {
                media_horizontal_fastscroller.allowBubbleDisplay = activity.config.showInfoBubble
                media_horizontal_fastscroller.setViews(media_grid) {
                    media_horizontal_fastscroller.updateBubbleText((media[it] as? Medium)?.getBubbleText(sorting, activity) ?: "")
                }
            } else {
                media_vertical_fastscroller.allowBubbleDisplay = activity.config.showInfoBubble
                media_vertical_fastscroller.setViews(media_grid) {
                    media_vertical_fastscroller.updateBubbleText((media[it] as? Medium)?.getBubbleText(sorting, activity) ?: "")
                }
            }
        }
    }
}
