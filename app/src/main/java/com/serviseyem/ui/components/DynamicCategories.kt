package com.serviseyem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.ui.theme.*
import com.serviseyem.utils.VisualThemePalette

/**
 * 🎨 SectionFamily Data Model (عوائل الأقسام الـ5)
 */
data class SectionFamilyItem(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val primaryColor: Color,
    val gradient: Brush,
    val subTabs: List<String>
)

/**
 * 🏷️ Category21Item Data Model (الـ21 قسم كاملة)
 */
data class Category21Item(
    val id: String,
    val title: String,
    val emoji: String,
    val familyId: String,
    val color: Color,
    val icon: ImageVector,
    val count: Int
)

object CategoryDataRepository {

    val SECTION_FAMILIES = listOf(
        SectionFamilyItem(
            id = "FOOD",
            title = "عائلة الأطعمة والمشروبات",
            emoji = "🍔",
            description = "مطاعم، كافيهات، وحلويات",
            primaryColor = FoodFamilyPrimary,
            gradient = Brush.horizontalGradient(listOf(FoodFamilyPrimary, FoodFamilySecondary)),
            subTabs = listOf("مطاعم", "كافيهات", "حلويات ومخابز", "عصائر ومثلجات", "توصيل طلبات")
        ),
        SectionFamilyItem(
            id = "TECH",
            title = "عائلة التقنية والمتاجر",
            emoji = "💻",
            description = "جوالات، حاسوب، وأجهزة منزلية",
            primaryColor = TechFamilySecondary,
            gradient = Brush.horizontalGradient(listOf(TechFamilyPrimary, TechFamilySecondary)),
            subTabs = listOf("جوالات وإكسسوارات", "إلكترونيات وحاسوب", "أجهزة منزلية", "ألعاب وترفيه رقمي", "صيانة أجهزة")
        ),
        SectionFamilyItem(
            id = "TRAVEL",
            title = "عائلة السياحة والسفر",
            emoji = "🏨",
            description = "فنادق، مكاتب سفر، وعقارات",
            primaryColor = TravelFamilyPrimary,
            gradient = Brush.horizontalGradient(listOf(TravelFamilyPrimary, TravelFamilySecondary)),
            subTabs = listOf("فنادق ومنتجعات", "مكاتب سفر وحجز", "شقق وإقامات", "معالم ورحلات سياحية", "تأجير سيارات")
        ),
        SectionFamilyItem(
            id = "HEALTH",
            title = "عائلة الصحة والأناقة",
            emoji = "🩺",
            description = "عيادات، صيدليات، وتجميل",
            primaryColor = HealthFamilyPrimary,
            gradient = Brush.horizontalGradient(listOf(HealthFamilyPrimary, HealthFamilySecondary)),
            subTabs = listOf("عيادات وأطباء", "أسنان وتجميل", "صالونات تجميل", "حلاقة وبريم", "صيدليات", "لياقة ورياضة")
        ),
        SectionFamilyItem(
            id = "SERVICES",
            title = "عائلة الخدمات والمهنيين",
            emoji = "⚡",
            description = "كهرباء، سباكة، مقاولات، ونقل",
            primaryColor = ServicesFamilyPrimary,
            gradient = Brush.horizontalGradient(listOf(ServicesFamilyPrimary, ServicesFamilyTeal)),
            subTabs = listOf("كهرباء وسباكة", "مقاولات وبناء", "نقليات وشحن", "تعليم وتدريب", "خدمات ٢٤ ساعة", "زراعة ومزارع")
        )
    )

