import kotlinx.cinterop.*
import platform.windows.*

fun getRegValue(root: HKEY?, keyPath: String, valueName: String): Any? {
    memScoped {
        val lpType = alloc<DWORDVar>()
        val lpcbData = alloc<DWORDVar>()
        val rc = RegGetValueW(root, keyPath, valueName, RRF_RT_ANY, lpType.ptr, null, lpcbData.ptr)
        val keyType = lpType.value.toInt()
        if (keyType == REG_NONE) {
            throw Exception("keyType == REG_NONE")
        }
        if (rc != ERROR_SUCCESS && rc != ERROR_INSUFFICIENT_BUFFER) {
            throw Exception("something wrong")
        }

        val byteSize = lpcbData.value.toInt()
        val byteData = allocArray<UByteVar>(byteSize + Short.SIZE_BYTES)

        checkError(
            RegGetValueW(root, keyPath, valueName, RRF_RT_ANY, lpType.ptr, byteData, lpcbData.ptr)
        )

        return bytesToValue(byteData, byteSize, keyType)
    }
}

fun listValues(root: HKEY?, keyPath: String): Map<String, Any?> {
    memScoped {
        val hkey = alloc<HKEYVar>()
        RegOpenKeyExW(root, keyPath, 0, KEY_READ.convert(), hkey.ptr)

        val lpcValues = alloc<DWORDVar>()
        val lpcMaxValueNameLen = alloc<DWORDVar>()
        val lpcMaxValueLen = alloc<DWORDVar>()
        checkError(
            RegQueryInfoKeyW(
                hkey.value, null, null, null, null, null, null,
                lpcValues.ptr, lpcMaxValueNameLen.ptr, lpcMaxValueLen.ptr, null, null
            )
        )

        val map = LinkedHashMap<String, Any?>()
        val byteDataLength = (lpcMaxValueLen.value.toInt() + 1) * Short.SIZE_BYTES
        val nameLength = lpcMaxValueNameLen.value.toInt() + 1

        for (n in 0 until lpcValues.value.toInt()) {
            memScoped {
                // Data
                val byteData = allocArray<UByteVar>(byteDataLength)
                val lpcbData = alloc<DWORDVar>().also { it.value = lpcMaxValueLen.value }

                // Name
                val nameArray = allocArray<WCHARVar>(nameLength)
                val lpcchValueName = alloc<DWORDVar>().also { it.value = nameLength.convert() }

                // Type
                val lpType = alloc<DWORDVar>()

                checkError(
                    RegEnumValueW(
                        hkey.value, n.convert(), nameArray, lpcchValueName.ptr,
                        null, lpType.ptr, byteData, lpcbData.ptr
                    )
                )

                val keyName = nameArray.toKString()
                val dataSize = lpcbData.value.toInt()
                val keyType = lpType.value.toInt()

                map[keyName] = bytesToValue(byteData, dataSize, keyType)
            }
        }
        return map
    }
}

private fun bytesToValue(byteData: CPointer<UByteVar>, dataSize: Int, keyType: Int): Any? {
    return when (keyType) {
        REG_NONE -> null
        REG_BINARY -> byteData.readBytes(dataSize)
        REG_QWORD -> if (dataSize == 0) 0L else byteData.reinterpret<LongVar>()[0]
        REG_DWORD -> if (dataSize == 0) 0L else byteData.reinterpret<IntVar>()[0]
        REG_SZ, REG_EXPAND_SZ -> byteData.reinterpret<WCHARVar>().toKString()
        REG_MULTI_SZ -> "Unimplemented reg type REG_MULTI_SZ"
        else -> error("Unsupported reg type $keyType")
    }
}

internal fun checkError(result: Int): Int {
    if (result != ERROR_SUCCESS) {
        memScoped {
            val nameArray = allocArray<WCHARVar>(1024)
            FormatMessageW(FORMAT_MESSAGE_FROM_SYSTEM, null, result.convert(), LANG_ENGLISH, nameArray, 1024, null)
            error("Error in Winreg (${result}) [${nameArray.toKString()}]")
        }
    }
    return result
}
