package com.pdfservice.infra.repository

import com.pdfservice.domain.SignatureRepository
import com.pdfservice.domain.Signature
import io.r2dbc.spi.Readable
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID


@Repository
class DatabaseSignatureRepository(
    private val databaseClient: DatabaseClient
) : SignatureRepository {

    override suspend fun save(signature: Signature): Signature {
        val sql = """
            INSERT INTO signature (
                id, signer_name, signer_position, signer_organization,
                certificate_serial_number, certificate_valid_from, certificate_valid_to,
                is_certificate_valid_at_signing_time, signed_at, signature_bytes, signature_base64,
                file_id, file_name, file_hash
            ) VALUES (
                :id, :signerName, :signerPosition, :signerOrganization,
                :serialNumber, :validFrom, :validTo,
                :isValidAtSigningTime, :signedAt, :bytes, :base64,
                :fileId, :fileName, :fileHash
            )
            RETURNING *
        """.trimIndent()

        return databaseClient.sql(sql)
            .bind("id", signature.id)
            .bind("signerName", signature.signerName)
            .bind("signerPosition", signature.signerPosition)
            .bind("signerOrganization", signature.signerOrganization)
            .bind("serialNumber", signature.certificateSerialNumber)
            .bind("validFrom", signature.certificateValidFrom)
            .bind("validTo", signature.certificateValidTo)
            .bind("isValidAtSigningTime", signature.isCertificateValidAtSigningTime)
            .bind("signedAt", signature.signedAt)
            .bindOrNull("bytes", signature.signatureBytes, ByteArray::class.java)
            .bindOrNull("base64", signature.signatureBase64, String::class.java)
            .bind("fileId", signature.fileId)
            .bind("fileName", signature.fileName)
            .bind("fileHash", signature.fileHash)
            .map(this::mapRow)
            .awaitSingle()
    }

    override suspend fun findById(id: UUID): Signature? {
        val sql = "SELECT * FROM signature WHERE id = :id"
        return databaseClient.sql(sql)
            .bind("id", id)
            .map(this::mapRow)
            .awaitOneOrNull()
    }

    private fun mapRow(row: Readable): Signature = Signature(
        id = requireNotNull(row.get("id", UUID::class.java)),
        signerName = requireNotNull(row.get("signer_name", String::class.java)),
        signerPosition = requireNotNull(row.get("signer_position", String::class.java)),
        signerOrganization = requireNotNull(row.get("signer_organization", String::class.java)),
        certificateSerialNumber = requireNotNull(row.get("certificate_serial_number", String::class.java)),
        certificateValidFrom = requireNotNull(row.get("certificate_valid_from", Instant::class.java)),
        certificateValidTo = requireNotNull(row.get("certificate_valid_to", Instant::class.java)),
        isCertificateValidAtSigningTime = requireNotNull(row.get("is_certificate_valid_at_signing_time", Boolean::class.java)),
        signedAt = requireNotNull(row.get("signed_at", Instant::class.java)),
        signatureBytes = requireNotNull(row.get("signature_bytes", ByteArray::class.java)),
        signatureBase64 = row.get("signature_base64", String::class.java),
        fileId = requireNotNull(row.get("file_id", UUID::class.java)),
        fileName = requireNotNull(row.get("file_name", String::class.java)),
        fileHash = requireNotNull(row.get("file_hash", String::class.java))
    )

    private fun DatabaseClient.GenericExecuteSpec.bindOrNull(
        name: String,
        value: Any?,
        type: Class<*>
    ): DatabaseClient.GenericExecuteSpec =
        if (value == null) this.bindNull(name, type) else this.bind(name, value)
}

