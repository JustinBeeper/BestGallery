package com.eagle.gallery.pro.dialogs

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.eagle.commons.activities.BaseSimpleActivity
import com.eagle.commons.extensions.setupDialogStuff
import com.eagle.commons.extensions.showKeyboard
import com.eagle.commons.extensions.value
import com.eagle.gallery.pro.R
import kotlinx.android.synthetic.main.dialog_custom_aspect_ratio.view.*

class CustomAspectRatioDialog(val activity: BaseSimpleActivity, val defaultCustomAspectRatio: Pair<Int, Int>?, val callback: (aspectRatio: Pair<Int, Int>) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_custom_aspect_ratio, null).apply {
            aspect_ratio_width.setText(defaultCustomAspectRatio?.first?.toString() ?: "")
            aspect_ratio_height.setText(defaultCustomAspectRatio?.second?.toString() ?: "")
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        showKeyboard(view.aspect_ratio_width)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val width = getViewValue(view.aspect_ratio_width)
                            val height = getViewValue(view.aspect_ratio_height)
                            callback(Pair(width, height))
                            dismiss()
                        }
                    }
                }
    }

    private fun getViewValue(view: EditText): Int {
        val textValue = view.value
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}
