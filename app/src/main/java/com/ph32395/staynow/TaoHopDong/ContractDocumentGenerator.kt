//import android.content.Context
//import org.apache.poi.xwpf.usermodel.*
//import java.io.File
//import java.io.FileOutputStream
//import java.text.SimpleDateFormat
//import java.util.*
//
//private fun createContractDocument(
//    context: Context,
//    paymentDate: String,
//    electricityMeter: String,
//    waterMeter: String,
//    residentCount: String
//): File {
//    // Create a new document
//    val document = XWPFDocument()
//
//    // Title
//    val titleParagraph = document.createParagraph()
//    titleParagraph.alignment = ParagraphAlignment.CENTER
//    val titleRun = titleParagraph.createRun()
//    titleRun.setText("HỢP ĐỒNG THUÊ NHÀ")
//    titleRun.fontSize = 18
//    titleRun.isBold = true
//
//    // Current date
//    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
//
//    // Add content paragraphs
//    val contentParagraphs = listOf(
//        "Ngày lập hợp đồng: $currentDate",
//        "",
//        "THÔNG TIN CHI TIẾT",
//        "1. Ngày thanh toán hàng tháng: $paymentDate",
//        "2. Chỉ số điện hiện tại: $electricityMeter kWh",
//        "3. Chỉ số nước hiện tại: $waterMeter m³",
//        "4. Số người ở: $residentCount người",
//        "",
//        "ĐIỀU KHOẢN CHUNG",
//        "- Hợp đồng này được lập dựa trên thông tin do bên thuê cung cấp",
//        "- Các chỉ số điện nước sẽ được ghi nhận và thanh toán vào ngày $paymentDate hàng tháng",
//        "- Số tiền thanh toán sẽ được tính dựa trên các chỉ số điện nước thực tế",
//        "",
//        "Xác nhận của bên thuê",
//        "Chữ ký: ____________________",
//        "Ngày: $currentDate"
//    )
//
//    contentParagraphs.forEach { text ->
//        val paragraph = document.createParagraph()
//        val run = paragraph.createRun()
//        run.setText(text)
//        run.fontSize = 12
//    }
//
//    // Save the document
//    val fileName = "HopDongThueNha_${System.currentTimeMillis()}.docx"
//    val file = File(context.getExternalFilesDir(null), fileName)
//
//    val outputStream = FileOutputStream(file)
//    document.write(outputStream)
//    outputStream.close()
//    document.close()
//
//    return file
//}
//
//
//// Tạo file res/xml/file_paths.xml:
///*
//<?xml version="1.0" encoding="utf-8"?>
//<paths xmlns:android="http://schemas.android.com/apk/res/android">
//    <external-files-path name="my_files" path="." />
//</paths>
//*/
//
