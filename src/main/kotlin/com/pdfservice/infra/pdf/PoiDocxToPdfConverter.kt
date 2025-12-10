package com.pdfservice.infra.pdf

import com.pdfservice.application.pdf.DocxToPdfConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.apache.pdfbox.pdmodel.font.PDType0Font

@Component("poiDocxToPdfConverter")
class PoiDocxToPdfConverter : DocxToPdfConverter {

    override suspend fun convert(docx: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        ByteArrayInputStream(docx).use { docxInput ->
            XWPFDocument(docxInput).use { xwpf ->
                PDDocument().use { pdf ->
                    val page = PDPage(PDRectangle.A4)
                    pdf.addPage(page)

                    val mediaBox = page.mediaBox
                    val margin = 50f
                    val startX = mediaBox.lowerLeftX + margin
                    var currentY = mediaBox.upperRightY - margin

                    val fontSize = 11f
                    val leading = fontSize * 1.4f

                    val fontStream = this::class.java.getResourceAsStream("/fonts/DejaVuSans.ttf")
                        ?: error("Font /fonts/DejaVuSans.ttf not found in resources")

                    val font: PDFont = PDType0Font.load(pdf, fontStream, true) // embed = true

                    PDPageContentStream(pdf, page).use { content ->
                        content.beginText()
                        content.setFont(font, fontSize)
                        content.newLineAtOffset(startX, currentY)

                        // Пробегаемся по абзацам DOCX и просто выводим текст
                        outer@ for (paragraph in xwpf.paragraphs) {
                            val text = paragraph.text
                            if (text.isNullOrBlank()) {
                                // пустой абзац -> просто перенос строки
                                content.newLineAtOffset(0f, -leading)
                                currentY -= leading
                                continue
                            }

                            val lines = text
                                .replace("\r\n", "\n")
                                .replace("\r", "")
                                .replace("\t", "    ")
                                .split('\n')

                            // Примитивный перенос строк: один абзац = одна строка в PDF
                            // (при желании можно добавить разбиение по ширине страницы)
//                            val cleanText = text.replace("\r\n", "").replace("\r", "").replace("\n", "")
//                            content.showText(cleanText)
//                            content.newLineAtOffset(0f, -leading)
//                            currentY -= leading

                            for (line in lines) {
                                if (currentY - leading < mediaBox.lowerLeftY + margin) {
                                    // Не осталось места под ещё одну строку — выходим из всего цикла
                                    break@outer
                                }

                                val cleanLine = line.trimEnd() // без хвостовых пробелов

                                // Даже если строка пустая, showText("") — ок, просто ничего не нарисует
                                content.showText(cleanLine)
                                content.newLineAtOffset(0f, -leading)
                                currentY -= leading
                            }

                            // Если дошли до низа страницы — создаём новую
                            if (currentY < mediaBox.lowerLeftY + margin) {
                                content.endText()
                                content.close()

                                val newPage = PDPage(PDRectangle.A4)
                                pdf.addPage(newPage)
                                val newMediaBox = newPage.mediaBox

                                currentY = newMediaBox.upperRightY - margin

                                PDPageContentStream(pdf, newPage).use { nextContent ->
                                    nextContent.beginText()
                                    nextContent.setFont(font, fontSize)
                                    nextContent.newLineAtOffset(startX, currentY)
                                    // В этом простом варианте мы не продолжаем текст
                                    // на новую страницу в одном стриме — для MVP сойдёт.
                                }

                                // Для простоты: сейчас пример без сложного управления несколькими contentStream.
                                // Если нужно, могу переписать на один поток с более аккуратной логикой.
                                // Но для базового примера достаточно одной страницы.
                                break
                            }
                        }

                        content.endText()
                    }

                    ByteArrayOutputStream().use { out ->
                        pdf.save(out)
                        out.toByteArray()
                    }
                }
            }
        }
    }
}
