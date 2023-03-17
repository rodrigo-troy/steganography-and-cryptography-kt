package cryptography

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        val input = readln()

        when (input) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> {
                exit()
                return
            }

            else -> println("Wrong task: $input")
        }
    }
}

fun exit() {
    println("Bye!")
}

fun show() {
    println("Obtaining message from image.")
}

fun hide() {
    println("Input image file:")
    val inputImageFile = readln()

    println("Output image file:")
    val outputImageFile = readln()

    //check if input file exists
    if (!File(inputImageFile).exists()) {
        println("Can't read input file!")
        return
    }

    saveImage(inputImageFile, outputImageFile)

    println("Input Image: $inputImageFile")
    println("Output Image: $outputImageFile")
    println("Image $outputImageFile is saved.")
}

fun saveImage(inputImageFile: String, outputImageFile: String) {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    image.apply {
        (0 until width).forEach { x ->
            (0 until height).forEach { y ->
                val color = getRGB(x, y)
                val red = color and 0x00ff0000 shr 16
                val green = color and 0x0000ff00 shr 8
                val blue = color and 0x000000ff

                ((red or 1 shl 16) or (green or 1 shl 8) or (blue or 1)).apply { setRGB(x, y, this) }
            }
        }
    }

    ImageIO.write(image, "png", File(outputImageFile))
}
