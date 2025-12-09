package com.pdfservice.application.usecase

import com.pdfservice.application.pdf.DocxToPdfConverter
import com.pdfservice.application.pdf.PdfSignatureService
import com.pdfservice.domain.SignatureRepository
import com.pdfservice.infra.files.FilesClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetSignedPdfUseCase(
    private val signatureRepository: SignatureRepository,
    private val filesClient: FilesClient,
    private val pdfSignatureService: PdfSignatureService,
    private val poiDocxToPdfConverter: DocxToPdfConverter,
) {

    suspend fun execute(signatureId: UUID): SignedPdfDto {
        val signature = signatureRepository.findById(signatureId)
            ?: throw IllegalArgumentException("Signature $signatureId not found")

        val original = filesClient.downloadFile(signature.fileId)

        val pdf = poiDocxToPdfConverter.convert(original)

        val signedPdf = pdfSignatureService.applySignatureStampToPdf(pdf, signature)
        return SignedPdfDto(
            fileName = signature.fileName,
            pdf = signedPdf
        )
    }
}

