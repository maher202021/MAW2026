package com.serviseyem.ui.screens.bookings

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.BookingEntity
import com.serviseyem.ui.dialogs.BookingCancellationDialog

/**
 * 📋 BookingListScreen
 * شاشة "حجوزاتي" المستقلة بالكامل لإدارة وعرض الحجوزات العادية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(
    bookings: List<BookingEntity>,
    currentUserId: String = "",
    isAdmin: Boolean = false,
    isProvider: Boolean = false,
    onBackClick: () -> Unit,
    onCreateNewBookingClick: () -> Unit,
    onUpdateBooking: (BookingEntity) -> Unit,
    onStatusChange: (BookingEntity, String) -> Unit = { _, _ -> },
    onCancelBooking: (BookingEntity, String, String) -> Unit, // booking, reason, password
    onDeleteBooking: (BookingEntity) -> Unit,
    onOpenChatClick: (BookingEntity) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val context = LocalContext.current

    var selectedTabFilter by remember { mutableStateOf("ALL") } // ALL, ACTIVE, COMPLETED, CANCELLED
    var searchQuery by remember { mutableStateOf("") }

    var bookingToEdit by remember { mutableStateOf<BookingEntity?>(null) }
    var bookingToCancel by remember { mutableStateOf<BookingEntity?>(null) }
    var bookingToDelete by remember { mutableStateOf<BookingEntity?>(null) }

    // Pre-calculate tab counts efficiently with remember
    val totalCount = remember(bookings) { bookings.size }
    val activeCount = remember(bookings) { bookings.count { it.status in listOf("PENDING", "APPROVED") } }
    val completedCount = remember(bookings) { bookings.count { it.status == "COMPLETED" } }
    val cancelledCount = remember(bookings) { bookings.count { it.status in listOf("CANCELLED", "REJECTED") } }

    // Filter bookings with derivedStateOf
    val filteredBookings by remember(bookings, selectedTabFilter, searchQuery) {
        derivedStateOf {
            val query = searchQuery.trim().lowercase()
            bookings.filter { bk ->
                val matchesTab = when (selectedTabFilter) {
                    "ACTIVE" -> bk.status in listOf("PENDING", "APPROVED")
                    "COMPLETED" -> bk.status == "COMPLETED"
                    "CANCELLED" -> bk.status in listOf("CANCELLED", "REJECTED")
                    else -> true
                }

                val matchesQuery = query.isEmpty() ||
                    bk.bookingCode.lowercase().contains(query) ||
                    bk.bookingNumber.lowercase().contains(query) ||
                    bk.fullName.lowercase().contains(query) ||
                    bk.customerName.lowercase().contains(query) ||
                    bk.clientPhone.lowercase().contains(query) ||
                    bk.providerName.lowercase().contains(query)

                matchesTab && matchesQuery
            }
        }
    }

    var showExportMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "حجوزاتي",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "تصدير التقرير", tint = MaterialTheme.colorScheme.primary)
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📄 تصدير مستند PDF") },
                                onClick = {
                                    showExportMenu = false
                                    val result = com.serviseyem.utils.BookingExportHelper.exportBookingsToPdf(context, filteredBookings)
                                    result.onSuccess { file ->
                                        com.serviseyem.utils.BookingExportHelper.shareExportedFile(context, file, "تقرير PDF للحجوزات")
                                        Toast.makeText(context, "تم تجهيز تقرير PDF بنجاح", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "فشل تصدير PDF", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📊 تصدير ملف Excel (XLS)") },
                                onClick = {
                                    showExportMenu = false
                                    val result = com.serviseyem.utils.BookingExportHelper.exportBookingsToExcel(context, filteredBookings)
                                    result.onSuccess { file ->
                                        com.serviseyem.utils.BookingExportHelper.shareExportedFile(context, file, "تقرير Excel للحجوزات")
                                        Toast.makeText(context, "تم تجهيز ملف Excel بنجاح", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "فشل تصدير Excel", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("📝 تصدير ملف CSV") },
                                onClick = {
                                    showExportMenu = false
                                    val result = com.serviseyem.utils.BookingExportHelper.exportBookingsToCsv(context, filteredBookings)
                                    result.onSuccess { file ->
                                        com.serviseyem.utils.BookingExportHelper.shareExportedFile(context, file, "تقرير CSV للحجوزات")
                                        Toast.makeText(context, "تم تجهيز ملف CSV بنجاح", Toast.LENGTH_SHORT).show()
                                    }.onFailure {
                                        Toast.makeText(context, "فشل تصدير CSV", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = onRefresh) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث البيانات")
                        }
                    }
                    IconButton(onClick = onCreateNewBookingClick) {
                        Icon(Icons.Default.AddCircle, contentDescription = "حجز جديد", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث برقم الحجز، الاسم، أو الهاتف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = when (selectedTabFilter) {
                    "ACTIVE" -> 1
                    "COMPLETED" -> 2
                    "CANCELLED" -> 3
                    else -> 0
                },
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabFilter == "ALL",
                    onClick = { selectedTabFilter = "ALL" },
                    text = { Text("الكل ($totalCount)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabFilter == "ACTIVE",
                    onClick = { selectedTabFilter = "ACTIVE" },
                    text = {
                        Text(
                            "الحالية ($activeCount)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTabFilter == "COMPLETED",
                    onClick = { selectedTabFilter = "COMPLETED" },
                    text = {
                        Text(
                            "المكتملة ($completedCount)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedTabFilter == "CANCELLED",
                    onClick = { selectedTabFilter = "CANCELLED" },
                    text = {
                        Text(
                            "الملغاة ($cancelledCount)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Booking List Content
            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "لا توجد حجوزات مسجلة في هذا التبويب",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onCreateNewBookingClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إنشاء طلب حجز جديد")
                        }
                    }
                }
            } else {
                var pageLimit by remember { mutableIntStateOf(20) }
                val paginatedList = remember(filteredBookings, pageLimit) {
                    filteredBookings.take(pageLimit)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
                ) {
                    items(
                        items = paginatedList,
                        key = { it.id.ifBlank { it.bookingCode } }
                    ) { bk ->
                        BookingCardItem(
                            booking = bk,
                            currentUserId = currentUserId,
                            isAdmin = isAdmin,
                            isProvider = isProvider,
                            onStatusChange = onStatusChange,
                            onEditClick = { bookingToEdit = it },
                            onCancelClick = { bookingToCancel = it },
                            onDeleteClick = { bookingToDelete = it },
                            onOpenChatClick = { onOpenChatClick(it) }
                        )
                    }

                    if (filteredBookings.size > paginatedList.size) {
                        item {
                            Button(
                                onClick = { pageLimit += 20 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("تحميل المزيد من الحجوزات (${paginatedList.size} من ${filteredBookings.size})", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    val currentBookingToEdit = bookingToEdit
    if (currentBookingToEdit != null) {
        BookingEditDialog(
            booking = currentBookingToEdit,
            isAdmin = isAdmin,
            onDismiss = { bookingToEdit = null },
            onConfirmEdit = { updatedBooking, pass ->
                onUpdateBooking(updatedBooking)
                bookingToEdit = null
                Toast.makeText(context, "تم تحديث بيانات الحجز بنجاح", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Cancellation Dialog
    val bkCancel = bookingToCancel
    if (bkCancel != null) {
        val bk = bkCancel
        BookingCancellationDialog(
            booking = bk,
            onDismiss = { bookingToCancel = null },
            onConfirmCancel = { pass, reason ->
                onCancelBooking(bk, reason, pass)
                bookingToCancel = null
                Toast.makeText(context, "تم إرسال طلب إلغاء الحجز", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    val bkDelete = bookingToDelete
    if (bkDelete != null) {
        val bk = bkDelete
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = { Text("تأكيد حذف الحجز النهائي", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف الحجز رقم (${bk.bookingCode}) بشكل نهائي؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBooking(bk)
                        bookingToDelete = null
                        Toast.makeText(context, "تم حذف الحجز بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف نهائي")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { bookingToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
