package com.serviseyem.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ProfileStepReport(
    val profileType: String,
    val collectionName: String,
    val docId: String,
    val fileName: String = "ProfileDiagnosticRunner.kt",
    val lineNumber: Int = 0,
    val functionName: String = "",
    var created: Boolean = false,
    var readVerified: Boolean = false,
    var updated: Boolean = false,
    var updateVerified: Boolean = false,
    var status: String = "PENDING", // SUCCESS, FAILED
    var durationMs: Long = 0L,
    var expectedOutcome: String = "",
    var actualOutcome: String = "",
    var notes: String = "",
    var error: TestError? = null
)

data class ProfileFullReport(
    val timestamp: String,
    val totalProfiles: Int,
    val passed: Int,
    val failed: Int,
    val steps: List<ProfileStepReport>,
    val details: List<TestDetail>
)

class ProfileDiagnosticRunner(context: Context) {

    // [تعديل مهندس النظام] فحص وتشخيص صارم لملفات المستخدمين ومطابقة البيانات والسرعة تحت الضغط
    private val db = FirebaseFirestore.getInstance()

    private val testProfileIds = listOf(
        "prof_test_client_777101",
        "prof_test_provider_777102",
        "prof_test_store_777103",
        "prof_test_restaurant_777104",
        "prof_test_medical_777105",
        "prof_test_property_777106",
        "prof_test_job_777107",
        "prof_test_seeker_777108"
    )

    suspend fun cleanAllTestData(onProgress: (String) -> Unit = {}) {
        onProgress("🧹 جاري تنظيف بيانات اختبار الملفات الشخصية الثمانية...")
        try {
            val batch = db.batch()
            val collections = listOf(
                "registered_users",
                "users",
                "providers",
                "stores",
                "properties",
                "jobs",
                "job_applicants"
            )
            for (col in collections) {
                for (docId in testProfileIds) {
                    batch.delete(db.collection(col).document(docId))
                }
            }
            batch.commit().await()
            onProgress("✅ تم تنظيف وثائق اختبار الملفات الشخصية بنجاح.")
        } catch (_: Exception) {
        }
    }

    suspend fun runProfileDiagnostics(
        onProgress: (String) -> Unit,
        onComplete: (ProfileFullReport) -> Unit
    ) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val steps = mutableListOf<ProfileStepReport>()
        val details = mutableListOf<TestDetail>()
        var errorCounter = 1

        cleanAllTestData(onProgress)
        delay(300)

        data class ProfileSpec(
            val label: String,
            val collection: String,
            val docId: String,
            val line: Int,
            val fnName: String,
            val initialData: Map<String, Any>,
            val updatedFields: Map<String, Any>,
            val verifyKey: String,
            val verifyExpectedValue: Any
        )

