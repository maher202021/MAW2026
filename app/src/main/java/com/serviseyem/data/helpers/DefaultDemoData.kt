package com.serviseyem.data.helpers

import com.serviseyem.data.*

/**
 * 📦 Default Demo Data Helper
 * Generates comprehensive, realistic Yemeni services seed data with direct Firestore synchronization.
 */
object DefaultDemoData {

    fun getDemoProviders(): List<ProviderEntity> {
        return listOf(
            ProviderEntity(
                id = "prov_demo_ghiryani",
                name = "أمين الغرياني",
                phone = "777123456",
                categoryId = "sub_1_2",
                area = "صنعاء - منطقة الدائري جوار مدرسة أسماء للبنات",
                cityId = "ye_sana_cap",
                localNeighborhood = "منطقة الدائري جوار مدرسة أسماء للبنات",
                rating = 5.0f,
                numReviews = 48,
                previewPrice = 3000.0,
                subscriptionStatus = "APPROVED",
                isAvailable = true,
                isVerified = true,
                isVip = true,
                profession = "صيانة وشبكات متكاملة",
                specialization = "شبكات وأنظمة وصيانة",
                providerType = "INDIVIDUAL",
                latitude = 15.3490,
                longitude = 44.1890,
                profileImage = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=80"
            ),
            ProviderEntity(
                id = "prov_demo_hababi",
                name = "سمير عبدالله الحبابي",
                phone = "777654321",
                categoryId = "sub_1_3",
                area = "صنعاء - شارع تعز",
                cityId = "ye_sana_cap",
                localNeighborhood = "شارع تعز العام",
                rating = 5.0f,
                numReviews = 36,
                previewPrice = 2500.0,
                subscriptionStatus = "APPROVED",
                isAvailable = true,
                isVerified = true,
                isVip = true,
                profession = "كهرباء ومولدات وتمديدات",
                specialization = "كهرباء ومولدات طاقة",
                providerType = "INDIVIDUAL",
                latitude = 15.3210,
                longitude = 44.2050,
                profileImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&auto=format&fit=crop&q=80"
            ),
            ProviderEntity(
                id = "prov_demo_wantari",
                name = "نايف الونتاري",
                phone = "771998877",
                categoryId = "sub_1_1",
                area = "صنعاء - حدة",
                cityId = "ye_sana_cap",
                localNeighborhood = "حي حدة السكني",
                rating = 5.0f,
                numReviews = 52,
                previewPrice = 2000.0,
                subscriptionStatus = "APPROVED",
                isAvailable = true,
                isVerified = true,
                isVip = true,
                profession = "سباكة وتمديدات صحية حديثة",
                specialization = "سباكة وصيانة مضخات",
                providerType = "INDIVIDUAL",
                latitude = 15.3120,
                longitude = 44.1860,
                profileImage = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1504384308090-c894fdcc538d?w=800&auto=format&fit=crop&q=80"
            ),
            ProviderEntity(
                id = "prov_demo_ahmed",
                name = "أحمد لصيانة الأجهزة المنزلية والطاقة الشمسية",
                phone = "777889900",
                categoryId = "sub_1_5",
                area = "صنعاء - حدة",
                cityId = "ye_sana_cap",
                localNeighborhood = "حدة - الحي التجاري",
                rating = 4.9f,
                numReviews = 94,
                previewPrice = 3000.0,
                subscriptionStatus = "APPROVED",
                isAvailable = true,
                isVerified = true,
                isVip = true,
                profession = "فني معتمد لصيانة الغسالات، الثلاجات، ومنظومات الطاقة الشمسية",
                specialization = "أجهزة منزلية وطاقة شمسية",
                providerType = "INDIVIDUAL",
                latitude = 15.3190,
                longitude = 44.1950,
                profileImage = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=800&auto=format&fit=crop&q=80"
            )
        )
    }

    fun getDemoStores(): List<StoreEntity> {
        return listOf(
            StoreEntity(
                id = "store_demo_alyusri",
                sectionId = "stores",
                name = "اليسري للبهارات اليمنية الأصيلة",
                description = "أجود أنواع البهارات والبن اليمني والمكسرات الفاخرة. بهارات شاورما مميزة - 750 YER",
                phone = "771234567",
                categoryId = "sub_store_4",
                cityId = "ye_sana_cap",
                localNeighborhood = "صنعاء - الدائري",
                rating = 5.0f,
                numReviews = 192,
                isApproved = true,
                isActive = true,
                isVerified = true,
                isVip = true,
                workingHours = "9:00 ص - 10:00 م",
                logoImage = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=800&auto=format&fit=crop&q=80",
                specialOffersJson = "[{\"name\":\"بهارات شاورما مميزة\",\"price\":750.0,\"currency\":\"YER\"}]",
                latitude = 15.3489,
                longitude = 44.2012
            ),
            StoreEntity(
                id = "store_demo_saeeda_center",
                sectionId = "medical",
                name = "مجمع السعيدة الطبي التخصصي",
                description = "عيادات أطفال، باطنية، أسنان، مختبر تحاليل متكامل وأشعة رقمية متطورة.",
                phone = "01456789",
                categoryId = "sub_center_2",
                cityId = "ye_sana_cap",
                localNeighborhood = "صنعاء - شارع الستين",
                rating = 4.8f,
                numReviews = 310,
                isApproved = true,
                isActive = true,
                isVerified = true,
                isVip = true,
                workingHours = "8:00 ص - 11:00 م",
                logoImage = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=400&auto=format&fit=crop&q=80",
                coverImage = "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=800&auto=format&fit=crop&q=80",
                medicalLicenseNo = "YE-MED-2026-SAEEDA",
                latitude = 15.3678,
                longitude = 44.1734
            ),
            StoreEntity(
                id = "store_demo_remas",
                sectionId = "restaurants",
                name = "مطاعم ومطابخ ريماس السياحية",
                description = "أشهى المأكولات اليمنية والشرقية، مظبي، مندي، فحسة وسلتة يمنية أصيلة.",
                phone = "770000001",
                categoryId = "sub_rest_1",
                cityId = "ye_sana_cap",
                localNeighborhood = "صنعاء - حدة",
                rating = 4.9f,
                numReviews = 480,
                isApproved = true,
                isActive = true,
                isVerified = true,
                workingHours = "10:00 ص - 12:00 م",
                latitude = 15.3123,
                longitude = 44.1901
            )
        )
    }

