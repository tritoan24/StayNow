package com.ph32395.staynow_datn.fragment

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ph32395.staynow_datn.BaoMat.CapNhatThongTin
import com.ph32395.staynow_datn.CCCD.CCCD
import com.ph32395.staynow_datn.TaoHopDong.TaoHopDong
import com.ph32395.staynow_datn.ThongTinThanhToan.PaymentInfoActivity
import com.ph32395.staynow_datn.databinding.FragmentRoomManagementBinding
import com.ph32395.staynow_datn.hieunt.base.BaseFragment
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CANCELED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.NotificationTitle.TITLE_LEAVED_BY_RENTER
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.CANCELED
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.CONFIRMED
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow_datn.hieunt.helper.Default.StatusRoom.WATCHED
import com.ph32395.staynow_datn.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow_datn.hieunt.helper.SharePrefUtils
import com.ph32395.staynow_datn.hieunt.view.dialog.RenterInterestDialog
import com.ph32395.staynow_datn.hieunt.view.dialog.UpdateRoomScheduleDialog
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter.RenterManageScheduleRoomAdapter
import com.ph32395.staynow_datn.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow_datn.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow_datn.hieunt.widget.gone
import com.ph32395.staynow_datn.hieunt.widget.toast
import com.ph32395.staynow_datn.hieunt.widget.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class RoomManagementFragment : BaseFragment<FragmentRoomManagementBinding, ManageScheduleRoomVM>() {
    private var manageScheduleRoomAdapter: RenterManageScheduleRoomAdapter? = null
    private var scheduleStateAdapter: ScheduleStateAdapter? = null
    override fun setViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRoomManagementBinding {
        return FragmentRoomManagementBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        scheduleStateAdapter = ScheduleStateAdapter { status ->
            viewModel.filerScheduleRoomState(status)
        }.apply {
            addList(listScheduleState)
            setSelectedState(WAIT)
        }

        manageScheduleRoomAdapter = RenterManageScheduleRoomAdapter(
            onClickWatched = {
                updateStatusRoom(it.maDatPhong, WATCHED)
            },
            onClickConfirm = { data ->
                updateStatusRoom(data.maDatPhong, CONFIRMED)
                viewModel.pushNotification(TITLE_CONFIRMED, data) { isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickLeaveSchedule = {
                UpdateRoomScheduleDialog(it, onClickConfirm = { newTime, newDate ->
                    showLoadingIfNotBaseActivity()
                    viewModel.updateScheduleRoom(
                        it.maDatPhong,
                        newTime,
                        newDate,
                        thayDoiBoiChuTro = true
                    ) { updateSuccess ->
                        if (updateSuccess) {
                            viewModel.filerScheduleRoomState(0) {
                                scheduleStateAdapter?.setSelectedState(0)
                            }
                            viewModel.pushNotification(
                                TITLE_LEAVED_BY_RENTER,
                                it.copy(thoiGianDatPhong = newTime, ngayDatPhong = newDate)
                            ) { isCompletion ->
                                toastNotification(isCompletion)
                            }
                        } else {
                            lifecycleScope.launch {
                                toast("Có lỗi xảy ra!")
                            }
                        }
                    }
                }).show(childFragmentManager, "UpdateRoomScheduleDialog")
            },
            onClickCancelSchedule = {
                updateStatusRoom(it.maDatPhong, CANCELED)
                viewModel.pushNotification(TITLE_CANCELED_BY_RENTER, it) { isCompletion ->
                    toastNotification(isCompletion)
                }
            },
            onClickCreateContract = {
                if (SharePrefUtils(requireContext()).isReadRenterInterest){
                    createContract(it.maPhongTro, it.maNguoiThue, it.maDatPhong)
                } else {
                    RenterInterestDialog{
                        createContract(it.maPhongTro, it.maNguoiThue, it.maDatPhong)
                    }.show(childFragmentManager,javaClass.name)
                }
            }
        )

        binding.rvState.adapter = scheduleStateAdapter
        binding.rvRoomState.adapter = manageScheduleRoomAdapter
    }

    override fun initClickListener() {

    }

    override fun initViewModel(): Class<ManageScheduleRoomVM> = ManageScheduleRoomVM::class.java

    override fun dataObserver() {
        showLoadingIfNotBaseActivity()
        viewModel.fetchAllScheduleByRenter(FirebaseAuth.getInstance().currentUser?.uid.toString()) {
            viewModel.filerScheduleRoomState(0)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.scheduleRoomState.collect {
                        if (it.isNotEmpty()) {
                            binding.tvNoData.gone()
                        } else {
                            binding.tvNoData.visible()
                        }
                        manageScheduleRoomAdapter?.addListObserver(it)
                        dismissLoadingIfNotBaseActivity()
                    }
                }
                launch {
                    viewModel.allScheduleRoomState.collect { allRoomStates ->
                        val newList = async(Dispatchers.IO) {
                            listScheduleState.map { scheduleState ->
                                val count = allRoomStates.filter { room -> room.trangThaiDatPhong == scheduleState.status }.size
                                scheduleState.copy(count = count)
                            }
                        }.await()
                        scheduleStateAdapter?.addListObserver(newList)
                    }
                }
            }
        }
    }

    private fun updateStatusRoom(maDatPhong: String, status: Int) {
        showLoadingIfNotBaseActivity()
        viewModel.updateScheduleRoomStatus(maDatPhong, status) { updateSuccess ->
            if (updateSuccess) {
                viewModel.filerScheduleRoomState(status) {
                    scheduleStateAdapter?.setSelectedState(status)
                    if (status == 3) binding.rvState.scrollToPosition(status)
                }
            } else {
                lifecycleScope.launch {
                    toast("Có lỗi xảy ra!")
                }
            }
        }
    }

    private fun toastNotification(isCompletion: Boolean) {
        lifecycleScope.launch {
            if (isCompletion)
                toast("Thông báo đã được gửi đến người thuê")
            else
                toast("Có lỗi xảy ra!")
        }
    }
    private fun navigateToUpdateSDT() {
        val intent = Intent(requireContext(), CapNhatThongTin::class.java)
        startActivity(intent)
    }
    private fun navigateToUpdateCCCD() {
        val intent = Intent(requireContext(), CCCD::class.java)
        startActivity(intent)
    }

    private fun navigateToUpdatePTTT() {
        val intent = Intent(requireContext(), PaymentInfoActivity::class.java)
        startActivity(intent)
    }

    private fun createContract(maPhongTro: String, maNguoiThue: String, idLichhen: String) {
        //lấy id của người dùng hiện tại
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        //lấy 2 trường statusCCCD và statusPTTT từ database realtime bảng NguoiDung
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("NguoiDung").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val statusCCCD = snapshot.child("StatusCCCD").value as? Boolean ?: false
            val statusPTTT = snapshot.child("StatusPttt").value as? Boolean ?: false
            val sdt = snapshot.child("sdt").value as? String ?: ""

            Log.d("RoomManagementFragment", "statusCCCD: $statusCCCD")
            Log.d("RoomManagementFragment", "StatusPttt: $statusPTTT")

            if (sdt == "ChuaCo")  {
                showWarningDialog(
                    context = requireContext(),
                    title = "Bạn chưa cập nhật số điện thoại",
                    content = "Hãy cập nhật số điện thoại để tiếp tục",
                    confirmAction = { navigateToUpdateSDT() }
                )
            } else if (!statusCCCD) {
                showWarningDialog(
                    context = requireContext(),
                    title = "Bạn chưa cập nhật CCCD",
                    content = "Hãy cập nhật CCCD để tiếp tục",
                    confirmAction = { navigateToUpdateCCCD() }
                )
            } else if (!statusPTTT) {
                showWarningDialog(
                    context = requireContext(),
                    title = "Bạn chưa cập nhật thông tin thanh toán",
                    content = "Hãy cập nhật PTTT để tiếp tục",
                    confirmAction = { navigateToUpdatePTTT() }
                )
            } else {
                //nếu đã cập nhật cả 2 thông tin CCCD và PTTT thì chuyển sang màn hình tạo hợp đồng
                val intent = Intent(requireContext(), TaoHopDong::class.java)
                //chuyển mã phòng trọ sang màn hình tạo hợp đồng
                intent.putExtra("maPhongTro", maPhongTro)
                intent.putExtra("maNguoiThue", maNguoiThue)
                intent.putExtra("idLichhen", idLichhen)
                startActivity(intent)
            }

        }
    }

    fun showWarningDialog(
        context: Context,
        title: String,
        content: String,
        confirmAction: () -> Unit
    ) {
        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText(title)
            .setContentText(content)
            .setConfirmText("Cập Nhật")
            .setCancelText("Không")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                confirmAction()
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
            }
            .show()
    }
}





