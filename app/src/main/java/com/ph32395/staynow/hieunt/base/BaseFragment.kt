package com.ph32395.staynow.hieunt.base

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ph32395.staynow.hieunt.helper.PermissionHelper
import com.ph32395.staynow.hieunt.helper.SystemUtils
import com.ph32395.staynow.hieunt.view.dialog.LoadingDialog

abstract class BaseFragment<VB : ViewBinding,VM : ViewModel> : Fragment() {
    private var _binding: VB? = null
    lateinit var binding: VB
    lateinit var viewModel: VM
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    val permissionHelper: PermissionHelper by lazy { PermissionHelper(requireActivity()) }

    protected abstract fun setViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected abstract fun initView()
    protected abstract fun initClickListener()
    protected abstract fun initViewModel(): Class<VM>
    protected abstract fun dataObserver()

    fun hideSoftKeyboard(activity: Activity) {
        activity.currentFocus?.let {
            val inputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        SystemUtils.setLocale(activity)
        _binding?.let {
            binding = it
        } ?: run {
            _binding = setViewBinding(inflater, container)
            binding = _binding!!
        }
        initView()
        initClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[initViewModel()]
        dataObserver()
    }

    fun safeNavigate(
        @IdRes resId: Int,
        args: Bundle? = null,
    ) {
        val navController = findNavControllerOrNull()
        navController?.navigate(resId, args)
    }

    private fun findNavControllerOrNull(): NavController? {
        return try {
            findNavController()
        } catch (e: Exception) {
            null
        }
    }

    fun popBackStack(
        destinationId: Int? = null,
        inclusive: Boolean = false
    ) {
        findNavControllerOrNull()?.let {
            if (destinationId != null) {
                it.popBackStack(destinationId, inclusive)
            } else {
                it.popBackStack()
            }
        }
    }

    fun replaceFragment(id: Int, fragment: Fragment) {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(id, fragment)
        ft.commit()
    }

    fun showLoading() {
        (activity as? BaseActivity<*, *>)?.showLoading()
    }

    fun dismissLoading() {
        (activity as? BaseActivity<*, *>)?.dismissLoading()
    }

    fun showLoadingIfNotBaseActivity(){
        if (loadingDialog.isShowing.not()) loadingDialog.show()
    }

    fun dismissLoadingIfNotBaseActivity(){
        if (loadingDialog.isShowing) loadingDialog.dismiss()
    }

    fun showPopupWindow(view: View, popupWindow: PopupWindow) {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val positionOfIcon = location[1]

        val displayMetrics = requireContext().resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(view, -22, -(view.height * 7), Gravity.BOTTOM or Gravity.END)
        } else {
            popupWindow.showAsDropDown(view, -22, 0, Gravity.TOP or Gravity.END)
        }
    }

}