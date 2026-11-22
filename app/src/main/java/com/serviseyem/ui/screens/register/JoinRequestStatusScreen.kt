@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.serviseyem.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.serviseyem.ui.*
import com.serviseyem.ui.MainViewModel
import com.serviseyem.ui.screens.register.status.*
import com.serviseyem.utils.VisualThemePalette

/**
 * JoinRequestStatusScreen - الشاشة الموحدة لمتابعة حالة طلب الانضمام أو الدخول للوحات التحكم
 */
@Composable
fun JoinRequestStatusScreen(
    viewModel: MainViewModel,
    themeColors: VisualThemePalette,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val useCase = remember { JoinStatusUseCase() }

    val joinPhone by viewModel.joinRequestPhone.collectAsState()
    val pendingProviders by viewModel.pendingProviders.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val stores by viewModel.stores.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val registeredUsersList by viewModel.registeredUsersList.collectAsState()
    val jobSeekers by viewModel.jobSeekers.collectAsState()

    val currentStatus = remember(
        joinPhone, pendingProviders, providers, stores, properties, categories, notifications, jobs, registeredUsersList, jobSeekers
    ) {
        useCase.determineStatus(
            joinPhone = joinPhone,
            pendingProviders = pendingProviders,
            providers = providers,
            stores = stores,
            properties = properties,
            categories = categories,
            notifications = notifications,
            jobs = jobs,
            registeredUsersList = registeredUsersList,
            jobSeekers = jobSeekers
        )
    }

    // Active Dashboards Routing
    if (currentStatus is JoinStatus.ActiveStore ||
        currentStatus is JoinStatus.ActiveProperty ||
        currentStatus is JoinStatus.ApprovedTechnician ||
        currentStatus is JoinStatus.ActiveJobPoster ||
        currentStatus is JoinStatus.ActiveJobSeeker ||
        currentStatus is JoinStatus.ActiveClient
    ) {
        JoinStatusRouter.RouteToDashboard(
            status = currentStatus,
            viewModel = viewModel,
            themeColors = themeColors,
            context = context,
            scope = scope,
            snackbarHostState = snackbarHostState,
            onOpenChat = { channel -> viewModel.openChatChannel(channel) }
        )
        return
    }

    // Pending & Rejected Views
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = themeColors.surface
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStatus) {
                is JoinStatus.Rejected -> {
                    RejectedView(
                        reason = currentStatus.reason,
                        onReapply = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingStore -> {
                    val store = currentStatus.store
                    val storeName = store.name.ifBlank { "متجر جديد" }
                    val ownerName = store.ownerName.ifBlank { "غير محدد" }
                    val phone = store.phone.ifBlank { "غير محدد" }
                    val city = store.cityId.ifBlank { "صنعاء" }
                    val neighborhood = store.localNeighborhood.ifBlank { "غير محدد" }

                    PendingApprovalView(
                        title = "🏪 طلب انضمام المتجر / المحل قيد المراجعة",
                        message = "تم استلام طلب انضمام المتجر '$storeName' وهو قيد المراجعة والاعتماد التجاري حالياً.",
                        detailsList = listOf(
                            "اسم المتجر / المحل" to storeName,
                            "اسم المالك / المسؤول" to ownerName,
                            "رقم الهاتف" to phone,
                            "المدينة والحي" to "$city - $neighborhood"
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingRestaurant -> {
                    val store = currentStatus.store
                    val storeName = store.name.ifBlank { "مطعم / كافيه جديد" }
                    val ownerName = store.ownerName.ifBlank { "غير محدد" }
                    val phone = store.phone.ifBlank { "غير محدد" }
                    val city = store.cityId.ifBlank { "صنعاء" }
                    val neighborhood = store.localNeighborhood.ifBlank { "غير محدد" }

                    PendingApprovalView(
                        title = "🍽️ طلب انضمام المطعم / الكافيه قيد المراجعة",
                        message = "تم استلام طلب انضمام المطعم '$storeName' وجاري مراجعته والاعتماد من قِبل إدارة التطبيق.",
                        detailsList = listOf(
                            "اسم المطعم / الكافيه" to storeName,
                            "اسم المالك / المدير" to ownerName,
                            "رقم الهاتف" to phone,
                            "المدينة والحي" to "$city - $neighborhood"
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingMedical -> {
                    val store = currentStatus.store
                    val storeName = store.name.ifBlank { "مركز طبي / عيادة جديدة" }
                    val ownerName = store.ownerName.ifBlank { "غير محدد" }
                    val phone = store.phone.ifBlank { "غير محدد" }
                    val city = store.cityId.ifBlank { "صنعاء" }
                    val neighborhood = store.localNeighborhood.ifBlank { "غير محدد" }

                    PendingApprovalView(
                        title = "🏥 طلب انضمام المركز الطبي / العيادة قيد المراجعة",
                        message = "تم استلام طلب انضمام '$storeName' وجاري مراجعة البيانات والاعتماد الطبي من قِبل إدارة التطبيق.",
                        detailsList = listOf(
                            "اسم المركز / العيادة" to storeName,
                            "اسم المسؤول / المدير" to ownerName,
                            "رقم الهاتف" to phone,
                            "المدينة والحي" to "$city - $neighborhood"
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingJob -> {
                    val job = currentStatus.job
                    val title = job.title.ifBlank { "وظيفة جديدة" }
                    val company = job.companyName.ifBlank { "منشأة غير محددة" }
                    val phone = job.phone.ifBlank { "غير محدد" }
                    val city = job.cityId.ifBlank { "صنعاء" }
                    PendingApprovalView(
                        title = "💼 طلب نشر الوظيفة قيد المراجعة",
                        message = "تم استلام طلب نشر الشاغر الوظيفي '$title' وجاري مراجعته ونشره في قسم الوظائف.",
                        detailsList = listOf(
                            "المسمى الوظيفي" to title,
                            "اسم المنشأة / الشركة" to company,
                            "رقم التواصل" to phone,
                            "المدينة" to city
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingJobSeeker -> {
                    val pending = currentStatus.provider
                    val name = pending.name.ifBlank { "متقدم للوظائف" }
                    val phone = pending.phone.ifBlank { "غير محدد" }
                    val area = pending.area.ifBlank { "صنعاء" }
                    val spec = pending.specialization.ifBlank { pending.customCategoryName }.ifBlank { "غير محدد" }
                    PendingApprovalView(
                        title = "👨‍💼 طلب التقديم للوظائف قيد المراجعة",
                        message = "تم استلام طلبك كباحث عن عمل بنجاح وجاري مراجعته من الإدارة.",
                        detailsList = listOf(
                            "الاسم الثلاثي" to name,
                            "المجال المطلوب" to spec,
                            "رقم التواصل" to phone,
                            "المدينة" to area
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingProperty -> {
                    val prop = currentStatus.property
                    val title = prop.title.ifBlank { "عقار / مكتب عقاري" }
                    val phone = prop.phone.ifBlank { "غير محدد" }
                    val city = prop.cityId.ifBlank { "صنعاء" }
                    val neighborhood = prop.localNeighborhood.ifBlank { "غير محدد" }
                    PendingApprovalView(
                        title = "🏢 طلب المكتب العقاري / العقار قيد المراجعة",
                        message = "تم استلام بيانات العقار/المكتب '$title' وهو قيد المراجعة والاعتماد.",
                        detailsList = listOf(
                            "اسم المكتب / العقار" to title,
                            "رقم التواصل" to phone,
                            "المدينة والحي" to "$city - $neighborhood"
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingTechnician -> {
                    val pending = currentStatus.provider
                    val name = pending.name.ifBlank { "فني جديد" }
                    val phone = pending.phone.ifBlank { "غير محدد" }
                    val area = pending.area.ifBlank { "صنعاء" }
                    val neighborhood = pending.localNeighborhood.ifBlank { "غير محدد" }
                    val profession = pending.specialization.ifBlank { pending.customCategoryName }.ifBlank { "مهنة فنية" }
                    PendingApprovalView(
                        title = "🔧 طلب الانضمام كفني / مهني قيد المراجعة",
                        message = "تم استلام بياناتك بنجاح وجاري مراجعة طلبك وتفعيل ملفك المهني في الدليل.",
                        detailsList = listOf(
                            "الاسم" to name,
                            "المهنة / التخصص" to profession,
                            "رقم التواصل" to phone,
                            "المدينة والحي" to "$area - $neighborhood"
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingClient -> {
                    val userMap = currentStatus.userMap
                    val name = (userMap["name"] as? String)?.ifBlank { "عميل جديد" } ?: "عميل جديد"
                    val phone = (userMap["phone"] as? String)?.ifBlank { "غير محدد" } ?: "غير محدد"
                    val res = (userMap["residence"] as? String)?.ifBlank { "صنعاء" } ?: "صنعاء"
                    PendingApprovalView(
                        title = "👤 طلب تسجيل حساب العميل قيد المراجعة",
                        message = "تم استلام بيانات تسجيلك كعميل وجاري تفعيل حسابك من قِبل الإدارة.",
                        detailsList = listOf(
                            "الاسم الثلاثي" to name,
                            "رقم الهاتف" to phone,
                            "المدينة / المحافظة" to res
                        ),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                is JoinStatus.PendingGeneric -> {
                    PendingApprovalView(
                        title = "⏳ طلب الانضمام قيد المراجعة والتدقيق",
                        message = "طلبك قيد المراجعة الإدارية. سيصلك إشعار فور الاعتماد وتفعيل حسابك.",
                        detailsList = listOf("رقم الهاتف المسجل" to currentStatus.phone.ifBlank { "غير محدد" }),
                        onCancelRequest = { viewModel.cancelOrResetJoinRequest(context) },
                        themeColors = themeColors
                    )
                }
                else -> {}
            }
        }
    }
}
