package com.ph32395.staynow.Maps

import android.Manifest
import android.annotation.SuppressLint
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ph32395.staynow.ChucNangTimKiem.BottomSheetFilter
import com.ph32395.staynow.ChucNangTimKiem.BottomSheetFragment
import com.ph32395.staynow.Model.PhongTroModel
import com.ph32395.staynow.R
import com.ph32395.staynow.databinding.ActivityMapsBinding
import com.ph32395.staynow.databinding.BottomSheetDialogDetaillRoomAboveMapsBinding
import com.ph32395.staynow.fragment.home.PhongTroAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    BottomSheetFragment.PriceRangeListener, BottomSheetFilter.FilterCriteriaListener {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastText: String? = null
    private val delay: Long = 60
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private val firestore = FirebaseFirestore.getInstance()
    private val dataRoom = firestore.collection("PhongTro")
    var addresses2 = mutableListOf<PhongTroModel>()
    var listRoom = mutableListOf<PhongTroModel>()
    val suggestionRoom = mutableListOf<PhongTroModel>()
    private val currentMarkers = mutableListOf<Marker>()
    var selectedTypesViewModel: MutableList<String> = mutableListOf()
    var selectedTienNghiViewModel: MutableList<String> = mutableListOf()
    var selectedNoiThatViewModel: MutableList<String> = mutableListOf()
    var min: Int = 0
    var max: Int = 0
    private val TAG = "zzzzzzMapsActivityzzzzzz"
    private val apiKeys = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        val apiKey = applicationContext.getString(R.string.api_key_1)
        val apiKey2 = applicationContext.getString(R.string.api_key_2)
        val apiKey3 = applicationContext.getString(R.string.api_key_3)
        val apiKey4 = applicationContext.getString(R.string.api_key_4)
        val apiKey5 = applicationContext.getString(R.string.api_key_5)
        val apiKey6 = applicationContext.getString(R.string.api_key_6)
        val apiKey7 = applicationContext.getString(R.string.api_key_7)
        val apiKey8 = applicationContext.getString(R.string.api_key_8)
        val apiKey9 = applicationContext.getString(R.string.api_key_9)
        val apiKey10 = applicationContext.getString(R.string.api_key_10)
        val apiKey11 = applicationContext.getString(R.string.api_key_11)
        val apiKey12 = applicationContext.getString(R.string.api_key_12)
        val apiKey13 = applicationContext.getString(R.string.api_key_13)
        val apiKey14 = applicationContext.getString(R.string.api_key_14)
        val apiKey15 = applicationContext.getString(R.string.api_key_15)
        val apiKey16 = applicationContext.getString(R.string.api_key_16)
        val apiKey17 = applicationContext.getString(R.string.api_key_17)
        val apiKey18 = applicationContext.getString(R.string.api_key_18)
        val apiKey19 = applicationContext.getString(R.string.api_key_19)
        val apiKey20 = applicationContext.getString(R.string.api_key_20)
        val apiKey21 = applicationContext.getString(R.string.api_key_21)
        val apiKey22 = applicationContext.getString(R.string.api_key_22)
        val apiKey23 = applicationContext.getString(R.string.api_key_23)
        val apiKey24 = applicationContext.getString(R.string.api_key_24)
        apiKeys.add(apiKey)
        apiKeys.add(apiKey2)
        apiKeys.add(apiKey3)
        apiKeys.add(apiKey4)
        apiKeys.add(apiKey5)
        apiKeys.add(apiKey6)
        apiKeys.add(apiKey7)
        apiKeys.add(apiKey8)
        apiKeys.add(apiKey9)
        apiKeys.add(apiKey10)
        apiKeys.add(apiKey11)
        apiKeys.add(apiKey12)
        apiKeys.add(apiKey13)
        apiKeys.add(apiKey14)
        apiKeys.add(apiKey15)
        apiKeys.add(apiKey16)
        apiKeys.add(apiKey17)
        apiKeys.add(apiKey18)
        apiKeys.add(apiKey19)
        apiKeys.add(apiKey20)
        apiKeys.add(apiKey21)
        apiKeys.add(apiKey22)
        apiKeys.add(apiKey23)
        apiKeys.add(apiKey24)
        //Goi y tim kiem start
        autoCompleteTextView = binding.autoComplete
        autoCompleteTextView.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                fetchSuggestions(s.toString())
                suggestionsRoom(s.toString().trim(), listRoom)

            }
        })
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
//                addMarkersFromAddressesSearch(mMap, address, this)
                searchRoomByNameOrDescription(address)
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
        binding.btnKhoangGia.setOnClickListener {
            val priceRangeBottomSheet = BottomSheetFragment()
            priceRangeBottomSheet.show(supportFragmentManager, priceRangeBottomSheet.tag)
            priceRangeBottomSheet.updatePriceRange(min, max)
        }
        binding.ivFilter.setOnClickListener {
            val bottomSheetFragment = BottomSheetFilter()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            Log.d("zzTAGzz", "onCreate: selectedTypesViewModel$selectedTypesViewModel")
            Log.d("zzTAGzz", "onCreate: selectedTienNghiViewModel$selectedTienNghiViewModel")
            Log.d("zzTAGzz", "onCreate: selectedNoiThatViewModel$selectedNoiThatViewModel")
            bottomSheetFragment.updateFilter(
                selectedTypesViewModel,
                selectedNoiThatViewModel,
                selectedTienNghiViewModel
            )
        }
        binding.btnCity.setOnClickListener {
            readListRoomPingMap()
        }


    }

    private var currentKeyIndex = 0
    private var usageMap = mutableMapOf<String, Int>() // Theo dõi số lượng requests mỗi key

    init {
        apiKeys.forEach { usageMap[it] = 0 } // Khởi tạo bộ đếm cho mỗi key
    }

    private fun getCurrentApiKey(): String {
        return apiKeys[currentKeyIndex]
    }

    private fun rotateApiKey() {
        currentKeyIndex = (currentKeyIndex + 1) % apiKeys.size
        Log.d("API_KEY_SWITCH", "Chuyển sang API Key: ${apiKeys[currentKeyIndex]}")
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
                    mMap.setPadding(0, 300, 0, 0)

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
        mMap.setPadding(0, 400, 0, 0)
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
                val trangThaiDiaChi = document.getBoolean("Trang_thaidc")
                if (trangThaiDiaChi == true) {
                    Log.d("zzzzzzzTAGzzzzz", "onMapReady: roomData cos dk true $roomData ")
                    addresses2.add(roomData!!)
                    listRoom.add(roomData)
                } else {
                    Log.d("zzzzzzzTAGzzzzz", "onMapReady: roomData cos dk false $roomData ")
                }
                Log.d("TAGzzz", "onMapReady: $trangThaiDiaChi ")

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


    fun getCoordinatesUsingNominatimGoong(address: String, onResult: (LatLng?) -> Unit) {

        val apiKey = getCurrentApiKey()
        RetrofitInstance.api.getGeocode(apiKey, address)
            .enqueue(object : Callback<GeocodeResponse> {
                override fun onResponse(
                    call: Call<GeocodeResponse>,
                    response: Response<GeocodeResponse>
                ) {
                    val currentKey = getCurrentApiKey()

                    // Kiểm tra giới hạn trong header
                    val remainingRequests =
                        response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()
                    if (remainingRequests != null) {
                        usageMap[currentKey] = 998 - remainingRequests
                        Log.d("RateLimit", "Key $currentKey còn $remainingRequests requests")
                    }

                    // Nếu vượt quá giới hạn, chuyển sang key mới
                    if (remainingRequests != null && remainingRequests <= 0) {
                        rotateApiKey()
                        getCoordinatesUsingNominatim(address, onResult) // Gọi lại với key mới
                        return
                    }
                    Log.d(TAG, "onResponse:getCoordinatesUsingNominatim $response")
                    if (response.isSuccessful || response.body()?.status == "OK") {

                        val data = response.body()?.results
                        val firstResult = data?.first()
                        Log.d(TAG, "onResponse getCoordinatesUsingNominatim: address $address")
                        Log.d(TAG, "onResponse getCoordinatesUsingNominatim: data $data")
                        onResult(
                            LatLng(
                                firstResult!!.geometry.location.lat.toDouble(),
                                firstResult.geometry.location.lng.toDouble()
                            )
                        )


                    } else {
                        Log.e(TAG, "onResponse: Error Retrofit")
                    }

                }

                override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: Error ${t.message.toString()}")
                }
            })


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
        clearMarkers()
        Log.d("zzzzzzzzzz", "addMarkersFromAddresses: $addresses")
        for (address in addresses) {
            val newAddressRoom = address.Dia_chi.removePrefix("Xã")
            getCoordinatesUsingNominatimGoong(newAddressRoom) { latLng ->
                latLng?.let {
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(it)
                            .icon(
                                createCustomMarkerWithLayoutXML(
                                    context,
                                    newAddressRoom,
                                    R.drawable.icon_board
                                )
                            )
                    )
                    marker?.tag = newAddressRoom
                    currentMarkers.add(marker!!)
                    map.animateCamera(CameraUpdateFactory.newLatLng(it))
                }
            }
        }
        map.setOnMarkerClickListener { it ->
            Log.d("zzTAGzzz", "addMarkersFromAddresses: ${it.title}")
            Log.d("zzTAGzzz", "addMarkersFromAddresses: it.id ${it.id}")
            Log.d("zzTAGzzz", "addMarkersFromAddresses: it.tag ${it.tag}")
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
            dialogLayoutParams.setMargins(
                32,
                0,
                32,
                0
            ) // Điều chỉnh margin (trái, trên, phải, dưới)
            binding.root.layoutParams = dialogLayoutParams
            Log.d("zzzzzTAGzzz", "addMarkersFromAddresses:addresses map dialog $addresses")
            // Tìm phòng trọ có địa chỉ trùng với marker.title
            val matchedRoom = addresses.find { room ->
                room.Dia_chi.removePrefix("Xã") == it.tag
            }
            if (matchedRoom != null) {

                Log.d("TAGzzzzzzzzzz", "addMarkersFromAddresses: room for $matchedRoom ")
                binding.tvNameRoom.text = matchedRoom.Ten_phongtro
                Glide.with(this).load(matchedRoom.imageUrls[0]).into(binding.imageRoom)
                binding.tvPriceRoom.text =
                    "${formatToVietnameseCurrency(matchedRoom.Gia_phong)} VND"
                binding.tvAddressRoom.text = matchedRoom.Dia_chi

            }

            bottomSheet.show()


            false
        }

    }

    // Hàm add marker địa chỉ
    fun addMarkersFromAddressesSearch(map: GoogleMap, address: String, context: Context) {
        getCoordinatesUsingNominatimGoong(address) { latLng ->
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


    // goi ý tim kiem dia chi maps
    private fun fetchSuggestions(query: String) {
        if (query.length < 2) return

        val apiKey = getCurrentApiKey()
        RetrofitInstance.api.getPlace(apiKey, query)
            .enqueue(object : Callback<SuggestionResponse> {
                override fun onResponse(
                    call: Call<SuggestionResponse>,
                    response: Response<SuggestionResponse>
                ) {
                    val currentKey = getCurrentApiKey()

                    // Kiểm tra giới hạn trong header
                    val remainingRequests =
                        response.headers()["X-RateLimit-Remaining"]?.toIntOrNull()
                    if (remainingRequests != null) {
                        usageMap[currentKey] = 998 - remainingRequests
                        Log.d("RateLimit", "Key $currentKey còn $remainingRequests requests")
                    }

                    // Nếu vượt quá giới hạn, chuyển sang key mới
                    if (remainingRequests != null && remainingRequests <= 0) {
                        rotateApiKey()
                        fetchSuggestions(query) // Gọi lại với key mới
                        return
                    }
                    Log.d(TAG, "onResponse: response $response")
                    if (response.isSuccessful && response.body()?.status == "OK") {
                        val suggestions = response.body()?.predictions ?: emptyList()
                        Log.d(TAG, "onResponse: suggestions $suggestions ")
                        val list = suggestions.map { it.description }
                        Log.d(TAG, "onResponse: list $list")
                        val adapter = ArrayAdapter(
                            this@MapsActivity,
                            android.R.layout.simple_list_item_1,
                            list
                        )
                        binding.autoComplete.setAdapter(adapter)
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.e("Retrofit", "Response error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<SuggestionResponse>, t: Throwable) {
                    Log.e("Retrofit", "API call failed: ${t.message}")
                }
            })
    }

    //suggestionsRoom
    private fun suggestionsRoom(query: String, listRoom: MutableList<PhongTroModel>) {

        // Chia chuỗi tìm kiếm thành các từ và loại bỏ các mục rỗng
        val queryWords = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        Log.d(TAG, "suggestionsRoom: $queryWords")
        suggestionRoom.clear()
        listRoom.forEach { room ->
            // Kiểm tra xem chuỗi tìm kiếm có nằm trong tên hoặc mô tả
            val queryInDescriptionOrName =
                room.Ten_phongtro.contains(query, ignoreCase = true) ||
                        room.Mota_chitiet?.contains(query, ignoreCase = true) == true

            // Kiểm tra nếu tất cả các từ trong queryWords xuất hiện trong tên hoặc mô tả
            val allWordsMatch = queryWords.all { word ->
                room.Ten_phongtro.contains(word, ignoreCase = true) ||
                        room.Mota_chitiet?.contains(word, ignoreCase = true) == true
            }

            // Nếu một trong hai điều kiện đúng, thêm phòng trọ vào danh sách gợi ý
            if (queryInDescriptionOrName || allWordsMatch) {
                suggestionRoom.add(room)
            }
        }

        Log.d(TAG, "Suggestions: ${suggestionRoom.map { it.Ten_phongtro }}")

        if (suggestionRoom.isNotEmpty()) {
            val listName = suggestionRoom.map { "${it.Ten_phongtro} ${it.Mota_chitiet}" }
            Log.d(TAG, "suggestionsRoom:listName $listName")
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listName)
            Log.d(TAG, "suggestionsRoom:adapter.count ${adapter.count}")
            binding.autoComplete.setAdapter(adapter)
            binding.autoComplete.showDropDown()
        } else {
            Log.d(TAG, "No suggestions found")
        }


    }


    //Search name room
    @SuppressLint("NotifyDataSetChanged")
    fun searchRoomByNameOrDescription(query: String) {
        val queryWords = query.split(" ").filter { it.isNotEmpty() }
        val listSearch = mutableListOf<PhongTroModel>()
        Log.d(TAG, "onDataChange: queryWords $queryWords")
        Log.d(TAG, "onDataChange: query $query")

        // Truy vấn tất cả các phòng trọ một lần
        dataRoom.get().addOnSuccessListener { snapshot ->
            val tasks = mutableListOf<Task<QuerySnapshot>>()

            for (document in snapshot.documents) {
                val id = document.id.toString()
                val roomData = document.toObject(PhongTroModel::class.java)
                Log.d(TAG, "searchRoomByNameOrDescription: room data $roomData")

                // Truy vấn chi tiết thông tin diện tích
                val task = firestore.collection("ChiTietThongTin")
                    .whereEqualTo("ma_phongtro", id) // Truy vấn theo mã phòng trọ
                    .whereEqualTo("ten_thongtin", "Diện tích") // Lọc theo thông tin "Diện tích"
                    .get()
                    .addOnSuccessListener { chiTietSnapshot ->
                        val chiTiet = chiTietSnapshot.documents.firstOrNull()
                        val dienTich = chiTiet?.getDouble("so_luong_donvi") // Lấy giá trị diện tích
                        Log.d(TAG, "searchRoomByNameOrDescription: chi tiet $chiTiet")
                        Log.d(TAG, "searchRoomByNameOrDescription: dien tich $dienTich")
                        roomData?.Dien_tich = dienTich?.toLong()

                        val roomName = roomData?.Ten_phongtro ?: ""
                        val roomDescription = roomData?.Mota_chitiet ?: ""

                        // Kiểm tra nếu toàn bộ chuỗi `query` xuất hiện trong tên hoặc mô tả
                        val queryInDescriptionOrName =
                            roomName.contains(query, ignoreCase = true) ||
                                    roomDescription.contains(query, ignoreCase = true)

                        // Kiểm tra nếu tất cả các từ trong `queryWords` xuất hiện trong tên hoặc mô tả
                        val allWordsMatch = queryWords.all { word ->
                            roomName.contains(word, ignoreCase = true) || roomDescription.contains(
                                word,
                                ignoreCase = true
                            )
                        }

                        // Thêm phòng trọ vào danh sách nếu một trong hai điều kiện đúng
                        if (queryInDescriptionOrName || allWordsMatch) {
                            listSearch.add(roomData!!)
                            Log.d(
                                TAG,
                                "onDataChange: Room $roomData (tìm kiếm chi tiết hoặc tương đối)"
                            )
                        }
                        Log.d(TAG, "searchRoomByNameOrDescription: listSearch $listSearch")
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error fetching room details: ${exception.message}")
                    }

                tasks.add(task)
            }

            // Đảm bảo tất cả các truy vấn đã hoàn thành
            addresses2.clear()
            Tasks.whenAllSuccess<QuerySnapshot>(tasks).addOnCompleteListener {
                // Sau khi tất cả các truy vấn hoàn thành, cập nhật giao diện
                Log.d(TAG, "searchRoomByNameOrDescription: listSearch Tasks $listSearch")
                addresses2 = listSearch
                addMarkersFromAddresses(mMap, addresses2, this)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error getting documents: ", exception)
        }
    }


    private fun hideKeyClearFocus() { // dung de clear focus va an ban phim khi nhan search
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.autoComplete.windowToken, 0)
        binding.autoComplete.clearFocus() // clear focus khi nhan search
    }

    //readList room
    private fun readListRoomPingMap(){
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
                val trangThaiDiaChi = document.getBoolean("Trang_thaidc")
                if (trangThaiDiaChi == true) {
                    Log.d("zzzzzzzTAGzzzzz", "onMapReady: roomData cos dk true $roomData ")
                    addresses2.add(roomData!!)
                } else {
                    Log.d("zzzzzzzTAGzzzzz", "onMapReady: roomData cos dk false $roomData ")
                }
                Log.d("TAGzzz", "onMapReady: $trangThaiDiaChi ")

            }
            Log.d("TAGzzzzzz", "onMapReady: addresses2 $addresses2 ")
            addMarkersFromAddresses(mMap, addresses2, this)
        }.addOnFailureListener {
            Log.d("TAGzzz", "onMapReady: $it")
        }
    }

    private fun clearMarkers() {
        for (marker in currentMarkers) {
            marker.remove() // Xóa marker khỏi bản đồ
        }
        currentMarkers.clear() // Xóa danh sách các marker đã lưu
    }

    fun formatToVietnameseCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val formattedValue = format.format(amount)

        // Loại bỏ ký hiệu "₫" và dấu phân cách hàng nghìn
        return formattedValue.replace("₫", "").replace(",", "").trim()
    }

    //loc theo khoang gia
    override fun onPriceRangeSelected(minPrice: Int, maxPrice: Int) {

        Log.d("zzzTAGzzz", "onPriceRangeSelected:min cu bo $min ")
        Log.d("zzzTAGzzz", "onPriceRangeSelected:max cu bo $min ")
        Log.d("zzzTAGzzz", "onPriceRangeSelected:minPrice  $minPrice ")
        Log.d("zzzTAGzzz", "onPriceRangeSelected:maxPrice  $maxPrice ")
        dataRoom.whereGreaterThanOrEqualTo("Gia_phong", minPrice.toDouble())
            .whereLessThanOrEqualTo("Gia_phong", maxPrice.toDouble())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tasks =
                    mutableListOf<Task<QuerySnapshot>>() // Danh sách các tác vụ truy vấn phụ
                val newListRoomAdds = mutableListOf<PhongTroModel>()
                for (document in querySnapshot) {
                    val id = document.id
                    val roomData = document.toObject(PhongTroModel::class.java)
//                    val trangThaiDiaChi = document.getBoolean("Trang_thaidc")
//                    Log.d("zzzzzTAGzzzzzzzz", "onPriceRangeSelected: trang thai chi tiet $trangThaiDiaChi ")

                    // Thêm truy vấn phụ vào danh sách
                    val task = firestore.collection("ChiTietThongTin")
                        .whereEqualTo("ma_phongtro", id)
                        .whereEqualTo("ten_thongtin", "Diện tích")
                        .get()
                        .addOnSuccessListener { detailSnapshot ->
                            val chiTiet = detailSnapshot.documents.firstOrNull()
                            val dienTich = chiTiet?.getDouble("so_luong_donvi")
                            roomData.Dien_tich = dienTich?.toLong()
                            // Thêm dữ liệu vào danh sách
                            val trangThaiDiaChi = document.getBoolean("Trang_thaidc")
                            Log.d(
                                "zzzzzTAGzzzzzzzz",
                                "onPriceRangeSelected: trang thai chi tiet trong task $trangThaiDiaChi "
                            )
                            if (trangThaiDiaChi == true) {
                                Log.d("zzzTAGzz", "onPriceRangeSelected: Room dc true $roomData")
                                newListRoomAdds.add(roomData)
                                Log.d("zzzTAGzzz", "onPriceRangeSelected: $newListRoomAdds")
                                addresses2 = newListRoomAdds
                            } else {
                                Log.d("zzzTAGzz", "onPriceRangeSelected: Room dc false $roomData")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "zzzTAGzzz",
                                "Error fetching ChiTietThongTin for room $id: ${e.message}"
                            )
                        }
                    tasks.add(task)
                }

                // Chờ tất cả truy vấn hoàn thành
                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    Log.d("TAGzzzzzzzzzz", "onPriceRangeSelected: it task $it ")
                    if (it.isEmpty()) {
                        Log.d("zzzzTAGzzzzz", "onPriceRangeSelected: list address null ")
                        Toast.makeText(
                            this,
                            "Not room price ${formatToVietnameseCurrency(minPrice.toDouble())} VND arrive ${
                                formatToVietnameseCurrency(
                                    maxPrice.toDouble()
                                )
                            } VND",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        addMarkersFromAddresses(mMap, addresses2, this)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("zzzzzzzTAGzzzzzzz", "Error fetching rooms: ${e.message}")
            }


        min = minPrice
        max = maxPrice

    }

    //lọc theo tien nghi noi that
    override fun onFilterSelected(
        selectedTypes: MutableList<String>,
        selectedTienNghi: MutableList<String>,
        selectedNoiThat: MutableList<String>
    ) {

        selectedTypesViewModel = selectedTypes
        selectedNoiThatViewModel = selectedNoiThat
        selectedTienNghiViewModel = selectedTienNghi
        Log.d(TAG, "onFilterSelected:selectedTypes $selectedTypes")
        Log.d(TAG, "onFilterSelected:selectedTienNghi $selectedTienNghi")
        Log.d(TAG, "onFilterSelected:selectedNoiThat $selectedNoiThat")

//        val firestore = FirebaseFirestore.getInstance()

        var loaiPhongTask: Task<QuerySnapshot>? = null
        loaiPhongTask = if (selectedTypes.isNotEmpty()) {
            firestore.collection("LoaiPhong")
                .whereIn("Ten_loaiphong", selectedTypes)
                .get()
        } else {
            firestore.collection("LoaiPhong").get() // Lấy tất cả nếu không có chọn
        }

        // Lọc TienNghi nếu có tiêu chí selectedTienNghi
        var tienNghiTask: Task<QuerySnapshot>? = null
        tienNghiTask = if (selectedTienNghi.isNotEmpty()) {
            firestore.collection("TienNghi")
                .whereIn("Ten_tiennghi", selectedTienNghi)
                .get()
        } else {
            firestore.collection("TienNghi").get() // Lấy tất cả nếu không có chọn
        }

        // Lọc NoiThat nếu có tiêu chí selectedNoiThat
        var noiThatTask: Task<QuerySnapshot>? = null
        noiThatTask = if (selectedNoiThat.isNotEmpty()) {
            firestore.collection("NoiThat")
                .whereIn("Ten_noithat", selectedNoiThat)
                .get()
        } else {
            firestore.collection("NoiThat").get() // Lấy tất cả nếu không có chọn
        }


        // Chờ tất cả các truy vấn hoàn thành
        val tasks = Tasks.whenAllComplete(loaiPhongTask, tienNghiTask, noiThatTask)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Xử lý kết quả của LoaiPhong
                    val maLoaiPhongList =
                        loaiPhongTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã loại phòng: $maLoaiPhongList")

                    // Xử lý kết quả của TienNghi
                    val maTienNghiList =
                        tienNghiTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã tiện nghi: $maTienNghiList")

                    // Xử lý kết quả của NoiThat
                    val maNoiThatList = noiThatTask.result?.documents?.map { it.id } ?: emptyList()
                    Log.d(TAG, "Mã nội thất: $maNoiThatList")

                    // Truy vấn bảng PhongTro và lọc theo mã loại phòng
                    firestore.collection("PhongTro")
                        .whereIn("Ma_loaiphong", maLoaiPhongList)
                        .get()
                        .addOnSuccessListener { phongTroSnapshot ->
                            val phongTroIds = phongTroSnapshot.documents.map { it.id }
                            Log.d(TAG, "Danh sách phòng trọ với loại phòng: $phongTroIds")

                            // Lọc các phòng trọ có nội thất
                            firestore.collection("PhongTroNoiThat")
                                .whereIn("ma_noithat", maNoiThatList)
                                .get()
                                .addOnSuccessListener { phongTroNoiThatSnapshot ->
                                    val phongTroWithNoiThatIds =
                                        phongTroNoiThatSnapshot.documents.map { it.getString("ma_phongtro") }
                                    Log.d(
                                        TAG,
                                        "Danh sách phòng trọ có nội thất: $phongTroWithNoiThatIds"
                                    )

                                    // Lọc các phòng trọ có tiện nghi
                                    firestore.collection("PhongTroTienNghi")
                                        .whereIn("ma_tiennghi", maTienNghiList)
                                        .get()
                                        .addOnSuccessListener { phongTroTienNghiSnapshot ->
                                            val phongTroWithTienNghiIds =
                                                phongTroTienNghiSnapshot.documents.map {
                                                    it.getString("ma_phongtro")
                                                }
                                            Log.d(
                                                TAG,
                                                "Danh sách phòng trọ có tiện nghi: $phongTroWithTienNghiIds"
                                            )
                                            // Tìm giao của ba danh sách phòng trọ
                                            val finalRooms = phongTroIds
                                                .intersect(phongTroWithNoiThatIds.toSet())
                                                .intersect(phongTroWithTienNghiIds.toSet())
                                            Log.d(TAG, "Danh sách phòng trọ cuối cùng: $finalRooms")
                                            if (finalRooms.isEmpty()) {
//                                                binding.layoutNullMsg.visibility = View.VISIBLE
//                                                binding.rvListRoom.visibility = View.GONE
                                                Toast.makeText(
                                                    this,
                                                    "Not found room filter",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val newListRoom = mutableListOf<PhongTroModel>()
                                                firestore.collection("PhongTro")
                                                    .whereIn(
                                                        FieldPath.documentId(),
                                                        finalRooms.toList()
                                                    ) // Lọc theo danh sách ID
                                                    .get()
                                                    .addOnSuccessListener { querySnapshot ->
                                                        // Chuyển dữ liệu từ querySnapshot thành list các đối tượng phòng trọ
                                                        for (document in querySnapshot) {
                                                            val id = document.id
                                                            val roomData =
                                                                document.toObject(PhongTroModel::class.java)
                                                            Log.d(
                                                                TAG,
                                                                "onFilterSelected: roomData $roomData"
                                                            )
                                                            firestore.collection("ChiTietThongTin")
                                                                .whereEqualTo(
                                                                    "ma_phongtro",
                                                                    id
                                                                ) // Truy vấn theo mã phòng trọ
                                                                .whereEqualTo(
                                                                    "ten_thongtin",
                                                                    "Diện tích"
                                                                ) // Lọc theo thông tin "Diện tích"
                                                                .get()
                                                                .addOnSuccessListener { chiTietSnapshot ->
                                                                    val chiTiet =
                                                                        chiTietSnapshot.documents.firstOrNull()
                                                                    val dienTich =
                                                                        chiTiet?.getDouble("so_luong_donvi") // Lấy giá trị diện tích

                                                                    // Cập nhật diện tích vào đối tượng phòng
                                                                    roomData.Dien_tich =
                                                                        dienTich?.toLong()
                                                                    val trangThaiDiaChi =
                                                                        document.getBoolean("Trang_thaidc")
                                                                    if (trangThaiDiaChi == true) {
                                                                        Log.d(
                                                                            TAG,
                                                                            "onFilterSelected:roomData $roomData"
                                                                        )
                                                                        newListRoom.add(roomData)
                                                                        addresses2 = newListRoom
                                                                    }

                                                                }
                                                                .addOnFailureListener { exception ->
                                                                    Log.e(
                                                                        TAG,
                                                                        "Error fetching room details: ${exception.message}"
                                                                    )
                                                                }
                                                        }
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.d(
                                                            TAG,
                                                            "Lỗi khi lấy dữ liệu phòng trọ: ${exception.message}"
                                                        )
                                                    }
                                            }


                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error fetching PhongTroTienNghi", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error fetching PhongTroNoiThat", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error fetching PhongTro", e)
                        }
                } else {
                    Log.e(TAG, "Error fetching filter data", task.exception)
                }
            }
        Tasks.whenAllComplete(tasks).addOnSuccessListener {

            if (it.isEmpty()) {
                Log.d(TAG, "onFilterSelected: not null room")
            } else {
                addMarkersFromAddresses(mMap, addresses2, this)
            }

        }.addOnSuccessListener {

        }

    }


}