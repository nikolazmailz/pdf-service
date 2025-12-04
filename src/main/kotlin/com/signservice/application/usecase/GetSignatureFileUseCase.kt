package com.signservice.application.usecase

import com.signservice.domain.SignatureRepository
import java.util.UUID

class GetSignatureFileUseCase(
    private val signatureRepository: SignatureRepository
) {

    suspend fun execute(signatureId: UUID): SignatureFileDto {
        val signature = signatureRepository.findById(signatureId)
            ?: throw IllegalArgumentException("Signature with id $signatureId not found")

        val bytes = signature.signatureBytes ?: throw IllegalStateException("Signature bytes are missing")
        return SignatureFileDto(
            fileId = signature.fileId,
            fileName = signature.fileName,
            bytes = bytes
        )
    }
}

