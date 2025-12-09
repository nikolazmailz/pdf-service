//package com.signservice.infra.pdf
//
//import com.signservice.application.service.PdfSignatureService
//import com.signservice.domain.Signature
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.pdmodel.PDPageContentStream
//import org.apache.pdfbox.pdmodel.common.PDRectangle
//import org.apache.pdfbox.pdmodel.font.PDType1Font
//import org.springframework.stereotype.Component
//import java.awt.Color
//import java.io.ByteArrayInputStream
//import java.io.ByteArrayOutputStream
//import java.time.format.DateTimeFormatter
//
//@Component
//class PdfSignatureServiceImpl : PdfSignatureService {
//
//    private val textFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
//
//    override suspend fun applySignatureStampToPdf(original: ByteArray, signature: Signature): ByteArray {
//        // TODO: Consider running PDFBox work on an IO dispatcher if this becomes a bottleneck.
//        ByteArrayInputStream(original).use { input ->
//            PDDocument.load(input).use { document ->
//                if (document.numberOfPages == 0) {
//                    return original
//                }
//
//                val page = document.getPage(0)
//                val mediaBox = page.mediaBox ?: PDRectangle.A4
//                val stampWidth = 240f
//                val stampHeight = 90f
//                val stampMargin = 40f
//                val stampX = stampMargin
//                val stampY = stampMargin
//
//                PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { content ->
//                    content.setNonStrokingColor(Color(230, 230, 240))
//                    content.addRect(stampX, stampY, stampWidth, stampHeight)
//                    content.fill()
//
//                    content.beginText()
//                    content.setFont(PDType1Font.HELVETICA_BOLD, 11f)
//                    content.setNonStrokingColor(Color.BLACK)
//                    content.newLineAtOffset(stampX + 8f, stampY + stampHeight - 16f)
//
//                    val lines = listOf(
//                        "Подписант: ${signature.signerName}",
//                        "Должность: ${signature.signerPosition}",
//                        "Организация: ${signature.signerOrganization}",
//                        "Сертификат: ${signature.certificateSerialNumber}",
//                        "Подписано: ${textFormatter.format(signature.signedAt)}",
//                        "Сертификат валиден: ${signature.isCertificateValidAtSigningTime}"
//                    )
//
//                    lines.forEach { line ->
//                        content.showText(line)
//                        content.newLineAtOffset(0f, -12f)
//                    }
//
//                    content.endText()
//                }
//
//                val output = ByteArrayOutputStream()
//                document.save(output)
//                return output.toByteArray()
//            }
//        }
//    }
//}
//
