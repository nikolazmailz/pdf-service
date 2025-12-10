package com.pdfservice.infra.render_html

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class HtmlToPdfConverter {

    fun convert(html: String): ByteArray {
        val out = ByteArrayOutputStream()

        val builder = PdfRendererBuilder()

        // если шрифт лежит в ресурсах
        val fontStream = this::class.java.getResourceAsStream("/fonts/DejaVuSans.ttf")
            ?: error("DejaVuSans.ttf not found in resources/fonts")

        builder.useFont(
            { fontStream },
            "DejaVu Sans"
        )

        builder.useFastMode()
        builder.withHtmlContent(html, null)
        builder.toStream(out)

        builder.run()

        return out.toByteArray()
    }
}