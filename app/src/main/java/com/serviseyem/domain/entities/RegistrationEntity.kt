package com.serviseyem.domain.entities

import androidx.annotation.Keep

/**
 * 🏛️ Domain Entity: RegistrationEntity
 * Representing registration payload models across all 7 registration types.
 * Completely decoupled from Android dependencies.
 */
@Keep
sealed class RegistrationEntity {

    @Keep
    data class Client(
        val fullName: String = "",
        val phone: String = "",
        val city: String = "",
        val rawPassword: String = "",
        val profileImageUrl: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Client(fullName='$fullName', phone='$phone', city='$city', rawPassword=***, profileImageUrl='$profileImageUrl')"
    }

    @Keep
    data class Provider(
        val fullName: String = "",
        val phone: String = "",
        val professionCategory: String = "",
        val city: String = "",
        val experienceYears: Int = 0,
        val bio: String = "",
        val identityDocumentUrl: String = "",
        val licenseNumber: String = "",
        val workImages: List<String> = emptyList(),
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Provider(fullName='$fullName', phone='$phone', professionCategory='$professionCategory', city='$city', experienceYears=$experienceYears, bio='$bio', rawPassword=***)"
    }

    @Keep
    data class Store(
        val storeName: String = "",
        val ownerName: String = "",
        val phone: String = "",
        val storeCategory: String = "",
        val city: String = "",
        val addressDetails: String = "",
        val commercialRegisterNumber: String = "",
        val logoUrl: String = "",
        val storeImages: List<String> = emptyList(),
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Store(storeName='$storeName', ownerName='$ownerName', phone='$phone', storeCategory='$storeCategory', city='$city', rawPassword=***)"
    }

    @Keep
    data class Restaurant(
        val restaurantName: String = "",
        val ownerName: String = "",
        val phone: String = "",
        val cuisineType: String = "",
        val city: String = "",
        val addressDetails: String = "",
        val logoUrl: String = "",
        val menuImageUrls: List<String> = emptyList(),
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Restaurant(restaurantName='$restaurantName', ownerName='$ownerName', phone='$phone', cuisineType='$cuisineType', city='$city', rawPassword=***)"
    }

    @Keep
    data class MedicalCenter(
        val centerName: String = "",
        val specialtyCategory: String = "",
        val doctorName: String = "",
        val phone: String = "",
        val city: String = "",
        val addressDetails: String = "",
        val licenseNumber: String = "",
        val logoUrl: String = "",
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "MedicalCenter(centerName='$centerName', specialtyCategory='$specialtyCategory', doctorName='$doctorName', phone='$phone', city='$city', rawPassword=***)"
    }

    @Keep
    data class Property(
        val title: String = "",
        val propertyType: String = "", // Sale, Rent
        val category: String = "", // Apartment, Land, Villa
        val ownerName: String = "",
        val phone: String = "",
        val city: String = "",
        val areaDetails: String = "",
        val priceYer: Double = 0.0,
        val description: String = "",
        val imageUrls: List<String> = emptyList(),
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Property(title='$title', propertyType='$propertyType', category='$category', ownerName='$ownerName', phone='$phone', priceYer=$priceYer, rawPassword=***)"
    }

    @Keep
    data class Job(
        val jobTitle: String = "",
        val companyName: String = "",
        val category: String = "",
        val contactPhone: String = "",
        val contactEmail: String = "",
        val city: String = "",
        val requirements: String = "",
        val salaryRange: String = "",
        val rawPassword: String = ""
    ) : RegistrationEntity() {
        override fun toString(): String =
            "Job(jobTitle='$jobTitle', companyName='$companyName', category='$category', contactPhone='$contactPhone', city='$city', rawPassword=***)"
    }
}

@Keep
data class JoinStatusEntity(
    val requestId: String = "",
    val applicantName: String = "",
    val registrationType: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Keep
data class AuthUserEntity(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "CLIENT",
    val token: String = "",
    val isVerified: Boolean = false
) {
    override fun toString(): String =
        "AuthUserEntity(uid='$uid', name='$name', phone='$phone', role='$role', token=***, isVerified=$isVerified)"
}
