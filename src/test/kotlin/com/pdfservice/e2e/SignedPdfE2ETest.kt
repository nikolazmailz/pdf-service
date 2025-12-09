package com.pdfservice.e2e

import com.pdfservice.controllers.dto.SignatureCreationRequestDto
import com.pdfservice.controllers.dto.SignatureCreatedResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.util.Base64
import java.util.UUID

internal class SignedPdfE2ETest : BaseE2ETest() {

    @Test
    fun `should return signed pdf`() {
        val fileName = "example.docx"
        val pdfContent = javaClass.getResourceAsStream("/wiremock/__files/example.docx")
            ?.use { it.readAllBytes() }
            ?: error("example.docx not found in resources")
//        stubFile(fileId, pdfContent, "application/pdf")

        val signatureBytes = "pdf-signature".toByteArray()
        val fileHash = runBlocking { hashingService.calculateGostHash(pdfContent) }

        val created = webTestClient.post()
            .uri("/api/v1/signatures")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest(fileId, fileName, fileHash, signatureBytes))
            .exchange()
            .expectStatus().isOk
            .expectBody(SignatureCreatedResponseDto::class.java)
            .returnResult()
            .responseBody!!

        val result = webTestClient.get()
            .uri("/api/v1/signatures/${created.id}/pdf")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_PDF)
            .expectHeader().valueEquals(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
            .expectBody()
            .returnResult()
            .responseBody!!

        assertTrue(result.size >= 4)
        val pdfPrefix = result.copyOfRange(0, 4)
        assertTrue(pdfPrefix.contentEquals("%PDF".toByteArray()))

        val outputDir = Paths.get("build", "test-pdfs")
        Files.createDirectories(outputDir)

        val outputFileName = fileName.replace(".docx", ".pdf")
        val outputPath = outputDir.resolve(outputFileName)

        Files.write(
            outputPath,
            result,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        )
        println("Saved signed PDF to: $outputPath")
    }

    private fun createRequest(
        fileId: UUID,
        fileName: String,
        fileHash: String,
        signatureBytes: ByteArray
    ): SignatureCreationRequestDto {
        val now = Instant.now()
        return SignatureCreationRequestDto(
            signerName = "Elena",
            signerPosition = "Head of Compliance",
            signerOrganization = "Acme",
            certificateSerialNumber = "cert-123",
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

