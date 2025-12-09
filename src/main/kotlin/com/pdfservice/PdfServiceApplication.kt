package com.pdfservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PdfServiceApplication

fun main(args: Array<String>) {
    runApplication<PdfServiceApplication>(*args)
}

