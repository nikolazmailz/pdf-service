package com.pdfservice.infra.render_html

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class PdfStampDrawerOld {

    fun drawStampAtBottom(
        originalPdf: ByteArray,
        pngBytes: ByteArray,
        bottomMargin: Float = 20f
    ): ByteArray {
        val doc = Loader.loadPDF(originalPdf)
        try {
            val page = doc.getPage(doc.numberOfPages - 1)
            val mediaBox = page.mediaBox

            val image = PDImageXObject.createFromByteArray(doc, pngBytes, "stamp")

            // Масштабируем картинку под ширину страницы (минус небольшие поля)
            val maxWidth = mediaBox.width - 40f
            val scale = maxWidth / image.width
            val stampWidth = image.width * scale
            val stampHeight = image.height * scale

            val x = (mediaBox.width - stampWidth) / 2f
            val y = bottomMargin

            PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
                cs.drawImage(image, x, y, stampWidth, stampHeight)
            }

            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }

    /**
     * Вставляет PNG штамп в конец последней страницы PDF
     *
     * @param originalPdf PDF исходного документа
     * @param pngBytes PNG изображения штампа
     * @param stampHeightPx высота штампа в пикселях (для масштабирования)
     * @param dpi масштабирование (обычно 96 или 110)
     */
    fun drawStampAtBottom(
        originalPdf: ByteArray,
        pngBytes: ByteArray,
        stampHeightPx: Int,
        dpi: Float = 96f,
        bottomMargin: Float = 20f
    ): ByteArray {

        val doc = Loader.loadPDF(originalPdf)

        try {
            val page = doc.getPage(doc.numberOfPages - 1)
            val mediaBox = page.mediaBox

            val pdImage = PDImageXObject.createFromByteArray(doc, pngBytes, "stamp")

            // переводим px → PDF points
            val scale = dpi / 72f
            val stampWidth = pdImage.width / scale
            val stampHeight = pdImage.height / scale

            val x = (mediaBox.width - stampWidth) / 2f      // по центру
            val y = bottomMargin                            // внизу

            PDPageContentStream(doc, page, AppendMode.APPEND, true, true).use { cs ->
                cs.drawImage(pdImage, x, y, stampWidth, stampHeight)
            }

            val out = ByteArrayOutputStream()
            doc.save(out)
            return out.toByteArray()

        } finally {
            doc.close()
        }
    }
}
