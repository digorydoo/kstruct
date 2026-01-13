package io.github.digorydoo.kstruct.parser

internal sealed class KstructToken<C>(val line: Int, val content: C) {
    final override fun toString() = "${this::class.simpleName}(line=$line, content=$content)"
}

internal class Literal(line: Int, content: String): KstructToken<String>(line, content)
internal class LiteralInSingleQuotes(line: Int, content: String): KstructToken<String>(line, content)
internal class LiteralInBackticks(line: Int, content: String): KstructToken<String>(line, content)
internal class StringToken(line: Int, content: String): KstructToken<String>(line, content)

internal class IntToken(line: Int, content: Int): KstructToken<Int>(line, content)
internal class LongToken(line: Int, content: Long): KstructToken<Long>(line, content)
internal class FloatToken(line: Int, content: Float): KstructToken<Float>(line, content)
internal class DoubleToken(line: Int, content: Double): KstructToken<Double>(line, content)

internal class ClosingBraceToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class ClosingBracketToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class ClosingParenToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class OpeningBraceToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class OpeningBracketToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class OpeningParenToken(line: Int, content: Char): KstructToken<Char>(line, content)

internal class NewlineToken(line: Int): KstructToken<String>(line, "\\n")
internal class AsteriskToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class CommaToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class EqualsToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class MinusToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class DotToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class PlusToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class SemicolonToken(line: Int, content: Char): KstructToken<Char>(line, content)
internal class SlashToken(line: Int, content: Char): KstructToken<Char>(line, content)
