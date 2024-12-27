package com.ph32395.staynow_datn.QuanLyNhaTro

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhong
import com.ph32395.staynow_datn.Maps.RetrofitInstance
import com.ph32395.staynow_datn.Maps.SuggestionResponse
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.databinding.BottomSheetCreateAndUpdateNhaTroBinding
import com.techiness.progressdialoglibrary.ProgressDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class BottomSheetCreateAndUpdateNhaTro(private val item: NhaTroModel?) :
    BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetCreateAndUpdateNhaTroBinding
    private var firestore = FirebaseFirestore.getInstance()
    private var typeRoomRef = firestore.collection("LoaiPhong")
    private var nhaTroRef = firestore.collection("NhaTro")
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val TAG = "ZZZBottomSheetCreateAndUpdateNhaTroZZZ"
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    var Dc_quanhuyen = ""
    var Dc_tinhtp = ""
    var fullAddressct = ""
    var fullAddressDeltail = ""

    private val apiKeys = mutableListOf<String>()
    private var currentKeyIndex = 0
    private var usageMap = mutableMapOf<String, Int>() // Theo dõi số lượng requests mỗi key

    init {
        apiKeys.forEach { usageMap[it] = 0 } // Khởi tạo bộ đếm cho mỗi key
    }

    private fun getCurrentApiKey(): String {
        return apiKeys[currentKeyIndex]
    }

    private fun rotateApiKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        Log.d("API_KEY_SWITCH", "Chuyển sang API Key: ${apiKeys[currentKeyIndex]}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetCreateAndUpdateNhaTroBinding.inflate(inflater, container, false)
        val idUser = FirebaseAuth.getInstance().currentUser?.uid
        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }

