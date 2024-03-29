package com.eagle.gallery.pro.dialogs

import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.eagle.commons.activities.BaseSimpleActivity
import com.eagle.commons.extensions.beVisibleIf
import com.eagle.commons.extensions.setupDialogStuff
import com.eagle.commons.helpers.*
import com.eagle.gallery.pro.R
import com.eagle.gallery.pro.extensions.config
import com.eagle.gallery.pro.helpers.SHOW_ALL
import kotlinx.android.synthetic.main.dialog_change_sorting.view.*

class ChangeSortingDialog(val activity: BaseSimpleActivity, val isDirectorySorting: Boolean, showFolderCheckbox: Boolean,
                          val path: String = "", val callback: () -> Unit) :
        DialogInterface.OnClickListener {
    private var currSorting = 0
    private var config = activity.config
    private var pathToUse = if (!isDirectorySorting && path.isEmpty()) SHOW_ALL else path
    private var view: View

    init {
        view = activity.layoutInflater.inflate(R.layout.dialog_change_sorting, null).apply {
            use_for_this_folder_divider.beVisibleIf(showFolderCheckbox)
            sorting_dialog_use_for_this_folder.beVisibleIf(showFolderCheckbox)
            sorting_dialog_bottom_note.beVisibleIf(!isDirectorySorting)
            sorting_dialog_use_for_this_folder.isChecked = config.hasCustomSorting(pathToUse)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.sort_by)
                }

        currSorting = if (isDirectorySorting) config.directorySorting else config.getFileSorting(pathToUse)
        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        val sortingRadio = view.sorting_dialog_radio_sorting

        val sortBtn = when {
            currSorting and SORT_BY_PATH != 0 -> sortingRadio.sorting_dialog_radio_path
            currSorting and SORT_BY_SIZE != 0 -> sortingRadio.sorting_dialog_radio_size
            currSorting and SORT_BY_DATE_MODIFIED != 0 -> sortingRadio.sorting_dialog_radio_last_modified
            currSorting and SORT_BY_DATE_TAKEN != 0 -> sortingRadio.sorting_dialog_radio_date_taken
            currSorting and SORT_BY_RANDOM != 0 -> sortingRadio.sorting_dialog_radio_random
            else -> sortingRadio.sorting_dialog_radio_name
        }
        sortBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        val orderRadio = view.sorting_dialog_radio_order
        var orderBtn = orderRadio.sorting_dialog_radio_ascending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = orderRadio.sorting_dialog_radio_descending
        }
        orderBtn.isChecked = true
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val sortingRadio = view.sorting_dialog_radio_sorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_name -> SORT_BY_NAME
            R.id.sorting_dialog_radio_path -> SORT_BY_PATH
            R.id.sorting_dialog_radio_size -> SORT_BY_SIZE
            R.id.sorting_dialog_radio_last_modified -> SORT_BY_DATE_MODIFIED
            R.id.sorting_dialog_radio_random -> SORT_BY_RANDOM
            else -> SORT_BY_DATE_TAKEN
        }

        if (view.sorting_dialog_radio_order.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        if (isDirectorySorting) {
            config.directorySorting = sorting
        } else {
            if (view.sorting_dialog_use_for_this_folder.isChecked) {
                config.saveFileSorting(pathToUse, sorting)
            } else {
                config.removeFileSorting(pathToUse)
                config.sorting = sorting
            }
        }
        callback()
    }
}
