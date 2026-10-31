package com.serviseyem.ui.helpers

import com.serviseyem.data.*
import com.serviseyem.ui.*
import com.serviseyem.utils.SecurityCryptoUtils
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Helper class to initialize and seed default Firestore collections if empty.
 */
class FirestoreSeedHelper(private val db: FirebaseFirestore) {

    private var isSeeded = false

    fun seedFirestoreIfEmpty(force: Boolean = false) {
        if (isSeeded && !force) return
        isSeeded = true

        android.util.Log.d("FirestoreSeedHelper", "🚀 Starting Firestore seed check (force=$force)...")

        // Check and seed default configurations
        db.collection("settings").document("main_settings").get().addOnSuccessListener { doc ->
            if (force || doc == null || !doc.exists()) {
                android.util.Log.d("FirestoreSeedHelper", "📄 Seeding main_settings...")
                db.collection("settings").document("main_settings").set(AdminSettingsEntity(), com.google.firebase.firestore.SetOptions.merge())
            }
        }

        db.collection("categories").limit(1).get().addOnSuccessListener { sn ->
            if (force || sn == null || sn.isEmpty) {
                android.util.Log.d("FirestoreSeedHelper", "📂 Seeding categories...")
                writeDefaultCategories()
            }
        }

        db.collection("cities").limit(1).get().addOnSuccessListener { sn ->
            if (force || sn == null || sn.isEmpty) {
                android.util.Log.d("FirestoreSeedHelper", "🏙️ Seeding cities...")
                writeDefaultCities()
            }
        }

        db.collection("providers").limit(1).get().addOnSuccessListener { sn ->
            if (force || sn == null || sn.isEmpty) {
                android.util.Log.d("FirestoreSeedHelper", "👷 Seeding demo providers...")
                writeDefaultProviders()
            }
        }

        db.collection("stores").limit(1).get().addOnSuccessListener { sn ->
            if (force || sn == null || sn.isEmpty) {
                android.util.Log.d("FirestoreSeedHelper", "🏪 Seeding demo stores...")
                writeDefaultStores()
            }
        }

        db.collection("properties").limit(1).get().addOnSuccessListener { sn ->
            if (force || sn == null || sn.isEmpty) {
                android.util.Log.d("FirestoreSeedHelper", "🏠 Seeding demo properties...")
                writeDefaultProperties()
            }
        }
    }

    fun writeDefaultSupervisors() {
        // Delete dummy supervisors
        listOf("2", "3", "4").forEach { id ->
            db.collection("supervisors").document(id).delete()
        }
    }

    fun writeDefaultColorPalettes() {
        val fbPalettes = listOf(
            ColorPaletteEntity("palette_preset_1", "🦅 اليمن الأحمر", "#CE1126", "#FFD700", "#0D1B1E", "#162A2D"),
            ColorPaletteEntity("palette_preset_2", "🔵 الأزرق الملكي", "#0D47A1", "#00E5FF", "#0A192F", "#172A45"),
            ColorPaletteEntity("palette_preset_3", "🌌 كوزميك سيلفر", "#9E9E9E", "#E0E0E0", "#121212", "#1C1C1C"),
            ColorPaletteEntity("palette_preset_4", "✨ ذهبي فاخر", "#D4AF37", "#FFD700", "#1A1A1A", "#2D2D2D"),
            ColorPaletteEntity("palette_preset_5", "🟢 زمردي راقي", "#004B49", "#50C878", "#0C1814", "#152A20"),
            ColorPaletteEntity("palette_preset_6", "⚫ الأسود الدخاني", "#121212", "#333333", "#080808", "#101010")
        )
        fbPalettes.forEach { pal ->
            db.collection("color_themes").document(pal.id).set(pal)
        }
    }