        val now = System.currentTimeMillis()
        val specs = listOf(
            ProfileSpec(
                label = "1. الملف الشخصي للعميل (Client Profile)",
                collection = "registered_users",
                docId = testProfileIds[0],
                line = 115,
                fnName = "testClientProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[0],
                    "phone" to "777101101",
                    "fullName" to "عميل فحص الملف الشخصي",
                    "role" to "CLIENT",
                    "city" to "صنعاء",
                    "area" to "حدة",
                    "avatarUrl" to "https://example.com/client_avatar.png",
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "fullName" to "عميل فحص الملف الشخصي - محدث",
                    "area" to "شارع الجزائر",
                    "updatedAt" to (now + 1000)
                ),
                verifyKey = "area",
                verifyExpectedValue = "شارع الجزائر"
            ),
            ProfileSpec(
                label = "2. الملف الشخصي للفني (Provider Profile)",
                collection = "providers",
                docId = testProfileIds[1],
                line = 142,
                fnName = "testProviderProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[1],
                    "phone" to "777102102",
                    "name" to "فني فحص الملف الشخصي",
                    "categoryId" to "electricity",
                    "city" to "صنعاء",
                    "bio" to "فني كهرباء معتمد خبرة 10 سنوات",
                    "previewPrice" to 3000.0,
                    "isAvailable" to true,
                    "workPhotos" to listOf("https://example.com/work1.jpg"),
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "previewPrice" to 4500.0,
                    "bio" to "فني كهرباء وطاقة شمسية معتمد - محدث",
                    "isAvailable" to true
                ),
                verifyKey = "bio",
                verifyExpectedValue = "فني كهرباء وطاقة شمسية معتمد - محدث"
            ),
            ProfileSpec(
                label = "3. الملف الشخصي للمتجر (Store Profile)",
                collection = "stores",
                docId = testProfileIds[2],
                line = 171,
                fnName = "testStoreProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[2],
                    "phone" to "777103103",
                    "name" to "متجر الفحص الشامل",
                    "type" to "STORE",
                    "city" to "صنعاء",
                    "workingHours" to "8:00 ص - 10:00 م",
                    "hasDelivery" to true,
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "workingHours" to "9:00 ص - 11:30 م",
                    "description" to "متجر إلكترونيات ومعدات حديثة"
                ),
                verifyKey = "workingHours",
                verifyExpectedValue = "9:00 ص - 11:30 م"
            ),
            ProfileSpec(
                label = "4. الملف الشخصي للمطعم (Restaurant Profile)",
                collection = "stores",
                docId = testProfileIds[3],
                line = 196,
                fnName = "testRestaurantProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[3],
                    "phone" to "777104104",
                    "name" to "مطعم الفحص الشامل",
                    "type" to "RESTAURANT",
                    "city" to "صنعاء",
                    "menuSummary" to "مندي، مظبي، مقبلات، عصائر",
                    "deliveryFee" to 1000.0,
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "menuSummary" to "مندي، مظبي، زربيان، مشاوي مشكلة",
                    "deliveryFee" to 800.0
                ),
                verifyKey = "menuSummary",
                verifyExpectedValue = "مندي، مظبي، زربيان، مشاوي مشكلة"
            ),
            ProfileSpec(
                label = "5. الملف الشخصي للمركز الطبي (Medical Center Profile)",
                collection = "stores",
                docId = testProfileIds[4],
                line = 221,
                fnName = "testMedicalProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[4],
                    "phone" to "777105105",
                    "name" to "عيادة الفحص الشامل",
                    "type" to "MEDICAL",
                    "specialty" to "طب عام وباطنية",
                    "consultationFee" to 3000.0,
                    "emergency24h" to true,
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "specialty" to "طب عام، باطنية، وأطفال",
                    "consultationFee" to 3500.0
                ),
                verifyKey = "specialty",
                verifyExpectedValue = "طب عام، باطنية، وأطفال"
            ),
            ProfileSpec(
                label = "6. الملف الشخصي للمكتب العقاري (Real Estate Profile)",
                collection = "properties",
                docId = testProfileIds[5],
                line = 246,
                fnName = "testPropertyProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[5],
                    "phone" to "777106106",
                    "title" to "شقة فاخرة للفحص العقاري",
                    "propertyType" to "Rent",
                    "price" to 150000.0,
                    "rooms" to 4,
                    "city" to "صنعاء",
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "price" to 140000.0,
                    "rooms" to 5,
                    "notes" to "شامل المياه والخدمات"
                ),
                verifyKey = "notes",
                verifyExpectedValue = "شامل المياه والخدمات"
            ),
            ProfileSpec(
                label = "7. الملف الشخصي لمعلن الوظائف (Job Poster Profile)",
                collection = "jobs",
                docId = testProfileIds[6],
                line = 272,
                fnName = "testJobPosterProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[6],
                    "phone" to "777107107",
                    "jobTitle" to "مهندس برمجيات أندرويد",
                    "companyName" to "شركة التقنية الحديثة",
                    "salary" to "800 USD",
                    "city" to "صنعاء",
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "salary" to "1000 USD",
                    "employmentType" to "دوام كامل"
                ),
                verifyKey = "salary",
                verifyExpectedValue = "1000 USD"
            ),
            ProfileSpec(
                label = "8. الملف الشخصي للباحث عن عمل (Job Seeker Profile)",
                collection = "job_applicants",
                docId = testProfileIds[7],
                line = 297,
                fnName = "testJobSeekerProfileLifecycle()",
                initialData = mapOf(
                    "id" to testProfileIds[7],
                    "phone" to "777108108",
                    "fullName" to "باحث عن عمل - فحص شامل",
                    "qualification" to "بكالوريوس هندسة حاسوب",
                    "experienceYears" to 3,
                    "skills" to "Kotlin, Jetpack Compose, Firebase",
                    "createdAt" to now
                ),
                updatedFields = mapOf(
                    "experienceYears" to 4,
                    "skills" to "Kotlin, Jetpack Compose, Firebase, Clean Architecture"
                ),
                verifyKey = "skills",
                verifyExpectedValue = "Kotlin, Jetpack Compose, Firebase, Clean Architecture"
            )
        )

        for (spec in specs) {
            val stepStart = System.currentTimeMillis()
            onProgress("👤 جاري فحص ${spec.label} (إنشاء + قراءة + تعديل + تحقق)...")
            val stepReport = ProfileStepReport(
                profileType = spec.label,
                collectionName = spec.collection,
                docId = spec.docId,
                fileName = "ProfileDiagnosticRunner.kt",
                lineNumber = spec.line,
                functionName = spec.fnName,
                expectedOutcome = "إنشاء الوثيقة في ${spec.collection} وتحديث الحقل (${spec.verifyKey}) إلى (${spec.verifyExpectedValue})"
            )

            try {
                // 1. Create Profile
                db.collection(spec.collection).document(spec.docId).set(spec.initialData).await()
                stepReport.created = true

                // 2. Read Profile
                val snap1 = db.collection(spec.collection).document(spec.docId).get().await()
                stepReport.readVerified = snap1.exists()

                // 3. Update Profile Fields
                db.collection(spec.collection).document(spec.docId)
                    .set(spec.updatedFields, SetOptions.merge()).await()
                stepReport.updated = true

                // 4. Verify Updated Fields
                val snap2 = db.collection(spec.collection).document(spec.docId).get().await()
                val actualVal = snap2.get(spec.verifyKey)?.toString() ?: ""
                val expectedVal = spec.verifyExpectedValue.toString()

                stepReport.durationMs = System.currentTimeMillis() - stepStart

                if (stepReport.readVerified && actualVal == expectedVal) {
                    stepReport.updateVerified = true
                    stepReport.status = "SUCCESS"
                    stepReport.actualOutcome = "تم الإنشاء والقراءة والتحديث بنجاح (${spec.verifyKey} = $actualVal)"
                    stepReport.notes = "✅ إنشاء + قراءة + تعديل وحفظ (${stepReport.durationMs}ms)"
                } else {
                    stepReport.status = "FAILED"
                    stepReport.actualOutcome = "القيمة الفعلية للحقل ${spec.verifyKey} هي '$actualVal' بدلاً من '$expectedVal'"
                    stepReport.notes = "❌ عدم تطابق الحقل بعد التحديث في ${spec.collection}"
                    stepReport.error = TestError(
                        errorIndex = errorCounter++,
                        testName = spec.label,
                        failedStep = "التحقق من حفظ التعديلات في ${spec.collection}",
                        fileName = "ProfileDiagnosticRunner.kt",
                        lineNumber = spec.line,
                        functionName = spec.fnName,
                        timestamp = timestamp,
                        errorType = "DataMismatchException",
                        fullMessage = "الحقل ${spec.verifyKey} لم يحتفظ بالقيمة المحدثة في الكولكشن ${spec.collection}",
                        probableCause = "تأخر مزامنة الكاش المحلي أو عدم تطبيق SetOptions.merge() بشكل صحيح.",
                        suggestedFix = "تأكد من استخدام set(updatedFields, SetOptions.merge()).await() والتحقق من صلاحيات الكتابة في ${spec.collection}.",
                        expectedOutcome = stepReport.expectedOutcome,
                        actualOutcome = stepReport.actualOutcome
                    )
                }
            } catch (e: Exception) {
                stepReport.durationMs = System.currentTimeMillis() - stepStart
                stepReport.status = "FAILED"
                val analyzedError = DiagnosticErrorAnalyzer.analyzeException(
                    e = e,
                    testName = spec.label,
                    failedStep = "فحص دورة حياة الملف الشخصي (${spec.collection})",
                    fallbackFile = "ProfileDiagnosticRunner.kt",
                    fallbackLine = spec.line,
                    fallbackFunction = spec.fnName,
                    expected = stepReport.expectedOutcome,
                    errorIndex = errorCounter++,
                    timestamp = timestamp
                )
                stepReport.error = analyzedError
                stepReport.actualOutcome = analyzedError.actualOutcome
                stepReport.notes = "❌ فشل: ${analyzedError.fullMessage}"
            }

            steps.add(stepReport)
            details.add(
                TestDetail(
                    id = spec.docId,
                    systemName = "11. نظام الملفات الشخصية (8 أنواع)",
                    subCategory = spec.collection,
                    testName = spec.label,
                    fileName = stepReport.fileName,
                    lineNumber = stepReport.lineNumber,
                    functionName = stepReport.functionName,
                    status = stepReport.status,
                    message = stepReport.notes,
                    durationMs = stepReport.durationMs,
                    expectedOutcome = stepReport.expectedOutcome,
                    actualOutcome = stepReport.actualOutcome,
                    error = stepReport.error
                )
            )
            delay(150)
        }

        // Clean up test profiles after verification
        cleanAllTestData(onProgress)

        val passedCount = steps.count { it.status == "SUCCESS" }
        val failedCount = steps.size - passedCount

        onComplete(
            ProfileFullReport(
                timestamp = timestamp,
                totalProfiles = steps.size,
                passed = passedCount,
                failed = failedCount,
                steps = steps,
                details = details
            )
        )
    }
}
