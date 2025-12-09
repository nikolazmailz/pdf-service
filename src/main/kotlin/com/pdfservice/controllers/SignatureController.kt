package com.pdfservice.controllers

import com.pdfservice.application.usecase.CreateSignatureUseCase
import com.pdfservice.application.usecase.GetSignatureFileUseCase
import com.pdfservice.application.usecase.CreateSignatureRequest
import com.pdfservice.application.usecase.GetSignedPdfUseCase
import com.pdfservice.controllers.dto.SignatureCreationRequestDto
import com.pdfservice.controllers.dto.SignatureCreatedResponseDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.Base64
import java.util.UUID

@RestController
@RequestMapping("/api/v1/signatures")
class SignatureController(
    private val createSignatureUseCase: CreateSignatureUseCase,
    private val getSignatureFileUseCase: GetSignatureFileUseCase,
    private val getSignedPdfUseCase: GetSignedPdfUseCase
) {

    @PostMapping
    suspend fun create(@RequestBody request: SignatureCreationRequestDto): SignatureCreatedResponseDto {
        val createRequest = request.toUseCaseRequest()
        val result = createSignatureUseCase.execute(createRequest)
        return SignatureCreatedResponseDto(id = result.id)
    }

    @GetMapping("/{id}/sig")
    suspend fun downloadSignature(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val signatureFile = getSignatureFileUseCase.execute(id)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${signatureFile.fileName}\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(signatureFile.bytes)
    }

    private fun SignatureCreationRequestDto.toUseCaseRequest(): CreateSignatureRequest =
        CreateSignatureRequest(
            signerName = signerName,
            signerPosition = signerPosition,
            signerOrganization = signerOrganization,
            certificateSerialNumber = certificateSerialNumber,
            certificateValidFrom = Instant.parse(certificateValidFrom),
            certificateValidTo = Instant.parse(certificateValidTo),
            isCertificateValidAtSigningTime = isCertificateValidAtSigningTime,
            signedAt = Instant.parse(signedAt),
            signatureBytes = Base64.getDecoder().decode(signatureBytesBase64),
            signatureBase64 = signatureBase64,
            fileId = fileId,
            fileName = fileName,
            fileHash = fileHash
        )

    @GetMapping("/{id}/pdf")
    suspend fun getSignedPdf(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val signedPdf = getSignedPdfUseCase.execute(id)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${signedPdf.fileName}\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(signedPdf.pdf)
    }
}

