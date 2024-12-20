package com.ph32395.staynow_datn.PhongTroDaXem

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.ph32395.staynow_datn.R

class PhongTroDaXemActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhongTroDaXemAdapter
    private lateinit var iconBackPhongTroDaXem: ImageView
    private lateinit var txtPhongDaXem: TextView
    private lateinit var progressBar: ProgressBar
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
        progressBar = findViewById(R.id.progressBarPhongDaXem)
        adapter = PhongTroDaXemAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        progressBar.visibility = View.VISIBLE //Hien thi ProgressBar khi bat  dau tai
        // Quan sát trạng thái tải dữ liệu
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.roomHistoryLiveDate.observe(this) {rooms ->
            progressBar.visibility = View.GONE //An di khi tai xong
            if (rooms.isEmpty()) {
                recyclerView.visibility = View.GONE
                txtPhongDaXem.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                txtPhongDaXem.visibility = View.GONE
                adapter.submitList(rooms)
            }

        }

        // Bắt đầu tải dữ liệu
        progressBar.visibility = View.VISIBLE
        viewModel.fetchRoomHistory(userId)

    }

}

