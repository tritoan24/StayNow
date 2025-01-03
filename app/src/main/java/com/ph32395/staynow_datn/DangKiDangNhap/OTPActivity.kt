package com.ph32395.staynow_datn.DangKiDangNhap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.ActivityOtpactivityBinding
import com.ph32395.staynow_datn.utils.Constants
import `in`.aabhasjindal.otptextview.OTPListener
import `in`.aabhasjindal.otptextview.OtpTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpactivityBinding
    private val baseUrl = Constants.URL_SERVER_QUYET
    private val endpointVerifyOtp = Constants.ENDPOINT_VERIFY_OTP
    private val endpointResendOtp = Constants.ENDPOINT_RESEND_OTP
    private var countDownTimer: CountDownTimer? = null
    private lateinit var loadingUtil: LoadingUtil

    private var otpTextView: OtpTextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResendOtp.isClickable = false
        binding.btnResendOtp.isEnabled = false
        startTimer()

        val uid = intent.getStringExtra("uid") ?: ""
        val email = intent.getStringExtra("email") ?: ""

        binding.tvEmail.text = email

        binding.btnReset.setOnClickListener {
            otpTextView?.setOTP("")
        }

        otpTextView = binding.otpView
        otpTextView?.requestFocusOTP()
        otpTextView?.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                loadingUtil = LoadingUtil(this@OTPActivity)
                loadingUtil.show()

                OtpService.sendOtpToServer(
                    this@OTPActivity,
                    uid,
                    otp,
                    baseUrl,
                    endpointVerifyOtp,
                    object : OtpService.OtpCallback {
                        @SuppressLint("SetTextI18n")
                        override fun onSuccess() {
                            otpTextView?.showSuccess()
                            runOnUiThread {
                                loadingUtil.hide()
                                binding.countMissOtp.text = "Xác thực thành công"
                                binding.countMissOtp.setTextColor(ContextCompat.getColor(this@OTPActivity, R.color.green))
                            }
                            checkAccountTypeInRealtimeDatabase(uid)
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onFailure(errorMessage: String) {
                            runOnUiThread {
                                loadingUtil.hide()
                            }
                            binding.countMissOtp.visibility = View.VISIBLE
                            binding.countMissOtp.text = errorMessage
                        }

                    })
            }
        }

        binding.btnResendOtp.setOnClickListener {
            loadingUtil.show()
            binding.btnResendOtp.isClickable = false  // Vô hiệu hóa nút "Resend OTP" ngay khi bấm

            OtpService.reSendOtpToServer(
                this,
                uid,
                baseUrl,
                endpointResendOtp,
                object : OtpService.OtpCallback {
                    override fun onSuccess() {
                        runOnUiThread {
                            loadingUtil.hide()
                            Log.d("OTP", "Gửi lại OTP thành công")
                        }
                    }

                    override fun onFailure(errorMessage: String) {
                        loadingUtil.hide()
                        Log.d("OTP", errorMessage)
                        binding.countMissOtp.visibility = View.VISIBLE
                        binding.countMissOtp.text = errorMessage
                    }
                })
            startTimer()
        }

    }

    // Hàm bắt đầu bộ đếm ngược
    private fun startTimer() {
        // Khởi tạo lại bộ đếm ngược
        countDownTimer?.cancel()  // Hủy bỏ bộ đếm cũ (nếu có)

        countDownTimer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timeOtp.text = "${secondsLeft}s"
            }

            override fun onFinish() {
                binding.btnResendOtp.isEnabled = true
                binding.btnResendOtp.isClickable = true
            }
        }
        countDownTimer?.start()  // Bắt đầu bộ đếm ngược
    }

    private fun checkAccountTypeInRealtimeDatabase(uid: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = databaseReference.get().await()
                if (snapshot.exists()) {
                    val accountType = snapshot.child("loaiTaiKhoan").value.toString()
                    CoroutineScope(Dispatchers.Main).launch {
                        navigateBasedOnAccountType(accountType)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            this@OTPActivity,
                            "Người dùng không tồn tại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(
                        this@OTPActivity,
                        "Lỗi khi kiểm tra tài khoản: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Điều hướng dựa trên loại tài khoản
    private fun navigateBasedOnAccountType(accountType: String) {
        if (accountType == "ChuaChon") {
            startActivity(Intent(this@OTPActivity, ChonLoaiTK::class.java))
        } else {
            startActivity(Intent(this@OTPActivity, MainActivity::class.java))
        }
        finish()
    }

}



