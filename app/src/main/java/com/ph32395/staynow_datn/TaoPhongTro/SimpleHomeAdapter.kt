package com.ph32395.staynow_datn.TaoPhongTro


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ph32395.staynow_datn.Interface.AdapterTaoPhongTroEnteredListenner
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.databinding.ItemLoaiphongBinding

class SimpleHomeAdapter(
    private val context: Context,
    private var nhatrolist: List<NhaTroModel>,
    private val listener: AdapterTaoPhongTroEnteredListenner
) : RecyclerView.Adapter<SimpleHomeAdapter.SimpleHomeViewHolder>() {

    // Biến lưu trữ vị trí loại phòng được chọn
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleHomeViewHolder {
        val binding = ItemLoaiphongBinding.inflate(LayoutInflater.from(context), parent, false)
        return SimpleHomeViewHolder(binding)
    }

    //new
    // Add method to update the list
    fun updateList(newList: List<NhaTroModel>) {
        nhatrolist = newList
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: SimpleHomeViewHolder, position: Int) {
        val loaiphong = nhatrolist[position]
        holder.bind(loaiphong, position == selectedPosition)
    }

    override fun getItemCount(): Int = nhatrolist.size

    inner class SimpleHomeViewHolder(private val binding: ItemLoaiphongBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                // Kiểm tra nếu item đã chọn khác với item hiện tại
                if (selectedPosition != adapterPosition) {
                    // Cập nhật vị trí được chọn và thông báo thay đổi
                    val previousPosition = selectedPosition
                    selectedPosition = adapterPosition

                    // Thông báo cập nhật giao diện cho item cũ và mới
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)

                    // Gọi listener để truyền dữ liệu về Activity
                    listener.onNhaTroSelected(nhatrolist[adapterPosition], true)
                }
            }
        }


        fun bind(nhatro: NhaTroModel, isSelected: Boolean) {
            binding.tenLoaiPhong.text = nhatro.tenNhaTro
            binding.root.isSelected = isSelected
        }
    }

    fun selectById(maNhaTro: String) {
        // Tìm vị trí của nhà trọ theo mã nhà trọ
        val newSelectedPosition = nhatrolist.indexOfFirst { it.maNhaTro == maNhaTro }

        // Nếu tìm thấy, cập nhật vị trí được chọn
        if (newSelectedPosition != -1) {
            val previousPosition = selectedPosition
            selectedPosition = newSelectedPosition

            // Thông báo cập nhật giao diện cho item cũ và mới
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // Gọi listener để thông báo về nhà trọ được chọn
            listener.onNhaTroSelected(nhatrolist[selectedPosition], true)
        } else {
            Log.e("SimpleHomeAdapter", "Không tìm thấy nhà trọ với ID: $maNhaTro")
        }
    }



}
