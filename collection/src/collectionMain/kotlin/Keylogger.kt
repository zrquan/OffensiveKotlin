import platform.posix.sleep
import platform.windows.GetAsyncKeyState

// TODO
fun logKeyState() {
    sleep(1)
    println("Staring...")

    while (true) {
        sleep(1)
        for (key in 0 until 255) {
            if ((GetAsyncKeyState(key).toInt() and 0x8000) != 0) {
                when (key) {
                    37 -> println("left")
                    38 -> println("up")
                    39 -> println("right")
                    40 -> println("down")
                }
            }
        }
    }
}
