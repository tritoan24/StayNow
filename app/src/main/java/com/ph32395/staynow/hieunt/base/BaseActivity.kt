package com.ph32395.staynow.hieunt.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.ph32395.staynow.Activity.SplashActivity
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.SCREEN
import com.ph32395.staynow.hieunt.helper.Default.IntentKeys.SPLASH_ACTIVITY
import com.ph32395.staynow.hieunt.helper.PermissionHelper
import com.ph32395.staynow.hieunt.helper.SystemUtils
import com.ph32395.staynow.hieunt.view.dialog.LoadingDialog
import com.ph32395.staynow.hieunt.view.feature.no_internet.NoInternetActivity
import com.ph32395.staynow.hieunt.widget.currentBundle
import com.ph32395.staynow.hieunt.widget.hideNavigation
import com.ph32395.staynow.hieunt.widget.hideStatusBar
import com.ph32395.staynow.hieunt.widget.launchActivity
import com.ph32395.staynow.network.NetworkCallbackHandler


abstract class BaseActivity<VB : ViewBinding, VM : ViewModel> : AppCompatActivity() {
    lateinit var binding: VB
    lateinit var viewModel: VM
    private var isRegistered = false
    private var networkCallback: NetworkCallbackHandler? = null
    val permissionHelper: PermissionHelper by lazy { PermissionHelper(this) }

    protected abstract fun setViewBinding(): VB
    private val loadingDialog by lazy { LoadingDialog(this) }
    protected abstract fun initViewModel(): Class<VM>
    protected abstract fun initView()
    protected abstract fun initClickListener()
    protected abstract fun dataObserver()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        SystemUtils.setLocale(this)
        window.hideNavigation()
        window.hideStatusBar()
        super.onCreate(savedInstanceState)
        binding = setViewBinding()
        //onBackPress
        onBackPressedDispatcher.addCallback(this) {
            isEnabled = false
            onBackPressedSystem()
            isEnabled = true
        }
        setContentView(binding.root)
        //view_model
        viewModel = ViewModelProvider(this)[initViewModel()]
        //internet
        networkCallback = NetworkCallbackHandler {
            if (!it) {
                if (this !is NoInternetActivity) {
                    launchActivity(NoInternetActivity::class.java)
                }
            } else {
                if (this is NoInternetActivity && this.currentBundle()
                        ?.getString(SCREEN) != SPLASH_ACTIVITY
                ) {
                    finish()
                } else if (this is NoInternetActivity && this.currentBundle()
                        ?.getString(SCREEN) == SPLASH_ACTIVITY
                ) {
                    val myIntent = Intent(this, SplashActivity::class.java)
                    myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(myIntent)
                }
            }
        }
        networkCallback?.register(this)
        initView()
        initClickListener()
        dataObserver()
    }

    override fun onResume() {
        super.onResume()
        window.hideStatusBar()
        window.hideNavigation()
    }

    open fun onBackPressedSystem() {
        onBackPressedDispatcher.onBackPressed()
    }

    fun showLoading() {
        runOnUiThread {
            if (loadingDialog.isShowing.not())
                loadingDialog.show()
        }
    }

    fun dismissLoading() {
        runOnUiThread {
            if (loadingDialog.isShowing) loadingDialog.dismiss()
        }
    }

//    open fun popBackStack(
//        destinationId: Int? = null,
//        inclusive: Boolean = false
//    ) {
//        if (destinationId != null) {
//            findNavController(R.id.fcv_app).popBackStack(destinationId, inclusive)
//        } else {
//            findNavController(R.id.fcv_app).popBackStack()
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.unregister()
    }

}