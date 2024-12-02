package com.ph32395.staynow.TaoHopDong

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.ph32395.staynow.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ChiTietHopDong : AppCompatActivity() {
    private lateinit var tvContractDetails: TextView
    private lateinit var scrollViewContract: ScrollView
    private lateinit var btnExportPdf: Button
    private val firestore = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chi_tiet_hop_dong)

        // Set up edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find views
        tvContractDetails = findViewById(R.id.tvContractDetails)
        scrollViewContract = findViewById(R.id.scrollViewContract)
        btnExportPdf = findViewById(R.id.btnExportPdf)

        // Get contract ID from intent
        val contractId = "BlStOrtr12PSMsxT166v"
        // intent.getStringExtra("CONTRACT_ID")

        // Fetch contract details
        contractId?.let { fetchContractDetails(it) }

        // Setup PDF export button
        btnExportPdf.setOnClickListener {
            exportContractToPdf()
        }
    }

    private fun fetchContractDetails(contractId: String) {
        firestore.collection("HopDong").document(contractId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("ChiTietHopDong", "DocumentSnapshot data: ${document.data}")
                    val contractDetails = document.data

                    // Format contract details with improved readability
                    val formattedContractText = formatContractDetails(contractDetails)
                    tvContractDetails.text = formattedContractText
                } else {
                    Toast.makeText(this, "Không tìm thấy hợp đồng", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Lỗi: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun formatContractDetails(contractDetails: Map<String, Any>?): String {
        if (contractDetails == null) return "Không có thông tin hợp đồng"

        val formattedDetails = StringBuilder()
        formattedDetails.append("CHI TIẾT HỢP ĐỒNG\n\n")

        // Sort and format contract details
        contractDetails.toSortedMap().forEach { (key, value) ->
            formattedDetails.append("${key.uppercase()}: ${value}\n")
        }

        return formattedDetails.toString()
    }

    private fun exportContractToPdf() {
        try {
            // Create a bitmap of the scrollView
            val bitmap = createBitmapFromView(scrollViewContract)

            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // Draw bitmap on PDF canvas
            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // Save PDF
            val fileName = "HopDong_${System.currentTimeMillis()}.pdf"
            val file = File(getExternalFilesDir(null), fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            // Share PDF using FileProvider
            val contentUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val shareIntent = androidx.core.app.ShareCompat.IntentBuilder(this)
                .setStream(contentUri)
                .setType("application/pdf")
                .intent
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(shareIntent, "Xuất hợp đồng"))

            Toast.makeText(this, "Xuất PDF thành công", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("ChiTietHopDong", "Lỗi xuất PDF: ${e.message}")
            Toast.makeText(this, "Lỗi xuất PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}