    val ALL_21_CATEGORIES = listOf(
        Category21Item("elec_plumb", "كهرباء وسباكة", "⚡", "SERVICES", ServicesFamilyPrimary, Icons.Default.Bolt, 48),
        Category21Item("restaurants", "مطاعم وأطعمة", "🍔", "FOOD", FoodFamilyPrimary, Icons.Default.Restaurant, 65),
        Category21Item("tech_mobile", "جوالات وإلكترونيات", "📱", "TECH", TechFamilySecondary, Icons.Default.Smartphone, 52),
        Category21Item("medical_clinics", "أطباء وعيادات", "🩺", "HEALTH", HealthFamilyPrimary, Icons.Default.MedicalServices, 34),
        Category21Item("hotels_resorts", "فنادق وإقامات", "🏨", "TRAVEL", TravelFamilyPrimary, Icons.Default.Hotel, 29),
        Category21Item("construction", "مقاولات وبناء", "🏗️", "SERVICES", ServicesFamilySecondary, Icons.Default.Construction, 22),
        Category21Item("cafes", "كافيهات ومقاهي", "☕", "FOOD", FoodFamilySecondary, Icons.Default.Coffee, 38),
        Category21Item("computer_maint", "صيانة حاسوب وتقنية", "💻", "TECH", TechFamilyCyan, Icons.Default.Computer, 27),
        Category21Item("dental_beauty", "أسنان وتجميل", "🦷", "HEALTH", HealthFamilySecondary, Icons.Default.Face, 19),
        Category21Item("travel_agency", "مكاتب سفر وحجوزات", "✈️", "TRAVEL", TravelFamilySecondary, Icons.Default.Flight, 24),
        Category21Item("cargo_shipping", "نقليات وشحن", "🚛", "SERVICES", ServicesFamilyTeal, Icons.Default.LocalShipping, 31),
        Category21Item("bakeries_sweets", "حلويات ومخابز", "🍰", "FOOD", FoodFamilyGold, Icons.Default.Cake, 41),
        Category21Item("home_appliances", "أجهزة منزلية", "🧺", "TECH", TechFamilySecondary, Icons.Default.Kitchen, 26),
        Category21Item("barber_salon", "صالونات تجميل وحلاقة", "✂️", "HEALTH", HealthFamilyPrimary, Icons.Default.ContentCut, 33),
        Category21Item("car_rental", "تأجير سيارات", "🚗", "TRAVEL", TravelFamilySunset, Icons.Default.DirectionsCar, 18),
        Category21Item("education", "تعليم وتدريب", "📚", "SERVICES", Color(0xFF14B8A6), Icons.Default.School, 25),
        Category21Item("juices_icecream", "عصائر ومثلجات", "🧃", "FOOD", GlowingCoralStart, Icons.Default.LocalBar, 20),
        Category21Item("pharmacies", "صيدليات ودواء", "💊", "HEALTH", SuccessGreen, Icons.Default.LocalPharmacy, 36),
        Category21Item("emergency_24h", "خدمات طوارئ ٢٤ ساعة", "🚨", "SERVICES", ServicesFamilyPrimary, Icons.Default.Warning, 15),
        Category21Item("real_estate", "عقارات وشقق", "🏢", "TRAVEL", SkyBlueStart, Icons.Default.Apartment, 28),
        Category21Item("agriculture", "زراعة ومزارع", "🌾", "SERVICES", Color(0xFF84CC16), Icons.Default.Grass, 14)
    )
}

/**
 * 🏷️ DynamicCategories Bar Component
 */
@Composable
fun DynamicCategories(
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isAll = selectedCategoryId.isEmpty() || selectedCategoryId == "ALL"
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isAll) GoldGradientStart else CardSurface,
                border = BorderStroke(1.dp, if (isAll) GoldGradientEnd else CardBorder),
                modifier = Modifier.clickable { onCategorySelected("ALL") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🌟", fontSize = 13.sp)
                    Text(
                        text = "كل الأقسام",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAll) Black else TextPrimaryWhite
                    )
                }
            }
        }

        items(CategoryDataRepository.ALL_21_CATEGORIES) { cat ->
            val isSelected = selectedCategoryId == cat.id

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) cat.color else CardSurface,
                border = BorderStroke(1.dp, if (isSelected) Color.White else CardBorder),
                modifier = Modifier.clickable { onCategorySelected(cat.id) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(cat.emoji, fontSize = 13.sp)
                    Text(
                        text = cat.title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Black else TextPrimaryWhite
                    )
                }
            }
        }
    }
}
