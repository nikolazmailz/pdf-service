package com.pdfservice.domain

import java.util.UUID

interface SignatureRepository {
    suspend fun save(signature: Signature): Signature
    suspend fun findById(id: UUID): Signature?
}

