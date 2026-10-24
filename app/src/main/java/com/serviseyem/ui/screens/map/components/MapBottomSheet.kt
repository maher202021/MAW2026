package com.serviseyem.ui.screens.map.components

import android.content.Intent
import com.serviseyem.ui.*
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Modern Material 3 Bottom Sheet Card displaying full details of the clicked entity:
 * - Real-time ETA and exact distance calculation
 * - Direct Call, WhatsApp, Navigation, and Booking actions
 * - Distinct category badge colors (#00E5FF, #10B981, #F59E0B, #EC4899)
 */
@Composable
fun MapBottomSheet(
    entity: Any,
    userLat: Double,
    userLng: Double,
    onDismiss: () -> Unit,
    onRequestBooking: (Any) -> Unit,
    onOpenDetails: (Any) -> Unit,
    onOpenChat: ((Any) -> Unit)? = null,
    onOpenAleph: ((Any) -> Unit)? = null,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Extract standardized information
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var categoryLabel by remember { mutableStateOf("") }
    var badgeColor by remember { mutableStateOf(Color(0xFF00E5FF)) }
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
            badgeColor = Color(0xFF00E5FF)
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

    val distanceMeters = MapDistanceCalculator.calculateDistanceMeters(userLat, userLng, lat, lng)
    val distanceText = MapDistanceCalculator.formatDistance(distanceMeters)
    val etaText = MapDistanceCalculator.computeEta(distanceMeters)

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        shadowElevation = 16.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("map_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(36.dp, 4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .align(Alignment.CenterHorizontally)
            )

            // Header Row: Avatar, Info, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Image / Icon Avatar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(2.dp, badgeColor.copy(alpha = 0.6f)),
                    modifier = Modifier.size(54.dp)
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
                                fontSize = 24.sp
                            )
                        }
                    }
                }

                // Title & Category
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(bottom = 2.dp)
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

                // Close & Share & Navigation Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Share Location
                    IconButton(
                        onClick = {
                            try {
                                val mapUrl = "https://www.google.com/maps/search/?api=1&query=$lat,$lng"
                                val shareText = "📍 $title\n$subtitle\nالموقع على الخريطة:\n$mapUrl"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "مشاركة الموقع"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.size(32.dp).testTag("share_location_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة الموقع", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }

                    // Google Maps Navigation
                    IconButton(
                        onClick = {
                            try {
                                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($title)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                    context.startActivity(browserIntent)
                                }
                            } catch (e: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                context.startActivity(browserIntent)
                            }
                        },
                        modifier = Modifier.size(32.dp).testTag("google_maps_nav_btn")
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "فتح في خرائط جوجل", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }

                    // Share Button (زر مشاركة البيانات)
                    IconButton(
                        onClick = {
                            try {
                                val shareText = "📍 $title\n🏷️ $subtitle ($categoryLabel)\n📞 الهاتف: $phone\n🗺️ تمت المشاركة عبر تطبيق دليل خدمات اليمن"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة بيانات الخدمة"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.size(32.dp).testTag("share_entity_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_bottom_sheet_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B))

            // Meta Info: Distance + ETA + Rating + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance & ETA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                    Text(
                        text = "$distanceText ($etaText)",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Rating & Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "⭐ $rating",
                        color = Color(0xFFFFD700),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isAvailable) "متاح 🟢" else "غير متاح 🔴",
                        color = if (isAvailable) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp
                    )
                }
            }

            // Action Buttons Group (Contact & Booking & Details)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Direct Communication (Call, WhatsApp, In-App Chat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Call Action
                    if (phone.isNotBlank()) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("call_action_btn")
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }

                        // 2. WhatsApp Action
                        Button(
                            onClick = {
                                try {
                                    val cleanWaPhone = phone.trim().removePrefix("+").removePrefix("00967").removePrefix("0")
                                    val waUrl = "https://wa.me/967$cleanWaPhone?text=" + Uri.encode("مرحباً، أود الاستفسار بخصوص خدماتكم المعروضة في دليل اليمن: $title")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "تطبيق واتساب غير متوفر", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(38.dp)
                                .testTag("whatsapp_action_btn")
                        ) {
                            Text("💬", fontSize = 13.sp)
                            Spacer(Modifier.width(3.dp))
                            Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                        }
                    }

                    // 3. In-App Chat Action
                    Button(
                        onClick = { onOpenChat?.invoke(entity) ?: onOpenDetails(entity) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF475569),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("chat_action_btn")
                    ) {
                        Icon(Icons.Default.MailOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("محادثة", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }

                // Row 2: Booking/Order & Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Booking / Request Action
                    val bookingBtnText = when (entity) {
                        is ProviderEntity -> "حجز موعد / طلب 🔧"
                        is StoreEntity -> {
                            val isMedical = entity.sectionId.contains("medical") || entity.categoryId.contains("medical") || entity.name.contains("طبي") || entity.name.contains("عياد")
                            val isRestaurant = entity.sectionId.contains("restaurant") || entity.categoryId.contains("restaurant") || entity.name.contains("مطعم") || entity.name.contains("كافيه")
                            when {
                                isMedical -> "حجز موعد طبي 🩺"
                                isRestaurant -> "حجز طاولة / وجبة 🍽️"
                                else -> "طلب شراء بضاعة 🛒"
                            }
                        }
                        is PropertyEntity -> "طلب معاينة العقار 🏡"
                        else -> "حجز موعد / طلب 📅"
                    }

                    Button(
                        onClick = { onRequestBooking(entity) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(40.dp)
                            .testTag("book_entity_btn")
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(bookingBtnText, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Details Action
                    Button(
                        onClick = { onOpenDetails(entity) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = badgeColor,
                            contentColor = Color(0xFF0F172A)
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("view_details_action_btn")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("التفاصيل", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Aleph Vision Assistant Action
                    Button(
                        onClick = { onOpenAleph?.invoke(entity) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(0.95f)
                            .height(40.dp)
                            .testTag("aleph_assistant_action_btn")
                    ) {
                        Text("👁️ Aleph", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}
