package calculator

import java.math.BigInteger
import java.util.Stack

class Calculator {
    private val operators = mapOf(')' to -1, '(' to 1, '+' to 2, '-' to 2, '*' to 3, '/' to 3, '^' to 4)
    private val memory = mutableMapOf<String, BigInteger>() // {a = 8, b = 0}

    private fun calculate(num1: BigInteger, num2: BigInteger, opr: Char): BigInteger {
        return when (opr) {
            '+' -> num1 + num2
            '-' -> num1 - num2
            '/' -> num1 / num2
            '*' -> num1 * num2
            '^' -> {
                if (num2 == BigInteger.ONE) num1 else num1 * calculate(num1, num2 - BigInteger.ONE,'^')
            }
            else -> BigInteger.ZERO
        }
    }

    /*
    * Метод получения ответа из любого выражения
    *
    * Если строка содержит только цифры и арифм.операторы -> выполняем выражение
    * Если в строке есть буквы без арифм. операторов (a, a = 9, a = 1 = 5, a = b) ->
    *  либо выводим это число (введена была просто буква
    *  либо добавляем в память переменную (при условии, что пройдены проверки)
    * Если в строке помимо букв есть ариф.операторы (a = b +7, a + b)
    * -> подставляем вместо букв их значения из памяти и проводим вычисления
    * */
    fun getAnswer(expr: String) {
        if (!checkParentheses(expr)) throw InvalidExpression()
        fun String.containsOnlyNumbers(): Boolean {
            var countNumbers = 0
            var countChars = 0
            val exprWithoutSpace = this.replace("\\s*".toRegex(), "")
            for (symbol in exprWithoutSpace) {
                if (operators.containsKey(symbol) || symbol == '=') continue

                if (symbol.isDigit()) {
                    countNumbers++
                } else if (symbol in 'a'..'z' || symbol in 'A'..'Z') {
                    countChars++
                } else {
                    throw UnknownOperator()
                }

            }

            return countNumbers > 0 && countChars == 0
        }

        val regexSimpleLetterExpr = "\\s*[a-zA-Z|(-?\\d)]+\\s*(=\\s*-?([a-zA-Z]|\\d)+\\s*)*".toRegex()
        val regexCorrectArithmeticExpr = "\\s*\\(*\\s*-?([a-zA-Z]|\\d)+\\s*(\\s*([-+]+|[*/^])\\s*\\(*\\s*([a-zA-Z]|\\d)+\\s*\\)*\\s*)+\\s*\\)*\\s*".toRegex()
        val regexCorrectAssignmentExpr = "\\s*[a-zA-Z]\\s*=(\\s*\\(*\\s*-?([a-zA-Z]|\\d)+\\s*(\\s*([-+]+|[*/^])\\s*\\(*\\s*([a-zA-Z]|\\d)+\\s*\\)*\\s*)+\\s*\\)*\\s*)".toRegex() //a = b + 7

        if (regexCorrectAssignmentExpr.matches(expr)) { // a = 9 + b
            val (variable,expression) = expr.split("=")
            val value: BigInteger = if (expression.containsOnlyNumbers()) {
                performArithmeticOperations(expression)
            } else {
                val numericExpr = convertLiteralToNumeric(expression)
                performArithmeticOperations(numericExpr)
            }
            val toPerform = "$variable = $value"
            println(toPerform)
            processLetterVariables(toPerform)
        } else if (regexCorrectArithmeticExpr.matches(expr)) {
            if (expr.containsOnlyNumbers()) { // -12 +- 9 , 7 9
                println(performArithmeticOperations(expr))
            } else { // a + 5
                val numericExpr = convertLiteralToNumeric(expr)
                println(performArithmeticOperations(numericExpr))
            }
        } else if (regexSimpleLetterExpr.matches(expr)){ // a, a = 7 = 8, a1a = n
            processLetterVariables(expr)
        } else {
            throw InvalidExpression()
        }
    }

    /*
    * Метод устраняет все повторения арифм операторов ( --- = -, -- = +)
    * */
    private fun replaceRepetitions(expr: String): String {
        val onlyDigitsRegex = "\\s*\\d+\\s*\\d+\\s*".toRegex()

        if (onlyDigitsRegex.matches(expr)) throw InvalidExpression()

        var last = ' '
        var curr: Char
        var next: Char
        var result = ""
        val characters = expr.filter { !it.isWhitespace() }

        for (i in characters.indices) {
            if (characters[i] == ' ') continue

            if (i == 0) {
                curr = characters[0]
                next = characters[1]
            } else if (i < characters.lastIndex) {
                curr = characters[i]
                next = characters[i + 1]
            } else { // i == characters.lastIndex
                curr = characters[i]
                next = ' '
            }

            if (curr.isDigit()) {
                if (next.isDigit()) result += curr else result += "$curr "
            } else if (operators.containsKey(curr)) {
                when (curr) {
                    '-' -> {
                        if (last == ' ' || last == '('){
                            result += curr
                        } else if (!last.isDigit()) {
                            if (curr == last) {
                                last = '+'
                                result = "${result.substring(0,result.lastIndex - 1)} $last "
                                continue
                            } else if (last == '+') {
                                last = '-'
                                result = "${result.substring(0,result.lastIndex - 1)} $last "
                                continue
                            } else if (last == ')') {
                                result += "$curr "
                            } else throw InvalidExpression()
                        } else {
                            result += "$curr "
                        }
                    }
                    '+' -> {
                        if (last == ' ' || last == '('){
                            last = curr
                            continue
                        } else if (!last.isDigit()) {
                            if (last == '+' || last == '-') {
                                last = curr
                                continue
                            } else throw InvalidExpression()
                        } else {
                            result += "$curr "
                        }
                    }
                    else -> result += "$curr "
                }
            } else throw InvalidExpression()
            last = curr
        }
        return result
    }

