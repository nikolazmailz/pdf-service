package com.signservice.application

interface HashingService {
    suspend fun calculateGostHash(bytes: ByteArray): String
}

