package com.ph32395.staynow_datn.hieunt.widget

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

//start activity
fun Fragment.launchActivity(
    clazz: Class<*>
) {
    startActivity(Intent(context, clazz))
}

fun Fragment.launchActivity(
    option: Bundle? = null,
    clazz: Class<*>
) {
    val intent = Intent(context, clazz)
    intent.putExtra("data_bundle", option)
    startActivity(intent)
}

fun Fragment.launchActivityForResult(
    callback: (Boolean) -> Unit
) {
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        callback.invoke(result.resultCode == AppCompatActivity.RESULT_OK)
    }
}

fun Fragment.toast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
