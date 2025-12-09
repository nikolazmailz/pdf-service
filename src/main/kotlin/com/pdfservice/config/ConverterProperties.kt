package com.pdfservice.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "converter")
data class ConverterProperties(
    val baseUrl: String,
    val connectTimeoutMs: Int = 5_000,
    val readTimeoutMs: Long = 60_000
)
