package com.ph32395.staynow.hieunt.widget

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

fun AppCompatActivity.changeStatusBarColor(@ColorRes color: Int, lightStatusBar: Boolean = false) {
    if (window != null) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, color)
    }
    if (lightStatusBar)
        this.lightStatusBar()

}

fun AppCompatActivity.lightStatusBar() {
    val decorView: View? = this.window?.decorView
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wic = decorView?.windowInsetsController
        wic?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else
        decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

//get multiple permissions
fun AppCompatActivity.callMultiplePermissions(
    callbackPermission: (Boolean) -> Unit
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { callback ->
        callbackPermission.invoke(!callback.containsValue(false))
    }
}

//start activity
fun AppCompatActivity.launchActivity(
    clazz: Class<*>
) {
    val intent = Intent(this, clazz)
    val option = Bundle()
    option.putString("last_activity", clazz.name)
    intent.putExtra("data_bundle", option)
    startActivity(intent)
}

fun AppCompatActivity.launchActivity(
    option: Bundle,
    clazz: Class<*>
) {
    option.putString("last_activity", clazz.name)
    val intent = Intent(this, clazz)
    intent.putExtra("data_bundle", option)
    startActivity(intent)
}

fun AppCompatActivity.launchActivityForResult(
    callback: (Boolean) -> Unit
) {
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode == AppCompatActivity.RESULT_OK)
    }
}

fun AppCompatActivity.currentBundle(): Bundle? {
    return intent.getBundleExtra("data_bundle")
}

fun AppCompatActivity.lastActivity(): String {
    return intent.getBundleExtra("data_bundle")?.putString("last_activity", "").toString()
}

fun Activity.finishWithAnimation() {
    finish()
}

fun Activity.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
