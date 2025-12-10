//package com.pdfservice.infra.render_html.x
//
//import com.pdfservice.infra.pdf.dto.StampData
//import com.pdfservice.infra.render_html.HtmlToPdfConverter
//import com.pdfservice.infra.render_html.StampTemplateRenderer
//import org.apache.pdfbox.Loader
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.pdmodel.PDPage
//import org.apache.pdfbox.pdmodel.common.PDRectangle
//import org.apache.pdfbox.pdmodel.PDPageContentStream
//import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
//import org.apache.pdfbox.pdmodel.font.PDType1Font
//import java.awt.Color
//import java.io.ByteArrayOutputStream
//
//class PdfStampAppender(
//    private val templateRenderer: StampTemplateRenderer,
//    private val htmlToPdfConverter: HtmlToPdfConverter
//) {
//
//    // те же параметры, что и в html-шаблоне
//    private val margin = 20f          // в HTML у нас margin: 20pt
//    private val headerFont = PDType1Font.HELVETICA_BOLD
//    private val textFont = PDType1Font.HELVETICA
//    private val headerFontSize = 11f
//    private val textFontSize = 10f
//    private val leading = 1.3f * textFontSize
//    private val cellPaddingV = 4f
//    private val cellPaddingH = 6f
//    private val separatorHeight = 4f
//
//    // верх свободной зоны снизу последней страницы (подстрой под свой docx-шаблон)
//    // ниже этой линии предполагается, что текста документа уже нет.
//    private val reservedAreaTopY = 220f
//    private val bottomMargin = margin
//
//    /**
//     * "Умное" добавление штампа:
//     * - если помещается внизу последней страницы — рисуем там;
//     * - иначе генерим отдельную страницу (HTML → PDF) и приклеиваем её.
//     */
//    fun addStampSmart(originalPdf: ByteArray, stampData: StampData): ByteArray {
//        val mainDoc: PDDocument = Loader.loadPDF(originalPdf)
//
//        try {
//            if (mainDoc.numberOfPages == 0) {
//                // пустой документ — просто вернём как есть
//                return originalPdf
//            }
//
//            val lastPageIndex = mainDoc.numberOfPages - 1
//            val lastPage: PDPage = mainDoc.getPage(lastPageIndex)
//            val mediaBox: PDRectangle = lastPage.mediaBox
//
//            val stampHeight = calculateStampHeight(stampData)
//
//            val freeHeightOnLastPage = reservedAreaTopY - bottomMargin
//
//            return if (stampHeight <= freeHeightOnLastPage) {
//                // 1) влезает — рисуем штамп внизу последней страницы
//                val stampTopY = bottomMargin + stampHeight
//                drawStampOnPage(
//                    doc = mainDoc,
//                    page = lastPage,
//                    mediaBox = mediaBox,
//                    startX = margin,
//                    topY = stampTopY,
//                    stampHeight = stampHeight,
//                    stampData = stampData
//                )
//
//                ByteArrayOutputStream().use { out ->
//                    mainDoc.save(out)
//                    out.toByteArray()
//                }
//            } else {
//                // 2) не влезает — fallback: HTML → PDF → новая страница
//                val withStamp = appendStampAsNewPage(mainDoc, stampData)
//                withStamp
//            }
//        } finally {
//            mainDoc.close()
//        }
//    }
//
//    /**
//     * Старый сценарий: сгенерить штамп отдельным PDF и приклеить как страницу в конец.
//     * mainDoc уже открыт и будет закрыт в addStampSmart.
//     */
//    private fun appendStampAsNewPage(mainDoc: PDDocument, stampData: StampData): ByteArray {
//        val html = templateRenderer.renderStampHtml(stampData)
//        val stampPdfBytes = htmlToPdfConverter.convert(html)
//
//        val stampDoc: PDDocument = Loader.loadPDF(stampPdfBytes)
//        try {
//            for (page in stampDoc.pages) {
//                mainDoc.importPage(page)
//            }
//            ByteArrayOutputStream().use { out ->
//                mainDoc.save(out)
//                return out.toByteArray()
//            }
//        } finally {
//            stampDoc.close()
//        }
//    }
//
//    /**
//     * Подсчёт высоты штампа (в поинтах) — логика согласована с нашей "табличной" версткой.
//     */
//    private fun calculateStampHeight(stampData: StampData): Float {
//        val headerHeight = headerFontSize * 1.8f
//        val columnHeaderHeight = textFontSize + 2 * cellPaddingV
//
//        var total = 0f
//        total += headerHeight
//        total += separatorHeight          // линия под шапкой
//        total += columnHeaderHeight
//        total += separatorHeight          // линия под заголовком
//
//        stampData.signers.forEach { signer ->
//            val maxLines = maxOf(
//                signer.signerBlockLines.size,
//                signer.certificateLines.size,
//                signer.signingTimeLines.size
//            )
//            val blockHeight = maxLines * leading + 2 * cellPaddingV
//            total += blockHeight
//            total += separatorHeight      // линия под блоком
//        }
//
//        return total
//    }
//
//    /**
//     * Рисуем штамп на указанной странице PDF.
//     */
//    private fun drawStampOnPage(
//        doc: PDDocument,
//        page: PDPage,
//        mediaBox: PDRectangle,
//        startX: Float,
//        topY: Float,
//        stampHeight: Float,
//        stampData: StampData
//    ) {
//        val tableWidth = mediaBox.width - 2 * margin
//
//        val col1Width = tableWidth * 0.28f  // Подписант
//        val col2Width = tableWidth * 0.47f  // Сертификат
//        val col3Width = tableWidth * 0.25f  // Дата и время
//
//        val outerBottomY = topY - stampHeight
//        var cursorY = topY
//
//        PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
//            cs.setLineWidth(0.5f)
//            cs.setStrokingColor(Color(0, 87, 174))
//            cs.setNonStrokingColor(Color(0, 87, 174))
//
//            // внешняя рамка
//            cs.addRect(startX, outerBottomY, tableWidth, stampHeight)
//            cs.stroke()
//
//            // ===== 1. Шапка =====
//            val headerText =
//                "Документ ${stampData.documentId} подписан в системе ${stampData.systemName}"
//            val headerHeight = headerFontSize * 1.8f
//
//            drawCenteredText(
//                cs,
//                headerText,
//                headerFont,
//                headerFontSize,
//                startX,
//                cursorY - headerHeight / 2,
//                tableWidth
//            )
//
//            cursorY -= headerHeight
//            cs.moveTo(startX, cursorY)
//            cs.lineTo(startX + tableWidth, cursorY)
//            cs.stroke()
//            cursorY -= separatorHeight
//
//            // ===== 2. Заголовки колонок =====
//            cursorY -= (cellPaddingV + textFontSize)
//
//            cs.beginText()
//            cs.setFont(headerFont, textFontSize)
//            cs.newLineAtOffset(startX + cellPaddingH, cursorY)
//            cs.showText("Подписант")
//            cs.endText()
//
//            cs.beginText()
//            cs.setFont(headerFont, textFontSize)
//            cs.newLineAtOffset(startX + col1Width + cellPaddingH, cursorY)
//            cs.showText("Сертификат")
//            cs.endText()
//
//            cs.beginText()
//            cs.setFont(headerFont, textFontSize)
//            cs.newLineAtOffset(startX + col1Width + col2Width + cellPaddingH, cursorY)
//            cs.showText("Дата и время подписания")
//            cs.endText()
//
//            cursorY -= (cellPaddingV + textFontSize)
//            cs.moveTo(startX, cursorY)
//            cs.lineTo(startX + tableWidth, cursorY)
//            cs.stroke()
//            cursorY -= separatorHeight
//
//            // ===== 3. Блоки подписантов =====
//            cs.setFont(textFont, textFontSize)
//
//            for (signer in stampData.signers) {
//                val leftLines = signer.signerBlockLines
//                val middleLines = signer.certificateLines
//                val rightLines = signer.signingTimeLines
//
//                val maxLines = maxOf(
//                    leftLines.size,
//                    middleLines.size,
//                    rightLines.size
//                )
//
//                val blockHeight = maxLines * leading + 2 * cellPaddingV
//                val startTextY = cursorY - cellPaddingV - textFontSize
//
//                drawLines(
//                    cs,
//                    leftLines,
//                    startX + cellPaddingH,
//                    startTextY,
//                    leading
//                )
//
//                drawLines(
//                    cs,
//                    middleLines,
//                    startX + col1Width + cellPaddingH,
//                    startTextY,
//                    leading
//                )
//
//                drawLines(
//                    cs,
//                    rightLines,
//                    startX + col1Width + col2Width + cellPaddingH,
//                    startTextY,
//                    leading
//                )
//
//                cursorY -= blockHeight
//                cs.moveTo(startX, cursorY)
//                cs.lineTo(startX + tableWidth, cursorY)
//                cs.stroke()
//                cursorY -= separatorHeight
//            }
//        }
//    }
//
//    private fun drawLines(
//        cs: PDPageContentStream,
//        lines: List<String>,
//        x: Float,
//        startY: Float,
//        leading: Float
//    ) {
//        if (lines.isEmpty()) return
//        cs.beginText()
//        cs.newLineAtOffset(x, startY)
//        lines.forEachIndexed { index, line ->
//            if (index > 0) cs.newLineAtOffset(0f, -leading)
//            cs.showText(line)
//        }
//        cs.endText()
//    }
//
//    private fun drawCenteredText(
//        cs: PDPageContentStream,
//        text: String,
//        font: org.apache.pdfbox.pdmodel.font.PDFont,
//        fontSize: Float,
//        x: Float,
//        centerY: Float,
//        width: Float
//    ) {
//        val textWidth = font.getStringWidth(text) / 1000 * fontSize
//        val textX = x + (width - textWidth) / 2
//        val textY = centerY - fontSize / 2
//
//        cs.beginText()
//        cs.setFont(font, fontSize)
//        cs.newLineAtOffset(textX, textY)
//        cs.showText(text)
//        cs.endText()
//    }
//}
