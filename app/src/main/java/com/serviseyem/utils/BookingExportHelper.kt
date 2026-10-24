package com.serviseyem.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.serviseyem.data.BookingEntity
import com.serviseyem.utils.DateFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.Locale

/**
 * 📊 BookingExportHelper
 * تصدير تقارير الحجوزات إلى ملفات CSV, PDF, و Excel (HTML format) منسقة واحترافية
 * ومشاركتها عبر مختلف التطبيقات.
 */
object BookingExportHelper {

    /**
     * تصدير الحجوزات إلى ملف CSV متوافق مع Excel والأحرف العربية
     */
    fun exportBookingsToCsv(context: Context, bookings: List<BookingEntity>): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            // ✨ م2-ج3: استخدام DateFormatter
            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "YemenServices_Bookings_$timeStamp.csv")

            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                // UTF-8 BOM for Excel Arabic compatibility
                writer.write("\uFEFF")
                // Headers
                writer.write("رقم الحجز,الخدمة,العميل,الهاتف,المزود,المدينة,التاريخ,الوقت,السعر,العملة,الحالة,رمز الأمان\n")

                bookings.forEach { b ->
                    val line = listOf(
                        "\"${b.bookingCode.ifBlank { b.id }}\"",
                        "\"${b.serviceName}\"",
                        "\"${b.effectiveCustomerName}\"",
                        "\"${b.effectiveCustomerPhone}\"",
                        "\"${b.providerName}\"",
                        "\"${listOf(b.userCity, b.effectiveCustomerArea).filter { it.isNotBlank() }.joinToString(" - ")}\"",
                        "\"${b.effectiveDate}\"",
                        "\"${b.effectiveTime}\"",
                        "\"${b.price}\"",
                        "\"${b.currency.ifBlank { "YER" }}\"",
                        "\"${b.status}\"",
                        "\"${b.effectivePin}\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * تصدير الحجوزات إلى مستند PDF رسمي مرمز بألوان وتصميم مميز
     */
    fun exportBookingsToPdf(context: Context, bookings: List<BookingEntity>): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            // ✨ م2-ج3: استخدام DateFormatter
            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "YemenServices_Report_$timeStamp.pdf")

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Dark Navy #0F172A
                textSize = 18f
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(2, 132, 199) // Sky Blue #0284C7
                textSize = 12f
                isFakeBoldText = true
            }

            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 10f
            }

            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            // Draw Header Banner
            canvas.drawText("دليل خدمات اليمن - تقرير الحجوزات", 40f, 50f, titlePaint)
            // ✨ م2-ج3: استخدام DateFormatter
            val dateStr = DateFormatter.formatDateTime(System.currentTimeMillis())
            canvas.drawText("تاريخ التقرير: $dateStr | إجمالي الحجوزات: ${bookings.size}", 40f, 70f, textPaint)
            canvas.drawLine(40f, 85f, 555f, 85f, linePaint)

            var yPos = 110f
            val maxRowsPerPage = 20

            bookings.take(maxRowsPerPage).forEachIndexed { idx, b ->
                canvas.drawText("${idx + 1}. [${b.bookingCode.ifBlank { b.id }}] ${b.serviceName}", 40f, yPos, headerPaint)
                yPos += 14f
                val details = "العميل: ${b.effectiveCustomerName} (${b.effectiveCustomerPhone}) | المزود: ${b.providerName} | التاريخ: ${b.effectiveDate} ${b.effectiveTime}"
                canvas.drawText(details, 40f, yPos, textPaint)
                yPos += 14f
                val statusPrice = "الحالة: ${b.status} | المبلغ: ${b.price} ${b.currency.ifBlank { "YER" }}"
                canvas.drawText(statusPrice, 40f, yPos, textPaint)
                yPos += 12f
                canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
                yPos += 16f
            }

            document.finishPage(page)
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * تصدير الحجوزات إلى ملف Excel بصيغة جدولية أنيقة (XLS)
     */
    fun exportBookingsToExcel(context: Context, bookings: List<BookingEntity>): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            // ✨ م2-ج3: استخدام DateFormatter
            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "YemenServices_Bookings_$timeStamp.xls")

            FileWriter(file).use { writer ->
                writer.write("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body dir=\"rtl\">")
                writer.write("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" style=\"font-family: Arial; border-collapse: collapse;\">")
                writer.write("<tr style=\"background-color: #0284C7; color: #FFFFFF; font-weight: bold;\">")
                writer.write("<th>رقم الحجز</th><th>اسم الخدمة</th><th>اسم العميل</th><th>هاتف العميل</th><th>مقدم الخدمة</th><th>المدينة</th><th>التاريخ</th><th>الوقت</th><th>السعر</th><th>العملة</th><th>الحالة</th>")
                writer.write("</tr>")

                bookings.forEach { b ->
                    writer.write("<tr>")
                    writer.write("<td>${b.bookingCode.ifBlank { b.id }}</td>")
                    writer.write("<td>${b.serviceName}</td>")
                    writer.write("<td>${b.effectiveCustomerName}</td>")
                    writer.write("<td>${b.effectiveCustomerPhone}</td>")
                    writer.write("<td>${b.providerName}</td>")
                    writer.write("<td>${b.userCity.ifBlank { b.effectiveCustomerArea }}</td>")
                    writer.write("<td>${b.effectiveDate}</td>")
                    writer.write("<td>${b.effectiveTime}</td>")
                    writer.write("<td>${b.price}</td>")
                    writer.write("<td>${b.currency.ifBlank { "YER" }}</td>")
                    writer.write("<td>${b.status}</td>")
                    writer.write("</tr>")
                }

                writer.write("</table></body></html>")
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareExportedFile(context: Context, file: File, title: String = "مشاركة تقرير الحجوزات") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = when (file.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                else -> "text/csv"
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            // Fallback plain share
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "تم حفظ تقرير الحجوزات: ${file.name}")
            }
            context.startActivity(Intent.createChooser(intent, title))
        }
    }
}
