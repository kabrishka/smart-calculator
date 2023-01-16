package calculator

fun main() {
    val commandRegex = "/[a-zA-Z]+".toRegex()

    val calculator = Calculator()

    while (true) {
        val request = readln()
        if (request.isBlank()) continue

        if (commandRegex.matches(request)) {
            when (request) {
                "/help" -> println("The program calculates the sum of numbers")
                "/exit" -> break
                else -> println("Unknown command")
            }
        } else {
            try {
                calculator.getAnswer(request)
            } catch (e: UnknownOperator) {
                println(e.message)
            } catch (e: UnknownVariable) {
                println(e.message)
            } catch (e: InvalidIdentifier) {
                println(e.message)
            } catch (e: InvalidAssignment) {
                println(e.message)
            } catch (e: InvalidExpression) {
                println(e.message)
            }
        }
    }
    println("Bye!")
}
