package com.serviseyem.utils

import com.serviseyem.ui.screens.map.utils.CityAliases

/**
 * CityIdResolver
 * يحوّل اسم المحافظة أو المدينة أو رمزها إلى معرّف المدينة النظامي (مثل ye_had, ye_ade, ye_tai...)
 * بدون أي تحويل قسري إلى صنعاء عند عدم التعرف على المدينة.
 */
object CityIdResolver {

    data class GovernorateOption(
        val id: String,
        val nameAr: String
    )

    val ALL_GOVERNORATES: List<GovernorateOption> = listOf(
        GovernorateOption("ye_san", "صنعاء"),
        GovernorateOption("ye_sana_cap", "أمانة العاصمة"),
        GovernorateOption("ye_ade", "عدن"),
        GovernorateOption("ye_tai", "تعز"),
        GovernorateOption("ye_hod", "الحديدة"),
        GovernorateOption("ye_ibb", "إب"),
        GovernorateOption("ye_dha", "ذمار"),
        GovernorateOption("ye_had", "حضرموت (المكلا / سيئون)"),
        GovernorateOption("ye_mar", "مأرب"),
        GovernorateOption("ye_saa", "صعدة"),
        GovernorateOption("ye_haj", "حجة"),
        GovernorateOption("ye_mah", "المهرة"),
        GovernorateOption("ye_soc", "سقطرى"),
        GovernorateOption("ye_sha", "شبوة"),
        GovernorateOption("ye_aby", "أبين"),
        GovernorateOption("ye_bay", "البيضاء"),
        GovernorateOption("ye_amr", "عمران"),
        GovernorateOption("ye_ray", "ريمة"),
        GovernorateOption("ye_jaw", "الجوف"),
        GovernorateOption("ye_lah", "لحج"),
        GovernorateOption("ye_dal", "الضالع"),
        GovernorateOption("ye_mhw", "المحويت")
    )

    private val canonicalAliasToId: Map<String, String> by lazy {
        val map = linkedMapOf<String, String>()
        // Direct IDs & official names
        ALL_GOVERNORATES.forEach { gov ->
            map[ArabicNormalizer.normalize(gov.id)] = gov.id
            map[ArabicNormalizer.normalize(gov.nameAr)] = gov.id
        }
        // Specific canonical city-key mapping (avoiding CityAliases collisions on ye_dha/ye_mah)
        val explicitCityMap = mapOf(
            "أمانة العاصمة" to "ye_sana_cap",
            "صنعاء" to "ye_san",
            "عدن" to "ye_ade",
            "تعز" to "ye_tai",
            "الحديدة" to "ye_hod",
            "إب" to "ye_ibb",
            "اب" to "ye_ibb",
            "ذمار" to "ye_dha",
            "حضرموت" to "ye_had",
            "المكلا" to "ye_had",
            "سيئون" to "ye_had",
            "مأرب" to "ye_mar",
            "مارب" to "ye_mar",
            "صعدة" to "ye_saa",
            "حجة" to "ye_haj",
            "شبوة" to "ye_sha",
            "عتق" to "ye_sha",
            "المهرة" to "ye_mah",
            "الغيضة" to "ye_mah",
            "أبين" to "ye_aby",
            "زنجبار" to "ye_aby",
            "لحج" to "ye_lah",
            "الحوطة" to "ye_lah",
            "الضالع" to "ye_dal",
            "البيضاء" to "ye_bay",
            "رداع" to "ye_bay",
            "عمران" to "ye_amr",
            "المحويت" to "ye_mhw",
            "ريمة" to "ye_ray",
            "الجوف" to "ye_jaw",
            "سقطرى" to "ye_soc",
            "حديبو" to "ye_soc",
            "ye_hud" to "ye_hod",
            "hodeidah" to "ye_hod",
            "hadramout" to "ye_had",
            "mukalla" to "ye_had",
            "seiyun" to "ye_had",
            "marib" to "ye_mar",
            "dhamar" to "ye_dha",
            "ibb" to "ye_ibb",
            "taiz" to "ye_tai",
            "aden" to "ye_ade",
            "sanaa" to "ye_san",
            "saada" to "ye_saa",
            "hajjah" to "ye_haj",
            "shabwah" to "ye_sha",
            "al-mahra" to "ye_mah",
            "mahrah" to "ye_mah",
            "abyan" to "ye_aby",
            "lahj" to "ye_lah",
            "dhale" to "ye_dal",
            "dale" to "ye_dal",
            "al-bayda" to "ye_bay",
            "bayda" to "ye_bay",
            "amran" to "ye_amr",
            "al-mahweet" to "ye_mhw",
            "mahwit" to "ye_mhw",
            "raymah" to "ye_ray",
            "al-jawf" to "ye_jaw",
            "jawf" to "ye_jaw",
            "socotra" to "ye_soc"
        )
        explicitCityMap.forEach { (k, v) ->
            map[ArabicNormalizer.normalize(k)] = v
        }
        // Include CityAliases districts (skipping short numeric codes or colliding legacy IDs)
        CityAliases.ALIASES.forEach { (cityKey, aliases) ->
            val targetId = map[ArabicNormalizer.normalize(cityKey)] ?: return@forEach
            aliases.forEach { alias ->
                val normAlias = ArabicNormalizer.normalize(alias)
                if (normAlias.length >= 2 && normAlias != "ye_dha" && normAlias != "ye_mah" && !map.containsKey(normAlias)) {
                    map[normAlias] = targetId
                }
            }
        }
        map
    }

    fun resolve(rawName: String?, knownIds: Map<String, String> = canonicalAliasToId): String? {
        val trimmed = rawName.orEmpty().trim()
        if (trimmed.isBlank()) return null
        val n = ArabicNormalizer.normalize(trimmed)
        if (n.length < 2) return null

        // Direct ID check (e.g., "ye_had")
        if (trimmed.startsWith("ye_") && ALL_GOVERNORATES.any { it.id.equals(trimmed, ignoreCase = true) }) {
            return ALL_GOVERNORATES.first { it.id.equals(trimmed, ignoreCase = true) }.id
        }

        knownIds[n]?.let { return it }
        return knownIds.entries.firstOrNull { (k, _) ->
            k.length >= 2 && (n.contains(k) || k.contains(n))
        }?.value
    }

    fun displayNameForId(cityId: String?): String {
        if (cityId.isNullOrBlank()) return "غير محدد"
        return ALL_GOVERNORATES.firstOrNull { it.id.equals(cityId.trim(), ignoreCase = true) }?.nameAr ?: cityId
    }
}
