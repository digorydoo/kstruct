package io.github.digorydoo.kstruct.parser

import kotlin.test.Test
import kotlin.test.assertEquals

internal class KstructStaticEvaluatorTest {
    private fun evaluateAndCheck(tokens: List<KstructToken<*>>, expected: List<String>) {
        KstructStaticEvaluator.evaluate(tokens).forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should return an empty list when given an empty list`() {
        evaluateAndCheck(emptyList(), emptyList())
    }

    @Test
    fun `should return the same list of tokens when there are no operators`() {
        evaluateAndCheck(
            listOf(
                Literal(1, "one"),
                LiteralInSingleQuotes(2, "two"),
                StringToken(3, "three"),
                LiteralInBackticks(4, "four"),
                StringToken(5, "five"),
                IntToken(6, 6000),
                LongToken(7, 700L),
                FloatToken(8, 8.8f),
                DoubleToken(9, 9.9),
                ClosingBraceToken(10, '}'),
                ClosingBracketToken(11, ']'),
                ClosingParenToken(12, ')'),
                OpeningBraceToken(13, '{'),
                OpeningBracketToken(14, '['),
                OpeningParenToken(15, '('),
                NewlineToken(16),
                CommaToken(17, ','),
                EqualsToken(18, '='),
                DotToken(19, '.'),
                SemicolonToken(20, ';'),
                SlashToken(4, '/'),
            ),
            listOf(
                "Literal(line=1, content=one)",
                "LiteralInSingleQuotes(line=2, content=two)",
                "StringToken(line=3, content=three)",
                "LiteralInBackticks(line=4, content=four)",
                "StringToken(line=5, content=five)",
                "IntToken(line=6, content=6000)",
                "LongToken(line=7, content=700)",
                "FloatToken(line=8, content=8.8)",
                "DoubleToken(line=9, content=9.9)",
                "ClosingBraceToken(line=10, content=})",
                "ClosingBracketToken(line=11, content=])",
                "ClosingParenToken(line=12, content=))",
                "OpeningBraceToken(line=13, content={)",
                "OpeningBracketToken(line=14, content=[)",
                "OpeningParenToken(line=15, content=()",
                "NewlineToken(line=16, content=\\n)",
                "CommaToken(line=17, content=,)",
                "EqualsToken(line=18, content==)",
                "DotToken(line=19, content=.)",
                "SemicolonToken(line=20, content=;)",
                "SlashToken(line=4, content=/)",
            )
        )
    }

    @Test
    fun `should not evaluate expressions when the operator is not currently implemented`() {
        evaluateAndCheck(
            listOf(
                IntToken(0, 42),
                MinusToken(0, '-'),
                IntToken(0, 7)
            ),
            listOf(
                "IntToken(line=0, content=42)",
                "MinusToken(line=0, content=-)",
                "IntToken(line=0, content=7)",
            )
        )
        evaluateAndCheck(
            listOf(
                IntToken(0, 42),
                AsteriskToken(0, '*'),
                IntToken(0, 7)
            ),
            listOf(
                "IntToken(line=0, content=42)",
                "AsteriskToken(line=0, content=*)",
                "IntToken(line=0, content=7)",
            )
        )
        evaluateAndCheck(
            listOf(
                IntToken(0, 42),
                SlashToken(0, '/'),
                IntToken(0, 7)
            ),
            listOf(
                "IntToken(line=0, content=42)",
                "SlashToken(line=0, content=/)",
                "IntToken(line=0, content=7)",
            )
        )
    }

    @Test
    fun `should not evaluate expressions when operand types do not match the operator`() {
        evaluateAndCheck(
            listOf(
                IntToken(0, 42),
                PlusToken(0, '+'),
                LongToken(0, 7L)
            ),
            listOf(
                "IntToken(line=0, content=42)",
                "PlusToken(line=0, content=+)",
                "LongToken(line=0, content=7)",
            )
        )
        evaluateAndCheck(
            listOf(
                LongToken(0, 42L),
                PlusToken(0, '+'),
                IntToken(0, 7)
            ),
            listOf(
                "LongToken(line=0, content=42)",
                "PlusToken(line=0, content=+)",
                "IntToken(line=0, content=7)",
            )
        )
        evaluateAndCheck(
            listOf(
                FloatToken(0, 42.0f),
                PlusToken(0, '+'),
                DoubleToken(0, 7.0)
            ),
            listOf(
                "FloatToken(line=0, content=42.0)",
                "PlusToken(line=0, content=+)",
                "DoubleToken(line=0, content=7.0)",
            )
        )
    }

    @Test
    fun `should properly evaluate an expression with Plus operator and Int operands`() {
        evaluateAndCheck(
            listOf(
                IntToken(0, 42),
                PlusToken(0, '+'),
                IntToken(0, 7)
            ),
            listOf("IntToken(line=0, content=49)")
        )
        evaluateAndCheck(
            listOf(
                Literal(0, "BEGIN"),
                IntToken(0, 1),
                PlusToken(0, '+'),
                IntToken(0, 1),
                PlusToken(0, '+'),
                IntToken(0, 2),
                PlusToken(0, '+'),
                IntToken(0, 3),
                PlusToken(0, '+'),
                IntToken(0, 5),
                Literal(0, "END"),
            ),
            listOf(
                "Literal(line=0, content=BEGIN)",
                "IntToken(line=0, content=12)",
                "Literal(line=0, content=END)",
            )
        )
    }

    @Test
    fun `should properly evaluate an expression with Plus operator and Long operands`() {
        evaluateAndCheck(
            listOf(
                LongToken(0, 42),
                PlusToken(0, '+'),
                LongToken(0, 7)
            ),
            listOf("LongToken(line=0, content=49)")
        )
        evaluateAndCheck(
            listOf(
                Literal(0, "BEGIN"),
                LongToken(0, 1),
                PlusToken(0, '+'),
                LongToken(0, 1),
                PlusToken(0, '+'),
                LongToken(0, 2),
                PlusToken(0, '+'),
                LongToken(0, 3),
                PlusToken(0, '+'),
                LongToken(0, 5),
                Literal(0, "END"),
            ),
            listOf(
                "Literal(line=0, content=BEGIN)",
                "LongToken(line=0, content=12)",
                "Literal(line=0, content=END)",
            )
        )
    }

    @Test
    fun `should properly evaluate an expression with Plus operator and Float operands`() {
        evaluateAndCheck(
            listOf(
                FloatToken(0, 42.2f),
                PlusToken(0, '+'),
                FloatToken(0, 7.7f)
            ),
            listOf("FloatToken(line=0, content=49.9)")
        )
        evaluateAndCheck(
            listOf(
                Literal(0, "BEGIN"),
                FloatToken(0, 1.5f),
                PlusToken(0, '+'),
                FloatToken(0, 1.5f),
                PlusToken(0, '+'),
                FloatToken(0, 2.5f),
                PlusToken(0, '+'),
                FloatToken(0, 3.5f),
                PlusToken(0, '+'),
                FloatToken(0, 5.5f),
                Literal(0, "END"),
            ),
            listOf(
                "Literal(line=0, content=BEGIN)",
                "FloatToken(line=0, content=14.5)",
                "Literal(line=0, content=END)",
            )
        )
    }

    @Test
    fun `should properly evaluate an expression with Plus operator and Double operands`() {
        evaluateAndCheck(
            listOf(
                DoubleToken(0, 42.3),
                PlusToken(0, '+'),
                DoubleToken(0, 7.6)
            ),
            listOf("DoubleToken(line=0, content=49.9)")
        )
        evaluateAndCheck(
            listOf(
                Literal(0, "BEGIN"),
                DoubleToken(0, 1.5),
                PlusToken(0, '+'),
                DoubleToken(0, 1.5),
                PlusToken(0, '+'),
                DoubleToken(0, 2.5),
                PlusToken(0, '+'),
                DoubleToken(0, 3.5),
                PlusToken(0, '+'),
                DoubleToken(0, 5.5),
                Literal(0, "END"),
            ),
            listOf(
                "Literal(line=0, content=BEGIN)",
                "DoubleToken(line=0, content=14.5)",
                "Literal(line=0, content=END)",
            )
        )
    }

    @Test
    fun `should properly evaluate an expression with Plus operator and String operands`() {
        evaluateAndCheck(
            listOf(
                StringToken(0, "A"),
                PlusToken(0, '+'),
                StringToken(0, "B")
            ),
            listOf("StringToken(line=0, content=AB)")
        )
        evaluateAndCheck(
            listOf(
                Literal(0, "BEGIN"),
                StringToken(0, ""),
                PlusToken(0, '+'),
                StringToken(0, "Gurk"),
                PlusToken(0, '+'),
                StringToken(0, ""),
                PlusToken(0, '+'),
                StringToken(0, "en"),
                PlusToken(0, '+'),
                StringToken(0, "salat"),
                Literal(0, "END"),
            ),
            listOf(
                "Literal(line=0, content=BEGIN)",
                "StringToken(line=0, content=Gurkensalat)",
                "Literal(line=0, content=END)",
            )
        )
    }

    @Test
    fun `should correctly concatenate Strings even if there are NewlineTokens in between`() {
        evaluateAndCheck(
            listOf(
                Literal(0, "x"),
                NewlineToken(0),
                EqualsToken(1, '='),
                StringToken(1, "a"),
                PlusToken(1, '+'),
                NewlineToken(1),
                NewlineToken(2),
                StringToken(3, "b")
            ),
            listOf(
                "Literal(line=0, content=x)",
                "NewlineToken(line=0, content=\\n)",
                "EqualsToken(line=1, content==)",
                "StringToken(line=1, content=ab)",
            )
        )

        // Note that it is not valid Kotlin if there's a \n before + in: val x = "A" + "B"
        // We also do not evaluate the expression in that case, which would allow that the Parser treats the Plus
        // as a unary operator.
        evaluateAndCheck(
            listOf(
                Literal(0, "x"),
                EqualsToken(0, '='),
                StringToken(0, "a"),
                NewlineToken(0),
                PlusToken(1, '+'),
                StringToken(1, "b")
            ),
            listOf(
                "Literal(line=0, content=x)",
                "EqualsToken(line=0, content==)",
                "StringToken(line=0, content=a)",
                "NewlineToken(line=0, content=\\n)",
                "PlusToken(line=1, content=+)",
                "StringToken(line=1, content=b)",
            )
        )
    }
}
