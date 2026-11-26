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
     * تصدير سند حجز إلكتروني رسمي مفصل لحجز واحد بصيغة PDF أنيقة ومختومة رقمياً
     */
    fun exportSingleBookingInvoicePdf(context: Context, booking: BookingEntity): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val code = booking.bookingCode.ifBlank { booking.id.take(8) }
            val file = File(exportDir, "Invoice_${code}.pdf")

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Dark Navy
                textSize = 20f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(2, 132, 199) // Sky Blue
                textSize = 13f
                isFakeBoldText = true
            }

            val labelPaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 11f
                isFakeBoldText = true
            }

            val valuePaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 12f
            }

            val borderPaint = Paint().apply {
                color = Color.rgb(203, 213, 225)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }

            val headerBgPaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            // Draw Header Box
            canvas.drawRect(35f, 35f, 560f, 105f, headerBgPaint)
            canvas.drawRect(35f, 35f, 560f, 105f, borderPaint)
            canvas.drawText("دليل خدمات اليمن - سند حجز إلكتروني رسمي 🇾🇪", 50f, 65f, titlePaint)
            canvas.drawText("المنصة الذكية الأولى للخدمات الميدانية والملاحة في الجمهورية اليمنية", 50f, 88f, subtitlePaint)

            // Invoice Meta
            var yPos = 135f
            canvas.drawText("رقم السند المعتمد: #$code", 40f, yPos, labelPaint)
            val dateStr = DateFormatter.formatDateTime(System.currentTimeMillis())
            canvas.drawText("تاريخ الإصدار: $dateStr", 350f, yPos, labelPaint)

            yPos += 20f
            canvas.drawLine(35f, yPos, 560f, yPos, borderPaint)

            // Details Table
            yPos += 30f
            val details = listOf(
                "اسم الخدمة المطلوبة" to booking.serviceName,
                "العميل المستفيد" to "${booking.effectiveCustomerName} (${booking.effectiveCustomerPhone})",
                "مزود الخدمة المعتمد" to booking.providerName,
                "الموقع والمدينة" to listOf(booking.userCity, booking.effectiveCustomerArea).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "اليمن" },
                "تاريخ وموعد الخدمة" to "${booking.effectiveDate} - ${booking.effectiveTime}",
                "المبلغ المقدر" to "${booking.price} ${booking.currency.ifBlank { "YER" }}",
                "حالة الحجز الحالية" to booking.status,
                "رمز التحقق والأمان (PIN)" to booking.effectivePin.ifBlank { "----" }
            )

            details.forEach { (label, value) ->
                canvas.drawText(label, 50f, yPos, labelPaint)
                canvas.drawText(value, 220f, yPos, valuePaint)
                yPos += 14f
                canvas.drawLine(45f, yPos, 550f, yPos, Paint().apply { color = Color.rgb(241, 245, 249); strokeWidth = 1f })
                yPos += 22f
            }

            // Official Verification Stamp Box
            yPos += 20f
            val stampBoxPaint = Paint().apply {
                color = Color.rgb(236, 253, 245)
                style = Paint.Style.FILL
            }
            val stampBorderPaint = Paint().apply {
                color = Color.rgb(16, 185, 129)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(40f, yPos, 555f, yPos + 65f, 12f, 12f, stampBoxPaint)
            canvas.drawRoundRect(40f, yPos, 555f, yPos + 65f, 12f, 12f, stampBorderPaint)

            val stampTextPaint = Paint().apply {
                color = Color.rgb(6, 95, 70)
                textSize = 12f
                isFakeBoldText = true
            }
            canvas.drawText("✓ سند رسمي موثق ومحمٍ رقمياً عبر منصة دليل خدمات اليمن", 55f, yPos + 28f, stampTextPaint)
            canvas.drawText("يرجى إبراز هذا السند أو رمز التحقق لمقدم الخدمة عند إتمام الزيارة.", 55f, yPos + 48f, Paint().apply { color = Color.rgb(4, 120, 87); textSize = 10f })

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

    fun shareSingleBookingInvoice(context: Context, booking: BookingEntity) {
        val result = exportSingleBookingInvoicePdf(context, booking)
        result.onSuccess { file ->
            shareExportedFile(context, file, "سند حجز رسمي #${booking.bookingCode.ifBlank { booking.id }}")
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
