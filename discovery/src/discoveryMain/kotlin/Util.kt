import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.windows.HKEY_LOCAL_MACHINE

fun getCPUModel(): String = getRegValue(
    HKEY_LOCAL_MACHINE,
    "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
    "Identifier"
) as String

fun getCPUName(): String = getRegValue(
    HKEY_LOCAL_MACHINE,
    "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
    "ProcessorNameString"
) as String

fun getOSInfo(): String {
    val info = listValues(HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion")
    return """
        InstallationType => ${info["InstallationType"]}
        ProductID => ${info["ProductID"]}
        ProductName => ${info["ProductName"]}
        RegisteredOwner => ${info["RegisteredOwner"]}
        ReleaseID => ${info["ReleaseID"]}
        CurrentBuild => ${info["CurrentBuild"]}
        InstallDate => ${parseTimeStamp(info["InstallDate"])}
    """.trimIndent()
}

private inline fun parseTimeStamp(time: Any?): String {
    return try {
        val timeNum = time.toString().toLong()
        Instant
            .fromEpochMilliseconds(timeNum)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
    } catch (e: Exception) {
        "null"
    }
}
