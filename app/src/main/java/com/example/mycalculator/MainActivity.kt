package com.example.mycalculator

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mycalculator.R

class MainActivity : AppCompatActivity() {

    private lateinit var display: TextView
    private var expression: String = ""
    private var isNewOperation: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.display)
        display.setBackgroundColor(Color.rgb(100, 10, 10))
        val buttonClear: Button = findViewById(R.id.button_clear)
        val buttonDivide: Button = findViewById(R.id.button_divide)
        val buttonMultiply: Button = findViewById(R.id.button_multiply)
        val button7: Button = findViewById(R.id.button_7)
        val button8: Button = findViewById(R.id.button_8)
        val button9: Button = findViewById(R.id.button_9)
        val buttonSubtract: Button = findViewById(R.id.button_subtract)
        val button4: Button = findViewById(R.id.button_4)
        val button5: Button = findViewById(R.id.button_5)
        val button6: Button = findViewById(R.id.button_6)
        val buttonAdd: Button = findViewById(R.id.button_add)
        val button1: Button = findViewById(R.id.button_1)
        val button2: Button = findViewById(R.id.button_2)
        val button3: Button = findViewById(R.id.button_3)
        val buttonEquals: Button = findViewById(R.id.button_equals)
        val button0: Button = findViewById(R.id.button_0)

        button0.setOnClickListener {
            display.setBackgroundColor(Color.rgb(100, 10, 10))
            appendNumber("0")
        }
        button1.setOnClickListener {
            appendNumber("1")
            display.setBackgroundColor(Color.rgb(0, 0, 0))
        }
        button2.setOnClickListener { appendNumber("2") }
        button3.setOnClickListener { appendNumber("3") }
        button4.setOnClickListener { appendNumber("4") }
        button5.setOnClickListener { appendNumber("5") }
        button6.setOnClickListener { appendNumber("6") }
        button7.setOnClickListener { appendNumber("7") }
        button8.setOnClickListener { appendNumber("8") }
        button9.setOnClickListener { appendNumber("9") }

        buttonAdd.setOnClickListener { appendOperation("+") }
        buttonSubtract.setOnClickListener { appendOperation("-") }
        buttonMultiply.setOnClickListener { appendOperation("×") }
        buttonDivide.setOnClickListener { appendOperation("÷") }

        buttonClear.setOnClickListener {
            expression = ""
            isNewOperation = true
            display.text = "0"
        }

        buttonEquals.setOnClickListener { calculateResult() }
    }

    private fun appendNumber(digit: String) {
        if (isNewOperation) {
            expression = digit
            isNewOperation = false
        } else {
            expression += digit
        }
        display.text = expression
    }

    private fun appendOperation(op: String) {
        if (expression.isNotEmpty() && !isOperator(expression.last())) {
            expression += op
            display.text = expression
            isNewOperation = false
        } else if (isNewOperation) {
            expression = "0$op"
            display.text = expression
            isNewOperation = false
        }
    }

    private fun isOperator(char: Char): Boolean {
        return char == '+' || char == '-' || char == '×' || char == '÷'
    }

    private fun calculateResult() {
        val expr = display.text.toString().trim()

        if (expr.isEmpty() || isOperator(expr.last())) {
            display.text = "Error"
            expression = ""
            isNewOperation = true
            return
        }

        var operatorIndex = -1
        var operatorChar = ' '

        for (i in expr.indices) {
            val char = expr[i]
            if (isOperator(char)) {
                operatorIndex = i
                operatorChar = char
                break
            }
        }

        if (operatorIndex == -1 || operatorIndex == 0 || operatorIndex == expr.length - 1) {
            display.text = "Error"
            expression = ""
            isNewOperation = true
            return
        }

        val num1Str = expr.substring(0, operatorIndex)
        val num2Str = expr.substring(operatorIndex + 1)

        try {
            val num1 = num1Str.toDouble()
            val num2 = num2Str.toDouble()
            val result = when (operatorChar) {
                '+' -> num1 + num2
                '-' -> num1 - num2
                '×' -> num1 * num2
                '÷' -> if (num2 != 0.0) num1 / num2 else Double.NaN
                else -> Double.NaN
            }

            if (result.isNaN()) {
                display.text = "Error"
                expression = ""
            } else {
                val resultStr = result.toString()
                display.text = resultStr
                expression = resultStr
                isNewOperation = true
            }
        } catch (e: NumberFormatException) {
            display.text = "Error"
            expression = ""
            isNewOperation = true
        }
    }
}