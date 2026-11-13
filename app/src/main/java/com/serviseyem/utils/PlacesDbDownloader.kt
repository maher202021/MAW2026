package com.serviseyem.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream

object PlacesDbDownloader {

    fun configuredUrl(context: Context): String {
        val custom = context.getSharedPreferences("valhalla_prefs", Context.MODE_PRIVATE)
            .getString("places_db_url", "")?.trim().orEmpty()
        return custom.ifBlank { NavConfig.PLACES_DB_URL.trim() }
    }

    suspend fun download(
        context: Context,
        customUrl: String = "",
        onProgress: (Float) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val targetUrl = customUrl.trim().ifBlank { configuredUrl(context) }
        if (targetUrl.isBlank()) {
            return@withContext Result.failure(
                IOException("رابط قاعدة الأماكن غير مضبوط. يمكنك إضافته من الإعدادات أو استيراد ملف places.db من ذاكرة الهاتف.")
            )
        }
        if (context.filesDir.usableSpace < 150L * 1024 * 1024) {
            return@withContext Result.failure(IOException("مساحة التخزين غير كافية (يلزم 150 ميجابايت حرة على الأقل)."))
        }

        val tmpDir = File(context.filesDir, "places_tmp").apply { deleteRecursively(); mkdirs() }
        val tmp = File(tmpDir, "places.db")
        var conn: HttpURLConnection? = null
        try {
            var currentUrl = targetUrl
            var redirects = 0
            while (redirects < 5) {
                val c = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 45_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "AlYemenServicesApp/1.0")
                }
                val code = c.responseCode
                if (code in listOf(301, 302, 303, 307, 308)) {
                    val next = c.getHeaderField("Location")
                    c.disconnect()
                    if (!next.isNullOrBlank()) {
                        currentUrl = next
                        redirects++
                        continue
                    }
                }
                conn = c
                break
            }
            val activeConn = conn ?: return@withContext Result.failure(IOException("تعذر الاتصال بالخادم"))
            if (activeConn.responseCode !in 200..299) {
                return@withContext Result.failure(IOException("رد الخادم ${activeConn.responseCode}"))
            }

            val total = activeConn.contentLengthLong
            val md = MessageDigest.getInstance("SHA-256")
            var found = false
            var bytes = 0L

            ZipInputStream(activeConn.inputStream.buffered()).use { zip ->
                var e = zip.nextEntry
                while (e != null) {
                    if (!e.isDirectory && e.name.substringAfterLast('/') == "places.db") {
                        FileOutputStream(tmp).use { out ->
                            val buf = ByteArray(32 * 1024)
                            while (true) {
                                val n = zip.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                md.update(buf, 0, n)
                                bytes += n
                                if (total > 0) {
                                    onProgress((bytes.toFloat() / (total * 2f)).coerceIn(0.05f, 0.92f))
                                }
                            }
                            out.fd.sync()
                        }
                        found = true
                        break
                    }
                    zip.closeEntry()
                    e = zip.nextEntry
                }
            }

            if (!found || bytes < 100_000L) {
                return@withContext Result.failure(IOException("الملف المستخرج غير صالح أو لا يحتوي places.db"))
            }

            val sha = md.digest().joinToString("") { "%02x".format(it) }
            if (NavConfig.PLACES_DB_SHA256.isNotBlank() && !sha.equals(NavConfig.PLACES_DB_SHA256, ignoreCase = true)) {
                return@withContext Result.failure(IOException("فشل التحقق من سلامة الملف (SHA-256)"))
            }

