package com.serviseyem.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.serviseyem.data.models.InstantRequestEntity
import com.serviseyem.data.models.Transaction
import com.serviseyem.utils.DateFormatter
import java.io.File
import java.io.FileWriter
import java.util.Locale

/**
 * 📈 ReportExporter
 * وحدة تصدير التقارير المتعددة (الطلبات العاجلة، المعاملات المالية، الإحصائيات)
 * بصيغة CSV المتوافقة تماماً مع Excel باللغة العربية ومشاركتها فوراً.
 */
object ReportExporter {

    fun exportUrgentRequestsToCsv(context: Context, requests: List<InstantRequestEntity>): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            // ✨ م2-ج3: استخدام DateFormatter
            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "Urgent_Requests_$timeStamp.csv")

            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF") // UTF-8 BOM
                writer.write("رمز الطلب,الخدمة,المدينة,الحي,الحالة,العميل,الهاتف,الوقت المتبقي (دقيقة),التاريخ\n")

                val now = System.currentTimeMillis()
                requests.forEach { req ->
                    val remainingMinutes = (((req.expiresAt - now) / 1000) / 60).coerceAtLeast(0)
                    // ✨ م2-ج3: استخدام DateFormatter
                    val dateFormatted = DateFormatter.formatDateTime(req.createdAt)

                    val line = listOf(
                        "\"${req.requestCode}\"",
                        "\"${req.serviceTitle}\"",
                        "\"${req.userCity}\"",
                        "\"${req.userNeighborhood}\"",
                        "\"${req.status}\"",
                        "\"${req.userName}\"",
                        "\"${req.userPhone}\"",
                        "\"$remainingMinutes\"",
                        "\"$dateFormatted\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportFinancialReportToCsv(
        context: Context,
        accountName: String,
        totalIncome: Double,
        currency: String,
        items: List<Pair<String, Double>>
    ): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            // ✨ م2-ج3: استخدام DateFormatter
            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "Financial_Report_${accountName}_$timeStamp.csv")

            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF") // UTF-8 BOM
                writer.write("تقرير المبيعات والنمو المالي للحساب: $accountName\n")
                writer.write("إجمالي الإيرادات: $totalIncome $currency\n\n")
                writer.write("البند / الخدمة,المبلغ ($currency)\n")

                items.forEach { (item, amount) ->
                    writer.write("\"$item\",\"$amount\"\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportTransactionsToCsv(
        context: Context,
        accountName: String,
        transactions: List<Transaction>
    ): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timeStamp = DateFormatter.formatCustom(System.currentTimeMillis(), "yyyyMMdd_HHmmss")
            val file = File(exportDir, "Transactions_${accountName}_$timeStamp.csv")

            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF") // UTF-8 BOM
                writer.write("سجل العمليات المالية للحساب: $accountName\n")
                writer.write("رمز العملية,النوع,المبلغ,العملة,الحالة,الملاحظات,التاريخ\n")

                transactions.forEach { tx ->
                    val dateFormatted = DateFormatter.formatDateTime(tx.timestamp)
                    val typeAr = when (tx.type.uppercase()) {
                        "DEPOSIT" -> "إيداع"
                        "WITHDRAWAL" -> "سحب"
                        "TRANSFER" -> "تحويل"
                        "PAYMENT" -> "دفع"
                        "REFUND" -> "استرداد"
                        else -> tx.type
                    }
                    val statusAr = when (tx.status.uppercase()) {
                        "COMPLETED" -> "مكتملة"
                        "PENDING" -> "قيد الانتظار"
                        "FAILED" -> "فشلت"
                        "CANCELLED" -> "ملغاة"
                        else -> tx.status
                    }
                    val line = listOf(
                        "\"${tx.id}\"",
                        "\"$typeAr\"",
                        "\"${tx.amount}\"",
                        "\"${tx.currency}\"",
                        "\"$statusAr\"",
                        "\"${tx.note}\"",
                        "\"$dateFormatted\""
                    ).joinToString(",")
                    writer.write(line + "\n")
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareExportedFile(context: Context, file: File, title: String = "مشاركة التقرير") {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "تم تصدير التقرير: ${file.name}")
            }
            context.startActivity(Intent.createChooser(intent, title))
        }
    }
}
