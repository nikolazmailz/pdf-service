package com.pdfservice.infra.stamp

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class HtmlToPdfConverter {

    fun convert(htmlTemplate: String): ByteArray {
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
        builder.withHtmlContent(htmlTemplate, null)
        builder.toStream(out)

        builder.run()

        return out.toByteArray()
    }
}