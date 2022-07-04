import platform.posix.system

/**
 * rundll32.exe libnative.dll,popCalc
 */
@CName(externName = "popCalc")
fun popCalc() {
    println("Spawning calculator...")
    system("cmd.exe /C C:\\Windows\\System32\\calc.exe")
}
