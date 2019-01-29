import com.google.gson.Gson
import data.Event
import java.io.File
import java.nio.file.Path
import java.util.*

class LogManager(val logPath: Path) {
    val gson = Gson()
    fun findNewestFile(): File {
        return logPath.toFile().listFiles().filter { it.extension.toLowerCase() == "log" }.reduce { f1, f2 -> if (f1.lastModified() > f2.lastModified()) f1 else f2 }
    }

    fun findeNewesScan(): Event? {
        val file = findNewestFile()
        val fr = Scanner(file)
        var lastScan: Event? = null
        var lastSystem: Event? = null
        while (fr.hasNextLine()) {
            val line = fr.nextLine()
            val event = gson.fromJson(line, Event::class.java)
            when {
                event.isDetailedScan() -> lastScan = event
                event.isFSDJump() -> lastSystem = event
            }
        }
        fr.close()
        if (lastScan != null && lastSystem != null) {
            lastScan.StarSystem = lastSystem.StarSystem
            return lastScan
        }
        return null
    }

}