package com.eagle.commons.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.eagle.commons.R
import com.eagle.commons.adapters.PasswordTypesAdapter
import com.eagle.commons.extensions.*
import com.eagle.commons.helpers.PROTECTION_FINGERPRINT
import com.eagle.commons.helpers.PROTECTION_PATTERN
import com.eagle.commons.helpers.PROTECTION_PIN
import com.eagle.commons.helpers.SHOW_ALL_TABS
import com.eagle.commons.interfaces.HashListener
import com.eagle.commons.views.MyDialogViewPager
import kotlinx.android.synthetic.main.dialog_security.view.*

class SecurityDialog(val activity: Activity, val requiredHash: String, val showTabIndex: Int, val callback: (hash: String, type: Int, success: Boolean) -> Unit)
    : HashListener {
    var dialog: AlertDialog? = null
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_security, null)
    lateinit var tabsAdapter: PasswordTypesAdapter
    lateinit var viewPager: MyDialogViewPager

    init {
        view.apply {
            viewPager = findViewById(R.id.dialog_tab_view_pager)
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(context, requiredHash, this@SecurityDialog, dialog_scrollview)
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialog_tab_layout.getTabAt(it)!!.select()
            }

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = context.baseConfig.textColor

                if (!activity.isFingerPrintSensorAvailable())
                    dialog_tab_layout.removeTabAt(PROTECTION_FINGERPRINT)

                dialog_tab_layout.setTabTextColors(textColor, textColor)
                dialog_tab_layout.setSelectedTabIndicatorColor(context.baseConfig.primaryColor)
                dialog_tab_layout.onTabSelectionChanged(tabSelectedAction = {
                    viewPager.currentItem = when {
                        it.text.toString().equals(resources.getString(R.string.pattern), true) -> PROTECTION_PATTERN
                        it.text.toString().equals(resources.getString(R.string.pin), true) -> PROTECTION_PIN
                        else -> PROTECTION_FINGERPRINT
                    }
                    updateTabVisibility()
                })
            } else {
                dialog_tab_layout.beGone()
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }

        dialog = AlertDialog.Builder(activity)
                .setOnCancelListener { onCancelFail() }
                .setNegativeButton(R.string.cancel) { dialog, which -> onCancelFail() }
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }

    private fun onCancelFail() {
        callback("", 0, false)
        dialog!!.dismiss()
    }

    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type, true)
        dialog!!.dismiss()
    }

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }
}
