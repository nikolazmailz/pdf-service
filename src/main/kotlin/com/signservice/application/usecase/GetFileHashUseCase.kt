package com.signservice.application.usecase

import com.signservice.application.HashingService
import com.signservice.infra.files.FilesClient

class GetFileHashUseCase(
    private val filesClient: FilesClient,
    private val hashingService: HashingService
) {

    suspend fun execute(fileId: String): FileHashDto {
        require(fileId.isNotBlank()) { "fileId is required" }
        val bytes = filesClient.downloadFile(fileId)
        val hash = hashingService.calculateGostHash(bytes)
        return FileHashDto(fileId = fileId, hash = hash)
    }
}

