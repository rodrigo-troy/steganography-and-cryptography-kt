package cryptography

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
    println("Hiding message in image.")
}

