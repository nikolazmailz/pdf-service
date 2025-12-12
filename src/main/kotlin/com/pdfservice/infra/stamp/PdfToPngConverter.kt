package com.pdfservice.infra.stamp

import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.abs

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

            val trimmed = trimBackgroundBorders(image, 10)

            val out = ByteArrayOutputStream()
            ImageIO.write(trimmed, "png", out)
            return out.toByteArray()
        } finally {
            doc.close()
        }
    }



    fun trimBackgroundBorders(
        src: BufferedImage,
        tolerance: Int = 8 // чем больше, тем агрессивнее "съедаем" почти-белое
    ): BufferedImage {
        if (src.width == 0 || src.height == 0) return src

        val bgRgb = src.getRGB(0, 0) // динамический фон

        fun isBackground(rgb: Int): Boolean {
            val a = rgb ushr 24 and 0xFF
            if (a == 0) return true // полностью прозрачный — фон

            val r = rgb shr 16 and 0xFF
            val g = rgb shr 8 and 0xFF
            val b = rgb and 0xFF

            val br = bgRgb shr 16 and 0xFF
            val bg = bgRgb shr 8 and 0xFF
            val bb = bgRgb and 0xFF

            return abs(r - br) <= tolerance &&
                    abs(g - bg) <= tolerance &&
                    abs(b - bb) <= tolerance
        }

        var top = 0
        var left = 0
        var right = src.width - 1
        var bottom = src.height - 1

        // сверху вниз
        outer@ for (y in 0 until src.height) {
            for (x in 0 until src.width) {
                if (!isBackground(src.getRGB(x, y))) {
                    top = y
                    break@outer
                }
            }
        }

        // снизу вверх
        outer@ for (y in src.height - 1 downTo top) {
            for (x in 0 until src.width) {
                if (!isBackground(src.getRGB(x, y))) {
                    bottom = y
                    break@outer
                }
            }
        }

        // слева направо
        outer@ for (x in 0 until src.width) {
            for (y in top..bottom) {
                if (!isBackground(src.getRGB(x, y))) {
                    left = x
                    break@outer
                }
            }
        }

        // справа налево
        outer@ for (x in src.width - 1 downTo left) {
            for (y in top..bottom) {
                if (!isBackground(src.getRGB(x, y))) {
                    right = x
                    break@outer
                }
            }
        }

        val newWidth = right - left + 1
        val newHeight = bottom - top + 1

        if (newWidth <= 0 || newHeight <= 0) {
            // на всякий случай, если вдруг вообще ничего не нашли
            return src
        }

        return src.getSubimage(left, top, newWidth, newHeight)
    }

}