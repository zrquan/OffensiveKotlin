import platform.posix.exit
import platform.windows.IsDebuggerPresent

fun main() = checkDebugger()

fun checkDebugger() {
    if (IsDebuggerPresent() == 1) {
        println("Current process is in a debugger.")
        exit(0)
    } else {
        println("Debugger not found.")
    }
}

fun checkVM() {
    // TODO: invoke EnumProcesses
}
