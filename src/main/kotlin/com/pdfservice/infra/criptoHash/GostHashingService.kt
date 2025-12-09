package com.pdfservice.infra.criptoHash

import com.pdfservice.application.HashingService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.Security

@Component
class GostHashingService : HashingService {

    companion object {
        private val provider = BouncyCastleProvider()
        private val providerName = provider.name
        private const val algorithm = "GOST3411-2012-256"

        init {
            if (Security.getProvider(providerName) == null) {
                Security.addProvider(provider)
            }
        }
    }

    override suspend fun calculateGostHash(bytes: ByteArray): String {
        // TODO: consider running on a dedicated dispatcher if hashing becomes CPU-heavy.
        val digest: MessageDigest = MessageDigest.getInstance(algorithm, providerName)
        return digest.digest(bytes).toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}