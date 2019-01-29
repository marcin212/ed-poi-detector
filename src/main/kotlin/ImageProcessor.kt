import info.debatty.java.stringsimilarity.WeightedLevenshtein
import net.sourceforge.tess4j.Tesseract
import org.bytedeco.javacpp.opencv_core.CV_8UC1
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.util.regex.Pattern

class ImageProcessor {
    val tes = Tesseract().apply { setDatapath("tessdata") }
    val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
    val point = Point(-1.0, -1.0)

    fun convertToBufferedImg(mat: Mat): BufferedImage {
        val buff = BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR)
        val buffByte = (buff.getRaster().dataBuffer as DataBufferByte).data
        mat.get(0, 0, buffByte)
        return buff
    }

    @Synchronized
    fun process(fileIn: File): Map<String, Int> {
        var rawimage = Imgcodecs.imread(fileIn.path)
      //  while (rawimage.width()==0 || rawimage.height()==0){
         //   rawimage = Imgcodecs.imread(fileIn.path)
          //  Thread.sleep(100)
         //   println("wait")
      //  }
        println("${fileIn.path}" )
        println("${rawimage.height()}x${rawimage.width()}" )
        val image = rawimage.submat(0, rawimage.height()/4, rawimage.width()/2, rawimage.width())

        val processedImage = rawimage.submat(0, rawimage.height()/4, rawimage.width()/2, rawimage.width())
        Imgcodecs.imwrite("test.bmp", processedImage)
        val ocrTest = processedImage.clone()
        val imageSize = Rect(0, 0, processedImage.width(), processedImage.height())

        Imgproc.cvtColor(processedImage, processedImage, Imgproc.COLOR_BGR2GRAY)

        Imgproc.threshold(processedImage, processedImage, 180.0, 255.0, Imgproc.THRESH_BINARY)
        Imgcodecs.imwrite("test1.bmp", processedImage)
        Imgproc.dilate(processedImage, processedImage, kernel, point, 8)
        Imgcodecs.imwrite("test2.bmp", processedImage)
        val listPOI = mutableListOf<MatOfPoint>()
        Imgproc.findContours(processedImage, listPOI, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)

        if(listPOI.isNotEmpty()) {
            val locationsPOI = listPOI.map { i -> Imgproc.boundingRect(i) }.reduce { r1, r2 ->
                if (distance(r1, imageSize) > distance(r2, imageSize)) {
                    r2
                } else {
                    r1
                }
            }
            //Imgproc.rectangle(image, locationsPOI, Scalar(0.0, 255.0, 0.0), 2)
            val imgLocations = image.submat(locationsPOI.y, locationsPOI.y + locationsPOI.height, locationsPOI.x, locationsPOI.x + locationsPOI.width)
            val mat = Mat(imgLocations.height()+20,imgLocations.width()+20, image.type())
            imgLocations.copyTo(mat.rowRange(10,imgLocations.height()+10).colRange(10, imgLocations.width()+10))
            Imgcodecs.imwrite("test3.bmp", mat)
            val buffImgLocations = convertToBufferedImg(ocrTest)
            return OcrProcess(tes.doOCR(buffImgLocations))
        }
        return mapOf()
    }


    fun distance(r1: Rect, r2: Rect) =
            Math.sqrt(Math.pow(r1.x - r2.width.toDouble(), 2.0) + Math.pow(r1.y - r2.y.toDouble(), 2.0))

    val alg = WeightedLevenshtein({ c1, c2 ->
        if (c1 == 'o' && c2 == '0') 0.5
        if (c1 == 'i' && c2 == 'l') 0.5
        1.0
    })

    val letters = Pattern.compile("([a-zA-Z]+)")
    val numbers = Pattern.compile("([0-9]+)")
    var map = listOf("ANY", "HUMAN", "THARGOID", "BIOLOGICAL", "GEOLOGICAL")
    val numgerPattern = "\\s?[(\\[{]([\\d]{1,3})[)\\]}]"
    var patternList = mapOf<String, Pattern>(
            "HUMAN" to Pattern.compile("HUMAN$numgerPattern"),
           "THARGOID" to Pattern.compile("THARG[Oo0][I1l]D$numgerPattern"),
           "BIOLOGICAL" to Pattern.compile("BI[Oo0]L[Oo0]G[I1l]CAL$numgerPattern"),
           "GEOLOGICAL" to Pattern.compile("GE[Oo0]L[Oo0]G[I1l]CAL$numgerPattern")
    )


    fun OcrProcess(str: String): Map<String, Int> {
        println("PROCESS:\n $str\n----------")
        val resultMap = mutableMapOf<String, Int>()
        val parts = str.split("\n")
        parts.forEach {
            val lett = letters.matcher(it)
            val nums = numbers.matcher(it)
            if (lett.find() && nums.find()) {
                val resultType = map.sortedBy { alg.distance(it, lett.group(1)) }.first()
                val resultNum = nums.group(1).toInt()
                resultMap[resultType] = resultNum
                println("$resultType ($resultNum)")
            }
        }
        return resultMap
    }
}