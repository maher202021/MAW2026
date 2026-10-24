package com.serviseyem.ui.screens.admin

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.serviseyem.data.*
import com.serviseyem.data.models.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette
import com.serviseyem.ui.screens.admin.components.*

@Composable
fun AdminDataManagementScreenContent(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val providers by viewModel.providers.collectAsState()

    var selectedSection by remember { mutableStateOf("المحلات والمتاجر") }
    val sectionTabs = listOf(
        "المحلات والمتاجر",
        "المطاعم والكافيهات",
        "المراكز الطبية",
        "العقارات",
        "إعلانات الوظائف",
        "الفنيين والمهن"
    )

    // Dialog لتعديل الكيان (الصور، الأسعار، الخدمات، الاسم، الهاتف)
    var editingEntity by remember { mutableStateOf<Any?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editLogoUrl by remember { mutableStateOf("") }
    var editCoverUrl by remember { mutableStateOf("") }
    var editPriceOrSalary by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editServicesOrProducts by remember { mutableStateOf("") }

    // Dialog لإضافة عنصر جديد
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var newLogoUrl by remember { mutableStateOf("") }
    var newCoverUrl by remember { mutableStateOf("") }
    var newPriceOrSalary by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("صنعاء") }
    var newServicesOrProducts by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📋 إدارة وتهيئة بيانات الأقسام الشاملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
            Button(
                onClick = {
                    newName = ""
                    newPhone = ""
                    newLogoUrl = ""
                    newCoverUrl = ""
                    newPriceOrSalary = ""
                    newServicesOrProducts = ""
                    showAddDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("+ إضافة عنصر جديد", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        // شريط الأقسام السريع
        AdminFilterChips(
            categories = sectionTabs,
            selectedCategory = selectedSection,
            onSelectCategory = { selectedSection = it },
            themeColors = themeColors
        )

        // عرض العناصر وتعديلها أو التوصية بها أو حذفها
        when (selectedSection) {
            "المحلات والمتاجر" -> {
                val list = stores.filter { it.sectionId != "restaurants" && it.sectionId != "medical" }
                if (list.isEmpty()) {
                    Text("لا توجد متاجر مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    list.forEach { store ->
                        AdminSectionEntityCard(
                            title = store.name,
                            phone = store.phone,
                            location = "${store.cityId} - ${store.localNeighborhood}",
                            logoUrl = store.logoImage,
                            coverUrl = store.coverImage,
                            priceOrDetails = "ساعات العمل: ${store.workingHours}",
                            isRecommended = store.isVerified,
                            onToggleRecommend = {
                                val newRec = !store.isVerified
                                viewModel.db.collection("stores").document(store.id).update("isVerified", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم تمييز المتجر والتوصية به!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = store
                                editName = store.name
                                editPhone = store.phone
                                editLogoUrl = store.logoImage
                                editCoverUrl = store.coverImage
                                editPriceOrSalary = store.workingHours
                                editCity = store.cityId
                                editServicesOrProducts = store.description
                            },
                            onDelete = {
                                viewModel.db.collection("stores").document(store.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف المتجر بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }

            "المطاعم والكافيهات" -> {
                val list = stores.filter { it.sectionId == "restaurants" }
                if (list.isEmpty()) {
                    Text("لا توجد مطاعم مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    list.forEach { rest ->
                        AdminSectionEntityCard(
                            title = rest.name,
                            phone = rest.phone,
                            location = "${rest.cityId} - ${rest.localNeighborhood}",
                            logoUrl = rest.logoImage,
                            coverUrl = rest.coverImage,
                            priceOrDetails = "المنتجات/الأطعمة: ${rest.description.take(40)}",
                            isRecommended = rest.isVerified,
                            onToggleRecommend = {
                                val newRec = !rest.isVerified
                                viewModel.db.collection("stores").document(rest.id).update("isVerified", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم تمييز المطعم والتوصية به!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = rest
                                editName = rest.name
                                editPhone = rest.phone
                                editLogoUrl = rest.logoImage
                                editCoverUrl = rest.coverImage
                                editPriceOrSalary = rest.workingHours
                                editCity = rest.cityId
                                editServicesOrProducts = rest.description
                            },
                            onDelete = {
                                viewModel.db.collection("stores").document(rest.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف المطعم بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }

            "المراكز الطبية" -> {
                val list = stores.filter { it.sectionId == "medical" }
                if (list.isEmpty()) {
                    Text("لا توجد مراكز طبية مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    list.forEach { med ->
                        AdminSectionEntityCard(
                            title = med.name,
                            phone = med.phone,
                            location = "${med.cityId} - ${med.localNeighborhood}",
                            logoUrl = med.logoImage,
                            coverUrl = med.coverImage,
                            priceOrDetails = "الخدمات الطبية: ${med.description.take(40)}",
                            isRecommended = med.isVerified,
                            onToggleRecommend = {
                                val newRec = !med.isVerified
                                viewModel.db.collection("stores").document(med.id).update("isVerified", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم تمييز المركز الطبي والتوصية به!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = med
                                editName = med.name
                                editPhone = med.phone
                                editLogoUrl = med.logoImage
                                editCoverUrl = med.coverImage
                                editPriceOrSalary = med.workingHours
                                editCity = med.cityId
                                editServicesOrProducts = med.description
                            },
                            onDelete = {
                                viewModel.db.collection("stores").document(med.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف المركز الطبي بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }

            "العقارات" -> {
                if (properties.isEmpty()) {
                    Text("لا توجد عقارات مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    properties.forEach { prop ->
                        AdminSectionEntityCard(
                            title = prop.title,
                            phone = prop.phone,
                            location = "${prop.cityId} - ${prop.localNeighborhood}",
                            logoUrl = prop.images.firstOrNull() ?: "",
                            coverUrl = prop.images.getOrNull(1) ?: "",
                            priceOrDetails = "السعر: ${prop.price} ريال",
                            isRecommended = prop.isRecommended,
                            onToggleRecommend = {
                                val newRec = !prop.isRecommended
                                viewModel.db.collection("properties").document(prop.id).update("isRecommended", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم تمييز العقار والتوصية به!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = prop
                                editName = prop.title
                                editPhone = prop.phone
                                editLogoUrl = prop.images.firstOrNull() ?: ""
                                editCoverUrl = prop.images.getOrNull(1) ?: ""
                                editPriceOrSalary = prop.price.toString()
                                editCity = prop.cityId
                                editServicesOrProducts = prop.description
                            },
                            onDelete = {
                                viewModel.db.collection("properties").document(prop.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف العقار بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }

            "إعلانات الوظائف" -> {
                if (jobs.isEmpty()) {
                    Text("لا توجد وظائف مضافة حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    jobs.forEach { job ->
                        AdminSectionEntityCard(
                            title = job.title,
                            phone = job.companyName,
                            location = "المدينة: ${job.cityId}",
                            logoUrl = "",
                            coverUrl = "",
                            priceOrDetails = "الراتب: ${job.salary}",
                            isRecommended = job.isVip,
                            onToggleRecommend = {
                                val newRec = !job.isVip
                                viewModel.db.collection("jobs").document(job.id).update("isVip", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم تمييز الوظيفة والتوصية بها!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = job
                                editName = job.title
                                editPhone = job.companyName
                                editLogoUrl = ""
                                editCoverUrl = ""
                                editPriceOrSalary = job.salary
                                editCity = job.cityId
                                editServicesOrProducts = job.description
                            },
                            onDelete = {
                                viewModel.db.collection("jobs").document(job.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف إعلان الوظيفة بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }

            "الفنيين والمهن" -> {
                if (providers.isEmpty()) {
                    Text("لا يوجد فنيين مسجلين حالياً.", fontSize = 11.sp, color = Color.Gray)
                } else {
                    providers.forEach { prov ->
                        AdminSectionEntityCard(
                            title = prov.name,
                            phone = "${prov.phone} • ${prov.profession}",
                            location = "📍 ${prov.cityId} - ${prov.area}",
                            logoUrl = prov.profileImage,
                            coverUrl = prov.coverImage,
                            priceOrDetails = "التقييم: ⭐ ${prov.rating} (${prov.numReviews})",
                            isRecommended = prov.isVerified,
                            onToggleRecommend = {
                                val newRec = !prov.isVerified
                                viewModel.db.collection("providers").document(prov.id).update("isVerified", newRec)
                                Toast.makeText(context, if (newRec) "⭐ تم توثيق الفني والتوصية به!" else "تم إلغاء التوصية", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = {
                                editingEntity = prov
                                editName = prov.name
                                editPhone = prov.phone
                                editLogoUrl = prov.profileImage
                                editCoverUrl = prov.coverImage
                                editPriceOrSalary = prov.profession
                                editCity = prov.cityId
                                editServicesOrProducts = prov.area
                            },
                            onDelete = {
                                viewModel.db.collection("providers").document(prov.id).delete()
                                Toast.makeText(context, "🗑️ تم حذف الفني بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            themeColors = themeColors
                        )
                    }
                }
            }
        }
    }

    // نافذة تعديل بيانات الكيان (الصور، الأسعار، الخدمات، التوصية)
    if (editingEntity != null) {
        AlertDialog(
            onDismissRequest = { editingEntity = null },
            title = { Text("✏️ تعديل بيانات وصور وأسعار العنصر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("الاسم / العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف / الشركة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLogoUrl,
                        onValueChange = { editLogoUrl = it },
                        label = { Text("رابط الصورة الشخصية / اللوجو") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCoverUrl,
                        onValueChange = { editCoverUrl = it },
                        label = { Text("رابط صورة الغلاف (Cover URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPriceOrSalary,
                        onValueChange = { editPriceOrSalary = it },
                        label = { Text("الأسعار / ساعات العمل / الراتب") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editServicesOrProducts,
                        onValueChange = { editServicesOrProducts = it },
                        label = { Text("الخدمات / المنتجات / الوصف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ent = editingEntity
                        when (ent) {
                            is StoreEntity -> {
                                viewModel.db.collection("stores").document(ent.id).update(
                                    mapOf(
                                        "name" to editName,
                                        "phone" to editPhone,
                                        "logoImage" to editLogoUrl,
                                        "coverImage" to editCoverUrl,
                                        "workingHours" to editPriceOrSalary,
                                        "description" to editServicesOrProducts
                                    )
                                )
                            }
                            is PropertyEntity -> {
                                val prc = editPriceOrSalary.toDoubleOrNull() ?: ent.price
                                viewModel.db.collection("properties").document(ent.id).update(
                                    mapOf(
                                        "title" to editName,
                                        "phone" to editPhone,
                                        "price" to prc,
                                        "description" to editServicesOrProducts
                                    )
                                )
                            }
                            is JobEntity -> {
                                viewModel.db.collection("jobs").document(ent.id).update(
                                    mapOf(
                                        "title" to editName,
                                        "companyName" to editPhone,
                                        "salary" to editPriceOrSalary,
                                        "description" to editServicesOrProducts
                                    )
                                )
                            }
                            is ProviderEntity -> {
                                viewModel.db.collection("providers").document(ent.id).update(
                                    mapOf(
                                        "name" to editName,
                                        "phone" to editPhone,
                                        "profileImage" to editLogoUrl,
                                        "coverImage" to editCoverUrl,
                                        "profession" to editPriceOrSalary,
                                        "area" to editServicesOrProducts
                                    )
                                )
                            }
                        }
                        editingEntity = null
                        Toast.makeText(context, "✅ تم حفظ التعديلات والمزامنة الفورية سحابياً!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("حفظ التعديلات", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntity = null }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // نافذة إضافة عنصر جديد
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("➕ إضافة عنصر جديد إلى $selectedSection", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = themeColors.accent) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("الاسم / العنوان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("رقم الهاتف للتواصل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("المدينة والمحافظة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newLogoUrl,
                        onValueChange = { newLogoUrl = it },
                        label = { Text("رابط الصورة الشخصية / الشعار") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCoverUrl,
                        onValueChange = { newCoverUrl = it },
                        label = { Text("رابط صورة الغلاف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPriceOrSalary,
                        onValueChange = { newPriceOrSalary = it },
                        label = { Text("الأسعار / المنتجات والخدمات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newId = "item_${System.currentTimeMillis()}"
                        val targetSection = when (selectedSection) {
                            "المطاعم والكافيهات" -> "restaurants"
                            "المراكز الطبية" -> "medical"
                            else -> "stores"
                        }
                        if (selectedSection == "العقارات") {
                            val newProp = PropertyEntity(
                                id = newId,
                                title = newName,
                                phone = newPhone,
                                cityId = newCity,
                                price = newPriceOrSalary.toDoubleOrNull() ?: 100000.0,
                                images = listOfNotNull(newLogoUrl.ifBlank { null }, newCoverUrl.ifBlank { null }),
                                isApproved = true
                            )
                            viewModel.db.collection("properties").document(newId).set(newProp)
                        } else if (selectedSection == "إعلانات الوظائف") {
                            val newJob = JobEntity(
                                id = newId,
                                title = newName,
                                companyName = newPhone,
                                cityId = newCity,
                                salary = newPriceOrSalary,
                                isActive = true
                            )
                            viewModel.db.collection("jobs").document(newId).set(newJob)
                        } else if (selectedSection == "الفنيين والمهن") {
                            val newProv = ProviderEntity(
                                id = newId,
                                name = newName,
                                phone = newPhone,
                                cityId = newCity,
                                profession = newPriceOrSalary.ifBlank { "فني متخصص" },
                                profileImage = newLogoUrl,
                                coverImage = newCoverUrl,
                                isVerified = true
                            )
                            viewModel.db.collection("providers").document(newId).set(newProv)
                        } else {
                            val newStore = StoreEntity(
                                id = newId,
                                name = newName,
                                phone = newPhone,
                                cityId = newCity,
                                sectionId = targetSection,
                                logoImage = newLogoUrl,
                                coverImage = newCoverUrl,
                                workingHours = newPriceOrSalary,
                                isVerified = true
                            )
                            viewModel.db.collection("stores").document(newId).set(newStore)
                        }
                        showAddDialog = false
                        Toast.makeText(context, "✅ تمت إضافة العنصر بنجاح والمزامنة الفورية!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
                ) {
                    Text("إضافة ومزامنة", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}


@Composable
fun AdminDataManagementScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) = AdminDataManagementScreenContent(viewModel, themeColors, modifier)
