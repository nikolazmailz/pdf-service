package com.pdfservice.infra.pdf

import com.pdfservice.infra.pdf.dto.StampData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.awt.Color

@Service
class PdfStampService {

    suspend fun addStampToPdf(originalPdf: ByteArray, stampData: StampData): ByteArray =
        Loader.loadPDF(originalPdf).use { doc: PDDocument ->
            val stampPage = createStampPage(doc, stampData)
            doc.addPage(stampPage)

            ByteArrayOutputStream().use { out ->
                doc.save(out)
                out.toByteArray()
            }
        }

    // suspend можно оставить, чтобы вызывать из корутин, но внутри — обычный синхронный код
//    suspend fun addStampToPdf(originalPdf: ByteArray, stampData: StampData): ByteArray =
//        withContext(Dispatchers.IO) {
//            ByteArrayInputStream(originalPdf).use { docxInput ->
//                XWPFDocument(docxInput).use { xwpf ->
//                    PDDocument().use { pdf ->
//
//                        val stampPage = createStampPage(stampData)
//
//                        ByteArrayOutputStream().use { out ->
//                            pdf.save(out)
//                            out.toByteArray()
//                        }
//                    }
//                }
//            }
//        }

//        PDDocument.load(ByteArrayInputStream(originalPdf)).use { doc ->
//            val stampPage = createStampPage(stampData)
//            doc.addPage(stampPage)
//
//            ByteArrayOutputStream().use { out ->
//                doc.save(out)
//                out.toByteArray()
//            }
//        }

