package com.bymarcin.automatedscoutingreport

import javafx.scene.Parent
import javafx.scene.control.TextField
import tornadofx.*
import java.nio.file.Paths

class SettingsView : View(){
    override val root: Parent by fxml("/Settings.fxml")
    val cmdrName by fxid<TextField>("cmdrName")
    val logPath by fxid<TextField>("logPath")
    val screenshotPath by fxid<TextField>("screenshotPath")
    val region by fxid<TextField>("region")
    val settings: ApplicationSettings by param()
    init {

    }

    override fun onBeforeShow() {
        cmdrName.text = settings.getCmdrName()
        logPath.text  = settings.getLogPath().toString()
        screenshotPath.text = settings.getScreenPath().toString()
        region.text = settings.getRegion()
    }

    fun onSaveForm(){
        settings.setCmdrName(cmdrName.text)
        settings.setLogPath(Paths.get(logPath.text))
        settings.setScreenPath(Paths.get(screenshotPath.text))
        settings.setRegion(region.text)
        settings.save()
        close()
    }

    fun onCancelForm(){
        close()
    }

}