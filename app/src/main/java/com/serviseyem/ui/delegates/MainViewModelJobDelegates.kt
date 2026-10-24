package com.serviseyem.ui

import com.serviseyem.domain.entities.RegistrationEntity

fun MainViewModel.setJobBlocked(jobId: String, isBlocked: Boolean, reason: String = "") =
    adminViewModel.setJobBlocked(jobId, isBlocked, reason)

suspend fun MainViewModel.registerJobPoster(job: RegistrationEntity.Job): Result<String> {
    val ctx = appContext ?: return Result.failure(Exception("Context unavailable"))
    val repo = com.serviseyem.data.repositories.RegistrationRepositoryImpl(ctx)
    return repo.registerJob(job)
}
