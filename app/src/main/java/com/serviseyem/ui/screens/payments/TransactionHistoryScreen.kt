package com.serviseyem.ui.screens.payments

import android.widget.Toast
import com.serviseyem.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serviseyem.data.models.Transaction
import com.serviseyem.utils.DateFormatter
import com.serviseyem.utils.WalletManager
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 💳 TransactionHistoryScreen
 * Displays transaction history logs, wallet balance, deposits, withdrawals and filters.
 * Refactored using MVVM to utilize [TransactionHistoryViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    currentUserId: String = "",
    userRole: String = "USER", // USER, PROVIDER, STORE, RESTAURANT, ADMIN
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (currentUserId.isBlank()) return

    val context = LocalContext.current
    val walletManager = remember { WalletManager(context) }
    val numberFormat = remember { DecimalFormat("#,###.##", DecimalFormatSymbols(Locale.US)) }

    val walletId = remember(currentUserId) { "wallet_$currentUserId" }
    val viewModel = remember(currentUserId) {
        TransactionHistoryViewModel(walletManager, walletId)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTypeFilter by viewModel.typeFilter.collectAsState()
    val selectedStatusFilter by viewModel.statusFilter.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }
    var depositAmountText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("سجل المعاملات والمحفظة", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (uiState is TransactionHistoryUiState.Success) {
                            val successState = uiState as TransactionHistoryUiState.Success
                            val result = com.serviseyem.utils.ReportExporter.exportTransactionsToCsv(
                                context = context,
                                accountName = currentUserId,
                                transactions = successState.transactions
                            )
                            result.onSuccess { file ->
                                com.serviseyem.utils.ReportExporter.shareExportedFile(context, file, "سجل المعاملات المالية")
                                Toast.makeText(context, "تم تصدير التقرير المالي بنجاح", Toast.LENGTH_SHORT).show()
                            }.onFailure { e ->
                                Toast.makeText(context, "فشل تصدير التقرير: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير التقرير", tint = Color(0xFF00668B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            when (val state = uiState) {
                is TransactionHistoryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF00668B))
                    }
                }
                is TransactionHistoryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Color.Red, fontSize = 14.sp)
                    }
                }
                is TransactionHistoryUiState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Card with balance & statistics
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF00668B)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val gatewayIntegration = remember { com.serviseyem.utils.PaymentGatewayIntegration(context) }
                                    val isGatewayActive = remember(context) { gatewayIntegration.isAnyRealGatewayActive(context) }

                                    Column {
                                        Text("الرصيد الحالي بالمحفظة", color = Color(0xFFE0F2FE), fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = numberFormat.format(state.balance),
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("ريال يمني", fontSize = 14.sp, color = Color(0xFFBAE6FD), modifier = Modifier.padding(bottom = 4.dp))
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isGatewayActive) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFFF59E0B).copy(alpha = 0.22f))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isGatewayActive) Color(0xFF10B981) else Color(0xFFF59E0B))
                                            )
                                            Text(
                                                text = if (isGatewayActive) "متصل ببوابة الدفع الحقيقية ✓" else "وضع المحفظة المحلية (تجريبي) ⚠",
                                                fontSize = 9.5.sp,
                                                color = if (isGatewayActive) Color(0xFFA7F3D0) else Color(0xFFFED7AA),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { showDepositDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00668B), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("شحن رصيد", color = Color(0xFF00668B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.2f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Total Deposits
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFF4CAF50).copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text("إجمالي الإيداعات", fontSize = 11.sp, color = Color(0xFFE0F2FE))
                                            Text("${numberFormat.format(state.totalDeposits)} ريال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    // Total Withdrawals
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFFE53935).copy(alpha = 0.3f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                                        }
                                        Column {
                                            Text("إجمالي السحوبات", fontSize = 11.sp, color = Color(0xFFE0F2FE))
                                            Text("${numberFormat.format(state.totalWithdrawals)} ريال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Search
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            placeholder = { Text("بحث برقم المعاملة أو الوصف...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00668B),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Filters row
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val typeFilters = listOf(
                                "ALL" to "الكل",
                                "DEPOSIT" to "إيداع 🟢",
                                "PAYMENT" to "دفع 🔴",
                                "WITHDRAWAL" to "سحب 💸",
                                "TRANSFER" to "تحويل 🔁",
                                "REFUND" to "استرداد ↩️"
                            )
                            items(typeFilters) { (key, label) ->
                                val isSelected = selectedTypeFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setTypeFilter(key) },
                                    label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00668B),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Transaction List
                        if (filteredTransactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        if (state.transactions.isEmpty()) "مرحباً بك! لا توجد معاملات سابقة في محفظتك بعد" else "لا توجد معاملات مطابقة للبحث أو التصفية",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredTransactions, key = { it.id }) { tx ->
                                    val isPositive = tx.type == "DEPOSIT" || tx.type == "REFUND"
                                    val formattedDate = remember(tx.timestamp) {
                                        // ✨ م2-ج3: استخدام DateFormatter
                                        DateFormatter.formatDisplay(tx.timestamp)
                                    }

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(
                                                        if (isPositive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    if (isPositive) Icons.Default.Check else Icons.Default.Close,
                                                    contentDescription = null,
                                                    tint = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = tx.note.ifEmpty { "معاملة مالية" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF1E293B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = formattedDate,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "رقم المرجع: ${tx.id}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${if (isPositive) "+" else "-"}${numberFormat.format(tx.amount)} ريال",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = when (tx.status.uppercase()) {
                                                        "COMPLETED" -> Color(0xFFE8F5E9)
                                                        "PENDING" -> Color(0xFFFFF3E0)
                                                        "CANCELLED" -> Color(0xFFECEFF1)
                                                        else -> Color(0xFFFFEBEE)
                                                    }
                                                ) {
                                                    Text(
                                                        text = when (tx.status.uppercase()) {
                                                            "COMPLETED" -> "مكتمل"
                                                            "PENDING" -> "قيد المعالجة"
                                                            "CANCELLED" -> "ملغي"
                                                            else -> "فاشل"
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = when (tx.status.uppercase()) {
                                                            "COMPLETED" -> Color(0xFF2E7D32)
                                                            "PENDING" -> Color(0xFFEF6C00)
                                                            "CANCELLED" -> Color(0xFF546E7A)
                                                            else -> Color(0xFFC62828)
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Deposit dialog
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("شحن المحفظة الإلكترونية", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("أدخل المبلغ بالريال اليمني للشحن الفوري:", fontSize = 13.sp)
                    OutlinedTextField(
                        value = depositAmountText,
                        onValueChange = { depositAmountText = it.filter { char -> char.isDigit() } },
                        placeholder = { Text("مثال: 25000") },
                        trailingIcon = { Text("YER", modifier = Modifier.padding(end = 8.dp), fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            viewModel.deposit(amount, "شحن رصيد محفظة فوري")
                            showDepositDialog = false
                            depositAmountText = ""
                            Toast.makeText(context, "تم إيداع ${numberFormat.format(amount)} ريال بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "يرجى إدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00668B))
                ) {
                    Text("تأكيد الشحن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
