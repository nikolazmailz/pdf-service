package com.signservice.domain

import java.time.Instant
import java.util.UUID

data class Signature(
    val id: UUID,
    val signerName: String,
    val signerPosition: String,
    val signerOrganization: String,
    val certificateSerialNumber: String,
    val certificateValidFrom: Instant,
    val certificateValidTo: Instant,
    val isCertificateValidAtSigningTime: Boolean,
    val signedAt: Instant,
    val signatureBytes: ByteArray?,
    val signatureBase64: String?,
    val fileId: String,
    val fileName: String,
    val fileHash: String
)