    fun writeDefaultCategories() {
        val fbCategories = listOf(
            CategoryEntity("1", "صيانة وخدمات مهنية", "🔧", 1, isMainCategory = true),
            CategoryEntity("sub_1_1", "سباكة وأنابيب", "🚰", 2, parentId = "1", isMainCategory = false),
            CategoryEntity("sub_1_2", "كهرباء ومولدات", "⚡", 3, parentId = "1", isMainCategory = false),
            CategoryEntity("sub_1_3", "تكييف وتبريد", "❄️", 4, parentId = "1", isMainCategory = false),
            CategoryEntity("sub_1_4", "نجارة وأثاث", "و", 5, parentId = "1", isMainCategory = false),
            CategoryEntity("sub_1_5", "صيانة أجهزة منزلية", "🧺", 6, parentId = "1", isMainCategory = false),
            CategoryEntity("2", "طب ورعاية صحية", "🏥", 7, isMainCategory = true),
            CategoryEntity("sub_2_1", "عيادات وأطباء", "🩺", 8, parentId = "2", isMainCategory = false),
            CategoryEntity("sub_2_2", "صيدليات ومستلزمات", "💊", 9, parentId = "2", isMainCategory = false),
            CategoryEntity("sub_2_3", "مختبرات تحاليل", "🔬", 10, parentId = "2", isMainCategory = false),
            CategoryEntity("sub_2_4", "مراكز علاج طبيعي", "🧘", 11, parentId = "2", isMainCategory = false),
            CategoryEntity("law", "محاماة واستشارات قانونية", "⚖️", 12, isMainCategory = true),
            CategoryEntity("eng", "هندسة وإنشاءات", "🏗️", 13, isMainCategory = true),
            CategoryEntity("cleaning", "تنظيف وتطهير", "🧹", 14, isMainCategory = true),
            CategoryEntity("3", "تعليم وتدريس خصوصي", "📚", 15, isMainCategory = true),
            CategoryEntity("4", "نقل ومواصلات لوجستية", "🚗", 16, isMainCategory = true),
            CategoryEntity("realestate", "عقارات وأراضي", "🏠", 17, isMainCategory = true),
            CategoryEntity("stores", "محلات ومعارض تجارية", "🏪", 18, isMainCategory = true),
            CategoryEntity("restaurants", "مطاعم وكافيهات", "🍔", 19, isMainCategory = true),
            CategoryEntity("beauty", "تجميل وعناية شخصية", "✂️", 20, isMainCategory = true),
            CategoryEntity("centers", "مراكز تخصصية وخدمية", "🏢", 21, isMainCategory = true),
            CategoryEntity("5", "تقنية وبرمجيات ذكية", "💻", 22, isMainCategory = true),
            CategoryEntity("other", "أخرى / خدمات عامة", "✏️", 23, isMainCategory = true),
            // Restaurants Subcategories
            CategoryEntity("sub_rest_1", "مطاعم يمنية وشرقية", "🍲", 24, parentId = "restaurants", isMainCategory = false),
            CategoryEntity("sub_rest_2", "وجبات سريعة وبرجر", "🍔", 25, parentId = "restaurants", isMainCategory = false),
            CategoryEntity("sub_rest_3", "كافيهات ومشروبات", "☕", 26, parentId = "restaurants", isMainCategory = false),
            CategoryEntity("sub_rest_4", "حلويات ومخابز", "🍰", 27, parentId = "restaurants", isMainCategory = false),
            CategoryEntity("sub_rest_5", "مشويات وأسماك", "🥩", 28, parentId = "restaurants", isMainCategory = false),
            // Stores Subcategories
            CategoryEntity("sub_store_1", "ملابس وأزياء", "👔", 29, parentId = "stores", isMainCategory = false),
            CategoryEntity("sub_store_2", "إلكترونيات وهواتف", "📱", 30, parentId = "stores", isMainCategory = false),
            CategoryEntity("sub_store_3", "أجهزة منزلية وكهربائية", "📺", 31, parentId = "stores", isMainCategory = false),
            CategoryEntity("sub_store_4", "سوبرماركت ومواد غذائية", "🛒", 32, parentId = "stores", isMainCategory = false),
            CategoryEntity("sub_store_5", "عطور ومستحضرات تجميل", "💄", 33, parentId = "stores", isMainCategory = false),
            // Centers Subcategories
            CategoryEntity("sub_center_1", "مراكز تجميل وصالونات", "✂️", 34, parentId = "centers", isMainCategory = false),
            CategoryEntity("sub_center_2", "مراكز طبية وتخصصية", "🏥", 35, parentId = "centers", isMainCategory = false),
            CategoryEntity("sub_center_3", "مراكز تعليم وتدريب", "🎓", 36, parentId = "centers", isMainCategory = false),
            CategoryEntity("sub_center_4", "أندية وصالات رياضية", "🏋️", 37, parentId = "centers", isMainCategory = false),
            // Real Estate Subcategories
            CategoryEntity("sub_prop_1", "شقق للإيجار والبيع", "🏢", 38, parentId = "realestate", isMainCategory = false),
            CategoryEntity("sub_prop_2", "فلل وقصور", "🏰", 39, parentId = "realestate", isMainCategory = false),
            CategoryEntity("sub_prop_3", "أراضي ومخططات", "🏞️", 40, parentId = "realestate", isMainCategory = false),
            CategoryEntity("sub_prop_4", "مكاتب ومحلات تجارية", "🏪", 41, parentId = "realestate", isMainCategory = false),
            CategoryEntity("sub_prop_5", "شاليهات واستراحات", "🏊", 42, parentId = "realestate", isMainCategory = false)
        )
        fbCategories.forEach { cat ->
            db.collection("categories").document(cat.id).set(cat)
        }
    }

