package com.pdfservice.e2e

import com.pdfservice.controllers.dto.SignatureCreationRequestDto
import com.pdfservice.controllers.dto.SignatureCreatedResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.time.Instant
import java.util.Base64
import java.util.UUID

internal class SignatureSigE2ETest : BaseE2ETest() {

    @Test
    fun `should create signature and download sig`() {
        val fileName = "example.docx"

        val exampleContent = javaClass.getResourceAsStream("/wiremock/__files/example.docx")
            ?.use { it.readAllBytes() }
            ?: error("example.docx not found in resources")
        val fileHash = runBlocking { hashingService.calculateGostHash(exampleContent) }

        val signatureBytes = "signature-data".toByteArray()

        val response = webTestClient.post()
            .uri("/api/v1/signatures")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(
                createRequest(
                    fileId = fileId,
                    fileName = fileName,
                    fileHash = fileHash,
                    signatureBytes = signatureBytes
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(SignatureCreatedResponseDto::class.java)
            .returnResult()
            .responseBody!!

        val download = webTestClient.get()
            .uri("/api/v1/signatures/${response.id}/sig")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .expectBody()
            .returnResult()
            .responseBody!!

        assertArrayEquals(signatureBytes, download)
    }

    private fun createRequest(
        fileId: UUID,
        fileName: String,
        fileHash: String,
        signatureBytes: ByteArray
    ): SignatureCreationRequestDto {
        val now = Instant.now()
        return SignatureCreationRequestDto(
            signerName = "Иван Иванов",
            signerPosition = "Главный инженер",
            signerOrganization = "Acme",
            certificateSerialNumber = "serial-001",
            certificateValidFrom = now.minusSeconds(3600).toString(),
            certificateValidTo = now.plusSeconds(3600).toString(),
            isCertificateValidAtSigningTime = true,
            signedAt = now.toString(),
            signatureBytesBase64 = Base64.getEncoder().encodeToString(signatureBytes),
            signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes),
            fileId = fileId,
            fileName = fileName,
            fileHash = fileHash
        )
    }
}

