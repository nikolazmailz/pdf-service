package com.pdfservice.infra.render_html

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Рисует PNG-штамп:
 *  - если помещается в зарезервированную снизу область — на последней странице;
 *  - иначе — создаёт новую страницу и рисует штамп на ней.
 *
 * contract: в оригинальном PDF/шаблоне снизу действительно есть свободная зона высотой reservedAreaHeight,
 * куда текст документа не заходит.
 */
@Service
class PdfStampDrawer(
    // высота "резервной" области под штампом (в поинтах, 1pt ~= 0.35 мм)
    private val reservedAreaHeight: Float = 180f,
    private val bottomMargin: Float = 20f,
    private val sideMargin: Float = 20f
) {

    fun drawStampSmart(
        originalPdf: ByteArray,
        pngBytes: ByteArray
    ): ByteArray {
        val doc: PDDocument = Loader.loadPDF(originalPdf)
        try {
            val lastPage = doc.getPage(doc.numberOfPages - 1)
            val mediaBox = lastPage.mediaBox

            // читаем PNG, чтобы знать реальные размеры
            val bimg = ImageIO.read(ByteArrayInputStream(pngBytes))
                ?: error("Cannot read PNG from bytes")

            // считаем итоговый размер штампа в поинтах, чтобы он влезал по ширине страницы
            val stampSize = computeStampSize(mediaBox, bimg.width.toFloat(), bimg.height.toFloat())

            return if (stampSize.height <= reservedAreaHeight) {
                // помещается в зарезервированную снизу область — рисуем на последней странице
                drawOnPage(
                    doc = doc,
                    page = lastPage,
                    mediaBox = mediaBox,
                    pngBytes = pngBytes,
                    stampWidth = stampSize.width,
                    stampHeight = stampSize.height,
                    // рисуем от bottomMargin вверх
                    x = (mediaBox.width - stampSize.width) / 2f,
                    y = bottomMargin
                )
                ByteArrayOutputStream().use { out ->
                    doc.save(out)
                    out.toByteArray()
                }
            } else {
                // не помещается — создаём новую страницу и рисуем штамп на ней
                val newPage = PDPage(mediaBox)
                doc.addPage(newPage)

                // на новой странице можем рисовать повыше — например, от верхнего поля
                val yTop = mediaBox.height - bottomMargin - stampSize.height
                drawOnPage(
                    doc = doc,
                    page = newPage,
                    mediaBox = mediaBox,
                    pngBytes = pngBytes,
                    stampWidth = stampSize.width,
                    stampHeight = stampSize.height,
                    x = (mediaBox.width - stampSize.width) / 2f,
                    y = yTop
                )

                ByteArrayOutputStream().use { out ->
                    doc.save(out)
                    out.toByteArray()
                }
            }
        } finally {
            doc.close()
        }
    }

    private data class Size(val width: Float, val height: Float)

    /**
     * Подбираем масштаб штампа:
     *  - по ширине: максимум (width - 2 * sideMargin)
     *  - по высоте: не больше reservedAreaHeight (для проверки) — но сам масштаб одинаковый по X/Y
     */
    private fun computeStampSize(
        mediaBox: PDRectangle,
        imgWidthPx: Float,
        imgHeightPx: Float
    ): Size {
        val maxWidth = mediaBox.width - 2 * sideMargin
        // масштаб в "поинтах на пиксель"
        val scaleByWidth = maxWidth / imgWidthPx
        // если хотим гарантированно влезть в reservedAreaHeight, можно учесть и её:
//        val scaleByHeight = reservedAreaHeight / imgHeightPx

        val scale = minOf(scaleByWidth, 0.9f)

        val stampWidth = imgWidthPx * scale
        val stampHeight = imgHeightPx * scale
        return Size(stampWidth, stampHeight)
    }

    private fun drawOnPage(
        doc: PDDocument,
        page: PDPage,
        mediaBox: PDRectangle,
        pngBytes: ByteArray,
        stampWidth: Float,
        stampHeight: Float,
        x: Float,
        y: Float
    ) {
        val image = PDImageXObject.createFromByteArray(doc, pngBytes, "stamp")

        PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
            cs.drawImage(image, x, y, stampWidth, stampHeight)
        }
    }
}
