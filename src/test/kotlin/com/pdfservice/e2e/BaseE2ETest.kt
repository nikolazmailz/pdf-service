package com.pdfservice.e2e

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.pdfservice.application.HashingService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
abstract class BaseE2ETest {

    companion object {
        private const val WIREMOCK_PORT = 8089
        private const val WIREMOCK_BASE_URL = "http://localhost:$WIREMOCK_PORT"

        val fileId = UUID.fromString("f953f787-e488-4a26-b79e-9db17dd441da")

        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("signature_service_test")
            withUsername("test_user")
            withPassword("test_password")
        }

        @Container
        @JvmStatic
        val converterContainer: GenericContainer<*> =
            GenericContainer("pdf-service:latest")
                .withExposedPorts(8080)
                .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(BaseE2ETest::class.java)))
                .waitingFor(Wait.forListeningPort())

        @DynamicPropertySource
        @JvmStatic
        fun dynamicProperties(registry: DynamicPropertyRegistry) {
            // гарантируем старт контейнера до чтения портов
            if (!postgres.isRunning) {
                postgres.start()
            }

            val r2dbcUrl =
                "r2dbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}"

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgres.username }
            registry.add("spring.r2dbc.password") { postgres.password }

            registry.add("spring.liquibase.url") { postgres.jdbcUrl }
            registry.add("spring.liquibase.user") { postgres.username }
            registry.add("spring.liquibase.password") { postgres.password }

            registry.add("files.base-url") { WIREMOCK_BASE_URL }
        }
    }

    // если хочешь автоподхват стаба из src/test/resources/wiremock,
    // можно добавить .usingFilesUnderClasspath("wiremock")
    protected val wireMockServer: WireMockServer = WireMockServer(
        wireMockConfig()
            .port(WIREMOCK_PORT)
         .usingFilesUnderClasspath("wiremock") // раскомментируй, если будешь хранить mappings/__files в ресурсах
    )

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var databaseClient: DatabaseClient

    @Autowired
    protected lateinit var hashingService: HashingService

    @BeforeAll
    fun startWireMock() {
        wireMockServer.start()
    }

    @AfterAll
    fun stopWireMock() {
        wireMockServer.stop()
    }

    @BeforeEach
    fun resetWireMock() {
        wireMockServer.resetAll()
    }

    @BeforeEach
    fun cleanDatabase() {
        runBlocking {
            databaseClient
                .sql("TRUNCATE signature CASCADE")
                .then()
                .awaitFirstOrNull()
        }
    }

//    protected fun stubFile(
//        fileId: String,
//        content: ByteArray,
//        contentType: String = "application/octet-stream",
//    ) {
//        wireMockServer.stubFor(
//            get(urlEqualTo("/files/$fileId"))
//                .willReturn(
//                    aResponse()
//                        .withHeader("Content-Type", contentType)
//                        .withBody(content),
//                ),
//        )
//    }
}
