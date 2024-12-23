package com.ph32395.staynow_datn.DangKiDangNhap

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.ChucNangChung.LoadingUtil
import com.ph32395.staynow_datn.MainActivity
import com.ph32395.staynow_datn.databinding.ActivityOtpactivityBinding
import com.ph32395.staynow_datn.utils.Constants
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadingUtil = LoadingUtil(this)

        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setOtpTextWatcher(binding.edtOtp1, binding.edtOtp2, null)
        setOtpTextWatcher(binding.edtOtp2, binding.edtOtp3, binding.edtOtp1)
        setOtpTextWatcher(binding.edtOtp3, binding.edtOtp4, binding.edtOtp2)
        setOtpTextWatcher(binding.edtOtp4, binding.edtOtp5, binding.edtOtp3)
        setOtpTextWatcher(binding.edtOtp5, binding.edtOtp6, binding.edtOtp4)
        setOtpTextWatcher(binding.edtOtp6, null, binding.edtOtp5)

        binding.btnResendOtp.isClickable = false
        binding.btnResendOtp.isEnabled = false
        startTimer()

        val uid = intent.getStringExtra("uid") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val pass = intent.getStringExtra("pass") ?: ""
        binding.tvEmail.text = email

        binding.btnVerifyOtp.setOnClickListener {
            loadingUtil.show()
            val otp = getOtpFromInputs()
            if (otp.length == 6) {

                OtpService.sendOtpToServer(
                    this,
                    uid,
                    otp,
                    baseUrl,
                    endpointVerifyOtp,
                    object : OtpService.OtpCallback {
                        override fun onSuccess() {
                            loadingUtil.hide()
                            checkAccountTypeInRealtimeDatabase(uid) // Xử lý logic thành công
                        }

                        override fun onFailure(errorMessage: String) {
                            loadingUtil.hide()
                            Log.d("OTP", "Lỗi gửi OTP: $errorMessage")
                        }

                    })

            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
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
                        Log.d("OTP", "Lỗi gửi lại OTP: $errorMessage")
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

    private fun setOtpTextWatcher(
        currentEditText: EditText,
        nextEditText: EditText?,
        previousEditText: EditText?
    ) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Khi nhập ký tự, focus sang ô tiếp theo
                if (s?.length == 1 && nextEditText != null) {
                    nextEditText.requestFocus()
                } else if (s.isNullOrEmpty()) {
                    previousEditText?.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }


    // Lấy OTP từ các EditText
    private fun getOtpFromInputs(): String {
        return binding.edtOtp1.text.toString() +
                binding.edtOtp2.text.toString() +
                binding.edtOtp3.text.toString() +
                binding.edtOtp4.text.toString() +
                binding.edtOtp5.text.toString() +
                binding.edtOtp6.text.toString()
    }

    private fun checkAccountTypeInRealtimeDatabase(uid: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("NguoiDung").child(uid)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = databaseReference.get().await()
                if (snapshot.exists()) {
                    val accountType = snapshot.child("loaiTaiKhoankhoan").value.toString()
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



