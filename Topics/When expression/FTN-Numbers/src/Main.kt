import kotlin.math.*

fun main() {
    fun Int.isFibonacci(): Boolean {
        return if (this <= 55) (sqrt(5 * this.toDouble().pow(2.0) - 4) % 1 ) == 0.0
                || (sqrt(5 * this.toDouble().pow(2.0) + 4) % 1 ) == 0.0
        else false
    }

    fun Int.isTriangle() : Boolean {
        if (this > 45) return false
        val value = -0.5 + sqrt((1 + 8 * this).toDouble()) / 2.0;
        if (value - value.toInt() == 0.0) return true
        return false
    }

    fun Int.isPower(): Boolean {
        if (this > 100000) return false
        return this % 10 == 0
    }

    val num = readln().toInt()
    when {
        num.isFibonacci() -> println("F")
        num.isTriangle() -> println("T")
        num.isPower() -> println("P")
        else -> println("N")
    }
}