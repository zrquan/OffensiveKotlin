import kotlinx.cinterop.*
import cipher.libsodium.*
import platform.posix.abort
import platform.windows.CHARVar

// This is an example using the C library.

fun main() = aes256gcm()

fun aes256gcm() {
    val message = "test".cstr
    val messageLength = 4
    val additionalData = "123456".cstr
    val additionalDataLength = 6

    memScoped {
        val nonce = allocArray<CHARVar>(crypto_aead_aes256gcm_NPUBBYTES.toInt())
        val key = allocArray<CHARVar>(crypto_aead_aes256gcm_KEYBYTES.toInt())
        val ciphertext = allocArray<CHARVar>(messageLength + crypto_aead_aes256gcm_ABYTES.toInt())
        val ciphertextLength = alloc<LongVar>()

        sodium_init()
        if (crypto_aead_aes256gcm_is_available() == 0) abort()

        crypto_aead_aes256gcm_keygen(key.reinterpret())
        randombytes_buf(nonce, crypto_aead_aes256gcm_NPUBBYTES.convert())

        crypto_aead_aes256gcm_encrypt(
            ciphertext.reinterpret(),
            ciphertextLength.ptr.reinterpret(),
            message.ptr.reinterpret(),
            messageLength.convert(),
            additionalData.ptr.reinterpret(),
            additionalDataLength.convert(),
            null,
            nonce.reinterpret(),
            key.reinterpret()
        )

        println("Encrypt(b64): ${base64(ciphertext.toKString())}")

        val decrypted = allocArray<CHARVar>(messageLength)
        val decryptedLength = alloc<LongVar>()

        crypto_aead_aes256gcm_decrypt(
            decrypted.reinterpret(),
            decryptedLength.ptr.reinterpret(),
            null,
            ciphertext.reinterpret(),
            ciphertextLength.value.convert(),
            additionalData.ptr.reinterpret(),
            additionalDataLength.convert(),
            nonce.reinterpret(),
            key.reinterpret()
        )

        println("Decrypt: ${decrypted.toKString()}")
    }
}

fun base64(message: String) = memScoped {
    val b64Len = sodium_base64_encoded_len(message.length.convert(), sodium_base64_VARIANT_ORIGINAL)
    val b64Str = allocArray<CHARVar>(b64Len.convert())

    sodium_bin2base64(b64Str, b64Len, message.cstr.ptr.reinterpret(), 5, sodium_base64_VARIANT_ORIGINAL)
    b64Str.toKString()
}
