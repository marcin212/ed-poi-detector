import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class ApplicationSettings {
    private val file = File("application.properties")
    private val props = Properties()

    constructor() {
        if (!file.exists()) {
            file.createNewFile()
        }
        props.load(FileInputStream(file))
    }

    fun save() {
        props.getProperty("screenshot-path") ?: props.setProperty("screenshot-path", defaultScreenPath())
        props.getProperty("logs-path") ?: props.setProperty("logs-path", defaultLogPath())
        props.store(FileWriter(file), "")
    }

    fun getScreenPath(): Path {
        return Paths.get(props.getProperty("screenshot-path", defaultScreenPath()))
    }

    fun getLogPath(): Path {
        return Paths.get(props.getProperty("logs-path", defaultLogPath()))
    }

    private fun defaultScreenPath(): String = "${System.getProperty("user.home")}/Pictures/Frontier Developments/Elite Dangerous"

    private fun defaultLogPath(): String = "${System.getProperty("user.home")}/Saved Games/Frontier Developments/Elite Dangerous"
}
