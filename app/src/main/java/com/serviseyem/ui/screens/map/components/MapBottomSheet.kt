package com.serviseyem.ui.screens.map.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.PropertyEntity
import com.serviseyem.data.ProviderEntity
import com.serviseyem.data.StoreEntity
import com.serviseyem.ui.screens.map.utils.MapDistanceCalculator
import com.serviseyem.utils.VisualThemePalette

/**
 * 📋 MapBottomSheet
 * Hidden by default, appears only upon tapping a place/pin:
 * - Accurate road-snapped distance & ETA
 * - Emerald Green [بدء الملاحة] button (#10B981)
 * - [اتصال] (Call)
 * - [حجز] (Book)
 * - Glassmorphism dark styling (#0B0F19)
 */
@Composable
fun MapBottomSheet(
    entity: Any,
    userLat: Double,
    userLng: Double,
    onDismiss: () -> Unit,
    onRequestBooking: (Any) -> Unit,
    onOpenDetails: (Any) -> Unit,
    themeColors: VisualThemePalette,
    onAskAleph: ((Any) -> Unit)? = null,
    onStartNavigation: ((Any) -> Unit)? = null,
    onAddStop: ((Any) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var categoryLabel by remember { mutableStateOf("") }
    var badgeColor by remember { mutableStateOf(Color(0xFF10B981)) }
    var rating by remember { mutableDoubleStateOf(5.0) }
    var phone by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var lat by remember { mutableDoubleStateOf(0.0) }
    var lng by remember { mutableDoubleStateOf(0.0) }

    when (entity) {
        is ProviderEntity -> {
            title = entity.name
            subtitle = entity.customCategoryName.ifEmpty { entity.specialization.ifEmpty { entity.profession } }
            categoryLabel = "👷 فني صيانة معتمد"
            badgeColor = Color(0xFF10B981)
            rating = entity.rating.toDouble()
            phone = entity.phone
            imageUrl = entity.profileImage.ifEmpty { entity.coverImage }
            isAvailable = entity.isAvailable
            lat = entity.latitude
            lng = entity.longitude
        }
        is StoreEntity -> {
            title = entity.name
            val isMedical = entity.sectionId.contains("medical") || entity.categoryId.contains("medical") || entity.name.contains("طبي") || entity.name.contains("صيدلية")
            val isRestaurant = !isMedical && (entity.sectionId.contains("restaurant") || entity.categoryId.contains("restaurant") || entity.name.contains("مطعم") || entity.name.contains("كافيه"))
            
            categoryLabel = if (isMedical) "🏥 مركز طبي / صيدلية" else if (isRestaurant) "🍔 مطعم / كافيه" else "🏪 متجر معتمد"
            badgeColor = if (isMedical) Color(0xFFEC4899) else if (isRestaurant) Color(0xFFF59E0B) else Color(0xFF10B981)
            subtitle = entity.description.take(50).ifEmpty { if (isMedical) "خدمات طبية وصيدلانية" else "محل تجاري معتمد" }
            rating = entity.rating.toDouble()
            phone = entity.phone
            imageUrl = entity.logoImage.ifEmpty { entity.coverImage }
            isAvailable = entity.isActive
            lat = entity.latitude
            lng = entity.longitude
        }
        is PropertyEntity -> {
            title = entity.title
            subtitle = "${entity.price} ${entity.currency} - ${entity.description.take(40)}"
            categoryLabel = "🏠 عقار معروض"
            badgeColor = Color(0xFF8B5CF6)
            rating = 5.0
            phone = entity.phone
            imageUrl = entity.images.firstOrNull() ?: ""
            isAvailable = entity.isActive
            lat = entity.latitude
            lng = entity.longitude
        }
    }
    val hasValidCoords = lat != 0.0 && lng != 0.0 && !lat.isNaN() && !lng.isNaN()
    val distanceMeters = if (hasValidCoords)
        MapDistanceCalculator.calculateDistanceMeters(userLat, userLng, lat, lng) else 0.0
    val distanceText = if (hasValidCoords) MapDistanceCalculator.formatDistance(distanceMeters) else "الموقع غير محدد"
    val etaText = if (hasValidCoords) MapDistanceCalculator.computeEta(distanceMeters) else "—"

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color(0xFF0B0F19).copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(36.dp, 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Row: Avatar, Info, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f)),
                    modifier = Modifier.size(50.dp)
                ) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = if (entity is ProviderEntity) "👷" else if (entity is StoreEntity) "🏪" else "🏠",
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = categoryLabel,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (isAvailable) "متاح 🟢" else "غير متاح 🔴",
                            color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Add Stop / Bookmark Action
                IconButton(
                    onClick = { onAddStop?.invoke(entity) },
                    modifier = Modifier.size(36.dp).testTag("add_stop_btn")
                ) {
                    Icon(
                        Icons.Default.AddLocationAlt,
                        contentDescription = "إضافة محطة",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Close Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_bottom_sheet_btn")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Distance & ETA Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Text(
                        text = "المسافة المباشرة: $distanceText",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = etaText,
                    color = Color(0xFF10B981),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!hasValidCoords) {
                Text(
                    text = "⚠️ لا يمكن الملاحة: موقع هذا المكان غير مسجّل بإحداثيات دقيقة",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Multi-Stop Waypoint Button
            OutlinedButton(
                onClick = { onAddStop?.invoke(entity) },
                enabled = hasValidCoords,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, if (hasValidCoords) Color(0xFF38BDF8) else Color.Gray),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0F172A), contentColor = Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("add_stop_main_btn")
            ) {
                Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("➕ إضافة هذه الوجهة كمحطة توقف للمسار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Action Buttons: [بدء الملاحة] Emerald Green + [اتصال] + [حجز]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. [بدء الملاحة] Emerald Green (#10B981)
                Button(
                    onClick = { onStartNavigation?.invoke(entity) },
                    enabled = hasValidCoords,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color.Gray
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("start_navigation_btn")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("بدء الملاحة", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                // 2. [اتصال] Call Action
                if (phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        modifier = Modifier
                            .weight(0.9f)
                            .height(44.dp)
                            .testTag("call_action_btn")
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("اتصال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // 3. [حجز] Booking Action
                val bookingBtnText = when (entity) {
                    is ProviderEntity -> "حجز"
                    is StoreEntity -> {
                        val isMedical = entity.sectionId.contains("medical") || entity.categoryId.contains("medical") || entity.name.contains("طبي")
                        val isRestaurant = entity.sectionId.contains("restaurant") || entity.categoryId.contains("restaurant") || entity.name.contains("مطعم")
                        when {
                            isMedical -> "موعد"
                            isRestaurant -> "طاولة"
                            else -> "طلب"
                        }
                    }
                    is PropertyEntity -> "معاينة"
                    else -> "حجز"
                }

                Button(
                    onClick = { onRequestBooking(entity) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(44.dp)
                        .testTag("book_entity_btn")
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(bookingBtnText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
