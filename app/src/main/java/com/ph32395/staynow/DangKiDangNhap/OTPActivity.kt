package com.ph32395.staynow.DangKiDangNhap

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ph32395.staynow.databinding.ActivityOtpactivityBinding
import com.ph32395.staynow.utils.constants.Constants
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private val baseUrl = Constants.URL_SERVER_OCEANTECH
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOtpTextWatcher(binding.edtOtp1, binding.edtOtp2)
        setOtpTextWatcher(binding.edtOtp2, binding.edtOtp3)
        setOtpTextWatcher(binding.edtOtp3, binding.edtOtp4)
        setOtpTextWatcher(binding.edtOtp4, binding.edtOtp5)
        setOtpTextWatcher(binding.edtOtp5, binding.edtOtp6)
        setOtpTextWatcher(binding.edtOtp6, null)

        binding.btnResendOtp.isClickable = false
        binding.btnResendOtp.isEnabled = false
        startTimer()

        val uid = intent.getStringExtra("uid") ?: ""
        binding.btnOtp.setOnClickListener {
            val otp = getOtpFromInputs()
            if (otp.length == 6) {
                sendOtpToServer(uid, otp)
            } else {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnResendOtp.setOnClickListener {
            reSendOtpToServer(uid)
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
                binding.btnResendOtp.isEnabled=true
                binding.btnResendOtp.isClickable = true
            }
        }
        countDownTimer?.start()  // Bắt đầu bộ đếm ngược
    }
    // next ô nhập
    private fun setOtpTextWatcher(currentEditText: EditText, nextEditText: EditText?) {
        currentEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 1 && nextEditText != null) {
                    nextEditText.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
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

    // request gửi otp
    private fun sendOtpToServer(uid: String, otp: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uid", uid)
            jsonObject.put("otpCode", otp.toInt())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Tạo requestBody với kiểu JSON
        val requestBody = RequestBody.create(
            "application/json;charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        // Tạo request
        val request = Request.Builder()
            .url("$baseUrl/verify-otp")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        // Thực hiện request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OTPActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Xử lý kết quả từ server
                    runOnUiThread {
                        Toast.makeText(
                            this@OTPActivity,
                            "Xác thực OTP thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@OTPActivity, "OTP chưa đúng", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }

    // request gửi lại otp
    private fun reSendOtpToServer(uid: String) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("uid", uid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val requestBody = RequestBody.create(
            "application/json;charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )

        // Tạo request
        val request = Request.Builder()
            .url("$baseUrl/resend-otp")
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        // Thực hiện request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@OTPActivity, "Lỗi kết nối", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Xử lý kết quả từ server
                    runOnUiThread {
                        Log.d("OTP", response.message);
                    }
                } else {
                    runOnUiThread {
                        Log.d("OTP", response.message);
                    }
                }
            }
        })

        binding.btnResendOtp.isClickable = false  // Vô hiệu hóa nút "Resend OTP" ngay khi bấm

    }


    }



