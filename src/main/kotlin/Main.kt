import com.google.gson.Gson
import com.sun.deploy.net.URLEncoder
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.web.WebView
import javafx.stage.Stage
import net.sourceforge.tess4j.Tesseract
import org.bytedeco.javacpp.Loader
import org.bytedeco.javacpp.opencv_java
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import tornadofx.*
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.imageio.ImageIO
import khttp.get

class EDApp : App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false
    }
}


fun String.encodeURL(): String {
    return URLEncoder.encode(this, "UTF-8")
}
fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

val region = "Inner Orion Spur"

class ScoutForm(
        val planetName: String,
        val planetType: String,
        val planetMaterials: String,
        val system: String,
        val region: String = "Inner Orion Spur",
        val bio: String = "0",
        val geo: String ="0",
        val thargoid: String="0",
        val scoutedBy: String="0",
        val stellarPhenomena: Boolean = false
        )

class MainView : View() {
    override val root: Parent by fxml("Form.fxml")
    val planetName by fxid<TextField>("planetName")
    val planetType by fxid<TextField>("planetType")
    val planetMaterials by fxid<TextArea>("planetMaterials")
    val system by fxid<TextField>("system")
    val region by fxid<TextField>("region")
    val bio by fxid<TextField>("bio")
    val geo by fxid<TextField>("geo")
    val human by fxid<TextField>("human")
    val thargoid by fxid<TextField>("thargoid")
    val scouted by fxid<TextField>("scouted")
    val stellarPhenomena by fxid<CheckBox>("stellarPhenomena")
    val progress by fxid<ProgressBar>("progress")
    val accept by fxid<Button>("accept")
    val webview by fxid<WebView>("webview")

    var watcher: Watcher? = null
    val settings: ApplicationSettings = ApplicationSettings()
   // val imgproc = ImageProcessor()
    val logManager = LogManager(settings.getLogPath())

    init {

        //accept.isDisable = true
        accept.action {
            val a = """
            {
            "planetName": "Pyramoe PM-X b33-6 C 7 a",
            "planetType": "ICY",
            "planetMaterials": "[sulphur: 27.256313%, carbon: 22.919737%, phosphorus: 14.67361%, iron: 12.209347%, nickel: 9.234638%, manganese: 5.042336%, zinc: 3.318043%, vanadium: 2.998191%, cadmium: 0.948111%, yttrium: 0.72925%, tungsten: 0.670416%]",
            "system": "Pyramoe PM-X b33-6",
            "region": "Inner Orion Spur",
            "bio": "0",
            "geo": "20",
            "thargoid": "0",
            "scoutedBy": "marcin212",
            "stellarPhenomena": false
            }
            """
            val map = mapOf<String, String>(
                    "HIGH METAL CONTENT" to "HMC",
                    "METAL RICH" to "Metal-Rich",
                    "ROCKY" to "Rocky",
                    "ICY" to "Ice",
                    "ROCKY ICE" to "Rocky Ice"
            )
            val form  = Gson().fromJson(a, ScoutForm::class.java)
            println(form.planetName)
            webview.engine.load(
            "https://airtable.com/shrpoiulL1A3IFGeu?" +
                    "prefill_Region=${form.region.encodeURL()}" +
                    "&prefill_System=${form.system.encodeURL()}" +
                    "&prefill_Planet+Name=${form.planetName.encodeURL()}" +
                    "&prefill_Planet+Type=${map[form.planetType]?.encodeURL()}" +
                    "&prefill_Planet+Materials=${form.planetMaterials
                            .replace("%","")
                            .replace("[","")
                            .replace("]","")
                            .replace(", ", ",\n")
                            .replace(Regex("([0-9.]+)")) { "[${it.groups[1]?.value?.toDouble()?.format(2)}]"}
                            .replace(Regex("(^|\n)([a-z])")) { "${it.groups[1]?.value}${it.groups[2]?.value?.toUpperCase()}"}

                            .trim().encodeURL()
                    }" +
                    "&prefill_Scouted+by=${form.scoutedBy.encodeURL()}"+
                    "&prefill_Bio+POI's=${form.bio}"+
                    "&prefill_Geo+POI's=${form.geo}"+
                    "&prefill_Human+POI's=${0}"+
                    "&prefill_Thargoid+POI's=${form.thargoid}"
            )
            webview.engine.userStyleSheetLocation = File("./my.css").toURI().toString()


          /*
            progress.progress = 0.0
            accept.isDisable = true
            val form  = ScoutForm(
                    planetName.text,
                    planetType.text,
                    planetMaterials.text,
                    system.text,
                    region.text,
                    bio.text,
                    geo.text,
                    thargoid.text,
                    "marcin212",
                    stellarPhenomena.isSelected
            )
            val fw = FileWriter("raport.json", true)
            fw.append(Gson().newBuilder().setPrettyPrinting().create().toJson(form)).append(",\n").flush()
            fw.close()
            */
        }

        watcher = Watcher(settings.getScreenPath()){
            runLater { progress.progress = ProgressBar.INDETERMINATE_PROGRESS }
            runAsync { logManager.findeNewesScan() }.ui {
                if(it!=null) {
                    planetName.text = it.BodyName
                    planetType.text = it.PlanetClass.toUpperCase().replace("BODY","").trim()
                    planetMaterials.text = Arrays.toString(it.Materials.toTypedArray())
                    system.text = it.StarSystem
                    region.text = "Inner Orion Spur"
                    scouted.text = "marcin212"
                }
            }
            runAsync {
                try {
                    ImageProcessor().process(it.toFile())
                }catch (e: Exception){
                    e.printStackTrace()
                    emptyMap<String,Int>()
                }
            }.ui { map ->
                bio.text = 0.toString()
                human.text =0.toString()
                geo.text =0.toString()
                thargoid.text = 0.toString()
                map.entries.stream().forEach { entry->
                    when(entry.key){
                        "BIOLOGICAL" -> bio.text = entry.value.toString()
                        "HUMAN" -> human.text = entry.value.toString()
                        "GEOLOGICAL" -> geo.text = entry.value.toString()
                        "THARGOID" -> thargoid.text = entry.value.toString()
                    }
                }
                progress.progress = 100.0
                accept.isDisable = false
            }
        }
        watcher?.start()
    }

    override fun onDelete() {
        super.onDelete()
        watcher?.isWorking = false
    }
}

fun main(args: Array<String>) {
    Loader.load(opencv_java::class.java)
    launch<EDApp>(args)
}
