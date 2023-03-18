package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

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

    println("Password:")
    val password = readln()

    val message = readMessageFromImage(inputImageFile, password)
    println("Message: $message")
}

fun hide() {
    println("Input image file:")
    val inputImageFile = readln()

    println("Output image file:")
    val outputImageFile = readln()

    println("Message to hide:")
    val message = readln()

    println("Password:")
    val password = readln()

    if (!File(inputImageFile).exists()) {
        println("Can't read input file!")
        return
    }

    if (!isImageBigEnough(inputImageFile, message)) {
        println("The input image is not large enough to hold this message.")
        return
    }

    saveImageWithMessage(inputImageFile, outputImageFile, message, password)

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

fun saveImageWithMessage(inputImageFile: String, outputImageFile: String, message: String, password: String) {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    val bits = xorBits(getFixedBits(message), password) + getFixedBits("\u0000\u0000\u0003")

    var index = 0
    start@ for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            if (index == bits.size) break@start
            val bit = bits[index++].toInt()
            val color = Color(image.getRGB(x, y)).let { Color(it.red, it.green, it.blue.and(254).or(bit)) }
            image.setRGB(x, y, color.rgb)
        }
    }

    ImageIO.write(image, "png", File(outputImageFile))
}

private fun getFixedBits(message: String): List<Byte> {
    return message.map { it.code }.map { it.toString(2).padStart(8, '0').map { char -> char.digitToInt().toByte() } }
        .flatten()
}


fun readMessageFromImage(inputImageFile: String, password: String): String {
    val image: BufferedImage = ImageIO.read(File(inputImageFile))

    val message = mutableListOf<Byte>()
    val end = getFixedBits("\u0000\u0000\u0003")

    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            message.add((Color(image.getRGB(x, y)).blue % 2).toByte())
            if (message.size >= end.size && message.size % 8 == 0 && message.takeLast(end.size) == end) {
                val joinToString =
                    xorBits(message, password).joinToString("").chunked(8).map { it.toInt(2).toChar() }.joinToString("")
                return "Message:\n$joinToString"
            }
        }
    }

    return "Message:\n${
        xorBits(message, password).joinToString("").chunked(8).map { it.toInt(2).toChar() }.joinToString("")
    }"
}

//refactor the xorBits function to use the xor function
fun xorBits(bits: List<Byte>, password: String): List<Byte> {
    val passwordBits = getFixedBits(password)
    return bits.mapIndexed { index, byte -> byte xor passwordBits[index % passwordBits.size] }
}
