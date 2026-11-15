package com.serviseyem.data

import androidx.annotation.Keep

@Keep
data class DynamicSection(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val isEnabled: Boolean = true,
    val type: String = "store", // "store" or "property"
    val order: Int = 0,
    val terms: String = "",
    val maxPhotos: Int = 5,
    val showPhotos: Boolean = true,
    val allowPdf: Boolean = true,
    val requiredFields: String = "الاسم,الوصف,الهاتف,الموقع"
) {
    companion object {
        fun parseDynamicSections(serialized: String): List<DynamicSection> {
            val trimmed = serialized.trim()
            if (trimmed.isEmpty()) {
                return listOf(
                    DynamicSection("stores", "المحلات والمراكز", "🏪", true, "store", 1, "شروط تسجيل المحل: يرجى إدخال بيانات صحيحة ومطابقة للواقع التجاري وصور واضحة.", 10, true, true, "الاسم,الوصف,الهاتف,الموقع"),
                    DynamicSection("restaurants", "المطاعم والكافيهات", "🍔", true, "store", 2, "شروط تسجيل المطعم: توضيح نوع الوجبات، الأسعار، وساعات العمل وصور الوجبات.", 10, true, true, "الاسم,الوصف,الهاتف,الموقع,المنيو"),
                    DynamicSection("medical", "المراكز الطبية والعيادات", "🏥", true, "store", 3, "شروط تسجيل المركز الطبي: إضافة اسم الدكتور/المركز والتخصص وأوقات الدوام.", 10, true, true, "الاسم,التخصص,الهاتف,الموقع"),
                    DynamicSection("properties", "العقارات والأراضي", "🏠", true, "property", 4, "شروط إضافة العقار: يرجى تأكيد ملكية العقار وصحة الصور والأسعار المعروضة.", 10, true, true, "العنوان,الوصف,السعر,الهاتف"),
                    DynamicSection("jobs", "إعلانات الوظائف", "💼", true, "store", 5, "شروط إعلان الوظيفة: توضيح المسمى الوظيفي والراتب والشروط المطلوبة ورقم التواصل.", 5, true, true, "المسمى,الجهة,الهاتف,الراتب"),
                    DynamicSection("services", "المهن والخدمات", "🛠️", false, "store", 6, "شروط تسجيل الفني: تحديد المهنة/التخصص والأسعار التقديرية ونطاق العمل.", 10, true, true, "الاسم,المهنة,الهاتف,المنطقة")
                )
            }
            if (trimmed == "[]") return emptyList()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                try {
                    val jsonArray = org.json.JSONArray(trimmed)
                    val list = mutableListOf<DynamicSection>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        list.add(
                            DynamicSection(
                                id = obj.optString("id", ""),
                                name = obj.optString("name", ""),
                                icon = obj.optString("icon", ""),
                                isEnabled = obj.optBoolean("isEnabled", true),
                                type = obj.optString("type", "store"),
                                order = obj.optInt("order", i),
                                terms = obj.optString("terms", ""),
                                maxPhotos = obj.optInt("maxPhotos", 5),
                                showPhotos = obj.optBoolean("showPhotos", true),
                                allowPdf = obj.optBoolean("allowPdf", true),
                                requiredFields = obj.optString("requiredFields", "الاسم,الهاتف,الوصف")
                            )
                        )
                    }
                    return list.sortedBy { it.order }
                } catch (_: Exception) {}
            }
            return try {
                trimmed.split(";;").filter { it.isNotEmpty() }.map { sectionStr ->
                    val parts = sectionStr.split("||")
                    DynamicSection(
                        id = parts.getOrElse(0) { "" },
                        name = parts.getOrElse(1) { "" },
                        icon = parts.getOrElse(2) { "" },
                        isEnabled = parts.getOrElse(3) { "true" }.toBoolean(),
                        type = parts.getOrElse(4) { "store" },
                        order = parts.getOrElse(5) { "0" }.toIntOrNull() ?: 0,
                        terms = parts.getOrElse(6) { "" },
                        maxPhotos = parts.getOrElse(7) { "5" }.toIntOrNull() ?: 5,
                        showPhotos = parts.getOrElse(8) { "true" }.toBoolean(),
                        allowPdf = parts.getOrElse(9) { "true" }.toBoolean(),
                        requiredFields = parts.getOrElse(10) { "الاسم,الهاتف,الوصف" }
                    )
                }.sortedBy { it.order }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun serializeDynamicSections(list: List<DynamicSection>): String {
            return try {
                val jsonArray = org.json.JSONArray()
                for (sec in list) {
                    val obj = org.json.JSONObject()
                    obj.put("id", sec.id)
                    obj.put("name", sec.name)
                    obj.put("icon", sec.icon)
                    obj.put("isEnabled", sec.isEnabled)
                    obj.put("type", sec.type)
                    obj.put("order", sec.order)
                    obj.put("terms", sec.terms)
                    obj.put("maxPhotos", sec.maxPhotos)
                    obj.put("showPhotos", sec.showPhotos)
                    obj.put("allowPdf", sec.allowPdf)
                    obj.put("requiredFields", sec.requiredFields)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } catch (_: Exception) {
                list.joinToString(";;") { sec ->
                    listOf(
                        sec.id,
                        sec.name,
                        sec.icon,
                        sec.isEnabled.toString(),
                        sec.type,
                        sec.order.toString(),
                        sec.terms,
                        sec.maxPhotos.toString(),
                        sec.showPhotos.toString(),
                        sec.allowPdf.toString(),
                        sec.requiredFields
                    ).joinToString("||")
                }
            }
        }
    }
}
