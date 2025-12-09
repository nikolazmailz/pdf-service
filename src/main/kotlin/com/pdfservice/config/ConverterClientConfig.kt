package com.pdfservice.config

import io.netty.channel.ChannelOption
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import io.netty.handler.timeout.ReadTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import java.util.concurrent.TimeUnit

@Configuration
@EnableConfigurationProperties(ConverterProperties::class)
class ConverterClientConfig {

    @Bean
    fun documentConverterWebClient(props: ConverterProperties): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(props.readTimeoutMs))
            .doOnConnected { conn ->
                conn.addHandlerLast(
                    ReadTimeoutHandler(props.readTimeoutMs, TimeUnit.MILLISECONDS)
                )
            }

        return WebClient.builder()
            .baseUrl(props.baseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}