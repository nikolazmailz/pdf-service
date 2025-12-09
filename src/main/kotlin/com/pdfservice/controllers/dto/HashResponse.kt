package com.pdfservice.controllers.dto

import java.util.UUID

data class HashResponse(
    val fileId: UUID,
    val hash: String
)

