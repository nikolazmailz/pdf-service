package com.signservice.application.usecase

import com.signservice.application.service.PdfSignatureService
import com.signservice.domain.SignatureRepository
import com.signservice.infra.files.FilesClient
import java.util.UUID

class GetSignedPdfUseCase(
    private val signatureRepository: SignatureRepository,
    private val filesClient: FilesClient,
    private val pdfSignatureService: PdfSignatureService
) {

    suspend fun execute(signatureId: UUID): SignedPdfDto {
        val signature = signatureRepository.findById(signatureId)
            ?: throw IllegalArgumentException("Signature $signatureId not found")

        val original = filesClient.downloadFile(signature.fileId)
        val signedPdf = pdfSignatureService.applySignatureStampToPdf(original, signature)
        return SignedPdfDto(
            fileName = signature.fileName,
            pdf = signedPdf
        )
    }
}