    private fun checkParentheses(expr: String): Boolean {
        var countRight = 0
        var countLeft = 0
        for (symbol in expr) {
            if (symbol == '(') countRight++
            else if (symbol == ')') countLeft++
            else continue
        }

        return countRight == countLeft
    }

    private fun getRPN(expr: String): String {
        var current = ""
        val stack = Stack<Char>()

        val term = replaceRepetitions(expr).split(" ")

        var priority: Int

        fun String.isNumeric(): Boolean {
            return this.toBigIntegerOrNull() != null
        }

        for (t in term) {
            if (t.isBlank()) continue

            if (t.isNumeric()) {
                current += "$t "
            } else if (operators.containsKey(t.first())) {
                priority = operators.getValue(t.first())

                when (priority) {
                    1 -> stack.push(t.first())
                    -1 -> {
                        while (operators.getValue(stack.peek()) != 1) current += "${stack.pop()} "
                        stack.pop() //delete '('
                    }
                    else -> {
                        while (stack.isNotEmpty()) {
                            if (operators.getValue(stack.peek()) >= priority) current += "${stack.pop()} " else break
                        }
                        stack.push(t.first())
                    }
                }

            }  else {
                continue
            }
        }

        while (stack.isNotEmpty()) current += "${stack.pop()} "

        return current
    }

    private fun performArithmeticOperations(expr: String): BigInteger {

        val stack: Stack<BigInteger> = Stack()

        val values = getRPN(expr).split(" ")

        for (value in values) {
            if (value.isBlank()) continue

            if (value.matches("-?\\d+(\\.\\d+)?".toRegex())) {
                stack.push(value.toBigInteger())
            } else if (operators.containsKey(value.first())) {
                val val1 = stack.pop()
                val val2 = stack.pop()
                stack.push(calculate(val2,val1,value.first()))
            } else throw InvalidExpression()
        }

        val result = stack.pop()

        if (!stack.isEmpty()) {
            throw InvalidExpression()
        }
        return result
    }

    private fun convertLiteralToNumeric(expr: String): String {
        var result = ""
        val symbols = expr.split("\\s*".toRegex())

        fun String.isNumeric(): Boolean {
            return this.toBigIntegerOrNull() != null
        }

        for (symbol in symbols) {
            if (symbol.isBlank()) continue

            result += if (symbol.isNumeric() || operators.containsKey(symbol.first())) {
                "$symbol "
            } else if (memory.containsKey(symbol)) {
                "${memory.getValue(symbol)} "
            } else if (!memory.containsKey(symbol)) {
                if (!operators.containsKey(symbol.first())) throw UnknownOperator()
                throw UnknownVariable()
            } else {
                throw InvalidExpression()
            }
        }

        return result
    }

    private fun processLetterVariables(str: String) {
        fun String.isNumeric(): Boolean {
            return this.toDoubleOrNull() != null
        }


        val regexOnlyValue = "\\s*[a-zA-Z]+\\s*".toRegex() //only value b
        val regexCorrectAssignment = "\\s*[a-zA-Z]+\\s*=\\s*-?(\\d+|\\w+)\\s*".toRegex() // value = digit(or other value)
        val regexInvalidAssignment = "[a-zA-Z]+\\s*(=\\s*((-?\\d+)|(\\w+))\\s*){2,}".toRegex() // value = digit(or other value) = ...

        if (str.isNumeric()) {
            println(str)
        } else if (regexInvalidAssignment.matches(str)) {
            throw InvalidAssignment()
        } else if (regexCorrectAssignment.matches(str)) {
            val (key,value) = str.replace("\\s".toRegex(), "").split("=")
            if (value.isNumeric()) {
                memory[key] = value.toBigInteger()
            } else if (!value.isNumeric() && memory.containsKey(value.replace("-",""))) { // b = 5; a = b
                if (!value.contains("-")) {
                    memory[key] = memory.getValue(value)
                } else {
                    memory[key] = memory.getValue(value.replace("-","")) * (-1).toBigInteger()
                }
            } else if (!memory.containsKey(value) && "[a-zA-Z]".toRegex().matches(value)) {
                throw UnknownVariable()
            } else {
                throw InvalidAssignment()
            }
        } else if (regexOnlyValue.matches(str)) {
            if (memory.containsKey(str.replace(" ",""))){
                println(memory.getValue(str.replace(" ","")))
            } else {
                throw UnknownVariable()
            }
        } else {
            throw InvalidIdentifier()
        }
    }
}