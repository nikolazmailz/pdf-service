//package com.pdfservice.e2e
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.pdfservice.controllers.dto.SignatureCreationRequestDto
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.core.io.ClassPathResource
//import org.springframework.http.MediaType
//import org.springframework.web.reactive.function.client.WebClient
//import org.testcontainers.containers.GenericContainer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
//import com.fasterxml.jackson.module.kotlin.readValue
//import com.fasterxml.jackson.module.kotlin.registerKotlinModule
//import com.pdfservice.controllers.dto.SignatureCreatedResponseDto
//import kotlinx.coroutines.runBlocking
//import org.slf4j.LoggerFactory
//import org.springframework.web.reactive.function.client.awaitBody
//import org.testcontainers.containers.output.Slf4jLogConsumer
//import org.testcontainers.containers.wait.strategy.Wait
//
////@Testcontainers
////@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class SystemDocumentConverterTest: BaseE2ETest() {
//
//    private val log = LoggerFactory.getLogger(SystemDocumentConverterTest::class.java)
//
////    @Autowired
//    private val objectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()
//
//    companion object {
////        @Container
////        @JvmStatic
////        val converterContainer: GenericContainer<*> =
////            GenericContainer("pdf-service:latest")
////                .withExposedPorts(8080)
//////                .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(SystemDocumentConverterTest::class.java)))
////                .waitingFor(Wait.forListeningPort())
//    }
//
//    @Test
//    fun `should convert real docx to pdf via docker container`() = runBlocking {
//        // 1. Собираем WebClient до контейнера
//        val host = converterContainer.host
//        val port = converterContainer.getMappedPort(8080)
//
//        val webClient = WebClient.builder()
//            .baseUrl("http://$host:$port")
//            .build()
//
//
//        // Собираем запрос на создание записи о подписи
//        val json = javaClass.classLoader.getResourceAsStream("data/SignatureCreationRequestDto.json")
//            ?.bufferedReader(Charsets.UTF_8)
//            ?.use { it.readText() } ?: throw RuntimeException()
//
//        val signatureCreationRequestDto = objectMapper.readValue<SignatureCreationRequestDto>(json)
//
//        val signatureCreatedResponseDto = webClient.post()
//            .uri("/api/v1/signatures")
//            .contentType(MediaType.APPLICATION_JSON)
//            .bodyValue(signatureCreationRequestDto)
//            .retrieve()
//            .awaitBody<SignatureCreatedResponseDto>()
//
//        println(signatureCreatedResponseDto)
//
//        assertTrue { signatureCreatedResponseDto.id != null }
//
////            .block() ?: error("No response from service")
//
//
//        // 1. Берём тестовый DOCX из ресурсов
////        val resource = ClassPathResource("example.docx")
////        val docxBytes = resource.inputStream.use { it.readBytes() }
//
//
//        // 3. POST -> получаем status + headers + body
////        data class HttpResult(
////            val status: Int,
////            val contentType: String?,
////            val body: ByteArray
////        )
//
////        val result = webClient.post()
////            .uri("/convert/docx-to-pdf")
////            .contentType(MediaType.APPLICATION_OCTET_STREAM)
////            .bodyValue(docxBytes)
////            .exchangeToMono { response ->
////                val status = response.statusCode()
////                val contentType = response.headers().asHttpHeaders().contentType?.toString()
////
////                response.bodyToMono(ByteArray::class.java)
////                    // на случай, если сервис вернёт пустое тело (чтобы не получить null)
////                    .defaultIfEmpty(ByteArray(0))
////                    .map { body -> HttpResult(status.value(), contentType, body) }
////            }
////            .block() ?: error("No response from service")
//
//        // 4. Проверки
////        assertEquals(200, result.status, "Expected 200 OK from document-converter-service")
//
////        assertTrue(result.contentType?.contains("application/pdf") == true) {
////            "Expected Content-Type to contain application/pdf but was: ${result.contentType}"
////        }
//
////        val pdfBytes = result.body
////        assertTrue(pdfBytes.isNotEmpty(), "PDF response is empty")
//
////        val prefix = pdfBytes.take(4).toByteArray()
////        assertTrue(prefix.contentEquals("%PDF".toByteArray())) {
////            "Response does not look like a PDF, prefix: ${prefix.decodeToString()}"
////        }
//    }
//}