    fun getDemoProperties(): List<PropertyEntity> {
        return listOf(
            PropertyEntity(
                id = "prop_demo_furnished_apt",
                sectionId = "realestate",
                title = "شقة مفروشة فاخرة للإيجار العائلي",
                description = "شقة مفروشة فاخرة وجديدة، 3 غرف نوم وصالة ومطبخ وحمامين ومصعد، خدمات مستقلة وموقع استراتيجي هادئ.",
                price = 150000.0,
                currency = "YER",
                cityId = "ye_sana_cap",
                localNeighborhood = "صنعاء - حي الأصبحي",
                phone = "773322110",
                isApproved = true,
                isActive = true,
                images = listOf("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800&auto=format&fit=crop&q=80"),
                latitude = 15.3056,
                longitude = 44.1878
            )
        )
    }

    fun getDemoBanners(): List<BannerEntity> {
        return emptyList()
    }

    fun getDemoProducts(): List<ProductEntity> {
        return listOf(
            ProductEntity(
                id = "prod_yusri_1",
                storeId = "store_demo_alyusri",
                name = "بهارات شاورما مميزة",
                description = "خلطة بهارات الشاورما العربية الخاصة المحضرة من أندر التوابل الطبيعية",
                price = 750.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=80",
                category = "توابل مشكلة",
                isOffer = true,
                discountPercent = 15,
                oldPrice = 900.0,
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_2",
                storeId = "store_demo_alyusri",
                name = "بهارات كبسة ومضغوط ملكي",
                description = "توليفة بهارات أرز الكبسة والمندي اليمني الأصلية لنكهة لا تقاوم",
                price = 1200.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1509358271058-acd22cc93898?w=400&auto=format&fit=crop&q=80",
                category = "توابل مشكلة",
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_3",
                storeId = "store_demo_alyusri",
                name = "خلطة تتبيل اللحوم المشوية",
                description = "تتبيلة فاخرة للمشاوي والكباب بنكهة يمنية أصيلة",
                price = 950.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=400&auto=format&fit=crop&q=80",
                category = "بهارات اللحوم",
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_4",
                storeId = "store_demo_alyusri",
                name = "بهارات الدجاج والمقلقل",
                description = "خلطة مميزة للتحمير والإيدامات والمقلقل الصنعاني",
                price = 800.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1532336414038-cf19250c5757?w=400&auto=format&fit=crop&q=80",
                category = "بهارات اللحوم",
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_5",
                storeId = "store_demo_alyusri",
                name = "شاي عدني بالهيل والزعفران",
                description = "توليفة الشاي العدني الممتاز مع الحليب والهيل والزعفران الطبيعي",
                price = 1500.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=400&auto=format&fit=crop&q=80",
                category = "بهارات شاي",
                isOffer = true,
                discountPercent = 20,
                oldPrice = 1800.0,
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_6",
                storeId = "store_demo_alyusri",
                name = "قهوة قشر يمنية بالزنجبيل",
                description = "قهوة القشر اليمنية المشهورة محوجة بالهيل والزنجبيل البلدي",
                price = 1100.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=400&auto=format&fit=crop&q=80",
                category = "بهارات شاي",
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_7",
                storeId = "store_demo_alyusri",
                name = "دقة يمنية بلدية فاخرة",
                description = "دقة التوابل الصنعانية بالسمسم والزعتر والكمون البلدي",
                price = 650.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=400&auto=format&fit=crop&q=80",
                category = "خلطات خاصة",
                isAvailable = true
            ),
            ProductEntity(
                id = "prod_yusri_8",
                storeId = "store_demo_alyusri",
                name = "سحاوق يمني جاف بالأعشاب",
                description = "خلطة السحاوق والشفوت المجففة بالكبزرة والثوم والحر البسباس",
                price = 500.0,
                currency = "YER",
                imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=400&auto=format&fit=crop&q=80",
                category = "خلطات خاصة",
                isAvailable = true
            )
        )
    }
}
