package com.ph32395.staynow.CCCD

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class CccdViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _cccdData = MutableLiveData<Map<String, String>>()
    val cccdData: LiveData<Map<String, String>> = _cccdData
    private val _cccdDataNT = MutableLiveData<Map<String, String>>()
    val cccdDataNT: LiveData<Map<String, String>> = _cccdDataNT

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Khóa bí mật giống với khóa dùng khi mã hóa
    private val SECRET_KEY = "MySecretKey12345"

    /**
     * Hàm giải mã dữ liệu
     */
    private fun decrypt(encryptedData: String, secretKey: String): String {
        try {
            val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            return String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("Decryption", "Lỗi giải mã: ${e.message}")
            throw e
        }
    }


    /**
     * Hàm lấy và giải mã dữ liệu CCCD từ Firestore
     */
    fun fetchCccdData(userId: String) {
        firestore.collection("CCCD").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val encryptedData = documentSnapshot.data as? Map<String, String>
                    if (encryptedData != null) {
                        // Giải mã từng trường trong dữ liệu
                        val decryptedData = encryptedData.mapValues { (_, value) ->
                            try {
                                decrypt(value, SECRET_KEY)
                            } catch (e: Exception) {
                                "Lỗi giải mã"
                            }
                        }
                        _cccdData.value = decryptedData
                    } else {
                        _cccdData.value = emptyMap()
                    }
                } else {
                    _error.value = "Không tìm thấy dữ liệu CCCD."
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Lỗi khi lấy dữ liệu: ${exception.message}"
            }
    }
    fun fetchCccdDataNT(userId: String) {
        firestore.collection("CCCD").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val encryptedData = documentSnapshot.data as? Map<String, String>
                    if (encryptedData != null) {
                        // Giải mã từng trường trong dữ liệu
                        val decryptedData = encryptedData.mapValues { (_, value) ->
                            try {
                                decrypt(value, SECRET_KEY)
                            } catch (e: Exception) {
                                "Lỗi giải mã"
                            }
                        }
                        _cccdDataNT.value = decryptedData
                    } else {
                        _cccdDataNT.value = emptyMap()
                    }
                } else {
                    _error.value = "Không tìm thấy dữ liệu CCCD."
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Lỗi khi lấy dữ liệu: ${exception.message}"
            }
    }
}
