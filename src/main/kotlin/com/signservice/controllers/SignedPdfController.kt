package com.signservice.controllers

import com.signservice.application.usecase.GetSignedPdfUseCase
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/signatures")
class SignedPdfController(
    private val getSignedPdfUseCase: GetSignedPdfUseCase
) {

    @GetMapping("/{id}/pdf")
    suspend fun getSignedPdf(@PathVariable id: UUID): ResponseEntity<ByteArray> {
        val signedPdf = getSignedPdfUseCase.execute(id)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${signedPdf.fileName}\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(signedPdf.pdf)
    }
}

