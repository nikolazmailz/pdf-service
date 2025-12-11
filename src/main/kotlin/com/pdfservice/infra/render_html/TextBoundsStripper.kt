package com.pdfservice.infra.render_html

import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition

class TextBoundsStripper(
    private val pageIndex: Int
) : PDFTextStripper() {

    var minY: Float = Float.MAX_VALUE  // самый низкий текст
        private set
    var maxY: Float = Float.MIN_VALUE  // самый верхний текст
        private set

    init {
        startPage = pageIndex + 1  // PDFBox страницы с 1
        endPage = pageIndex + 1
    }

    override fun processTextPosition(text: TextPosition) {
        val y = text.yDirAdj.toFloat()  // "экранная" Y-координата текста
        if (y < minY) minY = y
        if (y > maxY) maxY = y
        super.processTextPosition(text)
    }
}
