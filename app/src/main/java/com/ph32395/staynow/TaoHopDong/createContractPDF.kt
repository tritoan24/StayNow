package com.ph32395.staynow.TaoHopDong

import android.os.Environment
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File

fun createContractPDF(
    renterName: String,
    landlordName: String,
    roomInfo: String,
    rentPrice: String,
    startDate: String,
    endDate: String
) {
    val pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
    val file = File(pdfPath, "HopDongThuePhong.pdf")
    val pdfWriter = PdfWriter(file)
    val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
    val document = Document(pdfDocument)

    document.add(Paragraph("HỢP ĐỒNG THUÊ PHÒNG TRỌ").setBold().setFontSize(18f))
    document.add(Paragraph("Người thuê: $renterName"))
    document.add(Paragraph("Người cho thuê: $landlordName"))
    document.add(Paragraph("Thông tin phòng: $roomInfo"))
    document.add(Paragraph("Giá thuê: $rentPrice VNĐ"))
    document.add(Paragraph("Thời gian thuê: Từ $startDate đến $endDate"))
    document.add(Paragraph("Điều khoản khác: ..."))
    document.add(Paragraph("\nChữ ký bên thuê: ________________________"))
    document.add(Paragraph("Chữ ký bên cho thuê: ____________________"))

    document.close()
    println("Hợp đồng đã được lưu tại: ${file.absolutePath}")
}
