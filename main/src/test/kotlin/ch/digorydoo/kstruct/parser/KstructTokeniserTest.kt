package ch.digorydoo.kstruct.parser

import ch.digorydoo.kstruct.parser.KstructTokeniser.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class KstructTokeniserTest {
    @Test
    fun `should return a single Newline token given an empty string`() {
        var tokens = KstructTokeniser.tokenise("")
        assertEquals("NewlineToken(line=1, content=\\n)", tokens.joinToString(", "))

        tokens = KstructTokeniser.tokenise("  \r \t ")
        assertEquals("NewlineToken(line=1, content=\\n)", tokens.joinToString(", "))
    }

    @Test
    fun `should properly tokenise multiple literals`() {
        val tokens = KstructTokeniser.tokenise("one TWO three4 fi_ve si-x")
        val expected = arrayOf(
            "Literal(line=1, content=one)",
            "Literal(line=1, content=TWO)",
            "Literal(line=1, content=three4)",
            "Literal(line=1, content=fi_ve)",
            "Literal(line=1, content=si-x)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should throw or split the token if trying to define a literal starting with an illegal character`() {
        assertFailsWith<UnexpectedCharException> {
            KstructTokeniser.tokenise("this is _illegal")
        }
        val tokens = KstructTokeniser.tokenise("-minusIsNotPartOfLiteral")
        val expected = arrayOf(
            "MinusToken(line=1, content=-)",
            "Literal(line=1, content=minusIsNotPartOfLiteral)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise literals mixed with delimiters`() {
        val tokens = KstructTokeniser.tokenise("one(two)[three,four]{five}")
        val expected = arrayOf(
            "Literal(line=1, content=one)",
            "OpeningParenToken(line=1, content=()",
            "Literal(line=1, content=two)",
            "ClosingParenToken(line=1, content=))",
            "OpeningBracketToken(line=1, content=[)",
            "Literal(line=1, content=three)",
            "CommaToken(line=1, content=,)",
            "Literal(line=1, content=four)",
            "ClosingBracketToken(line=1, content=])",
            "OpeningBraceToken(line=1, content={)",
            "Literal(line=1, content=five)",
            "ClosingBraceToken(line=1, content=})",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise literals in single quotes`() {
        val tokens = KstructTokeniser.tokenise(
            """
            ' one ' 'two\tthree' 'four\nfive' 'six\rseven' 'eight\\nine' 'ten\'eleven' 'twelve''thirteen'
            """.trimIndent()
        )
        val expected = arrayOf(
            "LiteralInSingleQuotes(line=1, content= one )",
            "LiteralInSingleQuotes(line=1, content=two\tthree)",
            "LiteralInSingleQuotes(line=1, content=four\nfive)",
            "LiteralInSingleQuotes(line=1, content=six\rseven)",
            "LiteralInSingleQuotes(line=1, content=eight\\nine)",
            "LiteralInSingleQuotes(line=1, content=ten'eleven)",
            "LiteralInSingleQuotes(line=1, content=twelve)",
            "LiteralInSingleQuotes(line=1, content=thirteen)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise literals in double quotes`() {
        val tokens = KstructTokeniser.tokenise(
            """
            " one " "two\tthree" "four\nfive"
            "six\rseven" "eight\\nine" "ten\"eleven"
            """.trimIndent()
        )
        val expected = arrayOf(
            "StringToken(line=1, content= one )",
            "StringToken(line=1, content=two\tthree)",
            "StringToken(line=1, content=four\nfive)",
            "NewlineToken(line=1, content=\\n)",
            "StringToken(line=2, content=six\rseven)",
            "StringToken(line=2, content=eight\\nine)",
            "StringToken(line=2, content=ten\"eleven)",
            "NewlineToken(line=2, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should throw if quotes are unbalanced`() {
        assertFailsWith<MissingEndOfSingleQuoteException> {
            KstructTokeniser.tokenise("this has an 'unclosed single quote")
        }
        assertFailsWith<MissingEndOfDoubleQuoteException> {
            KstructTokeniser.tokenise("this has an \"unclosed double quote")
        }
        assertFailsWith<MissingEndOfBackticksException> {
            KstructTokeniser.tokenise("this has an `unclosed backtick")
        }
    }

    @Test
    fun `should throw if trying to use quotes span multiple lines`() {
        assertFailsWith<MissingEndOfSingleQuoteException> {
            KstructTokeniser.tokenise("this 'single quote\nis closed' on the next line")
        }
        assertFailsWith<MissingEndOfDoubleQuoteException> {
            KstructTokeniser.tokenise("this \"double quote\nis closed\" on the next line")
        }
        assertFailsWith<MissingEndOfBackticksException> {
            KstructTokeniser.tokenise("this `backtick\nis closed` on the next line")
        }
    }

    @Test
    fun `should properly tokenise verbatim strings spanning a single line`() {
        val triple = "\"\"\""
        val tokens = KstructTokeniser.tokenise(
            """
            $triple$triple
            ${triple}abc$triple
            ${triple}May 'contain' "quotes" ""and"" `backticks`.${triple}
            $triple   Should trim line.   $triple
            """.trimIndent()
        )
        val expected = arrayOf(
            "StringToken(line=1, content=)",
            "NewlineToken(line=1, content=\\n)",
            "StringToken(line=2, content=abc)",
            "NewlineToken(line=2, content=\\n)",
            "StringToken(line=3, content=May 'contain' \"quotes\" \"\"and\"\" `backticks`.)",
            "NewlineToken(line=3, content=\\n)",
            "StringToken(line=4, content=Should trim line.)",
            "NewlineToken(line=4, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise a verbatim strings spanning multiple lines`() {
        val triple = "\"\"\""
        val tokens = KstructTokeniser.tokenise(
            """
            $triple
            This text
              spans multiple lines
                and has indentation.
            $triple
            """ // no trimIndent here! we want to see what the Tokeniser does
        )
        val expected = arrayOf(
            "NewlineToken(line=1, content=\\n)",
            "StringToken(line=6, content=This text\n  spans multiple lines\n    and has indentation.)",
            "NewlineToken(line=6, content=\\n)",
            "NewlineToken(line=7, content=\\n)"
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should allow escaping in single quoted strings, double quoted strings and verbatim strings`() {
        val triple = "\"\"\""
        val bslash = "\\"

        // Note: Kotlin's trimIndent() transforms solo \r into \n! Since Kstruct verbatim strings implicitly
        // do trimIndent(), they cannot represent solo \r!

        // Another note: In Kotlin, backslash inside backticks is illegal. In Kstruct, we need to allow arbitrary
        // keys, and thus need to support escaping even in backticks.

        val tokens = KstructTokeniser.tokenise(
            """
            'newline${bslash}ntab${bslash}tcarriageRet${bslash}rdollar${bslash}$'
            "newline${bslash}ntab${bslash}tcarriageRet${bslash}rdollar${bslash}$"
            `newline${bslash}ntab${bslash}tcarriageRet${bslash}rdollar${bslash}$`
            ${triple}newline${bslash}ntab${bslash}tcarriageRetNOT${bslash}rdollar${bslash}$$triple
            """.trimIndent()
        )
        val expected = arrayOf(
            "LiteralInSingleQuotes(line=1, content=newline\ntab\tcarriageRet\rdollar$)",
            "NewlineToken(line=1, content=\\n)",
            "StringToken(line=2, content=newline\ntab\tcarriageRet\rdollar$)",
            "NewlineToken(line=2, content=\\n)",
            "LiteralInBackticks(line=3, content=newline\ntab\tcarriageRet\rdollar$)",
            "NewlineToken(line=3, content=\\n)",
            "StringToken(line=4, content=newline\ntab\tcarriageRetNOT\ndollar$)", // \r transformed, see above!
            "NewlineToken(line=4, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should throw if a dollar sign is used in a way that would mean template String in Kotlin`() {
        val dollar = "$"

        assertFailsWith<TemplateStringsNotImplementedException> { KstructTokeniser.tokenise("\"${dollar}a\"") }
        assertFailsWith<TemplateStringsNotImplementedException> { KstructTokeniser.tokenise("\"${dollar}{a}\"") }
        assertFailsWith<TemplateStringsNotImplementedException> { KstructTokeniser.tokenise("\"${dollar}_a\"") }
        assertFailsWith<TemplateStringsNotImplementedException> { KstructTokeniser.tokenise("\"${dollar}_\"") }
        assertFailsWith<TemplateStringsNotImplementedException> { KstructTokeniser.tokenise("\"$dollar${dollar}a\"") }

        assertFailsWith<TemplateStringsNotImplementedException> {
            KstructTokeniser.tokenise("\"\"\"${dollar}a\"\"\"")
        }
        assertFailsWith<TemplateStringsNotImplementedException> {
            KstructTokeniser.tokenise("\"\"\"${dollar}{a}\"\"\"")
        }
        assertFailsWith<TemplateStringsNotImplementedException> {
            KstructTokeniser.tokenise("\"\"\"${dollar}_a\"\"\"")
        }
        assertFailsWith<TemplateStringsNotImplementedException> {
            KstructTokeniser.tokenise("\"\"\"${dollar}_\"\"\"")
        }
        assertFailsWith<TemplateStringsNotImplementedException> {
            KstructTokeniser.tokenise("\"\"\"$dollar${dollar}a\"\"\"")
        }

        // Note that escape quotes in single quotes are not tested, because multiple chars in a single quote literal
        // will be detected by the Parser as an error anyway.

        // Note that there are quite a few cases where dollar does not need to be escaped in Kotlin.
        // We allow these in Kstruct, too. The tests are in KstructParserTest.
    }

    @Test
    fun `should properly tokenise positive and negative integers`() {
        val tokens = KstructTokeniser.tokenise("0 127 -32768")
        val expected = arrayOf(
            "IntToken(line=1, content=0)",
            "IntToken(line=1, content=127)",
            "IntToken(line=1, content=-32768)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should switch to LongToken if value exceeds range of Int`() {
        val tokens = KstructTokeniser.tokenise(
            "${Int.MAX_VALUE} ${Int.MIN_VALUE} " +
                "${Int.MAX_VALUE.toLong() + 1L} ${Int.MIN_VALUE.toLong() - 1L} " +
                "${Long.MAX_VALUE} ${Long.MIN_VALUE}"
        )
        val expected = arrayOf(
            "IntToken(line=1, content=2147483647)",
            "IntToken(line=1, content=-2147483648)",
            "LongToken(line=1, content=2147483648)",
            "LongToken(line=1, content=-2147483649)",
            "LongToken(line=1, content=9223372036854775807)",
            "LongToken(line=1, content=-9223372036854775808)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should be able to force a LongToken by adding suffix L`() {
        val tokens = KstructTokeniser.tokenise("0L 47l -3L")
        val expected = arrayOf(
            "LongToken(line=1, content=0)",
            "LongToken(line=1, content=47)",
            "LongToken(line=1, content=-3)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise positive and negative Floats`() {
        // Note that in Kotlin, the following mean extension functions, and therefore are not supported by Kstruct:
        // val x = 47.f
        // val y = -3.f
        val tokens = KstructTokeniser.tokenise("0f 47F -3f 4.5678f .55f -.55f")
        val expected = arrayOf(
            "FloatToken(line=1, content=0.0)",
            "FloatToken(line=1, content=47.0)",
            "FloatToken(line=1, content=-3.0)",
            "FloatToken(line=1, content=4.5678)",
            "FloatToken(line=1, content=0.55)",
            "FloatToken(line=1, content=-0.55)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise positive and negative Doubles`() {
        // Note that these are not valid Kotlin and therefore not supported by Kstruct either:
        // val x = 47.
        // val y = -3.
        val tokens = KstructTokeniser.tokenise("0.0 1.414 -4.56789 .42 -.42")
        val expected = arrayOf(
            "DoubleToken(line=1, content=0.0)",
            "DoubleToken(line=1, content=1.414)",
            "DoubleToken(line=1, content=-4.56789)",
            "DoubleToken(line=1, content=0.42)",
            "DoubleToken(line=1, content=-0.42)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly tokenise sequences that look like numbers but aren't`() {
        assertFailsWith<UnexpectedCharException> {
            KstructTokeniser.tokenise("4k")
        }
        assertFailsWith<UnexpectedCharException> {
            KstructTokeniser.tokenise(".5L")
        }

        KstructTokeniser.tokenise("0..1").let {
            assertEquals(
                arrayOf(
                    "IntToken(line=1, content=0)",
                    "DotToken(line=1, content=.)",
                    "DoubleToken(line=1, content=0.1)",
                    "NewlineToken(line=1, content=\\n)",
                ).joinToString(", "),
                it.joinToString(", ")
            )
        }

        KstructTokeniser.tokenise("4 . 5 .f 4L5 6-7").let {
            assertEquals(
                arrayOf(
                    "IntToken(line=1, content=4)",
                    "DotToken(line=1, content=.)",
                    "IntToken(line=1, content=5)",
                    "DotToken(line=1, content=.)",
                    "Literal(line=1, content=f)",
                    "LongToken(line=1, content=4)",
                    "IntToken(line=1, content=5)",
                    "IntToken(line=1, content=6)",
                    "MinusToken(line=1, content=-)",
                    "IntToken(line=1, content=7)",
                    "NewlineToken(line=1, content=\\n)",
                ).joinToString(", "),
                it.joinToString(", ")
            )
        }
    }

    @Test
    fun `should properly tokenise member paths`() {
        val tokens = KstructTokeniser.tokenise("one.two.three NaN Float.NaN")
        val expected = arrayOf(
            "Literal(line=1, content=one)",
            "DotToken(line=1, content=.)",
            "Literal(line=1, content=two)",
            "DotToken(line=1, content=.)",
            "Literal(line=1, content=three)",
            "Literal(line=1, content=NaN)",
            "Literal(line=1, content=Float)",
            "DotToken(line=1, content=.)",
            "Literal(line=1, content=NaN)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should properly handle line comments and block comments`() {
        val tokens = KstructTokeniser.tokenise(
            """|// ignored 4.5f -3 f(a=x){blah}
               |something//zap
               |also/*gone*/
               |and/*between*/too
               |/************
               | * Multi-line
               | ************/
               |return/*
               |   Comment's newline must not result
               |   in Newline token!*/42 // Kotlin allows this
               |""".trimMargin()
        )
        val expected = arrayOf(
            "NewlineToken(line=1, content=\\n)",
            "Literal(line=2, content=something)",
            "NewlineToken(line=2, content=\\n)",
            "Literal(line=3, content=also)",
            "NewlineToken(line=3, content=\\n)",
            "Literal(line=4, content=and)",
            "Literal(line=4, content=too)",
            "NewlineToken(line=4, content=\\n)",
            "NewlineToken(line=7, content=\\n)",
            "Literal(line=8, content=return)",
            "IntToken(line=10, content=42)",
            "NewlineToken(line=10, content=\\n)",
            "NewlineToken(line=11, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should allow nested block comments`() {
        val tokens = KstructTokeniser.tokenise("one/*two/*three*/*/=1")
        val expected = arrayOf(
            "Literal(line=1, content=one)",
            "EqualsToken(line=1, content==)",
            "IntToken(line=1, content=1)",
            "NewlineToken(line=1, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }

    @Test
    fun `should behave identically to Kotlin regarding comments`() {
        assertFailsWith<MissingEndOfBlockCommentException> {
            KstructTokeniser.tokenise("/*bla")
        }
        KstructTokeniser.tokenise("/**/bla").let { tokens ->
            val expected = arrayOf(
                "Literal(line=1, content=bla)",
                "NewlineToken(line=1, content=\\n)"
            )
            tokens.forEachIndexed { i, token ->
                assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
            }
        }
        KstructTokeniser.tokenise("/*/bla*/").let { tokens ->
            val expected = arrayOf("NewlineToken(line=1, content=\\n)")
            tokens.forEachIndexed { i, token ->
                assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
            }
        }
        KstructTokeniser.tokenise("//*bla\nblupp").let { tokens ->
            val expected = arrayOf(
                "NewlineToken(line=1, content=\\n)",
                "Literal(line=2, content=blupp)",
                "NewlineToken(line=2, content=\\n)"
            )
            tokens.forEachIndexed { i, token ->
                assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
            }
        }
    }

    @Test
    fun `should properly tokenise a complex structure`() {
        val tokens = KstructTokeniser.tokenise(
            """|flag = true
               |digit='A';text="One Two"//zap that comment!
               |emptyObj{}/*ignored*/emptyList=[]
               |nonEmptyObj(a=1,b=27.4,c=""){d="hi"+"there";e=5.0f}
               |""".trimMargin()
        )
        val expected = arrayOf(
            "Literal(line=1, content=flag)",
            "EqualsToken(line=1, content==)",
            "Literal(line=1, content=true)",
            "NewlineToken(line=1, content=\\n)",
            "Literal(line=2, content=digit)",
            "EqualsToken(line=2, content==)",
            "LiteralInSingleQuotes(line=2, content=A)",
            "SemicolonToken(line=2, content=;)",
            "Literal(line=2, content=text)",
            "EqualsToken(line=2, content==)",
            "StringToken(line=2, content=One Two)",
            "NewlineToken(line=2, content=\\n)",
            "Literal(line=3, content=emptyObj)",
            "OpeningBraceToken(line=3, content={)",
            "ClosingBraceToken(line=3, content=})",
            "Literal(line=3, content=emptyList)",
            "EqualsToken(line=3, content==)",
            "OpeningBracketToken(line=3, content=[)",
            "ClosingBracketToken(line=3, content=])",
            "NewlineToken(line=3, content=\\n)",
            "Literal(line=4, content=nonEmptyObj)",
            "OpeningParenToken(line=4, content=()",
            "Literal(line=4, content=a)",
            "EqualsToken(line=4, content==)",
            "IntToken(line=4, content=1)",
            "CommaToken(line=4, content=,)",
            "Literal(line=4, content=b)",
            "EqualsToken(line=4, content==)",
            "DoubleToken(line=4, content=27.4)",
            "CommaToken(line=4, content=,)",
            "Literal(line=4, content=c)",
            "EqualsToken(line=4, content==)",
            "StringToken(line=4, content=)",
            "ClosingParenToken(line=4, content=))",
            "OpeningBraceToken(line=4, content={)",
            "Literal(line=4, content=d)",
            "EqualsToken(line=4, content==)",
            "StringToken(line=4, content=hi)",
            "PlusToken(line=4, content=+)",
            "StringToken(line=4, content=there)",
            "SemicolonToken(line=4, content=;)",
            "Literal(line=4, content=e)",
            "EqualsToken(line=4, content==)",
            "FloatToken(line=4, content=5.0)",
            "ClosingBraceToken(line=4, content=})",
            "NewlineToken(line=4, content=\\n)",
            "NewlineToken(line=5, content=\\n)",
        )
        tokens.forEachIndexed { i, token ->
            assertEquals(expected.getOrNull(i), token.toString(), "tokens[$i]")
        }
    }
}
