package com.signservice.application.service

import com.signservice.domain.Signature

interface PdfSignatureService {
    suspend fun applySignatureStampToPdf(original: ByteArray, signature: Signature): ByteArray
}

