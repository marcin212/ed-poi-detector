package com.bymarcin.automatedscoutingreport

import com.bymarcin.automatedscoutingreport.data.Event
import com.google.gson.Gson
import java.io.File
import java.nio.file.Path
import java.util.*

class LogManager(val logPath: Path) {
    val gson = Gson()
    fun findNewestFile(): List<File> {
        return logPath.toFile().listFiles().filter { it.extension.toLowerCase() == "log" }.sortedByDescending { f1 -> f1.lastModified() }
    }

    fun findLatestScan(): Event? {
        val files = findNewestFile()
        var lastScan: Event? = null
        var lastSystem: Event? = null
        for (file in files) {
            val fr = Scanner(file)
            var lastScanInFile: Event? = null
            var lastSystemInFile: Event? = null
            while (fr.hasNextLine()) {
                val line = fr.nextLine()
                val event = gson.fromJson(line, Event::class.java)
                when {
                    event.isDetailedScan() -> lastScanInFile = event
                    event.isFSDJump() -> lastSystemInFile = event
                }

            }
            if (lastScan == null) {
                lastScan = lastScanInFile
            }
            if (lastSystem == null) {
                lastSystem = lastSystemInFile
            }

            fr.close()
            if (lastSystem != null && lastScan != null) {
                break
            }
        }

        if (lastScan != null && lastSystem != null) {
            lastScan.StarSystem = lastSystem.StarSystem
            return lastScan
        }
        return null
    }

}