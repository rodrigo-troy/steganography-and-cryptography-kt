package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    while (true) {
        println("Task (hide, show, exit):")

        when (val input = readln()) {
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
    println("Input image file:")
    val inputImageFile = readln()

    if (!File(inputImageFile).exists()) {
        println("Can't read input file!")
        return
    }

    val message = readMessageFromImage(inputImageFile)
    println("Message: $message")
}

fun hide() {
    println("Input image file:")
    val inputImageFile = readln()

    println("Output image file:")
    val outputImageFile = readln()

    println("Message to hide:")
    val message = readln()

    if (!File(inputImageFile).exists()) {
        println("Can't read input file!")
        return
    }

    if (!isImageBigEnough(inputImageFile, message)) {
        println("The input image is not large enough to hold this message.")
        return
    }

    saveImageWithMessage(inputImageFile, outputImageFile, message)

    println("Input Image: $inputImageFile")
    println("Output Image: $outputImageFile")
    println("Message saved in $outputImageFile image.")
}

fun isImageBigEnough(inputImageFile: String, message: String): Boolean {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))
    val messageBytes = message.encodeToByteArray().toMutableList()
    messageBytes.addAll(listOf(0.toByte(), 0.toByte(), 3.toByte()))

    val totalPixels = image.width * image.height
    val totalMessageBits = messageBytes.size * 8
    image.flush()

    return totalPixels >= totalMessageBits
}

fun saveImageWithMessage(inputImageFile: String, outputImageFile: String, message: String) {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    val binaryMessage: String = message.encodeToByteArray().toMutableList()
        .apply { addAll(listOf(0.toByte(), 0.toByte(), 3.toByte())) }
        .joinToString("") { it.toString(2).padStart(8, '0') }

    var index = 0
    (0 until image.height).forEach { y ->
        (0 until image.width).forEach { x ->
            if (index == binaryMessage.length) {
                ImageIO.write(image, "png", File(outputImageFile))
                return
            }

            val bit = binaryMessage[index++].digitToInt()
            val originalColor = Color(image.getRGB(x, y))
            val newColor = Color(originalColor.red, originalColor.green, originalColor.blue.and(254).or(bit))
            image.setRGB(x, y, newColor.rgb)
        }
    }

    ImageIO.write(image, "png", File(outputImageFile))
}

fun readMessageFromImage(inputImageFile: String): String {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    val messageBytes = mutableListOf<Byte>()
    var bitIndex = 0
    var currentByte = 0

    image.apply {
        loop@ for (y in 0 until height) {
            for (x in 0 until width) {
                val color = getRGB(x, y)
                val blue = color and 0x000000ff

                val messageBit = blue and 1
                currentByte = currentByte or (messageBit shl (7 - bitIndex))

                bitIndex++
                if (bitIndex == 8) {
                    messageBytes.add(currentByte.toByte())

                    if (messageBytes.size >= 3 &&
                        messageBytes[messageBytes.size - 3] == 0.toByte() &&
                        messageBytes[messageBytes.size - 2] == 0.toByte() &&
                        messageBytes[messageBytes.size - 1] == 3.toByte()
                    ) {
                        break@loop
                    }

                    bitIndex = 0
                    currentByte = 0
                }
            }
        }
    }

    messageBytes.removeAt(messageBytes.size - 1)
    messageBytes.removeAt(messageBytes.size - 1)
    messageBytes.removeAt(messageBytes.size - 1)

    return messageBytes.toByteArray().toString(Charsets.UTF_8)
}
