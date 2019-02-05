package com.bymarcin.automatedscoutingreport

import com.bymarcin.automatedscoutingreport.data.SrfUrlBuilder
import javafx.concurrent.Worker
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.web.WebView
import javafx.stage.Stage
import tornadofx.*
import java.io.File

class EDApp : App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
       // stage.isResizable = false
    }
}

class MainView : View() {
    override val root: Parent by fxml("/Form.fxml")
    private val progress by fxid<ProgressBar>("progress")
    private val progressStatus by fxid<Label>("progressStatus")
    private val webview by fxid<WebView>("webview")
    private var watcher: Watcher? = null
    private val settings: ApplicationSettings = ApplicationSettings()
    private val logManager = LogManager(settings.getLogPath())

    init {
        webview.engine.userStyleSheetLocation = File("./my.css").toURI().toString()
        webview.engine.loadWorker.stateProperty().addListener { observable, oldValue, newValue ->
            if (newValue == Worker.State.SUCCEEDED) {
                runLater {
                    this@MainView.progress.progress = 1.0
                    progressStatus.text = "Done"
                }
            }
        }


        //TODO rewrite this mess
        watcher = Watcher(settings.getScreenPath()) {
            runLater {
                progress.progress = 0.05
                progressStatus.text = "Searching planet info"
            }
            val log = logManager.findLatestScan()
            runLater {
                this@MainView.progress.progress = 0.4
                progressStatus.text = "Processing screenshot"
            }
            val imgProcessResult = try {
                ImageProcessor().process(it.toFile())
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap<String, Int>()
            }
            runLater {
                this@MainView.progress.progress = 0.6
                progressStatus.text = "Creating url"
            }
            val urlBuilder = SrfUrlBuilder()
            imgProcessResult.entries.stream().forEach { entry ->
                when (entry.key) {
                    "BIOLOGICAL" -> urlBuilder.bio(entry.value)
                    "HUMAN" -> urlBuilder.human(entry.value)
                    "GEOLOGICAL" -> urlBuilder.geo(entry.value)
                    "THARGOID" -> urlBuilder.thargoid(entry.value)
                }
            }
            urlBuilder.scoutedBy(settings.getCmdrName())
            urlBuilder.region(settings.getRegion())
            log?.let { l ->
                urlBuilder.planetName(l.BodyName)
                urlBuilder.planetType(l.PlanetClass)
                urlBuilder.materials(l.Materials)
                urlBuilder.system(l.StarSystem)
            }

            runLater {
                this@MainView.progress.progress = 0.75
                progressStatus.text = "Loading url"
                println(urlBuilder.build())
                webview.engine.load(urlBuilder.build())
            }


        }
        watcher?.start()
    }

    override fun onUndock() {
        super.onUndock()
        watcher?.isWorking = false
    }

    fun openSettings() {
        val modal = find<SettingsView>(scope, mapOf(SettingsView::settings to settings))
        modal.openModal(block = true)
    }
}

fun main(args: Array<String>) {
    launch<EDApp>(args)
}
