package com.pdfservice.infra.files

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID

@Component
class FilesClient(
    @Qualifier("filesServiceWebClient")
    private val webClient: WebClient
) {

    private val log = KotlinLogging.logger {}

//    suspend fun downloadFile(fileId: UUID): ByteArray =
//        webClient.get()
//            .uri("/files/{id}", fileId)
//            .retrieve()
//            .awaitBody()

    suspend fun downloadFile(fileId: UUID): ByteArray {
        log.info { "downloadFile $fileId" }
        val resourcePath = "/example_big.docx"

        val resource = this::class.java.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Fake file not found: $resourcePath")

        return resource.use { it.readBytes() }
    }
}

