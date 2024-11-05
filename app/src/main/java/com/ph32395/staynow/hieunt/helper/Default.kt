package com.ph32395.staynow.hieunt.helper

import android.Manifest
import android.os.Build

object Default {
    //Name permission
    val NOTIFICATION_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.POST_NOTIFICATIONS
    else ""

    val STORAGE_PERMISSION = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) arrayOf(
        Manifest.permission.READ_MEDIA_VIDEO
    ) else arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    object IntentKeys {
        const val SCREEN = "SCREEN"
        const val SPLASH_ACTIVITY = "SplashActivity"

        const val NEW_TAB = "new_tab"
        const val OPEN_TAB = "open_tab"

        const val TAB_IN_WEB_VIEW = "tab_in_web_view"
        const val NAME_SOCIAL = "name_social"

        const val THUMBNAIL = "thumbnail"
        const val VIDEO_EXTRACTED = "video_extracted"

        const val URL_VIDEO = "url_video"
        const val TITLE_VIDEO = "title_video"
        const val PATH_VIDEO = "path_video"
        const val TYPE_VIDEO = "type_video"
        const val CURRENT_POSITION = "current_position"
        const val CURRENT_TIME = "current_time"

        const val UPDATE_PROGRESS_M3U8 = "UPDATE_PROGRESS_M3U8"
        const val VIDEO_MODEL_M3U8 = "video_model_m3u8"

        const val TYPE_MY_FILES = "type_my_files"

        const val PASSWORD_SET = "password_set"
        const val CHANGE_PASSWORD = "change_password"

        const val IS_PRIVATE = "is_private"
        const val GO_TO_PREVIEW = "go_to_preview"
    }

    object SharePreKey {
        const val KEY_SEARCH_ENGINE = "key_search_engine"
    }

    object Domain {
        var DOMAIN_GOOGLE = "https://www.google.com/search?q="
        var DOMAIN_BING = "https://www.bing.com/search?q="
        var DOMAIN_YAHOO = "https://search.yahoo.com/search?q="
        var DOMAIN_DUCKDUCKGO = "https://www.duckduckgo.com/search?q="
        var DOMAIN_YANDEX = "https://www.yandex.com/search?q="
    }

    object OptionSearch {
        const val GOOGLE = "Google"
        const val BING = "Bing"
        const val YAHOO = "Yahoo"
        const val DUCKDUCKGO = "DuckDuckGO"
        const val YANDEX = "Yandex"
        const val WEB = "Web"
    }

    object SocialMedia {
        const val FACEBOOK = "Facebook"
        const val INSTAGRAM = "Instagram"
        const val TIKTOK = "Tiktok"
        const val TWITTER = "Twitter"
        const val DAILY_MOTION = "Dailymotion"
        const val BILIBILI = "Bilibili"
        const val XVIDEOS = "#videos"
        const val XNXX = "#n##"
    }

    object STATE_DOWNLOAD {
        const val PAUSE = "pause"
        const val RESUME = "resume"
        const val CANCEL = "cancel"
        const val SUCCESS = "success"
    }

}