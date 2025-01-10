package com.ph32395.staynow_datn.hieunt.helper

import android.content.Context
import android.content.SharedPreferences

class SharePrefUtils(context: Context) {
    private val pre: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pre.edit()

    var isReadTenantInterest
        get() = pre.getBoolean("isReadTenantInterest", false)
        set(value) {
            editor.putBoolean("isReadTenantInterest", value)
            editor.apply()
        }

    var isReadRenterInterest
        get() = pre.getBoolean("isReadRenterInterest", false)
        set(value) {
            editor.putBoolean("isReadRenterInterest", value)
            editor.apply()
        }

    var isViewedWarningSchedule
        get() = pre.getBoolean("isViewedWarningSchedule", false)
        set(value) {
            editor.putBoolean("isViewedWarningSchedule", value)
            editor.apply()
        }
    var isPassPermission
        get() = pre.getBoolean("pass_permission", false)
        set(value) {
            editor.putBoolean("pass_permission", value)
            editor.apply()
        }
    var countExitApp
        get() = pre.getInt("count_exit_app", 1)
        set(value) {
            editor.putInt("count_exit_app", value)
            editor.apply()
        }
    var isFirstSelectLanguage
        get() = pre.getBoolean("isFirstSelectLanguage", true)
        set(value) {
            editor.putBoolean("isFirstSelectLanguage", value)
            editor.apply()
        }
    var nameLanguageSelected
        get() = pre.getString("name_language_selected", "English")
        set(value) {
            editor.putString("name_language_selected", value)
            editor.apply()
        }

    var isSetPasswordSuccess
        get() = pre.getBoolean("is_set_password_success", false)
        set(value) {
            editor.putBoolean("is_set_password_success", value)
            editor.apply()
        }

    var securityQuestion: Int
        get() = pre.getInt("security_question", -1)
        set(value) {
            editor.putInt("security_question", value)
            editor.apply()
        }

    var securityAnswer: String
        get() = pre.getString("security_answer", "").toString()
        set(value) {
            editor.putString("security_answer", value)
            editor.apply()
        }

    var passwordPrivateVideo: String
        get() = pre.getString("password_private_video", "").toString()
        set(value) {
            editor.putString("password_private_video", value)
            editor.apply()
        }

    var isViewedTutorHome
        get() = pre.getBoolean("isViewedTutorHome", false)
        set(value) {
            editor.putBoolean("isViewedTutorHome", value)
            editor.apply()
        }

    var isViewedTutorWeb
        get() = pre.getBoolean("isViewedTutorWeb", false)
        set(value) {
            editor.putBoolean("isViewedTutorWeb", value)
            editor.apply()
        }

    var isViewedTutorApp
        get() = pre.getBoolean("isViewedTutorApp", false)
        set(value) {
            editor.putBoolean("isViewedTutorApp", value)
            editor.apply()
        }
}