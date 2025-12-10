package com.pdfservice.infra.render_html

import com.pdfservice.infra.pdf.dto.StampData
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import org.springframework.stereotype.Service
import java.io.StringWriter

@Service
class StampTemplateRenderer {

    private val cfg: Configuration = Configuration(Configuration.VERSION_2_3_32).apply {
        setClassForTemplateLoading(this@StampTemplateRenderer.javaClass, "/templates")
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        logTemplateExceptions = false
        wrapUncheckedExceptions = true
    }

    fun renderStampHtml(stampData: StampData): String {
        val template = cfg.getTemplate("stamp.ftl")

        val model = mapOf(
            "documentId" to stampData.documentId,
            "systemName" to stampData.systemName,
            "signers" to stampData.signers
        )

        val out = StringWriter()
        template.process(model, out)
        return out.toString()
    }
}