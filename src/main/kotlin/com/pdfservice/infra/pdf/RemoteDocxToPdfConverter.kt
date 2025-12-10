package com.pdfservice.infra.pdf

import com.pdfservice.application.pdf.DocxToPdfConverter
import com.pdfservice.config.ConverterProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("remoteDocxToPdfConverter")
class RemoteDocxToPdfConverter(
    @Qualifier("documentConverterWebClient")
    private val webClient: WebClient,
    private val props: ConverterProperties
) : DocxToPdfConverter {

    private val log = KotlinLogging.logger {}

    override suspend fun convert(docx: ByteArray): ByteArray {
        log.info { "docx ${docx.size} is \n $docx \n" }
        return webClient.post()
            .uri("/convert/docx-to-pdf")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(docx)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .awaitSingle()
    }
}
