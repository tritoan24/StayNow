package com.ph32395.staynow_datn.QuanLyNguoiThue

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ph32395.staynow_datn.databinding.BottomSheetCreateAndUpdateThanhVienBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BottomSheetCreateAndUpdateThanhVien(
    private val idHopDong: String?,
    private val dataTv: ThanhVien?
) :
    BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCreateAndUpdateThanhVienBinding
    private val TAG = "BottomSheetCreateAndUpdateThanhVienzzzz"
    private var avatarUri: Uri? = null // Để lưu URI ảnh đại diện đã chọn
    private val mStorageRef: StorageReference = FirebaseStorage.getInstance().reference
    val dbQuanLyNguoiThue = FirebaseFirestore.getInstance().collection("QuanLyNguoiThue")
    private val calender = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = BottomSheetCreateAndUpdateThanhVienBinding.inflate(inflater, container, false)
        Log.d(TAG, "onCreateView: dataTv $dataTv")
        Log.d(TAG, "onCreateView: idHopDong $idHopDong")
        if (dataTv != null) {
            binding.tvTitleBottomSheet.text = "Sửa thành viên"
            binding.edTenThanhVien.setText(dataTv.tenThanhVien)
            binding.edSdt.setText(dataTv.soDienThoai)
            binding.edtEmail.setText(dataTv.email)
            binding.tvNgayVao.text = dataTv.ngayVao
            binding.tvNgayVao.setTextColor(Color.BLACK)
            binding.iconAddAvata.visibility = View.GONE
            Glide.with(requireContext())
                .load(dataTv.hinhAnh)
                .circleCrop()
                .into(binding.ivAvatar)
        } else {
            binding.tvTitleBottomSheet.text = "Thêm thành viên"
        }


        binding.btnAddAvatar.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }

        binding.btnResetFilter.setOnClickListener {
            binding.ivAvatar.setImageURI(null)
            binding.iconAddAvata.visibility = View.VISIBLE
            binding.edSdt.setText("")
            binding.edTenThanhVien.setText("")
            binding.edtEmail.setText("")
            binding.tvNgayVao.text = "Ngày vào"
            binding.tvNgayVao.setTextColor(Color.GRAY)
        }


        binding.btnApply.setOnClickListener {
            if (dataTv == null) {
                saveUserToFirestore(idHopDong!!)
            } else {
                updateUserToFirestore(idHopDong, dataTv)
            }
        }

        binding.btnCalender.setOnClickListener {
            val calenderDialog = DatePickerDialog(
                requireContext(), { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formatDate = dateFormat.format(selectedDate.time)
                    binding.tvNgayVao.text = formatDate
                    binding.tvNgayVao.setTextColor(Color.BLACK)

                },
                calender.get(Calendar.YEAR),
                calender.get(Calendar.MONTH),
                calender.get(Calendar.DAY_OF_MONTH)

            )
            calenderDialog.show()
        }


        return binding.root
    }

    private fun updateUserToFirestore(idHopDong: String?, itemTv: ThanhVien?) {
        val name = binding.edTenThanhVien.text.toString().trim()
        val phone = binding.edSdt.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val ngayVao = binding.tvNgayVao.text.toString().trim()

        val progressDialog = ProgressDialog(requireContext())
        with(progressDialog) {
            theme = ProgressDialog.THEME_DARK
        }
        progressDialog.show()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || ngayVao.contains("Ngày vào")) {
            progressDialog.dismiss()
            Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidEmail(email)) {
            progressDialog.dismiss()
            Toast.makeText(context, "Email không đúng định dạng", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPhone(phone)) {
            progressDialog.dismiss()
            Toast.makeText(context, "Số điện thoại không đúng định dạng", Toast.LENGTH_SHORT).show()
            return
        }
        if (avatarUri == null) {

            val thanhVien = ThanhVien(
                maThanhVien = itemTv?.maThanhVien!!,
                tenThanhVien = name,
                email = email,
                soDienThoai = phone,
                ngayVao = ngayVao,
                hinhAnh = itemTv.hinhAnh.toString()
            )
            updateThanhVien(idHopDong!!, thanhVien, itemTv.maThanhVien, progressDialog)
            Toast.makeText(context, "update thanh cong", Toast.LENGTH_SHORT).show()
            dismiss()
        } else {
            uploadImage(avatarUri!!, itemTv?.maThanhVien!!, object : UploadCallback {
                override fun onSuccess(imageUrl: String?) {
                    val thanhVien = ThanhVien(
                        maThanhVien = itemTv.maThanhVien,
                        tenThanhVien = name,
                        email = email,
                        soDienThoai = phone,
                        hinhAnh = imageUrl.toString(),
                        ngayVao = ngayVao
                    )
                    updateThanhVien(idHopDong!!, thanhVien, itemTv.maThanhVien, progressDialog)
                    Toast.makeText(context, "update thanh cong", Toast.LENGTH_SHORT).show()
                    dismiss()
                }

                override fun onFailure(e: Exception?) {
                    Log.e(TAG, "onFailure: Error Add Thanh Vien msg ${e?.message.toString()}")

                }
            })
        }


    }

    private fun saveUserToFirestore(idHopDong: String) {
        val name = binding.edTenThanhVien.text.toString().trim()
        val phone = binding.edSdt.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val ngayVao = binding.tvNgayVao.text.toString().trim()

        val progressDialog = ProgressDialog(requireContext())
        with(progressDialog) {
            theme = ProgressDialog.THEME_DARK
        }
        progressDialog.show()

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || ngayVao.contains("Ngày vào")) {
            progressDialog.dismiss()
            Toast.makeText(context, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }
        if (avatarUri == null) {
            progressDialog.dismiss()
            Toast.makeText(context, "Vui lòng chọn ảnh đại diện!", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidEmail(email)) {
            progressDialog.dismiss()
            Toast.makeText(context, "Email không đúng định dạng", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidPhone(phone)) {
            progressDialog.dismiss()
            Toast.makeText(context, "Số điện thoại không đúng định dạng", Toast.LENGTH_SHORT).show()
            return
        }


        // Tải ảnh lên Firebase Storage
        val userId = dbQuanLyNguoiThue.document().id // Tạo ID duy nhất cho người dùng
        Log.d(TAG, "saveUserToFirestore: $userId")

        uploadImage(avatarUri!!, userId, object : UploadCallback {
            override fun onSuccess(imageUrl: String?) {
                val thanhVien = ThanhVien(
                    maThanhVien = userId,
                    tenThanhVien = name,
                    email = email,
                    soDienThoai = phone,
                    hinhAnh = imageUrl.toString(),
                    ngayVao = ngayVao
                )
                addThanhVienMoi(thanhVien, idHopDong, progressDialog)
                Toast.makeText(context, "Them thanh cong", Toast.LENGTH_SHORT).show()
                dismiss()
            }

            override fun onFailure(e: Exception?) {
                progressDialog.dismiss()
                Log.e(TAG, "onFailure: Error Add Thanh Vien msg ${e?.message.toString()}")

            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            // Lấy URI của ảnh
            avatarUri = data?.data // Lưu URI ảnh vào biến
            avatarUri?.let {
                // Hiển thị ảnh lên ImageView
                binding.ivAvatar.setImageURI(it)
                binding.iconAddAvata.visibility = View.GONE

            }
        }
    }

    fun uploadImage(imageUri: Uri, userId: String, callback: UploadCallback) {
        // Tạo đường dẫn để lưu ảnh
        val fileReference = mStorageRef.child("avatars/$userId.jpg")

        // Tải ảnh lên
        val uploadTask = fileReference.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            // Lấy URL của ảnh sau khi tải lên thành công
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                callback.onSuccess(imageUrl) // Gọi callback với URL ảnh
            }
        }.addOnFailureListener { e ->
            callback.onFailure(e) // Gọi callback khi có lỗi
        }
    }

    interface UploadCallback {
        fun onSuccess(imageUrl: String?)
        fun onFailure(e: Exception?)
    }

    private fun addThanhVienMoi(
        thanhVienMoi: ThanhVien,
        idHopDong: String,
        progressDialog: ProgressDialog
    ) {
        dbQuanLyNguoiThue.document(idHopDong)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nguoiThue = document.toObject(NguoiThueModel::class.java)
                    if (nguoiThue != null) {
                        // Thêm thành viên mới vào danh sách
                        val updatedList = nguoiThue.danhSachThanhVien.toMutableList()
                        updatedList.add(thanhVienMoi)

                        // Cập nhật danh sách trong Firestore
                        dbQuanLyNguoiThue.document(nguoiThue.idHopDong)
                            .update("thanhVienList", updatedList)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Thêm thành viên mới thành công")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Lỗi khi thêm thành viên mới: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi lấy document: ${e.message}")
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
            }
    }

    private fun updateThanhVien(
        idHopDong: String,
        updatedThanhVien: ThanhVien,
        idThanhVien: String,
        progressDialog: ProgressDialog
    ) {
        dbQuanLyNguoiThue.document(idHopDong)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nguoiThue = document.toObject(NguoiThueModel::class.java)
                    if (nguoiThue != null) {
                        // Lấy danh sách thành viên hiện tại
                        val updatedList = nguoiThue.danhSachThanhVien.toMutableList()

                        // Tìm thành viên cần cập nhật
                        val index = updatedList.indexOfFirst { it.maThanhVien == idThanhVien }
                        if (index != -1) {
                            // Cập nhật thông tin của thành viên
                            updatedList[index] = updatedThanhVien

                            // Cập nhật danh sách trong Firestore
                            dbQuanLyNguoiThue.document(nguoiThue.idHopDong)
                                .update("thanhVienList", updatedList)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Cập nhật thành viên thành công")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Lỗi khi cập nhật thành viên: ${e.message}")
                                }
                        } else {
                            Log.e("Firestore", "Không tìm thấy thành viên với id: $idThanhVien")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Lỗi khi lấy document: ${e.message}")
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
            }
    }


    //checkEmail
    fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    //Check phone
    fun isValidPhone(phone: String): Boolean {
        val phonePattern = "^(0|\\+84)[3|5|7|8|9][0-9]{8}$"
        return phone.matches(phonePattern.toRegex())
    }


    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val bottomSheet =
            dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

        // Đảm bảo BottomSheet được mở toàn màn hình
        val behavior = BottomSheetBehavior.from(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isHideable = false  // Không cho phép ẩn

        // Thay đổi chiều cao và chiều rộng của BottomSheet
        val params = bottomSheet.layoutParams
        params.height = ViewGroup.LayoutParams.MATCH_PARENT // Chiếm toàn bộ chiều cao
        params.width = ViewGroup.LayoutParams.MATCH_PARENT  // Chiếm toàn bộ chiều rộng

        bottomSheet.layoutParams = params

        // Cho phép kéo lên xuống
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
    }


}