package com.pdfservice.infra.pdf

import com.pdfservice.application.pdf.PdfSignatureService
import com.pdfservice.domain.Signature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.stereotype.Component
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@Component
class PdfBoxSignatureService : PdfSignatureService {

    private val textFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override suspend fun applySignatureStampToPdf(
        original: ByteArray,
        signature: Signature
    ): ByteArray = withContext(Dispatchers.IO) {

        // PDFBox 3.x: грузим через Loader, прямо из ByteArray
        Loader.loadPDF(original).use { document ->
            if (document.numberOfPages == 0) {
                // Нечего штамповать – возвращаем оригинал
                return@withContext original
            }

            val fontStream = this::class.java.getResourceAsStream("/fonts/DejaVuSans.ttf")
                ?: error("Font /fonts/DejaVuSans.ttf not found in resources")

            val font: PDFont = PDType0Font.load(document, fontStream, true) // embed = true


            val page = document.getPage(0)
            val mediaBox: PDRectangle = page.mediaBox ?: PDRectangle.A4

            val stampWidth = 240f
            val stampHeight = 90f
            val stampMargin = 40f

            // Привязываемся к нижнему левому углу страницы
            val stampX = mediaBox.lowerLeftX + stampMargin
            val stampY = mediaBox.lowerLeftY + stampMargin

            // В 3.x этот конструктор PDPageContentStream по-прежнему есть
            PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,  // compress
                true   // resetContext
            ).use { content ->
                // Фон штампа
                content.setNonStrokingColor(Color(230, 230, 240))
                content.addRect(stampX, stampY, stampWidth, stampHeight)
                content.fill()

                // Текст штампа
                content.beginText()
                content.setFont(font, 11f)
                content.setNonStrokingColor(Color.BLACK)
                content.newLineAtOffset(stampX + 8f, stampY + stampHeight - 16f)

                val textFormatter = DateTimeFormatter
                    .ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault())

                val signedAtText = signature.signedAt
                    ?.let { textFormatter.format(it) }
                    ?: "—"

                val lines = listOf(
                    "Подписант: ${signature.signerName ?: "—"}",
                    "Должность: ${signature.signerPosition ?: "—"}",
                    "Организация: ${signature.signerOrganization ?: "—"}",
                    "Сертификат: ${signature.certificateSerialNumber ?: "—"}",
                    "Подписано: $signedAtText",
                    "Сертификат валиден: ${signature.isCertificateValidAtSigningTime ?: false}"
                )

                lines.forEach { line ->
                    content.showText(line)
                    content.newLineAtOffset(0f, -12f)
                }

                content.endText()
            }

            ByteArrayOutputStream().use { output ->
                document.save(output)
                output.toByteArray()
            }
        }
    }
}
