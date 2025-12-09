package com.pdfservice.application.pdf

import com.pdfservice.domain.Signature

interface PdfSignatureService {
    suspend fun applySignatureStampToPdf(original: ByteArray, signature: Signature): ByteArray
}

