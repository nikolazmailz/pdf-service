package com.pdfservice.infra.pdf

import com.pdfservice.application.pdf.DocxToPdfConverter
import com.pdfservice.config.ConverterProperties
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

    override suspend fun convert(docx: ByteArray): ByteArray {
        return webClient.post()
            .uri("/convert/docx-to-pdf")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .accept(MediaType.APPLICATION_PDF)
            .bodyValue(docx)
            .retrieve()
            .bodyToMono(ByteArray::class.java)
            .awaitSingle()
    }
}