package com.ph32395.staynow.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.databinding.ActivityThanhToanBinding

class ThanhToanActivity : AppCompatActivity() {

    // Đối tượng Binding
    private lateinit var binding: ActivityThanhToanBinding

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanhToanBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}