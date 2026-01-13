package io.github.digorydoo.kstruct.parser

internal class KstructTokeniser private constructor(private val text: String) {
    class BadDigitSequenceException(line: Int, part: String): Exception("Line $line: Bad digit sequence: $part")
    class MissingEndOfBackticksException(line: Int): Exception("Line $line: Missing end of backticks sequence")
    class MissingEndOfBlockCommentException(line: Int): Exception("Line $line: Missing end of block comment")
    class MissingEndOfDoubleQuoteException(line: Int): Exception("Line $line: Missing end of double quote")
    class MissingEndOfSingleQuoteException(line: Int): Exception("Line $line: Missing end of single quote")
    class MissingEndOfVerbatimStringException(line: Int): Exception("Line $line: Missing end of verbatim string")
    class UnexpectedCharException(line: Int, c: Char): Exception("Line $line: Unexpected character: $c")
    class UnexpectedEndOfStreamException(line: Int): Exception("Line $line: Unexpected end of stream")
    class TemplateStringsNotImplementedException(line: Int): Exception("Line $line: Template strings not implemented")

    class UnsupportedEscapeSequenceException(line: Int, c: Char):
        Exception("Line $line: Unsupported escape sequence: \\$c")

    private val tokens = mutableListOf<KstructToken<*>>()

    private enum class State {
        INITIAL,
        BACKSLASH,
        BACKTICK,
        BLOCK_COMMENT,
        BLOCK_COMMENT_ASTERISK,
        BLOCK_COMMENT_SLASH,
        DIGITS,
        DIGITS_DOT,
        DIGITS_DOT_E,
        DIGITS_E,
        DOT,
        DOUBLE_DOUBLE_QUOTE,
        DOUBLE_QUOTE,
        LINE_COMMENT,
        LITERAL,
        MINUS,
        SINGLE_QUOTE,
        SLASH,
        VERBATIM,
    }

