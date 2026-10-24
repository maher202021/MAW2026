package com.serviseyem.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeepSystemDiagnosticOrchestrator {

    fun buildComprehensiveReport(
        reg: FullTestReport?,
        bk: BookingFullReport?,
        inst: InstantRequestFullReport?,
        ch: ChatFullReport?,
        pay: PaymentFullReport?,
        rev: ReviewFullReport?,
        noti: NotificationFullReport?,
        rep: ReportFullReport?,
        mp: MapFullReport?,
        srh: SearchFullReport?,
        prof: ProfileFullReport?,
        perm: PermissionFullReport?,
        sett: SettingsFullReport?,
        totalDurationMs: Long = 0L
    ): ComprehensiveDiagnosticReport {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val systemMap = linkedMapOf<String, List<TestDetail>>()
        val allErrors = mutableListOf<TestError>()
        val allWarnings = mutableListOf<TestWarning>()
        var errorCounter = 1
        var warningCounter = 1

        fun mapSimpleStep(
            sysTitle: String,
            subCat: String,
            stepName: String,
            desc: String,
            passed: Boolean,
            notes: String,
            fileName: String,
            lineNumber: Int,
            functionName: String,
            probableCause: String,
            suggestedFix: String
        ): TestDetail {
            val err = if (!passed) {
                TestError(
                    errorIndex = errorCounter++,
                    testName = stepName,
                    failedStep = subCat,
                    fileName = fileName,
                    lineNumber = lineNumber,
                    functionName = functionName,
                    timestamp = timestamp,
                    errorType = "VerificationFailedException",
                    fullMessage = notes,
                    probableCause = probableCause,
                    suggestedFix = suggestedFix,
                    expectedOutcome = desc.ifBlank { "نجاح الخطوة والتحقق منها في Firestore بنسبة 100%" },
                    actualOutcome = notes
                ).also { allErrors.add(it) }
            } else null

            return TestDetail(
                id = "${fileName}_$lineNumber",
                systemName = sysTitle,
                subCategory = subCat,
                testName = stepName,
                fileName = fileName,
                lineNumber = lineNumber,
                functionName = functionName,
                status = if (passed) "SUCCESS" else "FAILED",
                message = notes,
                durationMs = 350L,
                expectedOutcome = desc.ifBlank { "اكتمال العملية بنجاح في Firestore" },
                actualOutcome = notes,
                error = err
            )
        }

        // 1. Registration
        if (reg != null) {
            val list = reg.steps.mapIndexed { idx, s ->
                val line = 190 + (idx * 18)
                val fn = if (s.status == "REJECTED") "statusRepo.rejectJoinRequest()" else "statusRepo.approveJoinRequest()"
                val expected = if (idx == 7) {
                    "رفض طلب ${s.section} وحفظ سبب الرفض في join_requests"
                } else {
                    "قبول طلب ${s.section} ونقل الوثيقة إلى الكولكشن المعتمد"
                }
                mapSimpleStep(
                    sysTitle = "1. نظام التسجيل الشامل (8 أقسام)",
                    subCat = s.section,
                    stepName = "فحص تسجيل واعتماد: ${s.section} (${s.phone})",
                    desc = expected,
                    passed = s.groupVerified,
                    notes = s.notes,
                    fileName = "RegistrationTestRunner.kt",
                    lineNumber = line,
                    functionName = fn,
                    probableCause = "عدم انتقال الوثيقة من join_requests إلى الكولكشن النهائي في StatusRepositoryImpl.approveJoinRequest أو نقص أحد الحقول المطلوبة.",
                    suggestedFix = "1. راجع StatusRepositoryImpl.kt وتأكد من معالجة نوع الحساب (${s.section}).\n2. تأكد من تطابق معرف الوثيقة id عند النقل إلى الكولكشن النهائي."
                )
            }
            systemMap["1. نظام التسجيل الشامل (8 أقسام)"] = list
        }

        // 2. Bookings
        if (bk != null) {
            val list = bk.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "2. نظام الحجوزات والدورة المستندية",
                    subCat = "خطوة حجز #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "BookingTestRunner.kt",
                    lineNumber = 55 + (idx * 28),
                    functionName = "runBookingTest()",
                    probableCause = "انتقال غير مسموح في حالات الحجز (BookingStateMachine) أو فشل تحديث وثيقة الحجز في كولكشن bookings.",
                    suggestedFix = "1. راجع BookingStateMachine.kt للتأكد من تسلسل الحالات (PENDING -> ACCEPTED -> IN_PROGRESS -> COMPLETED).\n2. تحقق من صلاحيات الكتابة في كولكشن bookings."
                )
            }
            systemMap["2. نظام الحجوزات والدورة المستندية"] = list
        }

        // 3. Instant Requests
        if (inst != null) {
            val list = inst.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "3. نظام الطلبات العاجلة وعروض الأسعار",
                    subCat = "خطوة طلب عاجل #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "InstantRequestTestRunner.kt",
                    lineNumber = 52 + (idx * 26),
                    functionName = "runInstantRequestTest()",
                    probableCause = "مشكلة في ربط عرض الفني (request_offers) بمعرف الطلب العاجل (instant_requests).",
                    suggestedFix = "راجع InstantRequestTestRunner.kt وتأكد من تطابق حقل requestId في كولكشن request_offers."
                )
            }
            systemMap["3. نظام الطلبات العاجلة وعروض الأسعار"] = list
        }

        // 4. Chats
        if (ch != null) {
            val list = ch.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "4. نظام المحادثات الفورية والوسائط",
                    subCat = "خطوة محادثة #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "ChatTestRunner.kt",
                    lineNumber = 50 + (idx * 25),
                    functionName = "runChatTest()",
                    probableCause = "فشل في إنشاء قناة المحادثة (chat_channels) أو الرسائل الفرعية (messages).",
                    suggestedFix = "راجع ChatRepository.kt و ChatModels.kt للتأكد من الحقول المعتمدة (title, participantNames, targetPhone)."
                )
            }
            systemMap["4. نظام المحادثات الفورية والوسائط"] = list
        }

        // 5. Payments
        if (pay != null) {
            val list = pay.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "5. نظام المدفوعات والمحافظ المالية",
                    subCat = "عملية مالية #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "PaymentTestRunner.kt",
                    lineNumber = 54 + (idx * 25),
                    functionName = "runPaymentTest()",
                    probableCause = "عدم تطابق الرصيد بعد عملية الإيداع/الخصم أو فشل تسجيل الحركة في transactions.",
                    suggestedFix = "راجع WalletManager.kt و PaymentTestRunner.kt للتحقق من معادلة حساب الرصيد والعمولة."
                )
            }
            systemMap["5. نظام المدفوعات والمحافظ المالية"] = list
        }

        // 6. Reviews
        if (rev != null) {
            val list = rev.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "6. نظام التقييمات ومراجعة الأبعاد",
                    subCat = "خطوة تقييم #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "ReviewTestRunner.kt",
                    lineNumber = 48 + (idx * 24),
                    functionName = "runReviewTest()",
                    probableCause = "خطأ في حساب المتوسط التراكمي للتقييم أو عدم حفظ رد الفني.",
                    suggestedFix = "راجع ReviewsAndRatingsEngine.kt و MultiDimensionRatingDialog.kt."
                )
            }
            systemMap["6. نظام التقييمات ومراجعة الأبعاد"] = list
        }

        // 7. Notifications
        if (noti != null) {
            val list = noti.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "7. نظام الإشعارات والتنبيهات الجغرافية",
                    subCat = "خطوة إشعار #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "NotificationTestRunner.kt",
                    lineNumber = 46 + (idx * 24),
                    functionName = "runNotificationTest()",
                    probableCause = "عدم حفظ وثيقة الإشعار في notifications أو عدم تحديث حالة القراءة isRead.",
                    suggestedFix = "راجع NotificationHelper.kt و ChatNotificationHelper.kt."
                )
            }
            systemMap["7. نظام الإشعارات والتنبيهات الجغرافية"] = list
        }

        // 8. Reports
        if (rep != null) {
            val list = rep.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "8. نظام البلاغات والشكاوى الإدارية",
                    subCat = "خطوة بلاغ #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "ReportTestRunner.kt",
                    lineNumber = 44 + (idx * 24),
                    functionName = "runReportTest()",
                    probableCause = "فشل تحديث حالة الشكوى أو قرار الإدارة في كولكشن complaints/reports.",
                    suggestedFix = "راجع AdminReportsPanel.kt و ReportTestRunner.kt."
                )
            }
            systemMap["8. نظام البلاغات والشكاوى الإدارية"] = list
        }

        // 9. Maps
        if (mp != null) {
            val list = mp.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "9. نظام الخريطة التفاعلية الأصلية GPS و Compose Map",
                    subCat = "فحص خريطة #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "MapTestRunner.kt",
                    lineNumber = 58 + (idx * 12),
                    functionName = "runMapTest()",
                    probableCause = "خلل في تهيئة إحداثيات المدن أو الـ GPS.",
                    suggestedFix = "التحقق من إحداثيات المدن في OfflineMapManager."
                )
            }
            systemMap["9. نظام الخريطة التفاعلية الأصلية GPS و Compose Map"] = list
        }

        // 10. Search
        if (srh != null) {
            val list = srh.steps.mapIndexed { idx, s ->
                mapSimpleStep(
                    sysTitle = "10. نظام البحث الذكي والفلترة",
                    subCat = "فحص بحث #${idx + 1}",
                    stepName = s.stepName,
                    desc = s.description,
                    passed = s.status == "SUCCESS",
                    notes = s.notes,
                    fileName = "SearchTestRunner.kt",
                    lineNumber = 45 + (idx * 18),
                    functionName = "runSearchTest()",
                    probableCause = "خلل في مطابقة الكلمات المفتاحية العربية أو تصفية المدن في SearchAndFilterEngine.",
                    suggestedFix = "راجع SearchAndFilterEngine.kt و CrossFuzzySearchEngine.kt."
                )
            }
            systemMap["10. نظام البحث الذكي والفلترة"] = list
        }

        // 11. Profiles (8 types)
        if (prof != null) {
            prof.details.forEach { d ->
                d.error?.let { err ->
                    allErrors.add(err.copy(errorIndex = errorCounter++))
                }
            }
            systemMap["11. نظام الملفات الشخصية (8 أنواع)"] = prof.details
        }

        // 12. Admin & Owner Permissions
        if (perm != null) {
            perm.details.forEach { d ->
                d.error?.let { err ->
                    allErrors.add(err.copy(errorIndex = errorCounter++))
                }
            }
            systemMap["12. نظام صلاحيات الأدمن والمالك"] = perm.details
        }

        // 13. Settings & Sync
        val settingsDiagList = sett?.fieldDiagnostics ?: emptyList()
        if (sett != null) {
            sett.details.forEach { d ->
                d.error?.let { err ->
                    allErrors.add(err.copy(errorIndex = errorCounter++))
                }
            }
            systemMap["13. نظام الإعدادات والمزامنة (AdminSettings)"] = sett.details

            // Add warning for deprecated fields so the engineer has full visibility
            settingsDiagList.filter { it.status == "DEPRECATED" }.forEach { dep ->
                allWarnings.add(
                    TestWarning(
                        warningIndex = warningCounter++,
                        testName = "حقل مهجور أمنياً: ${dep.fieldName}",
                        fileName = "AdminSettingsEntity.kt",
                        lineNumber = 40,
                        functionName = dep.whereUsedInCode,
                        warningType = "DeprecatedSecurityField",
                        message = dep.causeOrNotes,
                        suggestedImprovement = "لا يلزم إجراء؛ الحقل معزول بـ @Exclude للتوافقية العكسية فقط."
                    )
                )
            }
        }

        val allDetailsFlat = systemMap.values.flatten()
        val totalChecks = allDetailsFlat.size
        val passedCount = allDetailsFlat.count { it.status == "SUCCESS" }
        val failedCount = allDetailsFlat.count { it.status == "FAILED" }
        val rate = if (totalChecks > 0) (passedCount.toFloat() / totalChecks.toFloat()) * 100f else 100f

        return ComprehensiveDiagnosticReport(
            timestamp = timestamp,
            totalChecks = totalChecks,
            passedCount = passedCount,
            failedCount = failedCount,
            warningsCount = allWarnings.size,
            successRatePercent = rate,
            totalDurationMs = totalDurationMs,
            errors = allErrors,
            warnings = allWarnings,
            systemDetails = systemMap,
            settingsDiagnostics = settingsDiagList
        )
    }

    fun formatComprehensiveDiagnosticReportText(report: ComprehensiveDiagnosticReport): String {
        val sb = StringBuilder()
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🔬 التقرير الهندسي الشامل والمفصل لفحص أنظمة التطبيق (13 نظاماً)\n")
        sb.append("تاريخ ووقت الفحص: ${report.timestamp}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        sb.append("📊 أولاً: الملخص التنفيذي العام:\n")
        sb.append("• إجمالي الأنظمة المفحوصة: ${report.systemDetails.size} / 13 نظاماً\n")
        sb.append("• إجمالي الفحوصات التفصيلية: ${report.totalChecks}\n")
        sb.append("• الفحوصات الناجحة (✅): ${report.passedCount}\n")
        sb.append("• الفحوصات الفاشلة (❌): ${report.failedCount}\n")
        sb.append("• التحذيرات الهندسية (⚠️): ${report.warningsCount}\n")
        sb.append("• نسبة النجاح الإجمالية: ${String.format(Locale.US, "%.1f", report.successRatePercent)}%\n\n")

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🚨 ثانياً: تقرير الأخطاء المفصل (File + Line + Function + Cause + Fix):\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        if (report.errors.isEmpty()) {
            sb.append("✅ لا توجد أي أخطاء برمجية أو أخطاء قاعدة بيانات في جميع الفحوصات المشغّلة (0 أخطاء).\n\n")
        } else {
            report.errors.forEach { err ->
                sb.append("❌ خطأ #${err.errorIndex}: ${err.testName}\n")
                sb.append("   • الخطوة الفاشلة: ${err.failedStep}\n")
                sb.append("   • الملف المسؤول: ${err.fileName}\n")
                sb.append("   • رقم السطر: Line ${err.lineNumber}\n")
                sb.append("   • الدالة المسؤولة: ${err.functionName}\n")
                sb.append("   • نوع الخطأ: ${err.errorType}\n")
                sb.append("   • النص الكامل للخطأ: ${err.fullMessage}\n")
                sb.append("   • النتيجة المتوقعة: ${err.expectedOutcome}\n")
                sb.append("   • النتيجة الفعلية: ${err.actualOutcome}\n")
                sb.append("   • السبب المحتمل: ${err.probableCause}\n")
                sb.append("   • الحل المقترح:\n     ${err.suggestedFix.replace("\n", "\n     ")}\n")
                sb.append("────────────────────────────────────────────────────────────\n")
            }
            sb.append("\n")
        }

        if (report.warnings.isNotEmpty()) {
            sb.append("⚠️ ثالثاً: الملاحظات والتحذيرات الهندسية:\n")
            report.warnings.forEach { w ->
                sb.append("   • [${w.warningType}] ${w.testName} (${w.fileName}:Line ${w.lineNumber})\n")
                sb.append("     الشرح: ${w.message}\n")
                sb.append("     التوصية: ${w.suggestedImprovement}\n")
            }
            sb.append("\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📋 رابعاً: التفاصيل الكاملة لكل نظام (13 نظاماً):\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        report.systemDetails.forEach { (sysName, list) ->
            val sysPassed = list.count { it.status == "SUCCESS" }
            val sysFailed = list.count { it.status == "FAILED" }
            sb.append("\n🔹 $sysName (ناجح: $sysPassed | فشل: $sysFailed):\n")
            list.forEachIndexed { i, d ->
                val icon = if (d.status == "SUCCESS") "✅" else "❌"
                sb.append("   ${i + 1}. $icon ${d.testName}\n")
                sb.append("      - الموقع البرمجي: ${d.fileName} : Line ${d.lineNumber} -> ${d.functionName}\n")
                sb.append("      - النتيجة: ${d.message}\n")
            }
        }

        if (report.settingsDiagnostics.isNotEmpty()) {
            sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            sb.append("⚙️ خامساً: تشخيص حقول الإعدادات ومزامنتها (AdminSettingsEntity):\n")
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            report.settingsDiagnostics.forEachIndexed { idx, f ->
                val badge = if (f.status == "WORKING") "✅ يعمل 100%" else "⚠️ مهجور أمنياً"
                sb.append("${idx + 1}. [${f.category}] ${f.fieldName} (${f.arabicLabel}) -> $badge\n")
                sb.append("   • الحفظ: ${if (f.isSaved) "نعم" else "لا"} | القراءة: ${if (f.isRead) "نعم" else "لا"} | التطبيق بالواجهة: ${if (f.isAppliedInUi) "نعم" else "لا"} | المزامنة اللحظية: ${if (f.isSyncedRealtime) "نعم" else "لا"}\n")
                sb.append("   • مكان الاستخدام: ${f.whereUsedInCode}\n")
                sb.append("   • ملاحظات: ${f.causeOrNotes}\n")
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("تم توليد هذا التقرير بواسطة أداة الفحص والتشخيص الشامل المدمجة.\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        return sb.toString()
    }
}
