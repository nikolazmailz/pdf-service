//package com.signservice.infra.criptoHash
//
//import com.signservice.application.HashingService
//import org.bouncycastle.crypto.Digest
//import org.bouncycastle.crypto.digests.GOST3411_2012_256Digest
//import org.springframework.stereotype.Component
//
//@Component
//class HashingServiceImpl : HashingService {
//
//    override suspend fun calculateGostHash(bytes: ByteArray): String {
//        val digest: Digest = GOST3411_2012_256Digest()
//        digest.update(bytes, 0, bytes.size)
//        val hash = ByteArray(digest.digestSize)
//        digest.doFinal(hash, 0)
//        return hash.joinToString("") { "%02x".format(it) }
//    }
//}