package com.pdfservice.infra.render_html

import com.openhtmltopdf.java2d.api.BufferedImagePageProcessor
import com.openhtmltopdf.java2d.api.Java2DRendererBuilder
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class HtmlToPngConverter {

    /**
     * Конвертация HTML → PNG (в памяти).
     *
     * @param html          HTML штампа
     * @param pageWidthMm   ширина "страницы" в мм (для лайаута HTML)
     * @param pageHeightMm  высота "страницы" в мм (можно сделать небольшой, штамп всё равно маленький)
     * @param scale         масштаб рендера (1.0 = 72dpi, 2.0 ~144dpi и т.п.)
     */
    fun convertHtmlToPng(
        html: String,
        pageWidthMm: Double = 210.0,
        pageHeightMm: Double = 60.0,
        scale: Double = 2.0
    ): ByteArray {
        // Буфер, куда OpenHTMLtoPDF нарисует картинку
        val processor = BufferedImagePageProcessor(
            /* imageType = */ BufferedImage.TYPE_INT_ARGB,
            /* scale =     */ scale
        )

        // Собираем рендерер
        Java2DRendererBuilder().apply {
            // Сам HTML, baseUrl = null, потому что тебя ресурсы из файловой системы не интересуют
            withHtmlContent(html, null)

            // Быстрый режим (рекомендованный)
            useFastMode()

            // Размер "страницы", на которой будет верстаться HTML, в миллиметрах
            useDefaultPageSize(
                pageWidthMm.toFloat(),
                pageHeightMm.toFloat(),
                BaseRendererBuilder.PageSizeUnits.MM
            )

            // Рендерим ровно одну страницу в наш processor
            toSinglePage(processor)

            // Собственно рендер первой (и единственной) страницы
            runFirstPage()
        }

        // Достаём картинку и кодируем в PNG
        val image = processor.pageImages.first()  // List<BufferedImage>

        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }
}
