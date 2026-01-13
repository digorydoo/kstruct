package io.github.digorydoo.kstruct.parser

import io.github.digorydoo.kstruct.*

class KstructParser private constructor(private val tokens: List<KstructToken<*>>) {
    class DuplicateKeyException(line: Int, content: String): Exception("Line $line: Duplicate key: $content")
    class EmptyKeyNotAllowedException(line: Int): Exception("Line $line: Empty key not allowed")
    class MissingCloseBraceException(line: Int): Exception("Line $line: Missing closing brace")
    class MissingCloseBracketException(line: Int): Exception("Line $line: Missing closing bracket")
    class MissingCloseParenException(line: Int): Exception("Line $line: Missing closing parenthesis")
    class MissingCommaException(line: Int): Exception("Line $line: Missing comma")
    class MissingEqualSignException(line: Int): Exception("Line $line: Missing equals sign")
    class MissingKeyException(line: Int): Exception("Line $line: Missing key")
    class MissingSemicolonException(line: Int): Exception("Line $line: A semicolon is required between assignments")
    class MissingValueException(line: Int): Exception("Line $line: Missing value")
    class SuperfluousCloseBraceException(line: Int): Exception("Line $line: Superfluous closing brace")
    class SuperfluousCommaException(line: Int): Exception("Line $line: Superfluous comma")
    class SuperfluousEqualSignException(line: Int): Exception("Line $line: Superfluous equal sign")

    class UnexpectedLiteralException(line: Int, content: String):
        Exception("Line $line: Unexpected literal: '$content'")

    class IllegalCharLiteralException(line: Int, content: String):
        Exception("Line $line: Illegal character literal: '$content'")

    class UnexpectedTokenException internal constructor(token: KstructToken<*>): Exception(
        "Line ${token.line}: Unexpected token" + (token.content.toString().firstOrNull()?.let { " $it" } ?: "")
    )

    private var nextTokenIdx = 0
    private val lastLine = tokens.lastOrNull()?.line ?: -1

    private fun nextToken() = when {
        nextTokenIdx !in tokens.indices -> null
        else -> tokens[nextTokenIdx++]
    }

    fun readMapChildren(map: KstructMap, isRoot: Boolean) {
        var nextRequiresSeparator = false

        while (true) {
            when (val token = nextToken()) {
                is Literal,
                is LiteralInBackticks,
                -> {
                    if (nextRequiresSeparator) throw MissingSemicolonException(token.line)
                    val key = token.content
                    if (key.isEmpty()) throw EmptyKeyNotAllowedException(token.line)
                    if (map.children.containsKey(key)) throw DuplicateKeyException(token.line, key)
                    val value = readAfterMapKey()
                    map.children[key] = KstructNode(value)

                    nextRequiresSeparator = tokens.getOrNull(nextTokenIdx - 1).let {
                        it !is NewlineToken && it !is ClosingBraceToken && it !is ClosingBracketToken && it !is SemicolonToken && it !is CommaToken
                    }
                }

                is NewlineToken,
                is SemicolonToken,
                -> nextRequiresSeparator = false

                is ClosingBraceToken -> {
                    if (isRoot) throw SuperfluousCloseBraceException(token.line)
                    else return
                }

                is DoubleToken,
                is EqualsToken,
                is FloatToken,
                is IntToken,
                is LiteralInSingleQuotes,
                is LongToken,
                is OpeningBraceToken,
                is OpeningBracketToken,
                is OpeningParenToken,
                is StringToken,
                -> throw MissingKeyException(token.line)

                is AsteriskToken,
                is ClosingBracketToken,
                is ClosingParenToken,
                is CommaToken,
                is MinusToken,
                is DotToken,
                is PlusToken,
                is SlashToken,
                -> throw UnexpectedTokenException(token)

                null -> {
                    if (isRoot) return
                    else throw MissingCloseBraceException(lastLine)
                }
            }
        }
    }

    fun readAfterMapKey(): KstructValue {
        var newMap: KstructMap? = null

        while (true) {
            when (val token = nextToken()) {
                is EqualsToken -> return readValue()

                is OpeningParenToken -> {
                    require(newMap == null) // because the OpenBrace case always exits
                    newMap = KstructMap(mutableMapOf(), mutableMapOf())
                    readAttributes(newMap.attributes)
                    // don't return, OpenBrace may follow
                }

                is OpeningBraceToken -> {
                    // newMap may already be defined if there were attributes
                    if (newMap == null) newMap = KstructMap(mutableMapOf(), mutableMapOf())
                    readMapChildren(newMap, isRoot = false) // eats CloseBrace
                    return newMap
                }

                is Literal,
                is ClosingBraceToken,
                is LiteralInBackticks,
                -> {
                    if (newMap == null) throw MissingValueException(token.line)
                    nextTokenIdx-- // read token again
                    return newMap
                }

                is NewlineToken -> Unit

                is SemicolonToken -> {
                    if (newMap == null) throw MissingValueException(token.line)
                    else return newMap
                }

                is DoubleToken,
                is FloatToken,
                is IntToken,
                is LiteralInSingleQuotes,
                is LongToken,
                is OpeningBracketToken,
                is StringToken,
                -> throw MissingEqualSignException(token.line)

                is AsteriskToken,
                is ClosingBracketToken,
                is ClosingParenToken,
                is CommaToken,
                is MinusToken,
                is DotToken,
                is PlusToken,
                is SlashToken,
                -> throw UnexpectedTokenException(token)

                null -> {
                    if (newMap == null) throw MissingEqualSignException(lastLine)
                    return newMap
                }
            }
        }
    }

