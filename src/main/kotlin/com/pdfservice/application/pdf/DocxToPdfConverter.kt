package com.pdfservice.application.pdf

interface DocxToPdfConverter {
    suspend fun convert(docx: ByteArray): ByteArray
}
