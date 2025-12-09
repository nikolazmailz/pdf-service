package com.pdfservice.application

interface HashingService {
    suspend fun calculateGostHash(bytes: ByteArray): String
}

