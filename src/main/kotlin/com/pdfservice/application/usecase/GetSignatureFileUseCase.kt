package com.pdfservice.application.usecase

import com.pdfservice.domain.SignatureRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
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

