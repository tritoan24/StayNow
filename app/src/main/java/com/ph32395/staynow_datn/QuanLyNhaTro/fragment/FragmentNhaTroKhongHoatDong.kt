package com.ph32395.staynow_datn.QuanLyNhaTro.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroAdapter
import com.ph32395.staynow_datn.QuanLyNhaTro.NhaTroModel
import com.ph32395.staynow_datn.databinding.FragmentNhaTroKhongHoatDongBinding

class FragmentNhaTroKhongHoatDong : Fragment() {

    private lateinit var binding: FragmentNhaTroKhongHoatDongBinding

    private val TAG = "ZZZFragmentNhaTroKhongHoatDongZZZ"
    private val firestore = FirebaseFirestore.getInstance()
    private val nhaTroRef = firestore.collection("NhaTro")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNhaTroKhongHoatDongBinding.inflate(
            inflater,
            container?.parent as ViewGroup?, false
        )
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        fetchNhaTro(userId)


        return binding.root
    }

    private fun fetchNhaTro(idUser: String?) {
        val list = mutableListOf<NhaTroModel>()
        if (idUser != null) {
            nhaTroRef.document(idUser).collection("DanhSachNhaTro")
                .whereEqualTo("trangThai", false)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error)
                        return@addSnapshotListener
                    }

                    for (docChange in value!!.documentChanges) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val nhaTro = docChange.document.toObject(NhaTroModel::class.java)
                                list.add(nhaTro)
                                Log.d(TAG, "fetchNhaTro:nhaTro $nhaTro")

                            }

                            DocumentChange.Type.MODIFIED -> {
                                val modifiedNhaTro =
                                    docChange.document.toObject(NhaTroModel::class.java)
                                // Find the index of the modified item and update it
                                val index =
                                    list.indexOfFirst { it.maNhaTro == modifiedNhaTro.maNhaTro }
                                if (index != -1) {
                                    list[index] = modifiedNhaTro
                                    Log.d(TAG, "Updated contract: ${docChange.document.data}")
                                }
                            }

                            DocumentChange.Type.REMOVED -> {
                                val removedNhaTroId = docChange.document.id
                                // Remove the item from the list
                                list.removeAll { it.maNhaTro == removedNhaTroId }
                                Log.d(TAG, "Removed contract: ${docChange.document.data}")
                            }
                        }
                    }

                    // Notify the adapter that the data has changed
                    val adapter = NhaTroAdapter(list)
                    binding.rcvNhaTroKhongHoatDong.layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                    binding.rcvNhaTroKhongHoatDong.adapter = adapter
                }
        }
    }

}