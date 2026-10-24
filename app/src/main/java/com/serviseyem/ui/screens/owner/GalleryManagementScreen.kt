package com.serviseyem.ui.screens.owner

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.serviseyem.data.UnifiedBusinessAccount
import com.serviseyem.rememberBase64Bitmap
import com.serviseyem.ui.MainViewModel
import com.serviseyem.utils.VisualThemePalette

data class GalleryImageItem(
    val id: String,
    val urlOrBase64: String,
    val isPrimary: Boolean = false
)

@Composable
fun GalleryManagementScreen(
    account: UnifiedBusinessAccount,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette
) {
    val context = LocalContext.current
    var showAddImageDialog by remember { mutableStateOf(false) }

    var galleryList by remember {
        mutableStateOf(
            listOf(
                GalleryImageItem("1", account.coverImage.ifBlank { account.logoImage }, isPrimary = true)
            ).filter { it.urlOrBase64.isNotBlank() }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddImageDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "رفع") },
                text = { Text("رفع صورة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White
            )
        },
        containerColor = themeColors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🖼️ معرض صور النشاط التجاري:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = themeColors.accent)
                Surface(
                    color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "${galleryList.size} صور",
                        fontSize = 11.sp,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            if (galleryList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لم تقم برفع أي صور لمعرض النشاط التجاري بعد", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(galleryList, key = { it.id }) { item ->
                        GalleryGridCard(
                            item = item,
                            themeColors = themeColors,
                            onSetPrimary = {
                                galleryList = galleryList.map {
                                    it.copy(isPrimary = it.id == item.id)
                                }
                                Toast.makeText(context, "تم تعيين الصورة كصورة غلاف رئيسية", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = {
                                galleryList = galleryList.filter { it.id != item.id }
                                Toast.makeText(context, "تم حذف الصورة من المعرض", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddImageDialog) {
        var imageUrlInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddImageDialog = false },
            title = { Text("رفع / إضافة صورة جديدة للمعرض", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("رابط الصورة أو كود Base64") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (imageUrlInput.isNotBlank()) {
                            val newItem = GalleryImageItem(
                                id = java.util.UUID.randomUUID().toString(),
                                urlOrBase64 = imageUrlInput,
                                isPrimary = galleryList.isEmpty()
                            )
                            galleryList = galleryList + newItem
                            showAddImageDialog = false
                            Toast.makeText(context, "تم إضافة الصورة لمعرض الصور!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("رفع الصورة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddImageDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun GalleryGridCard(
    item: GalleryImageItem,
    themeColors: VisualThemePalette,
    onSetPrimary: () -> Unit,
    onDelete: () -> Unit
) {
    val bitmap = rememberBase64Bitmap(item.urlOrBase64)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, if (item.isPrimary) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color.DarkGray)
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = "Gallery Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (item.urlOrBase64.startsWith("http")) {
                    AsyncImage(
                        model = item.urlOrBase64,
                        contentDescription = "Gallery Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🖼️", fontSize = 36.sp)
                    }
                }

                if (item.isPrimary) {
                    Surface(
                        color = Color(0xFF10B981),
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text("الغلاف الرئيسي ⭐", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!item.isPrimary) {
                    TextButton(onClick = onSetPrimary, contentPadding = PaddingValues(0.dp)) {
                        Text("تعيين رئيسية", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
