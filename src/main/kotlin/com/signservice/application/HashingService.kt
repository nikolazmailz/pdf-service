package com.signservice.application

interface HashingService {
    suspend fun calculateGOST3411_2012_256Hash(bytes: ByteArray): String
}

