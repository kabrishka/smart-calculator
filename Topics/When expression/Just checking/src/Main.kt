fun main() {
    val str = readln()
    val answer = when (str) {
        "1","3","4" -> "No!"
        "2" -> "Yes!"
        else -> "Unknown number"
    }
    println(answer)
}