    @Suppress("EmptyRange") // linter bug
    fun tokenise() {
        var line = 1
        var state = State.INITIAL
        var backslashCtx = State.INITIAL
        var digitStartIdx = -1
        var literalStartIdx = -1
        var literalPrefix = ""
        var verbatimQuotesCount = 0
        var commentLevel = 0
        val textLen = text.length
        var i = -1

        while (i + 1 < textLen) {
            val c = text[++i] // `i` incremented first!

            when (state) {
                State.INITIAL -> {
                    when (c) {
                        '\n' -> {
                            tokens.add(NewlineToken(line))
                            line++
                        }
                        '=' -> tokens.add(EqualsToken(line, c))
                        '+' -> tokens.add(PlusToken(line, c))
                        '(' -> tokens.add(OpeningParenToken(line, c))
                        ')' -> tokens.add(ClosingParenToken(line, c))
                        '[' -> tokens.add(OpeningBracketToken(line, c))
                        ']' -> tokens.add(ClosingBracketToken(line, c))
                        '{' -> tokens.add(OpeningBraceToken(line, c))
                        '}' -> tokens.add(ClosingBraceToken(line, c))
                        ',' -> tokens.add(CommaToken(line, c))
                        ';' -> tokens.add(SemicolonToken(line, c))
                        '*' -> tokens.add(AsteriskToken(line, c))
                        '/' -> state = State.SLASH
                        '-' -> state = State.MINUS
                        '.' -> state = State.DOT
                        '\'' -> {
                            state = State.SINGLE_QUOTE
                            literalStartIdx = i + 1
                            literalPrefix = ""
                        }
                        '"' -> {
                            state = State.DOUBLE_QUOTE
                            literalStartIdx = i + 1
                            literalPrefix = ""
                        }
                        '`' -> {
                            state = State.BACKTICK
                            literalStartIdx = i + 1
                            literalPrefix = ""
                        }
                        in '0' .. '9' -> {
                            state = State.DIGITS
                            digitStartIdx = i
                        }
                        in 'a' .. 'z', in 'A' .. 'Z' -> {
                            state = State.LITERAL
                            literalStartIdx = i
                            literalPrefix = ""
                        }
                        else -> when {
                            c.isWhitespaceExceptNewline() -> Unit
                            else -> throw UnexpectedCharException(line, c)
                        }
                    }
                }
                State.SLASH -> {
                    when (c) {
                        '/' -> state = State.LINE_COMMENT
                        '*' -> {
                            state = State.BLOCK_COMMENT
                            commentLevel = 1
                        }
                        else -> {
                            i-- // read c again
                            tokens.add(SlashToken(line, text[i]))
                            state = State.INITIAL
                        }
                    }
                }
                State.LINE_COMMENT -> {
                    if (c == '\n') {
                        tokens.add(NewlineToken(line))
                        line++
                        state = State.INITIAL
                    }
                }
                State.BLOCK_COMMENT -> {
                    when (c) {
                        '*' -> state = State.BLOCK_COMMENT_ASTERISK
                        '/' -> state = State.BLOCK_COMMENT_SLASH
                        '\n' -> line++ // update line counter, but don't emit newline token
                    }
                }
                State.BLOCK_COMMENT_ASTERISK -> {
                    when (c) {
                        '/' -> {
                            commentLevel--
                            state = if (commentLevel <= 0) State.INITIAL else State.BLOCK_COMMENT
                        }
                        '*' -> Unit
                        else -> {
                            state = State.BLOCK_COMMENT
                            i-- // read c again
                        }
                    }
                }
                State.BLOCK_COMMENT_SLASH -> {
                    when (c) {
                        '/' -> Unit
                        '*' -> {
                            commentLevel++
                            state = State.BLOCK_COMMENT
                        }
                        else -> {
                            state = State.BLOCK_COMMENT
                            i-- // read c again
                        }
                    }
                }
                State.MINUS -> {
                    when (c) {
                        '.' -> {
                            state = State.DIGITS_DOT
                            digitStartIdx = i - 1 // include minus
                            when (val ahead = text.getOrNull(i + 1)) {
                                in '0' .. '9' -> i++ // eat first digit
                                null -> throw UnexpectedEndOfStreamException(line)
                                else -> throw UnexpectedCharException(line, ahead)
                            }
                        }
                        in '0' .. '9' -> {
                            state = State.DIGITS
                            digitStartIdx = i - 1 // include minus
                        }
                        else -> {
                            i-- // read c again
                            tokens.add(MinusToken(line, text[i]))
                            state = State.INITIAL
                        }
                    }
                }
                State.DOT -> {
                    when (c) {
                        in '0' .. '9' -> {
                            state = State.DIGITS_DOT
                            digitStartIdx = i - 1 // include dot
                        }
                        else -> {
                            i-- // read c again
                            tokens.add(DotToken(line, text[i]))
                            state = State.INITIAL
                        }
                    }
                }
                State.DIGITS -> {
                    when (c) {
                        '\n' -> {
                            tokens.add(getIntOrLongToken(line, digitStartIdx ..< i))
                            tokens.add(NewlineToken(line))
                            line++
                            state = State.INITIAL
                        }
                        '-' -> {
                            // Minus belonging to the number has already been eaten before transition to DIGITS
                            tokens.add(getIntOrLongToken(line, digitStartIdx ..< i))
                            tokens.add(MinusToken(line, c)) // not part of the number
                            state = State.INITIAL
                        }
                        'l', 'L' -> {
                            tokens.add(LongToken(line, text.substring(digitStartIdx ..< i).toLong()))
                            state = State.INITIAL
                        }
                        'f', 'F' -> {
                            tokens.add(FloatToken(line, text.substring(digitStartIdx ..< i).toFloat()))
                            state = State.INITIAL
                        }
                        '.' -> {
                            val ahead = text.getOrNull(i + 1)
                            when (ahead) {
                                in '0' .. '9' -> state = State.DIGITS_DOT
                                else -> {
                                    tokens.add(getIntOrLongToken(line, digitStartIdx ..< i))
                                    state = State.INITIAL
                                    i-- // read the dot again
                                }
                            }
                        }
                        'e', 'E' -> {
                            var ahead = text.getOrNull(i + 1)

                            if (ahead == '+' || ahead == '-') {
                                i++ // eat exponent's plus or minus sign
                                ahead = text.getOrNull(i + 1)
                            }

                            when (ahead) {
                                in '0' .. '9' -> {
                                    i++ // eat first digit of exponent
                                    state = State.DIGITS_E
                                }
                                null -> throw UnexpectedEndOfStreamException(line)
                                else -> throw UnexpectedCharException(line, ahead)
                            }
                        }
                        in '0' .. '9' -> Unit
                        in 'a' .. 'z', in 'A' .. 'Z' -> throw UnexpectedCharException(line, c)
                        else -> {
                            tokens.add(getIntOrLongToken(line, digitStartIdx ..< i))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.DIGITS_DOT -> {
                    when (c) {
                        '\n' -> {
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(NewlineToken(line))
                            line++
                            state = State.INITIAL
                        }
                        '-' -> {
                            // Minus belonging to exponent has already been eaten before transition to DIGITS_DOT
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(MinusToken(line, c)) // not part of the number
                            state = State.INITIAL
                        }
                        'f', 'F' -> {
                            tokens.add(FloatToken(line, text.substring(digitStartIdx ..< i).toFloat()))
                            state = State.INITIAL
                        }
                        'e', 'E' -> {
                            val before = text.getOrNull(i - 1)

                            if (before == '.') {
                                tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                                state = State.INITIAL
                                i-- // read c again
                            } else {
                                var ahead = text.getOrNull(i + 1)

                                if (ahead == '+' || ahead == '-') {
                                    i++ // eat exponent's plus or minus sign
                                    ahead = text.getOrNull(i + 1)
                                }

                                when (ahead) {
                                    in '0' .. '9' -> {
                                        i++ // eat first digit of exponent
                                        state = State.DIGITS_DOT_E
                                    }
                                    null -> throw UnexpectedEndOfStreamException(line)
                                    else -> throw UnexpectedCharException(line, ahead)
                                }
                            }
                        }
                        '.', in 'a' .. 'z', in 'A' .. 'Z' -> throw UnexpectedCharException(line, c)
                        in '0' .. '9' -> Unit
                        else -> {
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.DIGITS_E -> {
                    when (c) {
                        '\n' -> {
                            // First digit of exponent was eaten before transition to DIGITS_E
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(NewlineToken(line))
                            line++
                            state = State.INITIAL
                        }
                        '-' -> {
                            // Minus belonging to exponent has already been eaten before transition to DIGITS_E
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(MinusToken(line, c)) // not part of the number
                            state = State.INITIAL
                        }
                        'l', 'L' -> {
                            tokens.add(LongToken(line, text.substring(digitStartIdx ..< i).toLong()))
                            state = State.INITIAL
                        }
                        'f', 'F' -> {
                            tokens.add(FloatToken(line, text.substring(digitStartIdx ..< i).toFloat()))
                            state = State.INITIAL
                        }
                        in '0' .. '9' -> Unit
                        in 'a' .. 'z', in 'A' .. 'Z' -> throw UnexpectedCharException(line, c)
                        else -> {
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.DIGITS_DOT_E -> {
                    when (c) {
                        '\n' -> {
                            // First digit of exponent was eaten before transition to DIGITS_DOT_E
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(NewlineToken(line))
                            line++
                            state = State.INITIAL
                        }
                        '-' -> {
                            // Minus belonging to exponent has already been eaten before transition to DIGITS_E
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            tokens.add(MinusToken(line, c)) // not part of the number
                            state = State.INITIAL
                        }
                        'f', 'F' -> {
                            tokens.add(FloatToken(line, text.substring(digitStartIdx ..< i).toFloat()))
                            state = State.INITIAL
                        }
                        in '0' .. '9' -> Unit
                        in 'a' .. 'z', in 'A' .. 'Z' -> throw UnexpectedCharException(line, c)
                        else -> {
                            tokens.add(DoubleToken(line, text.substring(digitStartIdx ..< i).toDouble()))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.LITERAL -> {
                    when (c) {
                        in 'a' .. 'z', in 'A' .. 'Z', in '0' .. '9', '-', '_' -> Unit
                        else -> {
                            tokens.add(Literal(line, literalPrefix + text.substring(literalStartIdx ..< i)))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.SINGLE_QUOTE -> {
                    when (c) {
                        '\'' -> {
                            tokens.add(
                                LiteralInSingleQuotes(line, literalPrefix + text.substring(literalStartIdx ..< i))
                            )
                            state = State.INITIAL
                        }
                        '\\' -> {
                            literalPrefix += text.substring(literalStartIdx ..< i)
                            literalStartIdx = -1
                            backslashCtx = state
                            state = State.BACKSLASH
                        }
                        '\n' -> throw MissingEndOfSingleQuoteException(line)
                    }
                }
                State.DOUBLE_QUOTE -> {
                    when (c) {
                        '"' -> {
                            if (literalStartIdx == i && literalPrefix.isEmpty()) {
                                state = State.DOUBLE_DOUBLE_QUOTE
                            } else {
                                tokens.add(StringToken(line, literalPrefix + text.substring(literalStartIdx ..< i)))
                                state = State.INITIAL
                            }
                        }
                        '\\' -> {
                            literalPrefix += text.substring(literalStartIdx ..< i)
                            literalStartIdx = -1
                            backslashCtx = state
                            state = State.BACKSLASH
                        }
                        '$' -> {
                            val next = text.getOrNull(i + 1)
                            when (next) {
                                null -> throw MissingEndOfDoubleQuoteException(line)
                                '"', in DOLLAR_TERMINATORS -> Unit
                                else -> throw TemplateStringsNotImplementedException(line)
                            }
                        }
                        '\n' -> throw MissingEndOfDoubleQuoteException(line)
                    }
                }
                State.DOUBLE_DOUBLE_QUOTE -> {
                    when (c) {
                        '"' -> {
                            state = State.VERBATIM
                            literalStartIdx = i + 1
                            literalPrefix = ""
                            verbatimQuotesCount = 0
                        }
                        else -> {
                            // The double double-quote was an empty string after all.
                            tokens.add(StringToken(line, ""))
                            state = State.INITIAL
                            i-- // read c again
                        }
                    }
                }
                State.BACKTICK -> {
                    when (c) {
                        '`' -> {
                            tokens.add(LiteralInBackticks(line, literalPrefix + text.substring(literalStartIdx ..< i)))
                            state = State.INITIAL
                        }
                        '\\' -> {
                            literalPrefix += text.substring(literalStartIdx ..< i)
                            literalStartIdx = -1
                            backslashCtx = state
                            state = State.BACKSLASH
                        }
                        '\n' -> throw MissingEndOfBackticksException(line)
                    }
                }
                State.VERBATIM -> {
                    if (c == '"') {
                        verbatimQuotesCount++

                        if (verbatimQuotesCount >= 3) {
                            val content = (literalPrefix + text.substring(literalStartIdx ..< i - 2))
                                .trimIndent()
                                .trimEnd()
                            tokens.add(StringToken(line, content))
                            state = State.INITIAL
                        }
                    } else {
                        if (verbatimQuotesCount > 0) {
                            literalPrefix += text.substring(literalStartIdx ..< i - verbatimQuotesCount)
                            repeat(verbatimQuotesCount) { literalPrefix += '"' }
                            verbatimQuotesCount = 0
                            literalStartIdx = i
                        }

                        when (c) {
                            '\\' -> {
                                literalPrefix += text.substring(literalStartIdx ..< i)
                                literalStartIdx = -1
                                backslashCtx = state
                                state = State.BACKSLASH
                            }
                            '$' -> {
                                val next = text.getOrNull(i + 1)
                                when (next) {
                                    null -> throw MissingEndOfVerbatimStringException(line)
                                    '"', in DOLLAR_TERMINATORS -> Unit
                                    else -> throw TemplateStringsNotImplementedException(line)
                                }
                            }
                            '\n' -> line++
                        }
                    }
                }
                State.BACKSLASH -> {
                    literalPrefix += when (c) {
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        '\\' -> '\\'
                        '\'' -> '\''
                        '"' -> '"'
                        '`' -> '`'
                        '$' -> '$'
                        else -> throw UnsupportedEscapeSequenceException(line, c)
                    }
                    state = backslashCtx
                    literalStartIdx = i + 1 // literal continues here
                    backslashCtx = State.INITIAL
                }
            }
        }

        // We have made sure that the text always ends in a newline (see companion object), therefore we should be
        // back to INITIAL state.
        when (state) {
            State.INITIAL -> Unit

            State.BLOCK_COMMENT,
            State.BLOCK_COMMENT_ASTERISK,
            State.BLOCK_COMMENT_SLASH,
            -> throw MissingEndOfBlockCommentException(line)

            State.BACKTICK -> throw MissingEndOfBackticksException(line)
            State.SINGLE_QUOTE -> throw MissingEndOfSingleQuoteException(line)
            State.DOUBLE_QUOTE, State.DOUBLE_DOUBLE_QUOTE -> throw MissingEndOfDoubleQuoteException(line)
            State.VERBATIM -> throw MissingEndOfVerbatimStringException(line)
            else -> throw UnexpectedEndOfStreamException(line)
        }
    }

    private fun Char.isWhitespaceExceptNewline() = when (this) {
        '\n' -> false
        else -> isWhitespace()
    }

    private fun getIntOrLongToken(line: Int, range: IntRange): KstructToken<*> {
        val part = text.substring(range)

        try {
            return IntToken(line, part.toInt())
        } catch (_: NumberFormatException) {
            // probably value out of range
        }

        try {
            return LongToken(line, part.toLong())
        } catch (_: NumberFormatException) {
            throw BadDigitSequenceException(line, part)
        }
    }

    companion object {
        fun tokenise(text: CharSequence): List<KstructToken<*>> =
            KstructTokeniser("$text\n") // always add a newline, which simplifies finishing the state
                .apply { tokenise() }
                .tokens

        private const val DOLLAR_TERMINATORS = "$ \n\r\t%&'()*+,-./:;<=>[]^`}~§°0123456789\\"
    }
}
