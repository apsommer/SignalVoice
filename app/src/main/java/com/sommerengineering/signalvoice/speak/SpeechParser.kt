package com.sommerengineering.signalvoice.speak

import com.sommerengineering.signalvoice.uitls.RomanNumerals

object SpeechParser {

    private val units = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen",
        "sixteen", "seventeen", "eighteen", "nineteen"
    )

    private val tens = listOf(
        "", "", "twenty", "thirty", "forty",
        "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    fun normalizeMessage(message: String): String {

        // punctuation
        var spokenText = message
            .replace(Regex("""\s*•\s*"""), ", ") // bullet to comma
            .replace(Regex("""(?<=\d),(?=\d)"""), "") // remove thousands separators

        // roman numerals to words, handle voice names
        spokenText =
            Regex("""\b(I|II|III|IV|V|VI|VII|VIII|IX|X|XI|XII|XIII|XIV|XV|XVI|XVII|XVIII|XIX|XX)\b""")
                .replace(spokenText) { RomanNumerals.toWord(it.value) }

        // 'm' to minutes, 123m -> 123 minutes
        spokenText = Regex("""\b(\d+)m\b""")
            .replace(spokenText) {
                val value = it.groupValues[1]
                if (value == "1") "$value minute" else "$value minutes"
            }

        // numbers to words, prevent "oh" instead of "zero"
        spokenText = Regex("""[+-]?\d+(\.\d+)?%?""")
            .replace(spokenText) { match ->

                val raw = match.value

                // remove percent symbol
                val isPercent = raw.endsWith("%")
                val clean = raw.removeSuffix("%")

                // +/- symbol to word
                val sign = when {
                    clean.startsWith("+") -> "plus "
                    clean.startsWith("-") -> "minus "
                    else -> ""
                }
                val number = clean.trimStart('+', '-')

                // parse number
                val spokenNumber =

                    if (number.contains(".")) {

                        // split into integer and decimal
                        val (intPart, rawDecPart) = number.split(".")
                        val decPart = rawDecPart.trimEnd('0')

                        val spokenInteger = numberToWords(intPart.toInt())

                        // trim trailing zeros
                        if (decPart.isEmpty()) {
                            spokenInteger

                            // . symbol to word
                        } else {
                            val decimals = decPart
                                .map { units[it.digitToInt()] }
                                .joinToString(" ")
                            "$spokenInteger point $decimals"
                        }

                    } else {
                        numberToWords(number.toInt())
                    }

                buildString {
                    append(sign)
                    append(spokenNumber)
                    if (isPercent) append(" percent")
                }
            }

        return spokenText
    }

    private fun numberToWords(number: Int): String {

        if (number < 20)
            return units[number]

        if (number < 100) {
            val tensPart = tens[number / 10]
            val unitsPart = number % 10
            return if (unitsPart == 0) tensPart else "$tensPart ${units[unitsPart]}"
        }

        if (number < 1000) {
            val hundredsPart = "${units[number / 100]} hundred"
            val remainder = number % 100
            return if (remainder == 0) hundredsPart else "$hundredsPart ${numberToWords(remainder)}"
        }

        if (number < 1_000_000) {
            val thousandsPart = "${numberToWords(number / 1000)} thousand"
            val remainder = number % 1000
            return if (remainder == 0) thousandsPart else "$thousandsPart ${numberToWords(remainder)}"
        }

        return number.toString()
    }
}