    fun writeDefaultCities() {
        val defaultCities = listOf(
            CityEntity("ye_sana_cap", "أمانة العاصمة", "Sanaa Secretariat"),
            CityEntity("ye_san", "صنعاء", "Sanaa"),
            CityEntity("ye_ade", "عدن", "Aden"),
            CityEntity("ye_tai", "تعز", "Taiz"),
            CityEntity("ye_hod", "الحديدة", "Hodeidah"),
            CityEntity("ye_ibb", "إب", "Ibb"),
            CityEntity("ye_dha", "ذمار", "Dhamar"),
            CityEntity("ye_had", "حضرموت", "Hadramout"),
            CityEntity("ye_mar", "مأرب", "Marib"),
            CityEntity("ye_saa", "صعدة", "Saada"),
            CityEntity("ye_haj", "حجة", "Hajjah"),
            CityEntity("ye_mah", "المهرة", "Al Mahrah"),
            CityEntity("ye_soc", "سقطرى", "Socotra"),
            CityEntity("ye_sha", "شبوة", "Shabwah"),
            CityEntity("ye_aby", "أبين", "Abyan"),
            CityEntity("ye_bay", "البيضاء", "Al Bayda"),
            CityEntity("ye_amr", "عمران", "Amran"),
            CityEntity("ye_ray", "ريمة", "Raymah"),
            CityEntity("ye_jaw", "الجوف", "Al Jawf"),
            CityEntity("ye_lah", "لحج", "Lahj"),
            CityEntity("ye_dal", "الضالع", "Ad Dali"),
            CityEntity("ye_mhw", "المحويت", "Al Mahwit")
        )
        defaultCities.forEach { city ->
            db.collection("cities").document(city.id).set(city)
        }
    }

    fun writeDefaultBanners() {
        // No fake default banners written automatically
    }

    fun writeDefaultProviders() {
        com.serviseyem.data.helpers.DefaultDemoData.getDemoProviders().forEach { provider ->
            db.collection("providers").document(provider.id).set(provider, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun writeDefaultStores() {
        com.serviseyem.data.helpers.DefaultDemoData.getDemoStores().forEach { store ->
            db.collection("stores").document(store.id).set(store, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun writeDefaultProperties() {
        com.serviseyem.data.helpers.DefaultDemoData.getDemoProperties().forEach { prop ->
            db.collection("properties").document(prop.id).set(prop, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    fun writeDefaultJobs() {
        // Reserved for future default jobs
    }

    fun writeDefaultProducts() {
        // Reserved for future default products
    }
}
