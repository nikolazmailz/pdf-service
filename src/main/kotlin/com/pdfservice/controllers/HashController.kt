package com.pdfservice.controllers

import com.pdfservice.application.usecase.GetFileHashUseCase
import com.pdfservice.controllers.dto.HashResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class HashController(
    private val getFileHashUseCase: GetFileHashUseCase
) {

    @GetMapping("/hash")
    suspend fun getHash(@RequestParam fileId: UUID): HashResponse {
        val result = getFileHashUseCase.execute(fileId)
        return HashResponse(fileId = result.fileId, hash = result.hash)
    }
}

