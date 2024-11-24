package com.ph32395.staynow.fragment

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
import com.ph32395.staynow.CCCD.CCCD
import com.ph32395.staynow.TaoHopDong.TaoHopDong
import com.ph32395.staynow.ThongTinThanhToan.PaymentInfoActivity
import com.ph32395.staynow.databinding.FragmentRoomManagementBinding
import com.ph32395.staynow.hieunt.base.BaseFragment
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CANCELED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.CONFIRMED
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WAIT
import com.ph32395.staynow.hieunt.helper.Default.StatusRoom.WATCHED
import com.ph32395.staynow.hieunt.helper.Default.listScheduleState
import com.ph32395.staynow.hieunt.view.dialog.UpdateRoomScheduleDialog
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.RenterManageScheduleRoomAdapter
import com.ph32395.staynow.hieunt.view.feature.manage_schedule_room.adapter.ScheduleStateAdapter
import com.ph32395.staynow.hieunt.view_model.ManageScheduleRoomVM
import com.ph32395.staynow.hieunt.widget.gone
import com.ph32395.staynow.hieunt.widget.toast
import com.ph32395.staynow.hieunt.widget.visible
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
                updateStatusRoom(it.roomScheduleId, WATCHED)
            },
            onClickConfirm = {
                updateStatusRoom(it.roomScheduleId, CONFIRMED)
            },
            onClickLeaveSchedule = {
                UpdateRoomScheduleDialog(it,onClickConfirm = { newTime, newDate ->
                    showLoading()
                        viewModel.updateScheduleRoom(it.roomScheduleId, newTime, newDate, isChangedScheduleByRenter = true) { updateSuccess ->
                        if (updateSuccess) {
                            viewModel.filerScheduleRoomState(0) {
                                scheduleStateAdapter?.setSelectedState(0)
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
                updateStatusRoom(it.roomScheduleId, CANCELED)
            },
            onClickCreateContract = {
               createContract(it.roomId,it.renterId);
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
            viewModel.filerScheduleRoomState(WAIT)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
        }
    }

    private fun updateStatusRoom(roomScheduleId: String, status: Int) {
        showLoadingIfNotBaseActivity()
        viewModel.updateScheduleRoomStatus(roomScheduleId, status) { updateSuccess ->
            if (updateSuccess) {
                viewModel.filerScheduleRoomState(status) {
                    scheduleStateAdapter?.setSelectedState(status)
                }
            } else {
                lifecycleScope.launch {
                    toast("Có lỗi khi hủy!")
                }
            }
        }
    }
    private fun navigateToUpdateCCCD() {
        val intent = Intent(requireContext(), CCCD::class.java)
        startActivity(intent)
    }
    private fun navigateToUpdatePTTT() {
        val intent = Intent(requireContext(),PaymentInfoActivity::class.java)
        startActivity(intent)
    }

    private fun createContract(maPhongTro: String, maNguoiThue: String) {
    //lấy id của người dùng hiện tại
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        //lấy 2 trường statusCCCD và statusPTTT từ database realtime bảng NguoiDung
        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("NguoiDung").child(userId)
        userRef.get().addOnSuccessListener { snapshot ->
            val statusCCCD = snapshot.child("StatusCCCD").value as? Boolean ?: false
            val statusPTTT = snapshot.child("StatusPttt").value as? Boolean ?: false
            Log.d("RoomManagementFragment", "statusCCCD: $statusCCCD")
            Log.d("RoomManagementFragment", "StatusPttt: $statusPTTT")

            if (!statusCCCD) {
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
            }
                else{
                    //nếu đã cập nhật cả 2 thông tin CCCD và PTTT thì chuyển sang màn hình tạo hợp đồng
                    val intent = Intent(requireContext(), TaoHopDong::class.java)
                //chuyển mã phòng trọ sang màn hình tạo hợp đồng
                intent.putExtra("maPhongTro",maPhongTro )
                intent.putExtra("maNguoiThue",maNguoiThue )

                    startActivity(intent)


                }

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