    // Тут suspend НЕ нужен, обычная синхронная функция
//    private fun createStampPage(pdf: PDDocument, stampData: StampData): PDPage {
//
//        val page = PDPage(PDRectangle.A4)
//        val mediaBox = page.mediaBox
//
//        val margin = 40f
//        val tableWidth = mediaBox.width - margin * 2
//        val tableLeftX = margin
//        var cursorY = mediaBox.height - margin
//
//        val col1Width = tableWidth * 0.28f
//        val col2Width = tableWidth * 0.47f
//        val col3Width = tableWidth * 0.25f // сейчас не используется, но оставил на будущее
//
//        val fontStream = this::class.java.getResourceAsStream("/fonts/DejaVuSans.ttf")
//            ?: error("Font /fonts/DejaVuSans.ttf not found in resources")
//
//        val font: PDFont = PDType0Font.load(pdf, fontStream, true)
////        val headerFont = PDType1Font.HELVETICA_BOLD
////        val textFont = PDType1Font.HELVETICA
//        val headerFontSize = 11f
//        val textFontSize = 10f
//        val leading = 1.3f * textFontSize
//        val cellPaddingV = 4f
//        val cellPaddingH = 4f
//
//        // "Временный" документ только ради рисования на странице
//        PDDocument().use { tmpDoc ->
//            tmpDoc.addPage(page)
//
//
//
//            PDPageContentStream(tmpDoc, page).use { cs ->
//                cs.setLineWidth(0.5f)
//                cs.setStrokingColor(Color(0, 87, 174))
//
//                // ===== 1. Внешняя рамка таблицы =====
//                cs.addRect(
//                    tableLeftX,
//                    margin,
//                    tableWidth,
//                    cursorY - margin
//                )
//                cs.stroke()
//
//                // ===== 2. Шапка =====
//                val headerText =
//                    "Документ ${stampData.documentId} подписан в системе ${stampData.systemName}"
//                val headerHeight = headerFontSize * 1.8f
//
//                drawCenteredText(
//                    cs,
//                    headerText,
//                    font,
//                    headerFontSize,
//                    tableLeftX,
//                    cursorY - headerHeight / 2,
//                    tableWidth
//                )
//
//                // линия под шапкой
//                cursorY -= headerHeight
//                cs.moveTo(tableLeftX, cursorY)
//                cs.lineTo(tableLeftX + tableWidth, cursorY)
//                cs.stroke()
//
//                // ===== 3. Заголовки колонок =====
//                cursorY -= cellPaddingV + textFontSize
//
//                cs.beginText()
//                cs.setFont(font, textFontSize)
//                cs.newLineAtOffset(tableLeftX + cellPaddingH, cursorY)
//                cs.showText("Подписант")
//                cs.endText()
//
//                cs.beginText()
//                cs.setFont(font, textFontSize)
//                cs.newLineAtOffset(tableLeftX + col1Width + cellPaddingH, cursorY)
//                cs.showText("Сертификат")
//                cs.endText()
//
//                cs.beginText()
//                cs.setFont(font, textFontSize)
//                cs.newLineAtOffset(tableLeftX + col1Width + col2Width + cellPaddingH, cursorY)
//                cs.showText("Дата и время подписания")
//                cs.endText()
//
//                // линия под заголовком
//                cursorY -= (cellPaddingV + textFontSize)
//                cs.moveTo(tableLeftX, cursorY)
//                cs.lineTo(tableLeftX + tableWidth, cursorY)
//                cs.stroke()
//
//                // ===== 4. Динамические блоки по подписантам =====
//                cs.setFont(font, textFontSize)
//
//                for ((index, signer) in stampData.signers.withIndex()) {
//                    val leftLines = signer.signerBlockLines
//                    val middleLines = signer.certificateLines
//                    val rightLines = signer.signingTimeLines
//
//                    val maxLines = maxOf(
//                        leftLines.size,
//                        middleLines.size,
//                        rightLines.size
//                    )
//
//                    val blockHeight = maxLines * leading + cellPaddingV * 2
//                    val startY = cursorY - cellPaddingV - textFontSize
//
//                    // Левый столбец
//                    drawLines(
//                        cs,
//                        leftLines,
//                        tableLeftX + cellPaddingH,
//                        startY,
//                        leading
//                    )
//
//                    // Средний
//                    drawLines(
//                        cs,
//                        middleLines,
//                        tableLeftX + col1Width + cellPaddingH,
//                        startY,
//                        leading
//                    )
//
//                    // Правый
//                    drawLines(
//                        cs,
//                        rightLines,
//                        tableLeftX + col1Width + col2Width + cellPaddingH,
//                        startY,
//                        leading
//                    )
//
//                    // Горизонтальная линия под блоком
//                    cursorY -= blockHeight
//                    cs.moveTo(tableLeftX, cursorY)
//                    cs.lineTo(tableLeftX + tableWidth, cursorY)
//                    cs.stroke()
//                }
//            }
//        }
//
//        return page
//    }

