package com.ph32395.staynow.DichVu

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ph32395.staynow.R
import com.ph32395.staynow.DichVu.DichVu


val PREDEFINED_ICONS_URLS = listOf(
    "https://img.icons8.com/?size=100&id=58121&format=png&color=000000",
    "https://img.icons8.com/?size=100&id=fdC1DJl3ZsMm&format=png&color=000000",
)

class IconAdapter(
    context: Context,
    private val icons: List<String> // Chuyển từ List<Int> sang List<String> chứa các URL
) : ArrayAdapter<String>(context, 0, icons) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val iconView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_icon_selection, parent, false)

        val imageView = iconView.findViewById<ImageView>(R.id.iconImageView)

        // Sử dụng Glide để tải và hiển thị hình ảnh từ URL
        Glide.with(context)
            .load(icons[position])  // URL hình ảnh từ danh sách
            .into(imageView)

        return iconView
    }
}


object DichVuAddServiceUtil {
    fun showAddServiceDialog(
        context: Context,
        onServiceAdded: (DichVu) -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_dichvu, null)

        val edtTenDichVu = dialogView.findViewById<EditText>(R.id.edtTenDichVu)
        val gridViewIcons = dialogView.findViewById<GridView>(R.id.gridViewIcons)
        val edtUnit = dialogView.findViewById<EditText>(R.id.edtUnit)

        // Thiết lập adapter cho grid view icon
        val iconAdapter = IconAdapter(context, PREDEFINED_ICONS_URLS)
        gridViewIcons.adapter = iconAdapter

        var selectedIcon: String? = null

        gridViewIcons.setOnItemClickListener { _, view, position, _ ->
            // Đánh dấu icon được chọn
            // Reset alpha của tất cả các item trong GridView
            for (i in 0 until gridViewIcons.count) {
                val itemView = gridViewIcons.getChildAt(i)
                itemView?.alpha = 0.5f
            }

            // Đánh dấu icon hiện tại được chọn
            view.alpha = 1.0f

            // Lưu URL của icon được chọn
            selectedIcon = PREDEFINED_ICONS_URLS[position]
        }


        SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText("Thêm Dịch Vụ Mới")
            .setCustomView(dialogView)
            .setConfirmText("Thêm")
            .setConfirmClickListener { sDialog ->
                val tenDichVu = edtTenDichVu.text.toString().trim()
                val unit = edtUnit.text.toString().trim()

                // Validate input
                when {
                    tenDichVu.isEmpty() -> {
                        Toast.makeText(context, "Vui lòng nhập tên dịch vụ", Toast.LENGTH_SHORT).show()
                        return@setConfirmClickListener
                    }
                    selectedIcon == null -> {
                        Toast.makeText(context, "Vui lòng chọn icon", Toast.LENGTH_SHORT).show()
                        return@setConfirmClickListener
                    }
                    unit.isEmpty() -> {
                        Toast.makeText(context, "Vui lòng nhập đơn vị", Toast.LENGTH_SHORT).show()
                        return@setConfirmClickListener
                    }
                }

                // Tạo dịch vụ mới
                val newDichVu = DichVu(
                    Ma_dichvu = "", // Để trống vì chưa có ID từ Firestore
                    Ten_dichvu = tenDichVu,
                    Icon_dichvu = selectedIcon.toString(), // Convert icon resource ID sang string
                    Don_vi = listOf(unit),
                    Status = true
                )

                // Gọi callback để thêm dịch vụ
                onServiceAdded(newDichVu)
                sDialog.dismissWithAnimation()
            }
            .setCancelButton("Hủy") {
                it.dismissWithAnimation()
            }
            .show()
    }
}