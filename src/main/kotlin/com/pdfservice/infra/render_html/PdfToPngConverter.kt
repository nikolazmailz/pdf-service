package com.pdfservice.infra.render_html

import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class PdfToPngConverter {

    /**
     * Берём первый лист PDF и рендерим его в PNG.
     *
     * @param pdfBytes PDF с штампом
     * @param dpi      качество (96–150 обычно достаточно)
     */
    fun convertFirstPageToPng(pdfBytes: ByteArray, dpi: Float = 144f): ByteArray {
        val doc = Loader.loadPDF(pdfBytes)
        try {
            val renderer = PDFRenderer(doc)
            val image = renderer.renderImageWithDPI(0, dpi, ImageType.ARGB)

            val out = ByteArrayOutputStream()
            ImageIO.write(image, "png", out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }
}
