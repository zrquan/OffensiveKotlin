import kotlinx.cinterop.*
import jvmti.*
import platform.posix.memset
import platform.windows.CHARVar

@CName("Agent_OnLoad")
fun agentOnLoad(vm: CPointer<JavaVMVar>, options: CPointer<CHARVar>, reserved: COpaquePointer): jint =
    memScoped {
        val vmFuns = vm.pointed.value?.pointed ?: error("No VM functions found")
        val getEnvFun = vmFuns.GetEnv ?: error("No GetEnv functions found")
        val jvmti = alloc<CPointerVar<jvmtiEnvVar>>()

        val rc = getEnvFun(vm, jvmti.ptr.reinterpret(), JVMTI_VERSION.convert())
        if (rc != JNI_OK) error("ERROR: Unable to create jvmtiEnv, GetEnv failed, error=$rc")

        val jvmtiFuns = jvmti.pointed?.value?.pointed ?: error("No JVMTI functions found")
        val callbacks = alloc<jvmtiEventCallbacks>()

        memset(callbacks.ptr, 0, sizeOf<jvmtiEventCallbacks>().convert())
        callbacks.VMInit = staticCFunction(::vmInit).reinterpret()
        jvmtiFuns.SetEventCallbacks!!(jvmti.value, callbacks.ptr, sizeOf<jvmtiEventCallbacks>().convert())
        setEventNotificationMode(jvmti.value)

        JNI_OK
    }

@CName("Agent_OnUnload")
fun agentOnUnload(vmPtr: Long) {
}

fun vmInit(jvmti: CPointer<jvmtiEnvVar>, env: CPointer<JNIEnvVar>, thread: jthread) =
    memScoped {
        val runtimeVersion = alloc<jintVar>()
        val jvmtiFuns = jvmti.pointed.value?.pointed ?: error("No JVMTI functions found")

        val err = jvmtiFuns.GetVersionNumber!!.invoke(jvmti, runtimeVersion.ptr)
        if (err != JVMTI_ERROR_NONE) {
            error("ERROR: GetVersionNumber failed, err=$err")
        } else {
            versionCheck(JVMTI_VERSION.convert(), runtimeVersion.value)
        }
    }

fun versionCheck(cver: jint, rver: jint) {
    val cmajor = (cver and JVMTI_VERSION_MASK_MAJOR.convert()) shr JVMTI_VERSION_SHIFT_MAJOR.convert()
    val cminor = (cver and JVMTI_VERSION_MASK_MINOR.convert()) shr JVMTI_VERSION_SHIFT_MINOR.convert()
    val cmicro = (cver and JVMTI_VERSION_MASK_MICRO.convert()) shr JVMTI_VERSION_SHIFT_MICRO.convert()
    val rmajor = (rver and JVMTI_VERSION_MASK_MAJOR.convert()) shr JVMTI_VERSION_SHIFT_MAJOR.convert()
    val rminor = (rver and JVMTI_VERSION_MASK_MINOR.convert()) shr JVMTI_VERSION_SHIFT_MINOR.convert()
    val rmicro = (rver and JVMTI_VERSION_MASK_MICRO.convert()) shr JVMTI_VERSION_SHIFT_MICRO.convert()
    println("Compile Time JVMTI Version: $cmajor.$cminor.$cmicro (0x${cver.toString(16)})")
    println("Run Time JVMTI Version: $rmajor.$rminor.$rmicro (0x${rver.toString(16)})")

    if (cmajor != rmajor || cminor != rminor)
        error("ERROR: Compile Time JVMTI and Run Time JVMTI are incompatible")
}
