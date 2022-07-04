import platform.posix.system

fun main(vararg args: String) = runCmd(*args)

fun runCmd(vararg args: String) {
    system("cmd.exe /C ${args.joinToString(" ")}")
}

fun runPowershell(vararg args: String) {
    system("powershell.exe ${args.joinToString(" ")}")
}
