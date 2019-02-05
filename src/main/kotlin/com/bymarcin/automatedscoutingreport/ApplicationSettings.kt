package com.bymarcin.automatedscoutingreport

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
        props.getProperty("cmdr") ?: props.setProperty("cmdr", defaultCmdrName())
        props.getProperty("region") ?: props.setProperty("region", defaultRegion())
        props.store(FileWriter(file), "")
    }

    fun setRegion(name: String){
        props.setProperty("region", name)
    }

    fun getRegion(): String{
        return props.getProperty("region", defaultRegion())
    }

    fun setScreenPath(p:Path){
        props.setProperty("screenshot-path", p.toString())
    }

    fun setLogPath(p:Path){
        props.setProperty("logs-path", p.toString())
    }

    fun setCmdrName(name: String) {
       props.setProperty("cmdr", name)
    }

    fun getScreenPath(): Path {
        return Paths.get(props.getProperty("screenshot-path", defaultScreenPath()))
    }

    fun getLogPath(): Path {
        return Paths.get(props.getProperty("logs-path", defaultLogPath()))
    }

    fun getCmdrName(): String {
        return props.getProperty("cmdr", defaultCmdrName())
    }

    private fun defaultScreenPath(): String = "${System.getProperty("user.home")}/Pictures/Frontier Developments/Elite Dangerous"

    private fun defaultLogPath(): String = "${System.getProperty("user.home")}/Saved Games/Frontier Developments/Elite Dangerous"

    private fun defaultCmdrName(): String = "N/A"
    private fun defaultRegion(): String = "Inner Orion Spur"
}
