package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.databinding.ActivityChoosePaymentBinding
import com.ph32395.staynow.hieunt.widget.tap

@Suppress("DEPRECATION")
class ChoosePaymentActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityChoosePaymentBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoosePaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.tap {
            onBackPressed()
            finish()
        }


    }
}