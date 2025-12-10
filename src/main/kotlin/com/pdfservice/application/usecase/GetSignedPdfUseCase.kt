package com.pdfservice.application.usecase

import com.pdfservice.application.pdf.DocxToPdfConverter
import com.pdfservice.application.pdf.PdfSignatureService
import com.pdfservice.domain.SignatureRepository
import com.pdfservice.infra.files.FilesClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetSignedPdfUseCase(
    private val signatureRepository: SignatureRepository,
    private val filesClient: FilesClient,
    private val pdfSignatureService: PdfSignatureService,
    @Qualifier("remoteDocxToPdfConverter")
    private val remoteDocxToPdfConverter: DocxToPdfConverter,
    @Qualifier("poiDocxToPdfConverter")
    private val poiDocxToPdfConverter: DocxToPdfConverter,
) {

    private val log = KotlinLogging.logger {}

    suspend fun execute(signatureId: UUID): SignedPdfDto {
        val signature = signatureRepository.findById(signatureId)
            ?: throw IllegalArgumentException("Signature $signatureId not found")

        log.info { "signature $signature \n" }

        val original = filesClient.downloadFile(signature.fileId)

        log.info { "original ${original.size} \n $original \n" }

        val pdf = poiDocxToPdfConverter.convert(original)

        log.info { "pdf ${pdf.size} \n $pdf \n" }

        val signedPdf = pdfSignatureService.applySignatureStampToPdf(pdf, signature)

        log.info { "signedPdf ${signedPdf.size} \n $signedPdf \n" }

        return SignedPdfDto(
            fileName = signature.fileName,
            pdf = signedPdf
        )
    }
}

