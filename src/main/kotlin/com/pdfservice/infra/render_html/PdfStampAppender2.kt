package com.pdfservice.infra.render_html

import com.pdfservice.infra.pdf.dto.StampData
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition

@Service
class PdfStampAppender2(
    private val templateRenderer: StampTemplateRenderer,
    private val htmlToPdfConverter: HtmlToPdfConverter,
    private val htmlToPngConverter: HtmlToPngConverter,   // пока не используем, но оставил
    private val pdfToPngConverter: PdfToPngConverter,
    private val drawer: PdfStampDrawer                    // тоже пока не используем
) {

    /**
     * Добавляет штамп (сгенерированный по StampData) на последнюю страницу PDF.
     *
     * @param originalPdf исходный PDF в виде ByteArray
     * @param stampData   данные для шаблона штампа
     * @return новый PDF с добавленным штампом как ByteArray
     */
    fun addStampPage(originalPdf: ByteArray, stampData: StampData): ByteArray {
        // 1. генерим HTML по шаблону
        val html = templateRenderer.renderStampHtml(stampData)

        // 2. HTML → PDF (штамп как отдельный документ, обычно одна страница)
        val stampPdfBytes: ByteArray = htmlToPdfConverter.convert(html)

        // 3. Берём первую страницу этого PDF и конвертим её в PNG
        val stampPngBytes: ByteArray =
            pdfToPngConverter.convertFirstPageToPng(stampPdfBytes, dpi = 144f)

        // 4. Встраиваем PNG как картинку в конец исходного PDF
        return addSignatureImageToPdf3(originalPdf, stampPngBytes)
    }

//    /**
//     * Добавляет PNG-подпись на последнюю страницу PDF.
//     *
//     * @param inputPdf      исходный PDF в виде ByteArray
//     * @param signaturePng  PNG с подписью/штампом в виде ByteArray
//     * @return новый PDF с добавленным изображением подписи как ByteArray
//     */
//    private fun addSignatureImageToPdf(
//        inputPdf: ByteArray,
//        signaturePng: ByteArray
//    ): ByteArray {
//        // Важно: используем Loader.loadPDF(byte[]) — это перегрузка под ByteArray
//        Loader.loadPDF(inputPdf).use { doc: PDDocument ->
//            // Берём последнюю страницу
//            val page: PDPage = doc.getPage(doc.numberOfPages - 1)
//
//            // Создаём изображение из ByteArray
//            val signatureImage: PDImageXObject =
//                PDImageXObject.createFromByteArray(doc, signaturePng, "signature")
//
//            // Размеры исходной картинки
//            val imgWidth = signatureImage.width.toFloat()
//            val imgHeight = signatureImage.height.toFloat()
//
//
//            // Пример: координаты от левого нижнего угла страницы
//            val mediaBox = page.mediaBox
////            val x = mediaBox.lowerLeftX + 50f
////            val y = mediaBox.lowerLeftY + 50f
//
//            // координата X — от края (можно +20f если нужен отступ)
//            val x = mediaBox.lowerLeftX
//// координата Y — например 50 pt от нижнего края страницы
//            val y = mediaBox.lowerLeftY + 50f
//
//            // ширина страницы (без учёта полей — можно добавить отступы)
//            val pageWidth = mediaBox.width
//
//            // Масштабируем подпись до нужной ширины, сохраняем пропорции
////            val scale = targetWidth / imgWidth
//            val scale = pageWidth / pageWidth
//
//            val targetWidth = pageWidth
//            val targetHeight = imgHeight * scale   // сохраняем пропорции
//
//            PDPageContentStream(
//                doc,
//                page,
//                PDPageContentStream.AppendMode.APPEND,
//                true,
//                true
//            ).use { cs ->
//
//
//                cs.drawImage(signatureImage, x, y, targetWidth, targetHeight)
//            }
//
//            // Сохраняем результат в ByteArray и возвращаем
//            val baos = ByteArrayOutputStream()
//            doc.save(baos)
//            return baos.toByteArray()
//        }
//    }






    private fun addSignatureImageToPdf(
        inputPdf: ByteArray,
        signaturePng: ByteArray
    ): ByteArray {
        Loader.loadPDF(inputPdf).use { doc ->
            val lastPageIndex = doc.numberOfPages - 1
            val page = doc.getPage(lastPageIndex)
            val mediaBox = page.mediaBox

            // 1. считаем, где на странице расположен текст
            val stripper = TextBoundsStripper(lastPageIndex)
            stripper.getText(doc) // запустит processTextPosition()

            val lowestTextY = stripper.minY  // самое нижнее место, где есть текст

            // 2. создаём изображение подписи
            val img = PDImageXObject.createFromByteArray(doc, signaturePng, "signature")

            val pageWidth = mediaBox.width
            val imgWidth = img.width.toFloat()
            val imgHeight = img.height.toFloat()

            println("Image width = $imgWidth px, height = $imgHeight px")

            val marginHorizontal = 20f
            val targetWidth = pageWidth - marginHorizontal * 2

            val scale = targetWidth / imgWidth
            val targetHeight = imgHeight * scale

            // 3. ставим штамп ПОД текстом, если есть место, иначе делаем новую страницу
            val marginVertical = 20f
            var x = mediaBox.lowerLeftX + marginHorizontal
            var y = lowestTextY - targetHeight - marginVertical

            val targetPage: PDPage =
                if (y < mediaBox.lowerLeftY + marginVertical) {
                    // не помещается под текстом — создаём новую страницу и ставим сверху на новой
                    val newPage = PDPage(mediaBox)
                    doc.addPage(newPage)

                    x = mediaBox.lowerLeftX + marginHorizontal
                    y = mediaBox.upperRightY - targetHeight - marginVertical
                    newPage
                } else {
                    page
                }

            PDPageContentStream(
                doc,
                targetPage,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
            ).use { cs ->
                cs.drawImage(img, x, y, targetWidth, targetHeight)
            }

            val out = java.io.ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        }
    }


    fun addSignatureImageToPdf2(
        inputPdf: ByteArray,
        signaturePng: ByteArray
    ): ByteArray {
        val STAMP_DPI = 144f

        Loader.loadPDF(inputPdf).use { doc: PDDocument ->
            val lastPageIndex = doc.numberOfPages - 1
            val lastPage: PDPage = doc.getPage(lastPageIndex)
            val mediaBox: PDRectangle = lastPage.mediaBox

            val pageWidthPt = mediaBox.width
            val pageHeightPt = mediaBox.height

            // 1. загружаем изображение
            val img = PDImageXObject.createFromByteArray(doc, signaturePng, "stamp")

            // размеры в пикселях
            val imgWidthPx = img.width.toFloat()
            val imgHeightPx = img.height.toFloat()

            // 2. переводим в "нативные" поинты с учётом DPI
            val imgWidthPt  = imgWidthPx  * 72f / STAMP_DPI
            val imgHeightPt = imgHeightPx * 72f / STAMP_DPI

            // 3. хотим растянуть штамп почти на всю ширину страницы
            val horizontalMargin = 36f   // 0.5 inch
            val bottomMargin = 36f       // отступ снизу

            val availableWidth = pageWidthPt - horizontalMargin * 2
            val scale = availableWidth / imgWidthPt

            val targetWidthPt = availableWidth
            val targetHeightPt = imgHeightPt * scale

            // 4. проверяем, поместится ли такая высота на странице
            val neededHeight = targetHeightPt + bottomMargin
            val fitsOnLastPage = neededHeight <= pageHeightPt

            val targetPage: PDPage =
                if (fitsOnLastPage) {
                    lastPage
                } else {
                    // не влезает по высоте — создаём новую страницу такого же размера
                    PDPage(mediaBox).also { doc.addPage(it) }
                }

            val targetMediaBox = targetPage.mediaBox

            // координаты: прижимаем к низу страницы с отступом
            val x = targetMediaBox.lowerLeftX + horizontalMargin
            val y = targetMediaBox.lowerLeftY + bottomMargin

            PDPageContentStream(
                doc,
                targetPage,
                AppendMode.APPEND,
                true,
                true
            ).use { cs ->
                cs.drawImage(img, x, y, targetWidthPt, targetHeightPt)
            }

            // 5. сохраняем результат
            val baos = ByteArrayOutputStream()
            doc.save(baos)
            return baos.toByteArray()
        }
    }





    private val STAMP_DPI = 144f // с таким dpi ты рендеришь PNG из PDF

    fun addSignatureImageToPdf3(
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
                AppendMode.APPEND,
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
