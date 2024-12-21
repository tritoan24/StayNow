package com.ph32395.staynow_datn.hieunt.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Context.goToSetting(activity: Activity) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts(
        "package",
        applicationContext.packageName,
        null
    )
    intent.data = uri
    startActivity(intent)
}

fun Context.goToWifiSetting(activity: Activity) {
    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
