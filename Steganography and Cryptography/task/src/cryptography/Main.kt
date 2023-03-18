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

    //check if input file exists
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

    //release the image
    image.flush()

    return totalPixels >= totalMessageBits
}


fun saveImageWithMessage(inputImageFile: String, outputImageFile: String, message: String) {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))
    val messageBytes = message.encodeToByteArray().toMutableList()

    messageBytes.addAll(listOf(0.toByte(), 0.toByte(), 3.toByte()))

    var messageIndex = 0
    var bitIndex = 0

    image.apply {
        loop@ for (y in 0 until height) {
            for (x in 0 until width) {
                val color = getRGB(x, y)
                val red = color and 0x00ff0000 shr 16
                val green = color and 0x0000ff00 shr 8
                val blue = color and 0x000000ff

                val messageBit = (messageBytes[messageIndex].toInt() shr bitIndex) and 1
                val newBlue = blue and 0b11111110 or messageBit

                val newColor = (red shl 16) or (green shl 8) or newBlue
                setRGB(x, y, newColor)

                if (messageIndex < messageBytes.size - 1) {
                    bitIndex++
                    if (bitIndex == 8) {
                        bitIndex = 0
                        messageIndex++
                    }
                } else {
                    break@loop
                }
            }
        }
    }
    ImageIO.write(image, "png", File(outputImageFile))
}

fun readMessageFromImage(inputImageFile: String): String {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    val messageBytes = mutableListOf<Byte>()

    var messageIndex = 0
    var bitIndex = 0
    var currentByte = 0

    image.apply {
        loop@ for (y in 0 until height) {
            for (x in 0 until width) {
                val color = getRGB(x, y)
                val blue = color and 0x000000ff

                val messageBit = blue and 1
                currentByte = currentByte or (messageBit shl bitIndex)

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

    // Remove the last 3 bytes (0, 0, 3) from the messageBytes list
    messageBytes.removeAt(messageBytes.size - 1)
    messageBytes.removeAt(messageBytes.size - 1)
    messageBytes.removeAt(messageBytes.size - 1)

    // Convert the messageBytes list to a String using UTF-8 charset
    return messageBytes.toByteArray().toString(Charsets.UTF_8)
}
