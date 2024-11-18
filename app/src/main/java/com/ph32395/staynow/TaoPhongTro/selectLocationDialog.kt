import android.app.Activity
import android.content.Context
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.ph32395.staynow.DiaChiGHN.Adapter.DistrictAdapter
import com.ph32395.staynow.DiaChiGHN.Adapter.ProvinceAdapter
import com.ph32395.staynow.DiaChiGHN.Adapter.WardAdapter
import com.ph32395.staynow.DiaChiGHN.GHNViewModel
import com.ph32395.staynow.DiaChiGHN.Model.District
import com.ph32395.staynow.DiaChiGHN.Model.Province
import com.ph32395.staynow.DiaChiGHN.Model.Ward
import com.ph32395.staynow.R
import android.text.Editable
import android.text.TextWatcher


fun selectLocationDialog(
    context: Context,
    title: String,
    itemType: String,
    ghnViewModel: GHNViewModel,
    provinceId: Int? = null,
    districtId: Int? = null,
    onItemSelected: (Any) -> Unit
) {
    val dialog = SweetAlertDialog(context, SweetAlertDialog.BUTTON_POSITIVE)
    dialog.setTitleText(title)

    val customView = (context as Activity).layoutInflater.inflate(R.layout.dialog_address, null)
    dialog.setCustomView(customView)

    val recyclerView = customView.findViewById<RecyclerView>(R.id.recycler_province)
    val searchEditText = customView.findViewById<EditText>(R.id.search_province)

    recyclerView.layoutManager = LinearLayoutManager(context)

    // LiveData để lưu danh sách đã lọc và danh sách gốc
    val filteredList = MutableLiveData<List<Any>>()
    var originalList: List<Any> = listOf()

    // Quan sát filteredList và cập nhật adapter khi có thay đổi
    filteredList.observe(context as LifecycleOwner) { items ->
        val adapter = when (itemType) {
            "Province" -> ProvinceAdapter(items as List<Province>) { selectedItem ->
                val selectedProvince = selectedItem as Province
                onItemSelected(selectedProvince)
                dialog.dismissWithAnimation()

                // Gọi lại để chọn District
                selectLocationDialog(
                    context = context,
                    title = "Chọn Quận/Huyện",
                    itemType = "District",
                    ghnViewModel = ghnViewModel,
                    provinceId = selectedProvince.ProvinceID,
                    onItemSelected = onItemSelected
                )
            }
            "District" -> DistrictAdapter(items as List<District>) { selectedItem ->
                val selectedDistrict = selectedItem as District
                onItemSelected(selectedDistrict)
                dialog.dismissWithAnimation()

                // Gọi lại để chọn Ward
                selectLocationDialog(
                    context = context,
                    title = "Chọn Phường/Xã",
                    itemType = "Ward",
                    ghnViewModel = ghnViewModel,
                    districtId = selectedDistrict.DistrictID,
                    onItemSelected = onItemSelected
                )
            }
            "Ward" -> WardAdapter(items as List<Ward>) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismissWithAnimation()

                // Hiển thị dialog nhập địa chỉ chi tiết
                showDetailedAddressDialog(context, onItemSelected)
            }
            else -> throw IllegalArgumentException("Invalid itemType")
        }
        recyclerView.adapter = adapter
    }

    // Lấy dữ liệu từ ViewModel và cập nhật vào filteredList và originalList
    when (itemType) {
        "Province" -> {
            ghnViewModel.getProvinces().observe(context as LifecycleOwner) { provinces ->
                if (provinces != null && provinces.isNotEmpty()) {
                    originalList = provinces
                    filteredList.value = originalList
                } else {
                    showNoDataDialog(context)
                }
            }
        }
        "District" -> {
            if (provinceId == null) {
                throw IllegalArgumentException("provinceId is required for District selection")
            }
            ghnViewModel.getDistricts(provinceId).observe(context as LifecycleOwner) { districts ->
                if (districts != null && districts.isNotEmpty()) {
                    originalList = districts
                    filteredList.value = originalList
                } else {
                    showNoDataDialog(context)
                }
            }
        }
        "Ward" -> {
            if (districtId == null) {
                throw IllegalArgumentException("districtId is required for Ward selection")
            }
            ghnViewModel.getWards(districtId).observe(context as LifecycleOwner) { wards ->
                if (wards != null && wards.isNotEmpty()) {
                    originalList = wards
                    filteredList.value = originalList
                } else {
                    showNoDataDialog(context)
                }
            }
        }
    }

    // Thêm TextWatcher để lọc dữ liệu khi nhập vào searchEditText
    searchEditText.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val query = s.toString().lowercase()

            // Lọc danh sách dựa trên từ khóa từ originalList
            val filtered = when (itemType) {
                "Province" -> (originalList as List<Province>).filter {
                    it.ProvinceName.lowercase().contains(query)
                }
                "District" -> (originalList as List<District>).filter {
                    it.DistrictName.lowercase().contains(query)
                }
                "Ward" -> (originalList as List<Ward>).filter {
                    it.WardName.lowercase().contains(query)
                }
                else -> originalList
            }

            // Cập nhật filteredList để RecyclerView thay đổi
            filteredList.value = filtered
        }
    })

    dialog.show()
}

// Hàm tiện ích để hiển thị dialog không có dữ liệu
private fun showNoDataDialog(context: Context) {
    SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
        .setTitleText("Không có dữ liệu")
        .setConfirmText("Đóng")
        .show()
}


// Hàm tiện ích để hiển thị dialog nhập địa chỉ chi tiết
private fun showDetailedAddressDialog(context: Context, onItemSelected: (Any) -> Unit) {
    val dialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
    dialog.setTitleText("Nhập địa chỉ chi tiết")

    val input = EditText(context)
    dialog.setCustomView(input)
    dialog.setConfirmText("Xác nhận")
    dialog.setConfirmClickListener {
        val detailedAddress = input.text.toString()
        onItemSelected(detailedAddress)  // Gửi địa chỉ chi tiết về hàm gọi
        dialog.dismissWithAnimation()
    }
    dialog.show()
}
