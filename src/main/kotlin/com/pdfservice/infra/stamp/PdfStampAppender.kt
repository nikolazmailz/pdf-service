package com.pdfservice.infra.stamp

import com.pdfservice.infra.pdf.dto.StampData
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class PdfStampAppender(
    private val stampTemplateRenderer: StampTemplateRenderer,
    private val htmlToPdfConverter: HtmlToPdfConverter,
    private val pdfToPngConverter: PdfToPngConverter,
) {

    private val STAMP_DPI = 144f

    /**
     * Добавляет штамп (сгенерированный по StampData) на последнюю страницу PDF.
     *
     * @param originalPdf исходный PDF в виде ByteArray
     * @param stampData   данные для шаблона штампа
     * @return новый PDF с добавленным штампом как ByteArray
     */
    fun addStampPage(originalPdf: ByteArray, stampData: StampData): ByteArray {
        // 1. генерим HTML по шаблону
        val htmlTemplate = stampTemplateRenderer.getHtmlTemplate(stampData)

        // 2. HTML → PDF (штамп как отдельный документ, обычно одна страница)
        val stampPdfBytes: ByteArray = htmlToPdfConverter.convert(htmlTemplate)

        // 3. Берём первую страницу этого PDF и конвертим её в PNG
        val stampPngBytes: ByteArray =
            pdfToPngConverter.convertFirstPageToPng(stampPdfBytes, dpi = 144f)

        // 4. Встраиваем PNG как картинку в конец исходного PDF
        return addSignatureImageToPdf(originalPdf, stampPngBytes)
    }

    fun addSignatureImageToPdf(
        inputPdf: ByteArray,
        signaturePng: ByteArray
    ): ByteArray {
        Loader.loadPDF(inputPdf).use { doc: PDDocument ->
            val lastPageIndex = doc.numberOfPages - 1
            val lastPage: PDPage = doc.getPage(lastPageIndex)
            val mediaBox: PDRectangle = lastPage.mediaBox

            val pageBottomY = mediaBox.lowerLeftY
            val pageTopY = mediaBox.upperRightY
            val pageHeightPt = pageTopY - pageBottomY

            // 1. Загружаем картинку штампа
            val img = PDImageXObject.createFromByteArray(doc, signaturePng, "stamp")

            val imgWidthPx = img.width.toFloat()
            val imgHeightPx = img.height.toFloat()

            // Переводим из пикселей в поинты с учётом dpi
            val imgWidthPt = imgWidthPx * 72f / STAMP_DPI
            val imgHeightPt = imgHeightPx * 72f / STAMP_DPI

            // Хотим почти всю ширину страницы
            val horizontalMargin = 36f   // 0.5"
            val bottomMargin = 36f       // отступ от низа
            val topMargin = 36f          // отступ сверху на новой странице
            val gapTextStamp = 12f       // зазор между текстом и штампом

            val availableWidth = mediaBox.width - horizontalMargin * 2
            val scale = availableWidth / imgWidthPt

            val targetWidthPt = availableWidth
            val targetHeightPt = imgHeightPt * scale

            // 2. Ищем, где заканчивается текст на последней странице
            val lowestTextYOnPage: Float? = findLowestTextBaselinePdfCoords(
                doc = doc,
                pageIndex = lastPageIndex,
                pageHeight = pageHeightPt
            )

            val (targetPage, x, y) = if (lowestTextYOnPage == null) {
                // ТЕКСТА НЕТ — считаем страницу пустой → ставим внизу
                val xStamp = pageBottomY + horizontalMargin
                val yStamp = pageBottomY + bottomMargin
                Triple(lastPage, xStamp, yStamp)
            } else {
                // Текст есть. lowestTextYOnPage — pdf-координата БЛИЖЕ всего к низу страницы (baseline текста)
                val yStampCandidate = lowestTextYOnPage - gapTextStamp - targetHeightPt

                val minYAllowed = pageBottomY + bottomMargin

                if (yStampCandidate >= minYAllowed) {
                    // Штамп помещается под текстом на этой странице
                    val xStamp = pageBottomY + horizontalMargin
                    Triple(lastPage, xStamp, yStampCandidate)
                } else {
                    // Не влезает — создаём новую страницу и ставим штамп сверху
                    val newPage = PDPage(mediaBox)
                    doc.addPage(newPage)

                    val xStamp = pageBottomY + horizontalMargin
                    val yStamp = pageTopY - topMargin - targetHeightPt
                    Triple(newPage, xStamp, yStamp)
                }
            }

            // 3. Рисуем штамп
            PDPageContentStream(
                doc,
                targetPage,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            ).use { cs ->
                cs.drawImage(img, x, y, targetWidthPt, targetHeightPt)
            }

            // 4. Сохраняем результат
            val baos = ByteArrayOutputStream()
            doc.save(baos)
            return baos.toByteArray()
        }
    }

    /**
     * Находит baseline самого нижнего текста на странице в pdf-координатах (origin снизу).
     * Возвращает null, если текста на странице нет.
     */
    private fun findLowestTextBaselinePdfCoords(
        doc: PDDocument,
        pageIndex: Int,
        pageHeight: Float
    ): Float? {
        val stripper = object : PDFTextStripper() {
            var lowestPdfY: Float = Float.MAX_VALUE

            init {
                startPage = pageIndex + 1 // PDFBox страницы с 1
                endPage = pageIndex + 1
            }

            override fun processTextPosition(text: TextPosition) {
                // yDirAdj — координата в системе "origin сверху"
                val yDir = text.yDirAdj.toFloat()

                // конвертим в pdf-координаты (origin снизу)
                val pdfY = pageHeight - yDir

                if (pdfY < lowestPdfY) {
                    lowestPdfY = pdfY
                }
                super.processTextPosition(text)
            }
        }

        stripper.getText(doc) // запускает обход текста

        return if (stripper.lowestPdfY == Float.MAX_VALUE) {
            null // текста нет
        } else {
            stripper.lowestPdfY
        }
    }
}