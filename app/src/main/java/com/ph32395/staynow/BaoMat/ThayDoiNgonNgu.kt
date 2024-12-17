package com.ph32395.staynow.BaoMat

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.MainActivity
import com.ph32395.staynow.R
import java.util.Locale

class ThayDoiNgonNgu : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thay_doi_ngon_ngu)

        // Lấy ngôn ngữ đã lưu trước đó và áp dụng
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("Language", "en") // Mặc định là tiếng việt
        setLocale(savedLanguage!!)

        val radioGroupLanguage = findViewById<RadioGroup>(R.id.radio_group_language)
        val buttonSaveLanguage = findViewById<Button>(R.id.button_save_language)
        val buttonBack = findViewById<ImageButton>(R.id.button_backLanguage)

        buttonBack.setOnClickListener {
            onBackPressed()
        }

        // Đặt radio button theo ngôn ngữ đã lưu
        when (savedLanguage) {
            "vi" -> radioGroupLanguage.check(R.id.radio_language_vietnamese)
            "en" -> radioGroupLanguage.check(R.id.radio_language_english)
            "fr" -> radioGroupLanguage.check(R.id.radio_language_french)
        }

        buttonSaveLanguage.setOnClickListener {
            val selectedRadioButtonId = radioGroupLanguage.checkedRadioButtonId

            if (selectedRadioButtonId != -1) {
                val selectedLanguage = when (selectedRadioButtonId) {
                    R.id.radio_language_vietnamese -> "vi"
                    R.id.radio_language_english -> "en"
                    R.id.radio_language_french -> "fr"
                    else -> "en" // Default to English
                }

                // Lưu ngôn ngữ đã chọn vào SharedPreferences
                saveLanguage(selectedLanguage)

                setLocale(selectedLanguage)
                Toast.makeText(this, "Ngôn ngữ đã được thay đổi", Toast.LENGTH_SHORT).show()

                // Chuyển đến MainActivity sau khi thay đổi ngôn ngữ
                startActivity(Intent(this@ThayDoiNgonNgu, MainActivity::class.java))
                finish() // Kết thúc activity hiện tại
            } else {
                Toast.makeText(this, "Vui lòng chọn ngôn ngữ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLanguage(languageCode: String) {
        val editor = sharedPreferences.edit()
        editor.putString("Language", languageCode)
        editor.apply()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources: Resources = resources
        val config: Configuration = resources.configuration
        val displayMetrics: DisplayMetrics = resources.displayMetrics

        config.setLocale(locale)
        resources.updateConfiguration(config, displayMetrics)
    }
}