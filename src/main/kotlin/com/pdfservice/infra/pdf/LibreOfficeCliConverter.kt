package com.pdfservice.infra.pdf

import com.pdfservice.application.pdf.DocxToPdfConverter
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.util.Comparator
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis
import kotlin.use

@Component("libreOfficeCliConverter")
class LibreOfficeCliConverter: DocxToPdfConverter {

    private val log = KotlinLogging.logger {}

    override suspend fun convert(docx: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        log.info { "convertDocxToPdf ${docx.size} \n $docx \n "}
        val tempDir = Files.createTempDirectory("libreoffice-converter")
        val outputDir = Files.createDirectories(tempDir.resolve("output"))
        val inputFile = tempDir.resolve("input.docx")

        var pdfBytes: ByteArray? = null
        val convertDuration = measureTimeMillis {
            try {
                Files.write(inputFile, docx)

                val process = ProcessBuilder(
                    "soffice",
                    "--headless",
                    "--nologo",
                    "--convert-to",
                    "pdf",
                    "--outdir",
                    outputDir.toAbsolutePath().toString(),
                    inputFile.toAbsolutePath().toString()
                ).redirectErrorStream(true).start()

                log.info { "process $process \n"}
                log.info { "process.info() ${process.info()} \n"}

                val finished = process.waitFor(60, TimeUnit.SECONDS)
                if (!finished) {
                    process.destroyForcibly()
                    throw IllegalStateException("LibreOffice conversion timeout")
                }

                if (process.exitValue() != 0) {
                    val output = process.inputStream.bufferedReader().use { it.readText() }
                    log.error { "output $output \n"}
                    throw IllegalStateException("LibreOffice conversion failed: $output")
                }

                val pdfFile = Files.list(outputDir)
                    .filter { path -> path.fileName.toString().endsWith(".pdf", ignoreCase = true) }
                    .findFirst()
                    .orElseThrow {
                        log.error { "No PDF file generated \n"}
                        IllegalStateException("No PDF file generated") }
                    .toFile()

                pdfBytes = Files.readAllBytes(pdfFile.toPath())
            } finally {
                Files.walk(tempDir).use { paths ->
                    paths.sorted(Comparator.reverseOrder())
                        .forEach { path ->
                            Files.deleteIfExists(path)
                        }
                }
            }
        }
        log.info { "libreoffice convert finished in ${convertDuration}ms" }
        checkNotNull(pdfBytes) { "PDF conversion did not produce a result" }
    }
}