            validateAndInstallPlacesDb(context, tmp)
            tmpDir.deleteRecursively()
            onProgress(1f)
            Result.success(true)
        } catch (e: CancellationException) {
            tmpDir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            tmpDir.deleteRecursively()
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * استيراد قاعدة الأماكن (places.db أو places.db.zip) مباشرة من ذاكرة الهاتف.
     */
    suspend fun importFromUri(
        context: Context,
        uri: Uri,
        onProgress: (Float, String) -> Unit
    ): Result<Long> = withContext(Dispatchers.IO) {
        val tmpDir = File(context.filesDir, "places_import_tmp").apply { deleteRecursively(); mkdirs() }
        val copied = File(tmpDir, "source_file")
        try {
            onProgress(0.15f, "جاري قراءة ملف قاعدة الأماكن من ذاكرة الهاتف...")
            val input = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(IOException("تعذر فتح الملف المختار"))

            input.use { stream ->
                FileOutputStream(copied).use { out ->
                    val buf = ByteArray(32 * 1024)
                    while (true) {
                        val n = stream.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                    }
                    out.fd.sync()
                }
            }

            onProgress(0.55f, "جاري فحص وتجهيز قاعدة الأماكن...")
            val rowCount = importFromLocalFile(context, copied, tmpDir)
            tmpDir.deleteRecursively()
            onProgress(1f, "تم استيراد قاعدة الأماكن بنجاح ✓ ($rowCount مكان)")
            Result.success(rowCount)
        } catch (e: CancellationException) {
            tmpDir.deleteRecursively()
            throw e
        } catch (e: Exception) {
            tmpDir.deleteRecursively()
            Result.failure(e)
        }
    }

    internal fun importFromLocalFile(context: Context, sourceFile: File, workDir: File): Long {
        if (!sourceFile.exists() || sourceFile.length() < 10_000L) {
            throw IOException("الملف المختار صغير جداً أو غير مكتمل")
        }

        val header = ByteArray(16)
        FileInputStream(sourceFile).use { it.read(header) }
        val isZip = header[0] == 'P'.code.toByte() && header[1] == 'K'.code.toByte()
        val isSqlite = String(header.copyOfRange(0, 15), Charsets.US_ASCII) == "SQLite format 3"

        val candidateDb = File(workDir, "places.db")
        when {
            isSqlite -> {
                if (sourceFile.absolutePath != candidateDb.absolutePath) {
                    sourceFile.copyTo(candidateDb, overwrite = true)
                }
            }
            isZip -> {
                var extracted = false
                ZipInputStream(sourceFile.inputStream().buffered()).use { zip ->
                    var e = zip.nextEntry
                    while (e != null) {
                        val entryBase = e.name.substringAfterLast('/')
                        if (!e.isDirectory && (entryBase == "places.db" || entryBase.endsWith(".db", ignoreCase = true))) {
                            FileOutputStream(candidateDb).use { out ->
                                val buf = ByteArray(32 * 1024)
                                while (true) {
                                    val n = zip.read(buf)
                                    if (n < 0) break
                                    out.write(buf, 0, n)
                                }
                                out.fd.sync()
                            }
                            extracted = true
                            break
                        }
                        zip.closeEntry()
                        e = zip.nextEntry
                    }
                }
                if (!extracted) {
                    throw IOException("ملف ZIP المختار لا يحتوي على قاعدة places.db")
                }
            }
            else -> throw IOException("صيغة الملف غير مدعومة (اختر ملف places.db أو places.db.zip)")
        }

        return validateAndInstallPlacesDb(context, candidateDb)
    }

    private fun validateAndInstallPlacesDb(context: Context, candidateDb: File): Long {
        if (!candidateDb.exists() || candidateDb.length() < 100_000L) {
            throw IOException("حجم قاعدة الأماكن غير مكتمل")
        }

        val count = SQLiteDatabase.openDatabase(
            candidateDb.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        ).use { testDb ->
            testDb.rawQuery("SELECT COUNT(*) FROM places", null).use { c ->
                if (c.moveToFirst()) c.getLong(0) else 0L
            }
        }

        if (count <= 500L) {
            throw IOException("قاعدة الأماكن فارغة أو تالفة ($count سجل)")
        }

        OfflinePlacesDb.close()
        val dest = File(context.filesDir, "places").apply { mkdirs() }
        val target = File(dest, "places.db")
        val old = File(dest, "places.db.old")
        old.delete()
        if (target.exists()) target.renameTo(old)
        if (!candidateDb.renameTo(target)) {
            candidateDb.copyTo(target, overwrite = true)
            candidateDb.delete()
        }
        old.delete()
        return count
    }
}
