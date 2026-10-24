package com.serviseyem.ui.screens.entities

import androidx.compose.foundation.BorderStroke
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.JobEntity
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.data.ProductEntity
import com.serviseyem.utils.VisualThemePalette

@Composable
fun ProfileSpecs(
    entityType: ProfileEntityType,
    provider: ProviderEntity?,
    store: StoreEntity?,
    property: PropertyEntity?,
    job: JobEntity?,
    products: List<ProductEntity>,
    themeColors: VisualThemePalette
) {
    when (entityType) {
        ProfileEntityType.TECHNICIAN -> TechnicianSpecificSpecsView(provider, themeColors)
        ProfileEntityType.STORE -> StoreSpecificSpecsView(store, products, themeColors)
        ProfileEntityType.RESTAURANT -> RestaurantSpecificSpecsView(store, products, themeColors)
        ProfileEntityType.MEDICAL -> MedicalSpecificSpecsView(provider, store, themeColors)
        ProfileEntityType.REAL_ESTATE -> RealEstateSpecificSpecsView(property, themeColors)
        ProfileEntityType.JOB -> JobSpecificSpecsView(job, themeColors)
        ProfileEntityType.GENERAL -> GeneralSpecificSpecsView(provider, store, themeColors)
    }
}

@Composable
fun TechnicianSpecificSpecsView(provider: ProviderEntity?, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔧 بيانات المهنة والاعتماد الفني", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("سعر المعاينة:", "${provider?.previewPrice?.toInt() ?: 1500} ر.ي", Icons.Default.CheckCircle)
                SpecBadge("الحالة:", if (provider?.isAvailable == true) "متاح للعمل الآن 🟢" else "مشغول 🔴", Icons.Default.Info)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("التوثيق:", if (provider?.isVerified == true) "موثق بالهوية ✅" else "قيد التدقيق", Icons.Default.Star)
                SpecBadge("المدينة:", provider?.cityId?.takeIf { it.isNotEmpty() } ?: "صنعاء", Icons.Default.LocationOn)
            }
        }
    }
}

@Composable
fun StoreSpecificSpecsView(store: StoreEntity?, products: List<ProductEntity>, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🛍️ تفاصيل المتجر وسياسة التوصيل", fontWeight = FontWeight.Bold, color = Color(0xFF34D399), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("أوقات الدوام:", store?.workingHours ?: "9:00 ص - 10:00 م", Icons.Default.AccountBox)
                SpecBadge("عدد المنتجات:", "${products.count { it.storeId == store?.id }} صنف", Icons.Default.ShoppingCart)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("طرق الدفع:", "محافظ إلكترونية + نقد", Icons.Default.Star)
                SpecBadge("السجل التجاري:", store?.commercialRegisterNo?.takeIf { it.isNotEmpty() } ?: "معتمد بالمنصة", Icons.Default.CheckCircle)
            }
        }
    }
}

@Composable
fun RestaurantSpecificSpecsView(store: StoreEntity?, products: List<ProductEntity>, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🍽️ خدمات المطعم والضيافة", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("أوقات الوجبات:", "فطور - غداء - عشاء", Icons.Default.Favorite)
                SpecBadge("خدمة التوصيل:", "سريع لجميع الأحياء 🛵", Icons.Default.Send)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("جلسات عائلية:", "متوفرة وقسم خاص 👨‍👩‍👧", Icons.Default.Home)
                SpecBadge("حجز مسبق:", "متاح عبر التطبيق 📱", Icons.Default.DateRange)
            }
        }
    }
}

@Composable
fun MedicalSpecificSpecsView(provider: ProviderEntity?, store: StoreEntity?, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🩺 بيانات الاعتماد الطبي والعيادات", fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("قسم الطوارئ:", "متاح 24 ساعة 🚨", Icons.Default.Warning)
                SpecBadge("الترخيص الطبي:", store?.medicalLicenseNo?.ifEmpty { "مرخص رسمياً 📄" } ?: "مرخص رسمياً 📄", Icons.Default.CheckCircle)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("حجز الكشوفات:", "مسبق لتجنب الانتظار ⏱️", Icons.Default.DateRange)
                SpecBadge("المختبر والأشعة:", "فحوصات متكاملة 🔬", Icons.Default.Star)
            }
        }
    }
}

@Composable
fun RealEstateSpecificSpecsView(property: PropertyEntity?, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🏢 مواصفات العقار والاستثمار", fontWeight = FontWeight.Bold, color = Color(0xFFA78BFA), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("نوع العرض:", if (property?.type == "rent") "إيجار شهري/سنوي 🔑" else "للبيع والشراء 📜", Icons.Default.Home)
                SpecBadge("السعر:", "${property?.price?.toInt() ?: 0} ${property?.currency ?: "YER"}", Icons.Default.Star)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("تصنيف العقار:", property?.propertyType?.takeIf { it.isNotEmpty() } ?: "شقة سكنية", Icons.Default.LocationOn)
                SpecBadge("المعاينة:", "متاحة بالتنسيق المباشر 🚶‍♂️", Icons.Default.CheckCircle)
            }
        }
    }
}

@Composable
fun JobSpecificSpecsView(job: JobEntity?, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFF06B6D4).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("💼 تفاصيل فرصة العمل", fontWeight = FontWeight.Bold, color = Color(0xFF22D3EE), fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("نوع الدوام:", job?.jobType?.takeIf { it.isNotEmpty() } ?: "دوام كامل", Icons.Default.AccountBox)
                SpecBadge("الراتب المتوقع:", job?.salary?.takeIf { it.isNotEmpty() } ?: "حسب الاتفاق", Icons.Default.Star)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("اسم الشركة:", job?.companyName?.takeIf { it.isNotEmpty() } ?: "جهة معتمدة", Icons.Default.CheckCircle)
                SpecBadge("المدينة:", job?.cityId?.takeIf { it.isNotEmpty() } ?: "صنعاء", Icons.Default.LocationOn)
            }
        }
    }
}

@Composable
fun GeneralSpecificSpecsView(provider: ProviderEntity?, store: StoreEntity?, themeColors: VisualThemePalette) {
    Card(
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("📋 معلومات النشاط", fontWeight = FontWeight.Bold, color = themeColors.accent, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SpecBadge("الخدمات:", "متنوعة ومعتمدة", Icons.Default.Build)
                SpecBadge("الحالة:", "نشط على المنصة ✅", Icons.Default.CheckCircle)
            }
        }
    }
}

@Composable
fun SpecBadge(title: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(title, fontSize = 9.sp, color = Color.Gray)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
