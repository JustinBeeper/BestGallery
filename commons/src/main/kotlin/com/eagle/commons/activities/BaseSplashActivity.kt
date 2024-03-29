package com.eagle.commons.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eagle.commons.R
import com.eagle.commons.extensions.baseConfig
import com.eagle.commons.extensions.getSharedTheme
import com.eagle.commons.extensions.isThankYouInstalled
import com.eagle.commons.extensions.showSideloadingDialog
import com.eagle.commons.helpers.SIDELOADING_FALSE
import com.eagle.commons.helpers.SIDELOADING_TRUE
import com.eagle.commons.helpers.SIDELOADING_UNCHECKED

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    abstract fun getAppPackageName(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (baseConfig.appSideloadingStatus == SIDELOADING_UNCHECKED) {
            val isSideloaded = isAppSideloaded()
            baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE
            if (isSideloaded) {
                baseConfig.appId = getAppPackageName()
                showSideloadingDialog()
                return
            }
        } else if (baseConfig.appSideloadingStatus == SIDELOADING_TRUE) {
            showSideloadingDialog()
            return
        }

        if (isThankYouInstalled() && baseConfig.appRunCount == 0) {
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        appIconColor = it.appIconColor
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }

    private fun isAppSideloaded(): Boolean {
        return try {
            getDrawable(R.drawable.ic_camera)
            false
        } catch (e: Exception) {
            true
        }
    }
}