// Khởi tạo FusedLocationProviderClient
        fusedLocationClient = context?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        val apiKey = requireActivity().getString(R.string.api_key_1)
        val apiKey2 = requireActivity().getString(R.string.api_key_2)
        val apiKey3 = requireActivity().getString(R.string.api_key_3)
        val apiKey4 = requireActivity().getString(R.string.api_key_4)
        val apiKey5 = requireActivity().getString(R.string.api_key_5)
        val apiKey6 = requireActivity().getString(R.string.api_key_6)
        val apiKey7 = requireActivity().getString(R.string.api_key_7)
        val apiKey8 = requireActivity().getString(R.string.api_key_8)
        val apiKey9 = requireActivity().getString(R.string.api_key_9)
        val apiKey10 = requireActivity().getString(R.string.api_key_10)
        val apiKey11 = requireActivity().getString(R.string.api_key_11)
        val apiKey12 = requireActivity().getString(R.string.api_key_12)
        val apiKey13 = requireActivity().getString(R.string.api_key_13)
        val apiKey14 = requireActivity().getString(R.string.api_key_14)
        val apiKey15 = requireActivity().getString(R.string.api_key_15)
        val apiKey16 = requireActivity().getString(R.string.api_key_16)
        val apiKey17 = requireActivity().getString(R.string.api_key_17)
        val apiKey18 = requireActivity().getString(R.string.api_key_18)
        val apiKey19 = requireActivity().getString(R.string.api_key_19)
        val apiKey20 = requireActivity().getString(R.string.api_key_20)
        val apiKey21 = requireActivity().getString(R.string.api_key_21)
        val apiKey22 = requireActivity().getString(R.string.api_key_22)
        val apiKey23 = requireActivity().getString(R.string.api_key_23)
        val apiKey24 = requireActivity().getString(R.string.api_key_24)
        apiKeys.add(apiKey)
        apiKeys.add(apiKey2)
        apiKeys.add(apiKey3)
        apiKeys.add(apiKey4)
        apiKeys.add(apiKey5)
        apiKeys.add(apiKey6)
        apiKeys.add(apiKey7)
        apiKeys.add(apiKey8)
        apiKeys.add(apiKey9)
        apiKeys.add(apiKey10)
        apiKeys.add(apiKey11)
        apiKeys.add(apiKey12)
        apiKeys.add(apiKey13)
        apiKeys.add(apiKey14)
        apiKeys.add(apiKey15)
        apiKeys.add(apiKey16)
        apiKeys.add(apiKey17)
        apiKeys.add(apiKey18)
        apiKeys.add(apiKey19)
        apiKeys.add(apiKey20)
        apiKeys.add(apiKey21)
        apiKeys.add(apiKey22)
        apiKeys.add(apiKey23)
        apiKeys.add(apiKey24)


        binding.btnApply.setOnClickListener {
            val progressDialog = ProgressDialog(requireContext())
            with(progressDialog) {
                theme = ProgressDialog.THEME_DARK
            }
            if (item == null) {
                createRoom(idUser,progressDialog)

            } else {
                updateRoom(idUser, item,progressDialog)
            }

        }

        //change ui start
        binding.tvTitleBottomSheet.text = if (item == null) "Thêm nhà trọ" else "Sửa nhà trọ"
        binding.edTenNhaTro.setText(item?.tenNhaTro)
        binding.autoComplete.setText(item?.diaChiChiTiet)
        binding.roomAddress.text =
            item?.diaChiChiTiet ?: requireActivity().getString(R.string.Address_not_selected)
        //change ui end


        typeRoomRef.get().addOnSuccessListener { documents ->
            val typeRooms = documents.map { it.toObject(LoaiPhong::class.java) }
            // Hiển thị từng loại phòng lên giao diện
            displayChipsTypeRom(typeRooms.filter {
                !it.tenLoaiPhong.contains("Tất Cả")
            }.toMutableList(), binding.chipGroupTypeRoom)


        }.addOnFailureListener {
            Log.d(TAG, "onCreateView: ${it.message.toString()}")
        }
        //Goi y tim kiem start
        autoCompleteTextView = binding.autoComplete
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                fetchSuggestions(s.toString())
            }
        })
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation()
        }


        //tạo sự kiện khi ấn vào nút update_button
        binding.btngetIndex.setOnClickListener {
            doiToaDoRaViTriCuThe(it)
        }

        //cài lại
        binding.btnResetFilter.setOnClickListener {
            binding.edTenNhaTro.setText("")
            binding.autoComplete.setText("")
            binding.roomAddress.text = requireActivity().getString(R.string.Address_not_selected)
            binding.chipGroupTypeRoom.clearCheck()

        }



        return binding.root
    }

    private fun updateRoom(idUser: String?, item: NhaTroModel?, progressDialog: ProgressDialog) {
        progressDialog.show()
        if (idUser == null) return
        if (item == null) return

        // Lấy tên nhà trọ
        val tenNhaTro = binding.edTenNhaTro.text.toString().trim()
        if (tenNhaTro.isEmpty()) {
            Toast.makeText(context, "Tên nhà trọ không được để trống", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Lấy id của Chip đã chọn
        val checkedChipId = binding.chipGroupTypeRoom.checkedChipId
        if (checkedChipId == View.NO_ID) {
            Toast.makeText(context, "Vui lòng chọn loại nhà trọ", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Lấy thông tin loại nhà trọ
        val selectedChip = binding.chipGroupTypeRoom.findViewById<Chip>(checkedChipId)
        val loaiNhaTro = selectedChip.text.toString()
        val maLoaiNhaTro = selectedChip.tag.toString()

        // Kiểm tra địa chỉ
        val address = binding.autoComplete.text.toString().trim()
        if (address.isEmpty()) {
            Toast.makeText(context, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Cập nhật mô hình nhà trọ
        val updatedNhaTro = NhaTroModel(
            maNhaTro = item.maNhaTro,
            maLoaiNhaTro = maLoaiNhaTro,
            maNguoiDung = idUser,
            dcQuanHuyen = if (Dc_quanhuyen == "") item.dcQuanHuyen else Dc_quanhuyen,
            dcTinhTP = if (Dc_tinhtp == "") item.dcTinhTP else Dc_tinhtp,
            diaChi = if (fullAddressct == "") item.diaChi else fullAddressct,
            diaChiChiTiet = if (fullAddressDeltail == "") item.diaChiChiTiet else fullAddressct,
            tenNhaTro = tenNhaTro,
            tenLoaiNhaTro = loaiNhaTro,
            ngayTao = System.currentTimeMillis()
        )
        Log.d(TAG, "updateRoom: update $updatedNhaTro")
        // Cập nhật Firestore
        nhaTroRef.document(idUser).collection("DanhSachNhaTro").document(item.maNhaTro)
            .set(updatedNhaTro, SetOptions.merge()).addOnSuccessListener {
                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                dismiss()
            }.addOnFailureListener {
                Log.e(TAG, "updateRoom: Cập nhật thất bại ${it.message.toString()}")
                progressDialog.dismiss()
            }.addOnCompleteListener {
                Log.d(TAG, "updateRoom: cập nhật nhà trọ hoàn thành")
                progressDialog.dismiss()
            }
    }


    private fun createRoom(idUser: String?, progressDialog: ProgressDialog) {
        // Kiểm tra xem idUser có rỗng không
        progressDialog.show()
        if (idUser.isNullOrEmpty()) {
            Toast.makeText(context, "Vui lòng đăng nhập trước khi tạo nhà trọ", Toast.LENGTH_SHORT)
                .show()
            progressDialog.dismiss()
            return
        }

        // Lấy tên nhà trọ
        val tenNhaTro = binding.edTenNhaTro.text.toString().trim()
        if (tenNhaTro.isEmpty()) {
            Toast.makeText(context, "Tên nhà trọ không được để trống", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Lấy id của Chip đã chọn
        val checkedChipId = binding.chipGroupTypeRoom.checkedChipId
        if (checkedChipId == View.NO_ID) {
            Toast.makeText(context, "Vui lòng chọn loại nhà trọ", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Lấy thông tin loại nhà trọ
        val selectedChip = binding.chipGroupTypeRoom.findViewById<Chip>(checkedChipId)
        val loaiNhaTro = selectedChip.text.toString()
        val maLoaiNhaTro = selectedChip.tag.toString()

        // Kiểm tra xem địa chỉ có hợp lệ không (có thể tùy chỉnh thêm)
        if (fullAddressct.isEmpty()) {
            Toast.makeText(context, "Địa chỉ không được để trống", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            return
        }

        // Lấy thời gian tạo
        val ngayTao = System.currentTimeMillis()

        // Tạo mã nhà trọ
        val maNhaTro = nhaTroRef.document(idUser).collection("DanhSachNhaTro").document().id

        // Log thông tin (có thể bỏ qua nếu không cần thiết)
        Log.d(TAG, "createRoom:maNha $maNhaTro")
        Log.d(TAG, "createRoom:maLoaiNha $maLoaiNhaTro")
        Log.d(TAG, "createRoom:maNguoiDung $idUser")
        Log.d(TAG, "createRoom:dcQuanHuyen $Dc_quanhuyen")
        Log.d(TAG, "createRoom:dcTinhTP $Dc_tinhtp")
        Log.d(TAG, "createRoom:diaChi $fullAddressct")
        Log.d(TAG, "createRoom:diaChiChiTiet $fullAddressDeltail")
        Log.d(TAG, "createRoom:ten $tenNhaTro")
        Log.d(TAG, "createRoom:tenLoaiNhaTro $loaiNhaTro")
        Log.d(TAG, "createRoom:ngayTao $ngayTao")

        // Tạo mô hình nhà trọ
        val nhaTro = NhaTroModel(
            maNhaTro = maNhaTro,
            maLoaiNhaTro = maLoaiNhaTro,
            maNguoiDung = idUser,
            dcQuanHuyen = Dc_quanhuyen,
            dcTinhTP = Dc_tinhtp,
            diaChi = fullAddressct,
            diaChiChiTiet = fullAddressDeltail,
            tenNhaTro = tenNhaTro,
            tenLoaiNhaTro = loaiNhaTro,
            ngayTao = ngayTao
        )

        // Lưu thông tin nhà trọ vào Firestore
        nhaTroRef.document(idUser).collection("DanhSachNhaTro").document(maNhaTro).set(nhaTro)
            .addOnSuccessListener {
                Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                dismiss()
            }.addOnFailureListener {
                Log.e(TAG, "createRoom: Thêm thất bại ${it.message.toString()}")
                progressDialog.dismiss()
            }.addOnCompleteListener {
                Log.d(TAG, "createRoom: thêm nhà trọ hoàn thành")
                progressDialog.dismiss()
            }
    }


    private fun displayChipsTypeRom(types: MutableList<LoaiPhong>, chipGroup: ChipGroup) {
//        chipGroup.removeAllViews() // Xóa chip cũ
        chipGroup.isSingleSelection = true

        for (type in types) {
            val chip = Chip(context)
            chip.text = type.tenLoaiPhong
            chip.tag = type.maLoaiPhong
            chip.textSize = 11f
            chip.isCheckable = true
            chip.isChecked = item?.tenLoaiNhaTro?.contains(type.tenLoaiPhong) == true
            chip.elevation = 8f
            chip.setChipStrokeColorResource(if (chip.isChecked) R.color.color_text else R.color.transparent)
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(TAG, "displayChips: buttonView.text ${buttonView.text}")
                Log.d(TAG, "displayChips: buttonView $buttonView")
                Log.d(TAG, "displayChips: isChecked $isChecked")
                if (isChecked) {
                    chip.setChipStrokeColorResource(R.color.color_text)
                } else {
                    chip.setChipStrokeColorResource(R.color.white)
                }
            }
            chip.typeface = Typeface.SERIF
            chipGroup.addView(chip)
        }
    }

    // Yêu cầu quyền truy cập vị trí
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Nếu quyền được cấp, lấy vị trí
                getCurrentLocation()
            } else {
                Log.e("Permission", "Quyền truy cập vị trí bị từ chối.")
            }
        }

    // Hàm lấy vị trí người dùng
    private fun getCurrentLocation() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền truy cập, yêu cầu quyền
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")


            } else {
                Log.e("Location", "Không lấy được vị trí")
            }
        }
    }

    fun doiToaDoRaViTriCuThe(view: View) {


        try {
            val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
            val addresses = geocoder?.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]

                // Lấy các thành phần của địa chỉ
                val streetName = address.thoroughfare ?: ""       // Tên đường hoặc ngõ
                val subLocality = address.subLocality ?: ""       // Khu vực nhỏ hơn trong thành phố
                val locality = address.locality ?: ""             // Thành phố hoặc thị xã
                val adminArea = address.adminArea ?: ""           // Tỉnh/thành phố
                val countryName = address.countryName ?: ""       // Tên quốc gia
                Dc_tinhtp = address.adminArea ?: ""          // Tỉnh/Thành phố
                Dc_quanhuyen = address.subAdminArea ?: ""

                //log quan huyen
                Log.d(TAG, "Quan huyen: $Dc_quanhuyen")
                Log.d(TAG, "TinhTP: ${Dc_tinhtp}")

                // Tạo địa chỉ cụ thể kết hợp các thành phần
                val detailedAddress = listOf(
                    streetName,
                    subLocality,
                    locality,
                    adminArea,
                    countryName
                ).filter { it.isNotEmpty() }                  // Lọc bỏ các thành phần trống
                    .joinToString(", ")                          // Ghép lại thành chuỗi, cách nhau bằng dấu phẩy

                val diachict = address.getAddressLine(0);
                // Kiểm tra nếu địa chỉ cụ thể không trống thì cập nhật Firestore
                if (detailedAddress.isNotEmpty() && diachict.isNotEmpty()) {

                    fullAddressct = detailedAddress
                    fullAddressDeltail = diachict
                    binding.autoComplete.setText(fullAddressDeltail)

                    Log.d("Location", "Địa chỉ cụ thể: $detailedAddress")
                    Log.d("Location", "Địa chỉ cụ thể chi tiets: $diachict")

                }
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Lỗi khi lấy địa chỉ: ${e.message}")
        }
    }

    private fun fetchSuggestions(query: String) {
        if (query.length < 2) return

        val apiKey = getCurrentApiKey()
        RetrofitInstance.api.getPlace(apiKey, query).enqueue(object : Callback<SuggestionResponse> {
            override fun onResponse(
                call: Call<SuggestionResponse>, response: Response<SuggestionResponse>
            ) {
                val currentKey = getCurrentApiKey()

                // Kiểm tra giới hạn trong header
                val remainingRequests =
                    response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()
                if (remainingRequests != null) {
                    usageMap[currentKey] = 998 - remainingRequests
                    Log.d("RateLimit", "Key $currentKey còn $remainingRequests requests")
                }

                // Nếu vượt quá giới hạn, chuyển sang key mới
                if (remainingRequests != null && remainingRequests <= 0) {
                    rotateApiKey()
                    fetchSuggestions(query) // Gọi lại với key mới
                    return
                }
                Log.d(TAG, "onResponse: response $response")
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val suggestions = response.body()?.predictions ?: emptyList()
                    Log.d(TAG, "onResponse: suggestions $suggestions")

                    // Lấy description và secondary_text từ mỗi prediction
                    val list = suggestions.map { prediction ->
                        val description = prediction.description
                        val secondaryText =
                            prediction.structured_formatting?.secondary_text ?: "N/A"
                        val district = prediction.compound?.district ?: "N/A" // Lấy district
                        val province = prediction.compound?.province ?: "N/A" // Lấy province


                        Dc_tinhtp = province
                        Dc_quanhuyen = district


                        //log quan huyen
                        Log.d(TAG, "Quan huyen: $Dc_quanhuyen")
                        Log.d(TAG, "TinhTP: $Dc_tinhtp")

                        // Gán giá trị cho fullAddressDeltail
                        fullAddressDeltail = description

                        // Kiểm tra "số" trong 10 ký tự đầu tiên của description
                        if (description.substring(0, minOf(description.length, 10))
                                .contains("số", ignoreCase = true)
                        ) {
                            val indexOfNgo = description.indexOf("ngõ", ignoreCase = true)
                            if (indexOfNgo != -1) {
                                // Nếu tìm thấy "ngõ", lấy từ vị trí "ngõ"
                                fullAddressct = description.substring(indexOfNgo)
                            } else {
                                // Nếu không tìm thấy "ngõ", mặc định lấy secondaryText
                                fullAddressct = secondaryText
                            }
                        } else {
                            // Nếu không thỏa mãn điều kiện "số", mặc định lấy secondaryText
                            fullAddressct = secondaryText
                        }
                    }
                    Log.d(TAG, "onResponse: addressDetails $fullAddressDeltail")
                    Log.d(TAG, "onResponse: addressDetails $fullAddressct")
                    binding.roomAddress.text = fullAddressDeltail

                    // Set full and partial addresses

                    val adapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, list
                    )
                    binding.autoComplete.setAdapter(adapter)
                    //địa chỉ chi tiết fullAddressDeltail
                    //địa chỉ bỏ đi địa chỉ đầu addressDetail
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
                }
                // Logic xử lý response như cũ
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val suggestions = response.body()?.predictions ?: emptyList()
                    val list = suggestions.map { it.description }

                    // Tạo adapter và set cho AutoCompleteTextView
                    val adapter = ArrayAdapter(
                        requireActivity(), android.R.layout.simple_list_item_1, list
                    )
                    binding.autoComplete.setAdapter(adapter)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<SuggestionResponse>, t: Throwable) {
                Log.e("Retrofit", "API call failed: ${t.message}")
            }
        })
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