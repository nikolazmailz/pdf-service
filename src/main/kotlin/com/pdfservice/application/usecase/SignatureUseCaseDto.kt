package com.pdfservice.application.usecase

import java.time.Instant
import java.util.UUID

data class FileHashDto(
    val fileId: UUID,
    val hash: String
)

data class CreateSignatureRequest(
    val signerName: String,
    val signerPosition: String,
    val signerOrganization: String,
    val certificateSerialNumber: String,
    val certificateValidFrom: Instant,
    val certificateValidTo: Instant,
    val isCertificateValidAtSigningTime: Boolean,
    val signedAt: Instant,
    val signatureBytes: ByteArray,
    val signatureBase64: String?,
    val fileId: UUID,
    val fileName: String,
    val fileHash: String
)

data class SignatureCreatedDto(val id: UUID)

data class SignatureFileDto(
    val fileId: UUID,
    val fileName: String,
    val bytes: ByteArray
)

data class SignedPdfDto(
    val fileName: String,
    val pdf: ByteArray
)

