import kotlin.math.sqrt

const val PI = 3.14
fun main() {
    val area = when (readln()) {
        "triangle" -> {
            val a = readln().toDouble()
            val b = readln().toDouble()
            val c = readln().toDouble()
            val p = (a + b + c) / 2
            sqrt(p * (p - a) * (p - b) * (p - c))
        }
        "rectangle" -> {
            val a = readln().toDouble()
            val b = readln().toDouble()
            a * b
        }
        "circle" -> {
            val r = readln().toDouble()
            PI * r * r
        }
        else -> "No"
    }
    println(area)
}