    private fun readValue(): KstructValue {
        while (true) {
            when (val token = nextToken()) {
                is Literal -> return when (token.content) {
                    "true" -> KstructBoolean(true)
                    "false" -> KstructBoolean(false)
                    "null" -> KstructNull()
                    else -> throw UnexpectedLiteralException(token.line, token.content)
                }

                is LiteralInSingleQuotes -> when {
                    token.content.length != 1 -> throw IllegalCharLiteralException(token.line, token.content)
                    else -> return KstructChar(token.content.first())
                }

                is StringToken -> return KstructString(token.content)
                is IntToken -> return KstructInt(token.content)
                is LongToken -> return KstructLong(token.content)
                is FloatToken -> return KstructFloat(token.content)
                is DoubleToken -> return KstructDouble(token.content)

                is OpeningBraceToken -> {
                    val newMap = KstructMap(mutableMapOf(), mutableMapOf())
                    readMapChildren(newMap, isRoot = false) // eats CloseBrace
                    return newMap
                }

                is OpeningBracketToken -> {
                    val newList = KstructList(mutableListOf())
                    readListChildren(newList) // eats CloseBracket
                    return newList
                }

                is ClosingBraceToken,
                is LiteralInBackticks,
                -> throw MissingValueException(token.line)

                is AsteriskToken,
                is ClosingBracketToken,
                is ClosingParenToken,
                is CommaToken,
                is MinusToken,
                is OpeningParenToken,
                is DotToken,
                is PlusToken,
                is SlashToken,
                -> throw UnexpectedTokenException(token)

                is NewlineToken -> Unit
                is SemicolonToken -> throw MissingValueException(token.line)
                is EqualsToken -> throw SuperfluousEqualSignException(token.line)
                null -> throw MissingValueException(lastLine)
            }
        }
    }

    fun readAttributes(attributes: MutableMap<String, KstructAttribute>) {
        var key: String? = null
        var nextRequiresComma = false

        while (true) {
            when (val token = nextToken()) {
                is Literal -> {
                    if (nextRequiresComma) throw MissingCommaException(token.line)
                    if (key != null) throw MissingEqualSignException(token.line)
                    key = token.content
                    if (key.isEmpty()) throw EmptyKeyNotAllowedException(token.line)
                    if (attributes.containsKey(key)) throw DuplicateKeyException(token.line, key)
                }

                is ClosingParenToken -> {
                    if (key != null) throw MissingEqualSignException(token.line)
                    return
                }

                is EqualsToken -> {
                    if (key == null) throw MissingKeyException(token.line)
                    val value = readValue()
                    attributes[key] = KstructAttribute(value)
                    key = null
                    nextRequiresComma = true
                }

                is CommaToken -> {
                    if (nextRequiresComma) nextRequiresComma = false
                    else throw SuperfluousCommaException(token.line)
                }

                is AsteriskToken,
                is ClosingBraceToken,
                is ClosingBracketToken,
                is DotToken,
                is DoubleToken,
                is FloatToken,
                is IntToken,
                is LiteralInBackticks,
                is LiteralInSingleQuotes,
                is LongToken,
                is MinusToken,
                is OpeningBraceToken,
                is OpeningBracketToken,
                is OpeningParenToken,
                is PlusToken,
                is SlashToken,
                is StringToken,
                -> throw UnexpectedTokenException(token)

                is NewlineToken -> Unit
                is SemicolonToken -> throw MissingCommaException(token.line)
                null -> throw MissingCloseParenException(lastLine)
            }
        }
    }

    fun readListChildren(list: KstructList) {
        var nextRequiresComma = false
        var newMap: KstructMap? = null

        while (true) {
            when (val token = nextToken()) {
                is CommaToken -> {
                    if (!nextRequiresComma) throw SuperfluousCommaException(token.line)
                    nextRequiresComma = false

                    if (newMap != null) {
                        list.children.add(KstructNode(newMap))
                        newMap = null
                    }
                }

                is NewlineToken -> Unit

                is ClosingBracketToken -> {
                    if (newMap != null) list.children.add(KstructNode(newMap))
                    return
                }

                is OpeningParenToken -> {
                    if (nextRequiresComma) throw MissingCommaException(token.line)
                    if (newMap != null) throw UnexpectedTokenException(token) // an attributes list was already read
                    newMap = KstructMap(mutableMapOf(), mutableMapOf())
                    readAttributes(newMap.attributes)
                    nextRequiresComma = true
                }

                is OpeningBraceToken -> {
                    // newMap may already be defined if there were attributes
                    if (newMap == null) newMap = KstructMap(mutableMapOf(), mutableMapOf())
                    readMapChildren(newMap, isRoot = false) // eats CloseBrace
                    list.children.add(KstructNode(newMap))
                    newMap = null
                    nextRequiresComma = true
                }

                null -> throw MissingCloseBracketException(lastLine)

                else -> {
                    if (nextRequiresComma) throw MissingCommaException(token.line)
                    nextTokenIdx-- // read token again
                    val value = readValue()
                    list.children.add(KstructNode(value))
                    nextRequiresComma = true
                }
            }
        }
    }

    companion object {
        fun parse(text: String): KstructNode {
            var tokens = KstructTokeniser.tokenise(text)
            tokens = KstructStaticEvaluator.evaluate(tokens)
            val parser = KstructParser(tokens)
            val root = KstructMap(mutableMapOf(), mutableMapOf())
            parser.readMapChildren(root, isRoot = true)
            return KstructNode(root)
        }
    }
}
