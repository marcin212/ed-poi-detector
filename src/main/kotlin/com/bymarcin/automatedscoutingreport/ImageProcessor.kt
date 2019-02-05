package com.bymarcin.automatedscoutingreport

import net.sourceforge.tess4j.Tesseract
import java.io.File
import java.util.regex.Pattern
import javax.imageio.ImageIO

class ImageProcessor {
    private val tes = Tesseract().apply { setDatapath("tessdata") }
    private val numberPattern = "\\s?[(\\[{]([\\d]{1,3})[)\\]}]"
    private val patternList = mapOf<String, Pattern>(
            "HUMAN" to Pattern.compile("HUMAN$numberPattern"),
            "THARGOID" to Pattern.compile("THARG[Oo0][I1l]D$numberPattern"),
            "BIOLOGICAL" to Pattern.compile("BI[Oo0]L[Oo0]G[I1l]CAL$numberPattern"),
            "GEOLOGICAL" to Pattern.compile("GE[Oo0]L[Oo0]G[I1l]CAL$numberPattern")
    )

    @Synchronized
    fun process(fileIn: File): Map<String, Int> {
        val image = ImageIO.read(fileIn)
        println("processing file(${image.width}x${image.height}): ${fileIn.path} ")
        val subImage = image.getSubimage(image.width / 2, 0, image.width / 2, image.height / 4)
        //ImageIO.write(subImage, "BMP", File("test.bmp"))
        return ocrProcess(tes.doOCR(subImage))
    }

    private fun ocrProcess(str: String): Map<String, Int> {
        println("PROCESS:\n----------\n $str \n----------")
        val resultMap = mutableMapOf<String, Int>()
        patternList.entries.forEach { entry ->
            val matcher = entry.value.matcher(str)
            if (matcher.find()) {
                resultMap[entry.key] = matcher.group(1).toInt()
                println("FIND: ${entry.key} (${resultMap[entry.key]})")
            }
        }
        return resultMap
    }
}