package com.pdfservice.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${files.base-url:http://files-service/}")
    private val filesBaseUrl: String
) {

    @Bean
    fun filesServiceWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(filesBaseUrl)
            .build()
}

