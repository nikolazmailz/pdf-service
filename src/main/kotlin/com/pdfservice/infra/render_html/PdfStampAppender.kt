package com.pdfservice.infra.render_html

import com.pdfservice.infra.pdf.dto.StampData
import org.springframework.stereotype.Service

@Service
class PdfStampAppender(
    private val templateRenderer: StampTemplateRenderer,
    private val htmlToPdfConverter: HtmlToPdfConverter,
    private val htmlToPngConverter: HtmlToPngConverter,
    private val pdfToPngConverter: PdfToPngConverter,
    private val drawer: PdfStampDrawer
) {

    fun addStampPage(originalPdf: ByteArray, stampData: StampData): ByteArray {
        // 1. генерим HTML по шаблону
        val html = templateRenderer.renderStampHtml(stampData)

        // 2. HTML → PDF (штамп как отдельный документ, обычно одна страница)
        val stampPdfBytes = htmlToPdfConverter.convert(html)

        val stampPng = pdfToPngConverter.convertFirstPageToPng(stampPdfBytes, dpi = 144f)

        // 4. PNG вставляем в низ последней страницы исходного PDF
        return drawer.drawStampSmart(
            originalPdf = originalPdf,
            pngBytes = stampPng,
        )

//        // 3. Склейка основных страниц и штампа
//        val mainDoc: PDDocument = Loader.loadPDF(originalPdf)
//        val stampDoc: PDDocument = Loader.loadPDF(stampPdfBytes)
//
//        return try {
//            // В pdfbox 3.x страницы другого документа нужно импортировать
//            for (page in stampDoc.pages) {
//                mainDoc.importPage(page)   // добавит страницу в конец mainDoc
//            }
//
//            val out = ByteArrayOutputStream()
//            mainDoc.save(out)
//            out.toByteArray()
//        } finally {
//            // обязательно закрываем оба документа
//            stampDoc.close()
//            mainDoc.close()
//        }

//        return drawer.drawStampAtBottom(
//            originalPdf = originalPdf,
//            pngBytes = stampPdfBytes,
//            stampHeightPx = 400          // регулируй под свой HTML
//        )

    }
}