    private fun createStampPage(doc: PDDocument, stampData: StampData): PDPage {
        val page = PDPage(PDRectangle.A4)
        val mediaBox = page.mediaBox

        val margin = 40f
        val tableWidth = mediaBox.width - margin * 2
        val tableLeftX = margin
        var cursorY = mediaBox.height - margin

        val col1Width = tableWidth * 0.28f
        val col2Width = tableWidth * 0.47f
        val col3Width = tableWidth * 0.25f

        // грузим TTF-шрифт в ТОТ ЖЕ doc
        val fontStream = this::class.java.getResourceAsStream("/fonts/DejaVuSans.ttf")
            ?: error("Font /fonts/DejaVuSans.ttf not found in resources")

        val font = PDType0Font.load(doc, fontStream, true)

        val headerFont = font
        val textFont = font
        val headerFontSize = 11f
        val textFontSize = 10f
        val leading = 1.3f * textFontSize
        val cellPaddingV = 4f
        val cellPaddingH = 4f

        // ВАЖНО: рисуем через PDPageContentStream(doc, page), без tmpDoc
        PDPageContentStream(doc, page).use { cs ->
            cs.setLineWidth(0.5f)
            cs.setStrokingColor(Color(0, 87, 174))
            cs.setNonStrokingColor(Color.BLACK) // на всякий случай явно

            // ===== 1. Внешняя рамка =====
            cs.addRect(
                tableLeftX,
                margin,
                tableWidth,
                cursorY - margin
            )
            cs.stroke()

            // ===== 2. Шапка =====
            val headerText =
                "Документ ${stampData.documentId} подписан в системе ${stampData.systemName}"
            val headerHeight = headerFontSize * 1.8f

            drawCenteredText(
                cs,
                headerText,
                headerFont,
                headerFontSize,
                tableLeftX,
                cursorY - headerHeight / 2,
                tableWidth
            )

            cursorY -= headerHeight
            cs.moveTo(tableLeftX, cursorY)
            cs.lineTo(tableLeftX + tableWidth, cursorY)
            cs.stroke()

            // ===== 3. Заголовки колонок =====
            cursorY -= cellPaddingV + textFontSize

            cs.beginText()
            cs.setFont(headerFont, textFontSize)
            cs.newLineAtOffset(tableLeftX + cellPaddingH, cursorY)
            cs.showText("Подписант")
            cs.endText()

            cs.beginText()
            cs.setFont(headerFont, textFontSize)
            cs.newLineAtOffset(tableLeftX + col1Width + cellPaddingH, cursorY)
            cs.showText("Сертификат")
            cs.endText()

            cs.beginText()
            cs.setFont(headerFont, textFontSize)
            cs.newLineAtOffset(tableLeftX + col1Width + col2Width + cellPaddingH, cursorY)
            cs.showText("Дата и время подписания")
            cs.endText()

            cursorY -= (cellPaddingV + textFontSize)
            cs.moveTo(tableLeftX, cursorY)
            cs.lineTo(tableLeftX + tableWidth, cursorY)
            cs.stroke()

            // ===== 4. Подписанты =====
            cs.setFont(textFont, textFontSize)

            for (signer in stampData.signers) {
                val leftLines = signer.signerBlockLines
                val middleLines = signer.certificateLines
                val rightLines = signer.signingTimeLines

                val maxLines = maxOf(
                    leftLines.size,
                    middleLines.size,
                    rightLines.size
                )

                val blockHeight = maxLines * leading + cellPaddingV * 2
                val startY = cursorY - cellPaddingV - textFontSize

                drawLines(
                    cs,
                    leftLines,
                    tableLeftX + cellPaddingH,
                    startY,
                    leading
                )

                drawLines(
                    cs,
                    middleLines,
                    tableLeftX + col1Width + cellPaddingH,
                    startY,
                    leading
                )

                drawLines(
                    cs,
                    rightLines,
                    tableLeftX + col1Width + col2Width + cellPaddingH,
                    startY,
                    leading
                )

                cursorY -= blockHeight
                cs.moveTo(tableLeftX, cursorY)
                cs.lineTo(tableLeftX + tableWidth, cursorY)
                cs.stroke()
            }
        }

        return page
    }


    private fun drawLines(
        cs: PDPageContentStream,
        lines: List<String>,
        x: Float,
        startY: Float,
        leading: Float
    ) {
        var y = startY
        cs.beginText()
        cs.newLineAtOffset(x, y)
        for ((i, line) in lines.withIndex()) {
            if (i > 0) cs.newLineAtOffset(0f, -leading)
            cs.showText(line)
        }
        cs.endText()
    }

    private fun drawCenteredText(
        cs: PDPageContentStream,
        text: String,
        font: org.apache.pdfbox.pdmodel.font.PDFont,
        fontSize: Float,
        x: Float,
        centerY: Float,
        width: Float
    ) {
        val textWidth = font.getStringWidth(text) / 1000 * fontSize
        val textX = x + (width - textWidth) / 2
        val textY = centerY - fontSize / 2

        cs.beginText()
        cs.setFont(font, fontSize)
        cs.newLineAtOffset(textX, textY)
        cs.showText(text)
        cs.endText()
    }
}
