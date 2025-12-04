package com.signservice.controllers

import com.signservice.application.usecase.GetFileHashUseCase
import com.signservice.controllers.dto.HashResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class HashController(
    private val getFileHashUseCase: GetFileHashUseCase
) {

    @GetMapping("/hash")
    suspend fun getHash(@RequestParam fileId: String): HashResponse {
        val result = getFileHashUseCase.execute(fileId)
        return HashResponse(fileId = result.fileId, hash = result.hash)
    }
}

