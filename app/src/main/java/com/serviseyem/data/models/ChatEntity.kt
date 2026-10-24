package com.serviseyem.data

import androidx.annotation.Keep

@Keep
enum class AttachmentType {
    EXCEL, CSV, PDF, IMAGE, JSON
}

@Keep
data class ProductAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String = "",
    val type: String = "PDF", // "EXCEL", "CSV", "PDF", "IMAGE", "JSON"
    val url: String = "",
    val fileName: String = "",
    val size: Long = 0,
    val mimeType: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true
) {
    companion object {
        fun parseList(jsonStr: String): List<ProductAttachment> {
            val trimmed = jsonStr.trim()
            if (trimmed.isBlank()) return emptyList()
            return try {
                if (trimmed.startsWith("[")) {
                    val array = org.json.JSONArray(trimmed)
                    val result = mutableListOf<ProductAttachment>()
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        result.add(
                            ProductAttachment(
                                id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
                                userId = obj.optString("userId", ""),
                                type = obj.optString("type", "PDF"),
                                url = obj.optString("url", ""),
                                fileName = obj.optString("fileName", ""),
                                size = obj.optLong("size", 0L),
                                mimeType = obj.optString("mimeType", ""),
                                uploadedAt = obj.optLong("uploadedAt", System.currentTimeMillis()),
                                isPublic = obj.optBoolean("isPublic", true)
                            )
                        )
                    }
                    result
                } else {
                    // Legacy fallback for existing strings formatted with ;; and ||
                    trimmed.split(";;").filter { it.isNotBlank() }.map { chunk ->
                        val parts = chunk.split("||")
                        ProductAttachment(
                            id = parts.getOrElse(0) { java.util.UUID.randomUUID().toString() },
                            userId = parts.getOrElse(1) { "" },
                            type = parts.getOrElse(2) { "PDF" },
                            url = parts.getOrElse(3) { "" },
                            fileName = parts.getOrElse(4) { "" },
                            size = parts.getOrElse(5) { "0" }.toLongOrNull() ?: 0L,
                            mimeType = parts.getOrElse(6) { "" },
                            uploadedAt = parts.getOrElse(7) { "0" }.toLongOrNull() ?: System.currentTimeMillis(),
                            isPublic = parts.getOrElse(8) { "true" }.toBoolean()
                        )
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeList(list: List<ProductAttachment>): String {
            if (list.isEmpty()) return "[]"
            val array = org.json.JSONArray()
            list.forEach { item ->
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("userId", item.userId)
                    put("type", item.type)
                    put("url", item.url)
                    put("fileName", item.fileName)
                    put("size", item.size)
                    put("mimeType", item.mimeType)
                    put("uploadedAt", item.uploadedAt)
                    put("isPublic", item.isPublic)
                }
                array.put(obj)
            }
            return array.toString()
        }
    }
}

typealias ChatMessageEntity = com.serviseyem.data.models.ChatMessage

@Keep
data class CallEntity(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val callerName: String = "",
    val timestamp: Long = 0L
)
