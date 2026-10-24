package com.serviseyem.data.repositories

import com.serviseyem.domain.entities.AuthUserEntity
import com.serviseyem.domain.entities.JoinStatusEntity
import com.serviseyem.domain.entities.RegistrationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🏛️ Contract Interface: IRegistrationRepository
 */
interface IRegistrationRepository {
    suspend fun registerClient(client: RegistrationEntity.Client): Result<String>
    suspend fun registerProvider(provider: RegistrationEntity.Provider): Result<String>
    suspend fun registerStore(store: RegistrationEntity.Store): Result<String>
    suspend fun registerRestaurant(restaurant: RegistrationEntity.Restaurant): Result<String>
    suspend fun registerMedicalCenter(medical: RegistrationEntity.MedicalCenter): Result<String>
    suspend fun registerProperty(property: RegistrationEntity.Property): Result<String>
    suspend fun registerJob(job: RegistrationEntity.Job): Result<String>
    fun getJoinStatusFlow(phoneNumber: String): Flow<JoinStatusEntity?>
}

/**
 * 🏛️ Contract Interface: IAuthRepository
 */
interface IAuthRepository {
    suspend fun loginWithPhone(phone: String, passwordPin: String): Result<AuthUserEntity>
    suspend fun restoreAccount(phone: String, passwordPin: String): Result<AuthUserEntity>
    suspend fun performSocialLogin(provider: String, token: String): Result<AuthUserEntity>
    fun getCurrentUser(): AuthUserEntity?
    suspend fun logout()
}

/**
 * 🏛️ Contract Interface: IStorageRepository
 */
interface IStorageRepository {
    suspend fun compressAndUploadImage(imageUriOrBytes: ByteArray, folderName: String): Result<String>
    suspend fun uploadMultipleImages(imagesBytes: List<ByteArray>, folderName: String): Result<List<String>>
}
