package com.signservice.controllers.dto

import java.util.UUID

data class SignatureCreationRequestDto(
    val signerName: String,
    val signerPosition: String,
    val signerOrganization: String,
    val certificateSerialNumber: String,
    val certificateValidFrom: String,
    val certificateValidTo: String,
    val isCertificateValidAtSigningTime: Boolean,
    val signedAt: String,
    val signatureBytesBase64: String,
    val signatureBase64: String?,
    val fileId: String,
    val fileName: String,
    val fileHash: String
)

data class SignatureCreatedResponseDto(
    val id: UUID
)

