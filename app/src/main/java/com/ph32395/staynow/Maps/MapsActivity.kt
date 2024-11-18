package com.ph32395.staynow.Maps

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.marginStart
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ActivityMapsBinding
import com.ph32395.staynow.databinding.BottomSheetCitySearchBinding
import com.ph32395.staynow.databinding.BottomSheetDialogDetaillRoomAboveMapsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.log

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastText: String? = null
    private val delay: Long = 60
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val firestore = FirebaseFirestore.getInstance()
    private val dataRoom = firestore.collection("PhongTro")
    val addresses2 = mutableListOf<PhongTroModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        //Goi y tim kiem start
        autoCompleteTextView = binding.autoComplete
//        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
//            override fun afterTextChanged(s: android.text.Editable?) {
//                val query = s.toString()
//                Log.d("zzzzz", "afterTextChanged: $query")
//
//                if (query.isNotEmpty() && query != lastText) {
//                    // Hủy bỏ tất cả các yêu cầu API trước đó
//                    handler.removeCallbacksAndMessages(null)
//                    // Cập nhật lại văn bản cuối cùng
//                    lastText = query
//                    // Gọi API ngay lập tức với độ trễ nhỏ
//                    handler.postDelayed({
//                        getLocationSuggestions(query)
//                    }, delay)
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
        //Goi y tim kiem end


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Kiểm tra quyền truy cập vị trí và yêu cầu vị trí người dùng
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("zzzzz", "onCreate: $location")
                    val userLocation =
                        LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            userLocation,
                            15f
                        )
                    )
                }
            }
        } else {
            // Yêu cầu quyền truy cập vị trí nếu chưa có
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }


        //Tìm kiem kiem dia chi
        binding.btnSearch.setOnClickListener {
            val address = binding.autoComplete.text.toString()
            if (address.isNotEmpty()) {
                addMarkersFromAddressesSearch(mMap, address, this)
            } else {
                Toast.makeText(this, "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show()
            }
            hideKeyClearFocus()

        }

        binding.autoComplete.setOnFocusChangeListener { v, hasFocus ->
            Log.d("TAGzzz", "onCreate: hasFocus-- $hasFocus")

            if (hasFocus) {
                binding.ivClearText.visibility = View.VISIBLE
                binding.ivClearText.setOnClickListener {
                    binding.autoComplete.text.clear()
                }
            } else {
                binding.ivClearText.visibility = View.GONE
            }

        }

        binding.autoComplete.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.d("TAGzzz", "onCreate: Content Text ${binding.autoComplete.text}")
                hideKeyClearFocus()
                true
            } else {
                false
            }
        }
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("zzz", "onRequestPermissionsResult: da vao")
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.uiSettings.isCompassEnabled = true
                    mMap.uiSettings.isZoomControlsEnabled = true
                    mMap.isTrafficEnabled = true
                    mMap.uiSettings.isScrollGesturesEnabled = true
                    mMap.uiSettings.isZoomGesturesEnabled = true
                    mMap.uiSettings.isRotateGesturesEnabled = true
                    mMap.uiSettings.isTiltGesturesEnabled = true


                    mMap.setOnMyLocationButtonClickListener {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            if (location != null) {
                                Log.d("zzzzz", "onMapReady: $location")
                                val userLocation =
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLocation,
                                        15f
                                    )
                                )
                            }
                        }
                        true // Trả về true nếu bạn muốn xử lý sự kiện này
                    }
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("zzzz", "onMapReady: Da vao onMapReady")
        val addresses = listOf(
            "Cốm Vòng, Cầu Giấy, Hà Nội",
            "Mỹ Đình 2, Nam Từ Liêm, Hà Nội",
            "Mỹ Đình 1, Nam Từ Liêm, Hà Nội",
            "Mai Dịch, Cầu Giấy, Hà Nội",
            "Tô Hiệu, Huyện Thường Tín, Hà Nội",
            "Suối Giàng, Huyện Văn Chấn, Yên Bái",
            "Nậm Cha, Nậm Nhùn, Lai Châu"

        )

        dataRoom.get().addOnSuccessListener {
            Log.d("TAGzzzz", "onMapReady: it ${it.documents}")
            Log.d("TAGzzzz", "onMapReady: it.toObjects ${it.toObjects(PhongTroModel::class.java)}")
            addresses2.clear()
            for (document in it.documents) {
                Log.d("TAGzzz", "onMapReady: document for $document.")
                val roomData = document.toObject(PhongTroModel::class.java)
                Log.d("TAGzzz", "onMapReady: roomData $roomData")
                Log.d("TAGzzz", "onMapReady: roomData.tenPhong ${roomData?.Ten_phongtro}")
                Log.d("TAGzzz", "onMapReady: roomData.tenPhong ${roomData?.Dia_chi}")
                val newRoomData = roomData!!.Dia_chi.removePrefix("Xã")
                Log.d("TAGzzzzzzzzz", "onMapReady: newRoomData $newRoomData")
                addresses2.add(roomData)

            }
            Log.d("TAGzzzzzz", "onMapReady: addresses2 $addresses2 ")
            addMarkersFromAddresses(mMap, addresses2, this)
            Log.d("TAGzzzzzz", "onMapReady: addresses $addresses ")
        }.addOnFailureListener {
            Log.d("TAGzzz", "onMapReady: $it")
        }
        // Bật tính năng xác định vị trí người dùng
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isCompassEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.isTrafficEnabled = true
            mMap.uiSettings.isScrollGesturesEnabled = true
            mMap.uiSettings.isZoomGesturesEnabled = true
            mMap.uiSettings.isRotateGesturesEnabled = true
            mMap.uiSettings.isTiltGesturesEnabled = true


            mMap.setOnMyLocationButtonClickListener {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        Log.d("zzzzz", "onMapReady: $location")
                        val userLocation =
                            LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                userLocation,
                                15f
                            )
                        )
                    }
                }
                true // Trả về true nếu bạn muốn xử lý sự kiện này
            }
        } else {
            // Nếu chưa có quyền, yêu cầu quyền vị trí từ người dùng
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        // Add a marker in Sydney and move the camera
        val hanoi = LatLng(21.0285, 105.8542)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 10f))
    }

    fun getCoordinatesUsingNominatim(address: String, onResult: (LatLng?) -> Unit) {
        val encodedAddress = URLEncoder.encode(address, "UTF-8")
        val url =
            "https://nominatim.openstreetmap.org/search?q=${encodedAddress}&format=json&limit=1"
        Log.d("TAGzzzzzzzz", "getCoordinatesUsingNominatim: $url")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible)")

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                CoroutineScope(Dispatchers.Main).launch {
                    // Parse JSON and extract latitude and longitude
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        val lat = firstResult.getString("lat")
                        val lon = firstResult.getString("lon")
                        Log.d("Nominatimzzzzzzzzzzz", "Latitude: $lat, Longitude: $lon")
                        onResult(LatLng(lat.toDouble(), lon.toDouble()))
                    } else {
                        Toast.makeText(
                            this@MapsActivity,
                            "Không tìm thấy địa chỉ",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d("Nominatimzzzzzzzzzzzzzz", "No results found.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(
                    "TAGzzzzz",
                    "getCoordinatesUsingNominatim: ${e.printStackTrace()} msg ${e.message.toString()}"
                )
            }
        }
    }

    fun createCustomMarkerWithLayoutXML(
        context: Context,
        text: String,
        iconResId: Int
    ): BitmapDescriptor {
        // Inflate layout từ XML
        val layoutInflater = LayoutInflater.from(context)
        val layout = layoutInflater.inflate(R.layout.custom_marker, null)

        // Cập nhật TextView với nội dung văn bản
        val textView = layout.findViewById<TextView>(R.id.markerText)
        textView.text = text

        // Cập nhật ImageView với icon
        val imageView = layout.findViewById<ImageView>(R.id.markerIcon)
        imageView.setImageResource(iconResId)

        // Đảm bảo layout có kích thước phù hợp
        layout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        layout.layout(0, 0, layout.measuredWidth, layout.measuredHeight)

        // Tạo bitmap từ layout đã được đo
        val bitmap = Bitmap.createBitmap(
            layout.measuredWidth,
            layout.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        layout.draw(canvas)  // Vẽ layout lên canvas

        // Trả về BitmapDescriptor
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    // Hàm gọi hàm trên cho nhiều địa chỉ và thêm vào bản đồ
    fun addMarkersFromAddresses(
        map: GoogleMap,
        addresses: MutableList<PhongTroModel>,
        context: Context
    ) {
        Log.d("zzzzzzzzzz", "addMarkersFromAddresses: $addresses")
        for (address in addresses) {
            val newAddressRoom = address.Dia_chi.removePrefix("Xã")
            getCoordinatesUsingNominatim(newAddressRoom) { latLng ->
                latLng?.let {
                    map.addMarker(
                        MarkerOptions()
                            .position(it)
                            .title(newAddressRoom)
                            .icon(
                                createCustomMarkerWithLayoutXML(
                                    context,
                                    newAddressRoom,
                                    R.drawable.icon_board
                                )
                            )
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLng(it))
                }
            }
        }
        map.setOnMarkerClickListener { it ->
            Log.d("TAGzzz", "addMarkersFromAddresses: ${it.title}")
            val bottomSheet = Dialog(context)
            val binding = BottomSheetDialogDetaillRoomAboveMapsBinding.inflate(layoutInflater)
            bottomSheet.setContentView(binding.root)
            bottomSheet.setCancelable(true)
            bottomSheet.window?.setDimAmount(0f)
            bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            bottomSheet.window?.attributes?.gravity = Gravity.BOTTOM
            bottomSheet.window?.attributes?.y = 350

            // Lấy `LayoutParams` và đặt `margin`
            val layoutParams = bottomSheet.window?.attributes
            layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT // Đặt chiều rộng
            bottomSheet.window?.attributes = layoutParams

            // Thêm margin vào nội dung chính của `Dialog`
            val dialogLayoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
            dialogLayoutParams.setMargins(32, 0, 32, 0) // Điều chỉnh margin (trái, trên, phải, dưới)
            binding.root.layoutParams = dialogLayoutParams

            // Tìm phòng trọ có địa chỉ trùng với marker.title
            val matchedRoom = addresses.find { room ->
                room.Dia_chi.removePrefix("Xã") == it.title
            }
            if (matchedRoom != null) {

                Log.d("TAGzzzzzzzzzz", "addMarkersFromAddresses: room for $matchedRoom ")
                binding.tvNameRoom.text = matchedRoom.Ten_phongtro
                Glide.with(this).load(matchedRoom.imageUrls[0]).into(binding.imageRoom)
                binding.tvPriceRoom.text = matchedRoom.Gia_phong.toString()
                binding.tvAddressRoom.text = matchedRoom.Dia_chi

            }

            bottomSheet.show()


            false
        }

    }

    // Hàm tìm kiếm địa chỉ
    fun addMarkersFromAddressesSearch(map: GoogleMap, address: String, context: Context) {
        getCoordinatesUsingNominatim(address) { latLng ->
            latLng?.let {
                map.addMarker(
                    MarkerOptions()
                        .position(it)
                        .title(address)
                        .icon(
                            createCustomMarkerWithLayoutXML(
                                context,
                                address,
                                R.drawable.icon_board
                            )
                        )
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }


    }


    // goi ý tim kiem
//    v3
    private fun getLocationSuggestions(query: String) {
        Log.d("zzzzz", "getLocationSuggestions: $query ")
        val client = OkHttpClient()
        val url =
            "https://nominatim.openstreetmap.org/search?q=${query}&format=json&addressdetails=1&limit=5&countrycodes=VN"

        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "YourAppName/1.0 (nguyencong04@email.com)")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MapsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("TAGzzzz", "onFailure: Error: ${e.message}")
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("TAGzzzzzzzzz", "onResponse:suggestions $response")
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val suggestions = parseSuggestions(responseBody)
                    Log.d("TAGzzzzzzzzz", "onResponse:suggestionsList $suggestions")
                    runOnUiThread {
                        val adapter = ArrayAdapter(
                            this@MapsActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            suggestions
                        )
                        autoCompleteTextView.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun parseSuggestions(responseBody: String?): List<String> {
        val suggestions = mutableListOf<String>()
        Log.d("TAGzzzzzz", "parseSuggestions: suggestions mutableListOf $suggestions")
        if (responseBody != null) {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val displayName = jsonObject.optString("display_name")
                val newDisplayName = getBasicAddress(displayName)
                Log.d("TAGzzzzzz", "parseSuggestions: newDisplayName $newDisplayName ")
                if (displayName.isNotEmpty()) {
                    suggestions.add(newDisplayName)
                }
            }
        }
        return suggestions
    }

    private fun getBasicAddress(displayName: String): String {
        val parts = displayName.split(",")
        return if (parts.size >= 3) {
            parts.dropLast(2).joinToString(", ").trim()
        } else {
            displayName
        }
    }


    //v2
//    private fun getLocationSuggestions(query: String) {
//        Log.d("zzzzz", "getLocationSuggestions: $query ")
//        val client = OkHttpClient()
//        val url =
//            "https://nominatim.openstreetmap.org/search?q=${query}&format=json&addressdetails=1&limit=5&countrycodes=VN"
//
//        val request = Request.Builder()
//            .url(url)
//            .addHeader("User-Agent", "YourAppName/1.0 (nguyencong04@email.com)")
//            .build()
//
//        client.newCall(request).enqueue(object : okhttp3.Callback {
//            override fun onFailure(call: okhttp3.Call, e: IOException) {
//                runOnUiThread {
//                    Toast.makeText(this@MapsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
//                        .show()
//                    Log.d("TAGzzzz", "onFailure: Error: ${e.message}")
//                }
//            }
//
//            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
//                Log.d("TAGzzzzzzzzz", "onResponse:suggestions $response")
//                if (response.isSuccessful) {
//                    val responseBody = response.body?.string()
//                    val suggestions = parseSuggestions(responseBody)
//                    Log.d("TAGzzzzzzzzz", "onResponse:suggestions $suggestions")
//                    runOnUiThread {
//                        val adapter = ArrayAdapter(
//                            this@MapsActivity,
//                            android.R.layout.simple_dropdown_item_1line,
//                            suggestions
//                        )
//                        autoCompleteTextView.setAdapter(adapter)
//                        adapter.notifyDataSetChanged()
//                    }
//                }
//            }
//        })
//    }
//
//    private fun parseSuggestions(responseBody: String?): List<String> {
//        val suggestions = mutableListOf<String>()
//        Log.d("TAGzzzzzz", "parseSuggestions: suggestions $suggestions")
//        if (responseBody != null) {
//            val jsonArray = JSONArray(responseBody)
//            for (i in 0 until jsonArray.length()) {
//                val jsonObject = jsonArray.getJSONObject(i)
//                val displayName = jsonObject.optString("display_name")
//                val newDisplayName = getBasicAddress(displayName)
//                Log.d("TAGzzzzzz", "parseSuggestions: newDisplayName $newDisplayName ")
//                if (newDisplayName.isNotEmpty()) {
//                    suggestions.add(newDisplayName)
//                }
//            }
//        }
//        return suggestions
//    }
//
//    private fun getBasicAddress(displayName: String): String {
//        val parts = displayName.split(",")
//        return if (parts.size >= 3) {
//            parts.dropLast(2).joinToString(", ").trim()
//        } else {
//            displayName
//        }
//    }
    private fun hideKeyClearFocus() { // dung de clear focus va an ban phim khi nhan search
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.autoComplete.windowToken, 0)
        binding.autoComplete.clearFocus() // clear focus khi nhan search
    }


}