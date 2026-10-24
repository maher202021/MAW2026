package com.serviseyem.ui.screens.dashboard.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.data.ProductEntity
import com.serviseyem.ui.*
import com.serviseyem.utils.FirebaseStorageUploader
import com.serviseyem.utils.VisualThemePalette
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 📦 StoreAddProductDialog - نافذة إضافة منتج جديد للكتالوج
 */
@Composable
fun StoreAddProductDialog(
    storeId: String,
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    context: Context = LocalContext.current,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var prodName by remember { mutableStateOf("") }
    var prodDesc by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodImageUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val prodUriPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectedUri ->
            scope.launch {
                val productId = UUID.randomUUID().toString()
                val path = FirebaseStorageUploader.getStoreProductPath(storeId, productId)
                val uploadRes = FirebaseStorageUploader.uploadImageUri(context, selectedUri, path)
                prodImageUrl = uploadRes.getOrElse { selectedUri.toString() }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📦 إضافة منتج جديد للكتالوج", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) },
        containerColor = themeColors.secondary,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = prodName,
                    onValueChange = { 
                        prodName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("اسم المنتج") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                )

                OutlinedTextField(
                    value = prodDesc,
                    onValueChange = { prodDesc = it },
                    label = { Text("وصف ومواصفات المنتج التفصيلية") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                )

                OutlinedTextField(
                    value = prodPrice,
                    onValueChange = { 
                        prodPrice = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("سعر المنتج (ريال يمني)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("🖼️ صورة المنتج:", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)

                if (prodImageUrl.isNotBlank()) {
                    coil.compose.AsyncImage(
                        model = prodImageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(6.dp)).align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = { prodUriPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColors.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("اختر صورة للمنتج 📸", color = Color.White, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dPrice = prodPrice.trim().toDoubleOrNull()
                    if (prodName.trim().isBlank()) {
                        errorMessage = "⚠️ يرجى إدخال اسم المنتج بشكل صحيح"
                        Toast.makeText(context, "⚠️ اسم المنتج مطلوب!", Toast.LENGTH_SHORT).show()
                    } else if (dPrice == null || dPrice <= 0.0) {
                        errorMessage = "⚠️ يرجى إدخال سعر صحيح أكبر من الصفر"
                        Toast.makeText(context, "⚠️ يرجى إدخال سعر صالح!", Toast.LENGTH_SHORT).show()
                    } else {
                        val newProduct = ProductEntity(
                            id = UUID.randomUUID().toString(),
                            storeId = storeId,
                            name = prodName.trim(),
                            description = prodDesc.trim(),
                            price = dPrice,
                            imageUrl = prodImageUrl
                        )
                        viewModel.saveProduct(newProduct)
                        onDismiss()
                        Toast.makeText(context, "✅ تم إضافة المنتج ونشره بكتالوجك بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = themeColors.accent)
            ) {
                Text("حفظ ونشر المنتج 📢", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("إلغاء", color = Color.White, fontSize = 11.sp)
            }
        }
    )
}
