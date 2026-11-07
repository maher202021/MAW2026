package com.serviseyem.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

/**
 * 🗺️ GpxPoint - Data class for track points
 */
data class GpxPoint(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double = 0.0,
    val timeFormatted: String = ""
)

/**
 * 🗺️ GpxTrack - Parsed track containter
 */
data class GpxTrack(
    val trackName: String,
    val points: List<GpxPoint>
)

/**
 * 🛰️ GpxManager
 * Professional GPX / KML track importer & exporter for offline map tracks (100% Free, Local On-Device).
 */
object GpxManager {

    private const val TAG = "GpxManager"

    /**
     * Export coordinates list to a valid GPX XML file
     */
    fun exportRouteToGpx(
        context: Context,
        trackTitle: String,
        coordinates: List<Pair<Double, Double>>
    ): Result<File> {
        return try {
            val exportDir = File(context.cacheDir, "gpx_exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
            val file = File(exportDir, "Yemen_Route_${timeStamp}.gpx")

            file.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.write("<gpx version=\"1.1\" creator=\"Yemen Services Navigation Engine\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
                writer.write("  <metadata>\n")
                writer.write("    <name>${escapeXml(trackTitle)}</name>\n")
                writer.write("    <time>${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).format(Date())}</time>\n")
                writer.write("  </metadata>\n")
                writer.write("  <trk>\n")
                writer.write("    <name>${escapeXml(trackTitle)}</name>\n")
                writer.write("    <trkseg>\n")

                for ((lat, lng) in coordinates) {
                    writer.write("      <trkpt lat=\"$lat\" lon=\"$lng\">\n")
                    writer.write("      </trkpt>\n")
                }

                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
                writer.write("</gpx>\n")
            }

            Result.success(file)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export GPX", e)
            Result.failure(e)
        }
    }

    /**
     * Parse GPX track from InputStream or file Uri
     */
    fun importGpxFromInputStream(inputStream: InputStream): Result<GpxTrack> {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(inputStream)
            doc.documentElement.normalize()

            var title = "مسار مستورد"
            val nameNodes = doc.getElementsByTagName("name")
            if (nameNodes.length > 0) {
                title = nameNodes.item(0).textContent.trim()
            }

            val trkptList = doc.getElementsByTagName("trkpt")
            val points = mutableListOf<GpxPoint>()

            for (i in 0 until trkptList.length) {
                val node = trkptList.item(i)
                val attrs = node.attributes
                val latStr = attrs.getNamedItem("lat")?.nodeValue
                val lonStr = attrs.getNamedItem("lon")?.nodeValue

                if (latStr != null && lonStr != null) {
                    val lat = latStr.toDoubleOrNull() ?: continue
                    val lon = lonStr.toDoubleOrNull() ?: continue
                    points.add(GpxPoint(latitude = lat, longitude = lon))
                }
            }

            Result.success(GpxTrack(trackName = title, points = points))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse GPX stream", e)
            Result.failure(e)
        }
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
