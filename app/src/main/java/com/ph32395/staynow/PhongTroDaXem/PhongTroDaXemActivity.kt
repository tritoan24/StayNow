package com.ph32395.staynow.PhongTroDaXem

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.R

class PhongTroDaXemActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhongTroDaXemAdapter
    private lateinit var iconBackPhongTroDaXem: ImageView
    private lateinit var txtPhongDaXem: TextView
    private val viewModel: PhongTroDaXemViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phong_tro_da_xem)

        iconBackPhongTroDaXem = findViewById(R.id.iconBackPhongTroDaXem)
        iconBackPhongTroDaXem.setOnClickListener {
            finish()
        }
        txtPhongDaXem = findViewById(R.id.txtPhongDaXem)
        recyclerView = findViewById(R.id.recyclerViewPhongDaXem)
        adapter = PhongTroDaXemAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModel.fetchRoomHistory(userId)

        viewModel.roomHistoryLiveDate.observe(this) {rooms ->
            if (rooms.isEmpty()) {
                recyclerView.visibility = View.GONE
                txtPhongDaXem.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                txtPhongDaXem.visibility = View.GONE
                adapter.submitList(rooms)
            }

        }

    }

}

