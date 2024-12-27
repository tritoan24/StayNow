package com.ph32395.staynow_datn.QuanLyNhaTro

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow_datn.databinding.ActivityQuanLyNhaTroBinding

class QuanLyNhaTroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuanLyNhaTroBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val nhaTroRef = firestore.collection("NhaTro")
    private val TAG = "ZZZQuanLyNhaTroActivityZZZ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuanLyNhaTroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idUser = FirebaseAuth.getInstance().currentUser?.uid

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.addNhaTro.setOnClickListener {
            val bottomSheetCreateAndUpdateNhaTro = BottomSheetCreateAndUpdateNhaTro(null)
            bottomSheetCreateAndUpdateNhaTro.show(
                this.supportFragmentManager,
                bottomSheetCreateAndUpdateNhaTro.tag
            )
        }


        fetchNhaTro(idUser)


    }

//    private fun fetchNhaTro(idUser: String?) {
//        val list = mutableListOf<NhaTroModel>()
//        if (idUser != null) {
//            nhaTroRef.document(idUser).collection("DanhSachNhaTro")
//                .addSnapshotListener { value, error ->
//
//                    for (docChange in value!!.documentChanges) {
//                        when (docChange.type) {
//                            DocumentChange.Type.ADDED -> {
//                                val nhaTro = docChange.document.toObject(NhaTroModel::class.java)
//                                Log.d(TAG, "fetchNhaTro:nhaTro $nhaTro")
//                                list.add(nhaTro)
//
//                                Log.d(TAG, "fetchNhaTro:list $list")
//                                val adapter = NhaTroAdapter(list)
//                                binding.rvQuanLyNhaTro.layoutManager = LinearLayoutManager(
//                                    applicationContext,
//                                    LinearLayoutManager.VERTICAL,
//                                    false
//                                )
//                                binding.rvQuanLyNhaTro.adapter = adapter
//
//                            }
//
//                            DocumentChange.Type.MODIFIED -> {
//                                Log.d(TAG, "Updated contract: ${docChange.document.data}")
//                            }
//
//                            DocumentChange.Type.REMOVED -> {
//                                Log.d(TAG, "Removed contract: ${docChange.document.data}")
//                            }
//                        }
//                    }
//
//
//                }
//        }
//
//
//    }
private fun fetchNhaTro(idUser: String?) {
    val list = mutableListOf<NhaTroModel>()
    if (idUser != null) {
        nhaTroRef.document(idUser).collection("DanhSachNhaTro")
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
                            val modifiedNhaTro = docChange.document.toObject(NhaTroModel::class.java)
                            // Find the index of the modified item and update it
                            val index = list.indexOfFirst { it.maNhaTro == modifiedNhaTro.maNhaTro }
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
                binding.rvQuanLyNhaTro.layoutManager = LinearLayoutManager(
                    applicationContext,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                binding.rvQuanLyNhaTro.adapter = adapter
            }
    }
}

}