package com.ph32395.staynow.hieunt.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest

class NetworkCallbackHandler(private val onNetworkChange: (Boolean) -> Unit) {

    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun register(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().build()
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onNetworkChange.invoke(true)
            }

            override fun onLost(network: Network) {
                onNetworkChange.invoke(false)
            }
        }
        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    fun unregister() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            networkCallback = null
        }
    }
}

