package com.signservice.application.usecase

import com.signservice.domain.SignatureRepository
import com.signservice.domain.Signature
import java.util.UUID

class CreateSignatureUseCase(
    private val signatureRepository: SignatureRepository
) {

    suspend fun execute(request: CreateSignatureRequest): SignatureCreatedDto {
        require(request.signerName.isNotBlank()) { "signerName is required" }
        require(request.signerPosition.isNotBlank()) { "signerPosition is required" }
        require(request.signerOrganization.isNotBlank()) { "signerOrganization is required" }
        require(request.certificateSerialNumber.isNotBlank()) { "certificateSerialNumber is required" }
        require(request.signatureBytes.isNotEmpty()) { "signatureBytes is required" }
        require(request.fileId.isNotBlank()) { "fileId is required" }
        require(request.fileName.isNotBlank()) { "fileName is required" }
        require(request.fileHash.isNotBlank()) { "fileHash is required" }

        val signature = Signature(
            id = UUID.randomUUID(),
            signerName = request.signerName.trim(),
            signerPosition = request.signerPosition.trim(),
            signerOrganization = request.signerOrganization.trim(),
            certificateSerialNumber = request.certificateSerialNumber.trim(),
            certificateValidFrom = request.certificateValidFrom,
            certificateValidTo = request.certificateValidTo,
            isCertificateValidAtSigningTime = request.isCertificateValidAtSigningTime,
            signedAt = request.signedAt,
            signatureBytes = request.signatureBytes,
            signatureBase64 = request.signatureBase64,
            fileId = request.fileId.trim(),
            fileName = request.fileName.trim(),
            fileHash = request.fileHash.trim()
        )

        val saved = signatureRepository.save(signature)
        return SignatureCreatedDto(id = saved.id)
    }
}

