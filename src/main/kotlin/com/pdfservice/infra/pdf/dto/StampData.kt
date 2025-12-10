package com.pdfservice.infra.pdf.dto

data class StampData(
    val documentId: String,
    val systemName: String,      // "КЭДО HRlink"
    val signers: List<SignerBlock>
)

data class SignerBlock(
    val signerBlockLines: List<String>,     // левый столбец
    val certificateLines: List<String>,     // средний столбец
    val signingTimeLines: List<String>      // правый столбец
)