package com.ph32395.staynow.hieunt.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat

class PermissionHelper(val activity: Activity?) {
    fun isOverlayPermissionGranted(): Boolean {
        return Settings.canDrawOverlays(activity)
    }

    fun requestOverlayPermission(activity: Activity, requestCode: Int = 1000) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:${activity.packageName}")
        activity.startActivityForResult(intent, requestCode)
    }

    fun isGrantPermissionStorage(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun isGrantPermission(permission: String): Boolean {
        activity?.let {
            return ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    fun isGrantMultiplePermissions(permissions: Array<String>): Boolean {
        activity?.let {
            for (permission in permissions) {
                val allow = ActivityCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                if (!allow) return false
            }
            return true
        }
        return false
    }

    fun canShowPermissionDialogSystem(permission: String): Boolean {
        return if (activity != null)
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        else
            true
    }

    fun canShowAllListPermissionDialogSystem(permissions: Array<String>): Boolean {
        if (activity != null) {
            permissions.forEach { permission ->
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return true
                }
            }
            return false
        } else
            return true
    }

    fun shouldShowRequestPermissionRationale(permissions: String): Boolean {
        if (activity == null)
            return false
        if (!isGrantPermission(permissions)) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions)) {
                return false
            }
        }
        return true
    }
}