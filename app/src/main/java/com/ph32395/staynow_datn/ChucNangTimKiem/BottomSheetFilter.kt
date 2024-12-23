package com.ph32395.staynow_datn.ChucNangTimKiem

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.LoaiPhong.LoaiPhong
import com.ph32395.staynow_datn.Model.LoaiPhongTro
import com.ph32395.staynow_datn.NoiThat.NoiThat
import com.ph32395.staynow_datn.R
import com.ph32395.staynow_datn.TienNghi.TienNghi
import com.ph32395.staynow_datn.databinding.BottomSheetFilterSearchBinding

class BottomSheetFilter : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetFilterSearchBinding
    private var firestore = FirebaseFirestore.getInstance()
    private var typeRoomRef = firestore.collection("LoaiPhong")
    private var tienNghiRoomRef = firestore.collection("TienNghi")
    private var noiThatRoomRef = firestore.collection("NoiThat")
    private val TAG = "zzzzzzzBottomFilterzzzzzzz"
    private var filterCriteriaListener: FilterCriteriaListener? = null
    var selectedTypes = mutableListOf<String>()
    var selectedTienNghi = mutableListOf<String>()
    var selectedNoiThat = mutableListOf<String>()

    interface FilterCriteriaListener {
        // Hàm này sẽ nhận các tiêu chí lọc
        fun onFilterSelected(
            selectedTypes: MutableList<String>, // Loại phòng
            selectedTienNghi: MutableList<String>, // Tiện nghi
            selectedNoiThat: MutableList<String>, // Nội thất
        )
    }

    // Đảm bảo gán listener trong Activity/Fragment chứa FilterFragment
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FilterCriteriaListener) {
            filterCriteriaListener = context
        } else {
            throw ClassCastException("$context must implement FilterCriteriaListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        filterCriteriaListener = null
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetFilterSearchBinding.inflate(inflater, container, false)

        typeRoomRef.get().addOnSuccessListener { documents ->
            val typeRooms = documents.map { it.toObject(LoaiPhong::class.java) }
            // Hiển thị từng loại phòng lên giao diện
            displayChipsTypeRom(typeRooms.toMutableList(), binding.chipGroupTypeRoom)


        }.addOnFailureListener {
            Log.d(TAG, "onCreateView: ${it.message.toString()}")
        }

        tienNghiRoomRef.get().addOnSuccessListener { documents ->
            Log.d(TAG, "onCreateView: $documents")
            val tienNghiRooms = documents.map { it.toObject(TienNghi::class.java) }
            // Hiển thị từng tiện nghi lên giao diện
            displayChipsTienNghi(tienNghiRooms.toMutableList(), binding.chipGroupTienNghi)
        }.addOnFailureListener {
            Log.d(TAG, "onCreateView: ${it.message.toString()}")
        }

        noiThatRoomRef.get().addOnSuccessListener { document ->
            Log.d(TAG, "onCreateView: $document")
            val noiThatRooms = document.map { it.toObject(NoiThat::class.java) }
            // Hiển thị từng nội thất lên giao diện
            displayChipsNoiThat(noiThatRooms.toMutableList(), binding.chipGroupNoiThat)
        }.addOnFailureListener {
            Log.d(TAG, "onCreateView: ${it.message.toString()}")
        }

        binding.btnCloseDialog.setOnClickListener {
            dismiss()
        }
        binding.btnResetFilter.setOnClickListener {
            binding.chipGroupNoiThat.clearCheck()
            binding.chipGroupTypeRoom.clearCheck()
            binding.chipGroupTienNghi.clearCheck()

            selectedTypes.clear()
            selectedTienNghi.clear()
            selectedNoiThat.clear()

        }
        binding.btnApply.setOnClickListener {
            filterCriteriaListener?.onFilterSelected(
                selectedTypes,
                selectedTienNghi,
                selectedNoiThat
            )
            dismiss()
        }


        return binding.root
    }


    private fun displayChipsTypeRom(types: MutableList<LoaiPhong>, chipGroup: ChipGroup) {
//        chipGroup.removeAllViews() // Xóa chip cũ
        Log.d(TAG, "displayChipsTypeRom: listType $selectedTypes")
        for (type in types) {
            val chip = Chip(context)
            chip.text = type.tenLoaiPhong
            chip.textSize = 11f
            chip.isCheckable = true
            chip.isChecked = selectedTypes.contains(type.tenLoaiPhong)
            chip.elevation = 8f
            chip.setChipStrokeColorResource(if (chip.isChecked) R.color.color_text else R.color.transparent)
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(TAG, "displayChips: buttonView.text ${buttonView.text}")
                Log.d(TAG, "displayChips: buttonView $buttonView")
                Log.d(TAG, "displayChips: isChecked $isChecked")
                if (isChecked) {
                    selectedTypes.add(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.color_text)
                } else {
                    selectedTypes.remove(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.white)
                }
            }
            chip.typeface = Typeface.SERIF
            chipGroup.addView(chip)
        }
    }

    private fun displayChipsTienNghi(types: MutableList<TienNghi>, chipGroup: ChipGroup) {
//        chipGroup.removeAllViews() // Xóa chip cũ
        Log.d(TAG, "displayChipsTypeRom: listTienNghi $selectedTienNghi")
        for (type in types) {
            val chip = Chip(context)
            chip.text = type.tenTienNghi
            chip.textSize = 11f
            chip.isCheckable = true
            chip.isChecked = selectedTienNghi.contains(type.tenTienNghi)
            chip.elevation = 8f
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setChipStrokeColorResource(if (chip.isChecked) R.color.color_text else R.color.transparent)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(TAG, "displayChips: buttonView.text ${buttonView.text}")
                Log.d(TAG, "displayChips: buttonView $buttonView")
                Log.d(TAG, "displayChips: isChecked $isChecked")
                if (isChecked) {
                    selectedTienNghi.add(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.color_text)
                } else {
                    selectedTienNghi.remove(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.white)
                }
            }
            chip.typeface = Typeface.SERIF
            chipGroup.addView(chip)
        }
    }

    private fun displayChipsNoiThat(types: MutableList<NoiThat>, chipGroup: ChipGroup) {
//        chipGroup.removeAllViews() // Xóa chip cũ
        Log.d(TAG, "displayChipsTypeRom: listNoiThat $selectedNoiThat")
        for (type in types) {
            val chip = Chip(context)
            chip.text = type.tenNoiThat
            Log.d(TAG, "displayChipsNoiThat: ${selectedNoiThat.contains(type.tenNoiThat)}")
            chip.textSize = 11f
            chip.isCheckable = true
            chip.isChecked = selectedNoiThat.contains(type.tenNoiThat)
            chip.elevation = 8f
            chip.setChipStrokeColorResource(
                if (chip.isChecked) R.color.color_text else R.color.transparent
            )
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(TAG, "displayChips: buttonView.text ${buttonView.text}")
                Log.d(TAG, "displayChips: buttonView $buttonView")
                Log.d(TAG, "displayChips: isChecked $isChecked")
                if (isChecked) {
                    selectedNoiThat.add(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.color_text)
                } else {
                    selectedNoiThat.remove(buttonView.text.toString())
                    chip.setChipStrokeColorResource(R.color.white)
                }
            }
            chip.typeface = Typeface.SERIF
            chipGroup.addView(chip)
        }
    }

    fun updateFilter(
        listType: MutableList<String>,
        listNoiThat: MutableList<String>,
        listTienNghi: MutableList<String>
    ) {

        Log.d(TAG, "updateFilter: listType $listType")
        Log.d(TAG, "updateFilter: listNoiThat $listNoiThat")
        Log.d(TAG, "updateFilter: listTienNghi $listTienNghi")
        selectedTypes = listType
        selectedNoiThat = listNoiThat
        selectedTienNghi = listTienNghi

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
