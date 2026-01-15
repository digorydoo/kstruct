package io.github.digorydoo.kstruct.parser

import io.github.digorydoo.kstruct.KstructSerialiser
import io.github.digorydoo.kstruct.parser.KstructParser.*
import io.github.digorydoo.kstruct.parser.KstructTokeniser.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class KstructParserTest {
    @Test
    fun `should return an empty map given an empty string`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("")
        assertEquals("", s.serialise(result))
    }

    @Test
    fun `should correctly parse a map with childless values`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|nullValue=null
               |boolValue=true
               |charValue='a'
               |intValue=42
               |floatValue=0.1f
               |doubleValue=99.99
               |stringValue="Der Hans der kann's"
               |""".trimMargin()
        )
        assertEquals(
            """|nullValue = null
               |boolValue = true
               |charValue = 'a'
               |intValue = 42
               |floatValue = 0.1f
               |doubleValue = 99.99
               |stringValue = "Der Hans der kann's"
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should correctly parse a Float in various notations`() {
        val s = KstructSerialiser()


        KstructParser.parse("f=0f").let { assertEquals("f = 0.0f", s.serialise(it)) }
        KstructParser.parse("f=-0f").let { assertEquals("f = -0.0f", s.serialise(it)) }
        KstructParser.parse("f=.5f").let { assertEquals("f = 0.5f", s.serialise(it)) }
        KstructParser.parse("f=-.5f").let { assertEquals("f = -0.5f", s.serialise(it)) }
        KstructParser.parse("f=4e4f").let { assertEquals("f = 40000.0f", s.serialise(it)) }
        KstructParser.parse("f=-4e4f").let { assertEquals("f = -40000.0f", s.serialise(it)) }
        KstructParser.parse("f=4e+4f").let { assertEquals("f = 40000.0f", s.serialise(it)) }
        KstructParser.parse("f=4e-4f").let { assertEquals("f = 4.0E-4f", s.serialise(it)) }
        KstructParser.parse("f=1.2345678E4f").let { assertEquals("f = 12345.678f", s.serialise(it)) }
        KstructParser.parse("f=-1.2345678e3f").let { assertEquals("f = -1234.5677f", s.serialise(it)) }
        KstructParser.parse("f=3.4028235E38f").let { assertEquals("f = ${Float.MAX_VALUE}f", s.serialise(it)) }
    }

    @Test
    fun `should correctly parse a Double in various notations`() {
        val s = KstructSerialiser()

        // NOTE: In Kotlin, 4e4 is a Double, not an Int!
        KstructParser.parse("d=0.0").let { assertEquals("d = 0.0", s.serialise(it)) }
        KstructParser.parse("d=-0.0").let { assertEquals("d = -0.0", s.serialise(it)) }
        KstructParser.parse("d=.5").let { assertEquals("d = 0.5", s.serialise(it)) }
        KstructParser.parse("d=-.5").let { assertEquals("d = -0.5", s.serialise(it)) }
        KstructParser.parse("d=4e4").let { assertEquals("d = 40000.0", s.serialise(it)) }
        KstructParser.parse("d=-4e4").let { assertEquals("d = -40000.0", s.serialise(it)) }
        KstructParser.parse("d=4e+4").let { assertEquals("d = 40000.0", s.serialise(it)) }
        KstructParser.parse("d=4e-4").let { assertEquals("d = 4.0E-4", s.serialise(it)) }
        KstructParser.parse("d=1.2345678E4").let { assertEquals("d = 12345.678", s.serialise(it)) }
        KstructParser.parse("d=-1.2345678e3").let { assertEquals("d = -1234.5678", s.serialise(it)) }

        KstructParser.parse("d=1.7976931348623157E308").let {
            assertEquals("d = ${Double.MAX_VALUE}", s.serialise(it))
        }
    }

    @Test
    fun `should throw if trying to parse a number in an illegal or unsupported format`() {
        // Illegal in both Kotlin and Kstruct
        assertFailsWith<UnexpectedTokenException> { KstructParser.parse("x=0.") }
        assertFailsWith<UnexpectedTokenException> { KstructParser.parse("x=0.f") }
        assertFailsWith<UnexpectedTokenException> { KstructParser.parse("x=0.e4") }
        assertFailsWith<UnexpectedTokenException> { KstructParser.parse("x=0.e4f") }

        // These are valid in Kotlin, but not currently supported by Kstruct
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=0x5f") }
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=0X5f") }
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=0b0100") }
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=0B0100") }
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=5u") }
        assertFailsWith<UnexpectedCharException> { KstructParser.parse("x=5U") }
    }

    @Test
    fun `should allow correctly formatted keys even if they clash with Kotlin keywords`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            "null=1;true=false;false=true;Char=1;Int=2;Long=3;Float=4;Double=5;fun=6;also42=9;a_n_d=10"
        )
        assertEquals(
            """|null = 1
               |true = false
               |false = true
               |Char = 1
               |Int = 2
               |Long = 3
               |Float = 4
               |Double = 5
               |fun = 6
               |also42 = 9
               |a_n_d = 10
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should allow any character sequence as a key when enclosed in backticks`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|`-a`=42
               |`_a`=42
               |`数字`=42
               |`3one`=31
               |`0`=1
               |`=`="="
               |""".trimMargin()
        )
        assertEquals(
            """|`-a` = 42
               |`_a` = 42
               |`数字` = 42
               |`3one` = 31
               |`0` = 1
               |`=` = "="
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should fail if the key is empty`() {
        assertFailsWith<MissingKeyException> {
            KstructParser.parse("=42")
        }
        assertFailsWith<EmptyKeyNotAllowedException> {
            KstructParser.parse("``=42")
        }
    }

    @Test
    fun `should reject illegal keys in maps when unquoted`() {
        assertFailsWith<UnexpectedTokenException> {
            KstructParser.parse("-a=42")
        }
        assertFailsWith<UnexpectedCharException> {
            KstructParser.parse("_a=42")
        }
        assertFailsWith<UnexpectedCharException> {
            KstructParser.parse("数字=42")
        }
        assertFailsWith<UnexpectedCharException> {
            KstructParser.parse("3one=31")
        }
    }

    @Test
    fun `should correctly parse strings with escape sequences`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|s0="";s1="\n";s2="\r";s3="\t";s4="\\";s5="'";s6="\'";s7="\"";s8="`";s9="\`"
               |""".trimMargin()
        )
        assertEquals(
            """|s0 = ""
               |s1 = "\n"
               |s2 = "\r"
               |s3 = "\t"
               |s4 = "\\"
               |s5 = "'"
               |s6 = "'"
               |s7 = "\""
               |s8 = "`"
               |s9 = "`"
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should not remove comments inside strings, nor should it trim them`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|s0="  this // is not a comment  "
               |s1="  and neither /* is this */  "
               |""".trimMargin()
        )
        assertEquals(
            """|s0 = "  this // is not a comment  "
               |s1 = "  and neither /* is this */  "
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should fail if string is unterminated, or does not terminate on same line`() {
        assertFailsWith<MissingEndOfDoubleQuoteException> {
            KstructParser.parse("s=\"one two")
        }
        assertFailsWith<MissingEndOfDoubleQuoteException> {
            KstructParser.parse("s=\"one two\nthree\"")
        }
    }

    @Test
    fun `should correctly parse chars with escape sequences`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|c0='A';c1='\n';c2='\r';c3='\t';c4='\\';c5='\'';c6='"';c7='\"';c8='`';c9='\`'
               |""".trimMargin()
        )
        assertEquals(
            """|c0 = 'A'
               |c1 = '\n'
               |c2 = '\r'
               |c3 = '\t'
               |c4 = '\\'
               |c5 = '\''
               |c6 = '"'
               |c7 = '"'
               |c8 = '`'
               |c9 = '`'
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should fail if char literal is empty, too long, unterminated, or does not terminate on same line`() {
        assertFailsWith<IllegalCharLiteralException> {
            KstructParser.parse("c=''")
        }
        assertFailsWith<IllegalCharLiteralException> {
            KstructParser.parse("c='abcdefg'")
        }
        assertFailsWith<MissingEndOfSingleQuoteException> {
            KstructParser.parse("c='")
        }
        assertFailsWith<MissingEndOfSingleQuoteException> {
            KstructParser.parse("c='\n'")
        }
    }

    @Test
    fun `should not remove comments inside char literals`() {
        assertFailsWith<IllegalCharLiteralException> {
            KstructParser.parse("c='a//this is not a comment, therefore the char literal is too long'")
        }
        assertFailsWith<IllegalCharLiteralException> {
            KstructParser.parse("c='a/* this is not a comment, therefore the char literal is too long */'")
        }
    }

    @Test
    fun `should fail if trying to add the same key more than once in a map`() {
        assertFailsWith<DuplicateKeyException> {
            KstructParser.parse("a=1;b=2;c=3;b=2")
        }
    }

    @Test
    fun `should correctly parse nested maps with no attributes and childless values`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|mapOne
               |{
               |  nullValue=null;boolValue=true;charValue='a';
               |  intValue=42 } mapTwo {
               |     floatValue=0.1f
               |     doubleValue=99.99
               |     stringValue="Der Hans der kann's"
               |  } mapThree = { msg = "The equal sign is optional" }
               |""".trimMargin()
        )
        val expected = listOf(
            "mapOne {",
            "   nullValue = null",
            "   boolValue = true",
            "   charValue = 'a'",
            "   intValue = 42",
            "}",
            "mapTwo {",
            "   floatValue = 0.1f",
            "   doubleValue = 99.99",
            "   stringValue = \"Der Hans der kann's\"",
            "}",
            "mapThree { msg = \"The equal sign is optional\" }",
        )
        s.serialise(result).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }

    @Test
    fun `should correctly parse nested maps with attributes no children`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|mapOne(ans=1, zwa=2)
               |mapTwo (
               |  dra=3  ) ; mapThree(via=4)
               |""".trimMargin()
        )
        assertEquals(
            """|mapOne(ans = 1, zwa = 2)
               |mapTwo(dra = 3)
               |mapThree(via = 4)
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should correctly parse nested maps with both attributes and children`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|mapOne(ans=1, zwa=2){dra=3}
               |mapTwo ( via=4, foif=5) { six=6 }
               |""".trimMargin()
        )
        assertEquals(
            """|mapOne(ans = 1, zwa = 2) {
               |   dra = 3
               |}
               |mapTwo(via = 4, foif = 5) {
               |   six = 6
               |}
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should correctly parse a nested list with childless values`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("list=[ null,true,'a',42,0.1f,99.99,\"Gurk\", ]")
        assertEquals("list = [null, true, 'a', 42, 0.1f, 99.99, \"Gurk\"]", s.serialise(result))
    }

    @Test
    fun `should allow dropping the equal sign when defining a map`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("a = { p = 1 }; b { q = 1 }")
        assertEquals(
            """|a { p = 1 }
               |b { q = 1 }
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should not allow dropping the equal sign when defining a list`() {
        assertFailsWith<MissingEqualSignException> {
            KstructParser.parse("d [ 's' ]")
        }
    }

    @Test
    fun `should correctly parse a nested list containing maps`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("list=[(a=1),{},{b=2},(c=3,d=4){e=5;f=6}]")
        assertEquals(
            """|list = [
               |   (a = 1),
               |   {},
               |   { b = 2 },
               |   (c = 3, d = 4) {
               |      e = 5
               |      f = 6
               |   }
               |]
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should allow one trailing comma in a list with at least one value`() {
        val s = KstructSerialiser(indent = 0)
        val result = KstructParser.parse("list=[ 1,2,3, ]")
        assertEquals("list = [1, 2, 3]", s.serialise(result))

    }

    @Test
    fun `should reject comma in an empty list, or multiple commas trailing or in between`() {
        assertFailsWith<SuperfluousCommaException> {
            KstructParser.parse("list=[,]")
        }
        assertFailsWith<SuperfluousCommaException> {
            KstructParser.parse("list=[1,,]")
        }
        assertFailsWith<SuperfluousCommaException> {
            KstructParser.parse("list=[1,,2]")
        }
    }

    @Test
    fun `should allow putting more than one key-value pair in a map on the same line by use of a semicolon`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("a =\n4 ; b\n= 5 ;\nc{d=7;e=8}")
        assertEquals(
            """|a = 4
               |b = 5
               |c {
               |   d = 7
               |   e = 8
               |}
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should allow leading, trailing or multiple semicolons inside a map`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(";;a=\n4;;b\n=5;;\nc{;;d=7;;e=8;;};;")
        assertEquals(
            """|a = 4
               |b = 5
               |c {
               |   d = 7
               |   e = 8
               |}
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should disallow semicolon in front or after the equal sign`() {
        assertFailsWith<MissingValueException> {
            KstructParser.parse("a ;= 4")
        }
        assertFailsWith<MissingValueException> {
            KstructParser.parse("a =; 4")
        }
    }

    @Test
    fun `should disallow semicolon before the attributes list or before the opening brace`() {
        assertFailsWith<MissingValueException> {
            KstructParser.parse("a;(x=1){ b = 1 }")
        }
        assertFailsWith<MissingKeyException> {
            KstructParser.parse("a(x=1);{ b = 1 }")
        }
        assertFailsWith<MissingValueException> {
            KstructParser.parse("a;{ b = 1 }")
        }
    }

    @Test
    fun `should disallow empty values`() {
        assertFailsWith<MissingValueException> {
            KstructParser.parse("x=")
        }
        assertFailsWith<MissingValueException> {
            KstructParser.parse("x=;")
        }
    }

    @Test
    fun `should disallow dots in keys unless enclosed in backticks`() {
        assertFailsWith<UnexpectedTokenException> {
            KstructParser.parse("a.b.c=7")
        }

        val s = KstructSerialiser()
        val result = KstructParser.parse("`a.b.c`=7")
        assertEquals("`a.b.c` = 7", s.serialise(result)) // i.e. not parsed as a path
    }

    @Test
    fun `should disallow multiple assignments inside a map without semicolon in between`() {
        assertFailsWith<MissingSemicolonException> {
            KstructParser.parse("a = 1 b = 2")
        }
        assertFailsWith<MissingSemicolonException> {
            KstructParser.parse("a(b = 1) c = 2")
        }
    }

    @Test
    fun `should disallow multiple assignments inside an attributes list without comma in between`() {
        assertFailsWith<MissingCommaException> {
            KstructParser.parse("a(b = 1 c = 2)")
        }
    }

    @Test
    fun `should disallow seprating attributes by semicolon`() {
        assertFailsWith<MissingCommaException> {
            KstructParser.parse("a(x=1;y=2)")
        }
    }

    @Test
    fun `should disallow seprating attributes by newline`() {
        assertFailsWith<MissingCommaException> {
            KstructParser.parse("a(x=1\ny=2)")
        }
    }

    @Test
    fun `should disallow seprating list members by semicolon`() {
        assertFailsWith<MissingCommaException> {
            KstructParser.parse("list=[1;2]")
        }
    }

    @Test
    fun `should disallow seprating list members by newline`() {
        assertFailsWith<MissingCommaException> {
            KstructParser.parse("list=[1\n2]")
        }
    }

    @Test
    fun `should disallow seprating key assignments by comma`() {
        assertFailsWith<UnexpectedTokenException> {
            KstructParser.parse("x=1,y=2")
        }
    }

    @Test
    fun `should allow continuing a map definition after a closing brace, bracket`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse("a = { b = 1 } c = [ 2 ] d = 3")
        assertEquals(
            """|a { b = 1 }
               |c = [2]
               |d = 3
               |""".trimMargin().trim(),
            s.serialise(result)
        )
    }

    @Test
    fun `should correctly parse a deeply nested structure`() {
        val s = KstructSerialiser()
        val result = KstructParser.parse(
            """|deepMap {
               |   mapWithEmptyList { emptyList = [] }
               |   emptyMapWithAttrs ( x = 1 , y = 2 )
               |   nested {
               |      listWithEmptyMap = [{}]
               |      nested {
               |         list = [
               |            {
               |               emptyMap {}
               |               listWithTwoEmptyMaps = [{},{}]
               |               TYPE = "Stuff"
               |               CONTENT {
               |                  VAL = 3
               |               }
               |            }
               |         ]
               |      }
               |   }
               |}
               |""".trimMargin()
        )
        val expected = listOf(
            "deepMap {",
            "   mapWithEmptyList {",
            "      emptyList = []",
            "   }",
            "   emptyMapWithAttrs(x = 1, y = 2)",
            "   nested {",
            "      listWithEmptyMap = [{}]",
            "      nested {",
            "         list = [{",
            "            emptyMap {}",
            "            listWithTwoEmptyMaps = [",
            "               {},",
            "               {}",
            "            ]",
            "            TYPE = \"Stuff\"",
            "            CONTENT { VAL = 3 }",
            "         }]",
            "      }",
            "   }",
            "}",
        )
        s.serialise(result).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }

    @Test
    fun `should correctly parse a complex structure that contains all the elements`() {
        val s = KstructSerialiser()
        val triple = "\"\"\""
        val dollar = "$"
        val bslash = "\\"
        // TODO move this out into a File, to be able to simply copy/paste this into the README
        val result = KstructParser.parse(
            """|// Kstructs supports line comments
               |/*
               |   Block comments are also supported
               |   /* Nested comments are allowed, just like in Kotlin */
               |*/
               |
               |// Keys which are conform to common variable rules do not need to be quoted.
               |booleanFlag=true
               |someChar='x'
               |Int_Value=-42
               |longer=0L
               |weight=0.001f
               |epsilon=0.000000001
               |a0123456789="That's right"
               |
               |// null is supported, but does not come with a type. Similar to JSON.stringify, the
               |// KstructSerialiser will silently emit Floats and Doubles as null if they are not
               |// finite. The empty string and null are not treated as the same.
               |nothing=null
               |empty=""
               |
               |// Unlike Kotlin, keywords do not clash with keys. You do not need to enclose the
               |// following in backticks.
               |null = 1
               |true = false
               |false = true
               |Char = 1
               |Int = 2
               |Long = 3
               |Float = 4
               |Double = 5
               |fun = 6 // isn't it?
               |
               |// For Floats, Kotlin's various notations are supported.
               |f00=0f
               |f01=-0f
               |f02=.5f
               |f03=-.5f
               |f04=4e4f
               |f05=-4e4f
               |f06=4e+4f
               |f07=4e-4f
               |f08=1.2345678E4f
               |f09=-1.2345678e3f
               |
               |// The same goes for Double.
               |d00=-0.0
               |d01=.5
               |d02=-.5
               |d03=4e4 // Note that this is a Double in Kotlin, not an Int
               |d04=-4e4
               |d05=4e+4
               |d06=4e-4
               |d07=1.2345678E4
               |d08=-1.2345678e3
               |
               |// Some esacpe sequences are supported inside Strings.
               |s1 = "\n"
               |s2 = "\r"
               |s3 = "\t"
               |s4 = "\\"
               |s5 = "\""
               |s6 = "'" // does not need to be escaped
               |s7 = "`" // dito
               |s8 = "\'" // but it is allowed
               |s9 = "\`" // dito
               |
               |// Escaping of dollar signs is required even though Kstruct does not allow template strings at present.
               |s10 = "Escaping $bslash$dollar bills"
               |
               |// Just like in Kotlin, escaping is not required in certain edge cases.
               |s11 = "$" // at end of String
               |s12 = "$\n" // when an escape sequence follows
               |s13 = "$ ws" // when whitespace follows
               |
               |// Similar to Kotlin, keys which are not conform to those rules can be enclosed in
               |// backticks.
               |`This key contains spaces and 漢字` = true
               |`=`="="
               |
               |// Note that a key escaped in backticks can even contain dots and other characters that Kotlin would
               |// not allow as a variable name even with backticks. Even though backslash character is illegal in
               |// Kotlin backticks, Kstruct allows it to support arbitrary keys.
               |`a.b.c`=true; `\\`=true; `\n`=true; `\r`=true; `\t`=true
               |wrapper { `$bslash$dollar`=true } // escaping of dollar is optional here
               |duplicate { `$dollar`=true } // will result in the same key
               |
               |// In Kotlin, sometimes dollar does not need to be escaped. Kstruct emulates this.
               |noEscapingNeeded = [
               |   '$', // when it's a Char
               |   "$", // when a String ends in dollar
               |   $triple$dollar$triple, // when a verbatim string ends in dollar
               |   { `$` = "dollar" }, // when a backticks key ends in dollar
               |   "${dollar}0", // when dollar is followed by a digit
               |
               |   // or when a word delimiting character other than an opening brace follows:
               |   "$dollar%",
               |   "$dollar&",
               |   "$dollar'",
               |   "$dollar(",
               |   "$dollar)",
               |   "$dollar*",
               |   "$dollar+",
               |   "$dollar,",
               |   "$dollar-",
               |   "$dollar.",
               |   "$dollar/",
               |   "$dollar:",
               |   "$dollar;",
               |   "$dollar<",
               |   "$dollar=",
               |   "$dollar>",
               |   "$dollar[",
               |   "$dollar]",
               |   "$dollar^",
               |   "$dollar`",
               |   "$dollar}",
               |   "$dollar~",
               |   "$dollar§",
               |   "$dollar°",
               |   "$dollar$bslash"",
               |   "$dollar$dollar",
               |]
               |
               |// The following would all mean template strings in Kotlin. The Kstruct parser throws if you try these,
               |// because Kstruct does not currently allow template strings.
               |// invalid = ["${dollar}a", "${dollar}_a", "${dollar}_", "$dollar${dollar}a"]
               |
               |// Single quotes vs. double quotes are important, they denote Char or String,
               |// respectively, just like in Kotlin.
               |single='X'
               |double="Mr X"
               |
               |// Long strings can span multiple lines when concatenated with plus. Note that the plus needs to be on
               |// the right side and cannot be on the left end of the line just like in Kotlin. (Kotlin would end the
               |// statement of the preceding line and interprete the plus as an unary operator.)
               |concatenated
               |   =
               |      "Once " +
               |      "and" +
               |      " for all! " + "Sometimes, I even amaze myself!"
               |
               |// NOTE: Kotlin's trimIndent() transforms solo \r's into \n's. Therefore, even if you include explicit
               |// escape sequences of \r in a Kstruct verbatim string, the result will always be \n! Luckily, solo \r
               |// is extremely rare nowadays.
               |
               |// Kotlin's verbatim strings are also supported. However, Kstruct parser implicitly always applies
               |// a trimIndent().trimEnd(), and trimMargin() is not supported.
               |spansMultipleLines = $triple
               |   This text can span multiple lines.
               |   Escaping of "double quotes", 'single quotes' and `backticks` is unnecessary.
               |   Because of implicit trimIndent(), common leading whitespace and the first newline is removed.
               |   Because of implicit trimEnd(), all trailing whitespace is removed.
               |$triple
               |
               |// The plus operator also supports adding Ints, Longs, Floats or Doubles, but take care
               |// not to mix them. This is just for completeness; no other operators are allowed at this
               |// time.
               |willBeInt = 1 + 2
               |willBeLong = 3L + 4L
               |willBeFloat = 5.5f + 6.6f
               |willBeDouble = 7.0 + 8.0
               |// notSupported = "nine" + 10
               |
               |// Multiple assignments can be kept on the same line by delimiting them with semicolon. This
               |// is similar to Kotlin, but unlike JSON, which uses commas.
               |oneThe="same"; line=true
               |
               |// The things we've defined so far define the keys and values of a map. In fact, the root of a
               |// serialised Kstruct is always a map. If you construct a Kstruct from code and serialise it,
               |// it will wrap it in a map with a single key named "value". Of course, we can also define a
               |// key whose value is a nested map.
               |map = { one = true; two = false }
               |
               |// We can drop the equal sign with above assignment. This is semantically identical, but looks
               |// closer to Kotlin DSL.
               |alsoMap { one = true; two = false }
               |
               |// Keys in a map must be unique. If they aren't, the parser will throw. This is unlike JSON,
               |// which typically silently overwrites the keys (even though that behaviour does not appear to
               |// be exactly documented). It is also unlike HOCON, which merges the values of the two
               |// definition.
               |
               |// A map's closing brace is enough to separate it from the next assignment. Therefore, semicolon
               |// is not needed if another key follows on the same line.
               |anotherMap { yes = true } anotherKey = 1
               |
               |// Superfluous semicolons are ignored
               |; thisIsntAComment = 1 ;; doubleSemicolonAre { fine = true; };
               |
               |// Like XML, keys whose values are maps can have attributes. This looks like Kotlin DSL with
               |// arguments.
               |keyOfAMap(keyOfAttribute=42) { keyOfChild=true }
               |
               |// The braces can be omitted if the map has only attributes and no children.
               |keyOfAnotherMap(keyOfAttribute="andValue")
               |
               |// Like with Kotlin Map, the order of the keys of a Kstruct map is defined as/ the order how
               |// they are added, i.e. listed in the serialisation. This is unlike JSON, which leaves the
               |// order of keys arbitrary (which sometimes leads to unexpected behaviour and bugs). JSON uses
               |// arrays to list values whose order are important. Kstruct also supports this as lists.
               |// Note that the equals sign is required here, because without it would look like an indexed
               |// lookup rather than a definition.
               |keyOfAList = [ 1, "two", 3L ]
               |
               |// The members of a list are separated by comma, not semicolon. This is similar to Kotlin if
               |// you would use arrayOf(1, 2). When spanning multiple lines, the comma may not be dropped,
               |// but one trailing comma is allowed.
               |keyOfList2 = [
               |   "four",
               |   "five",
               |]
               |
               |// A list can contain other lists.
               |nestedList = [ [ 1 ], [ 2, 3 ], 4 ]
               |
               |// A list can also contain maps.
               |listWithNestedMaps = [ { a = true }, { a = false } ]
               |
               |// Nested maps can still have attributes. If a map with attributes has no children, the
               |// braces can be omitted.
               |listWithNestedMapsWithAttr = [
               |   (attr = 1) { child = "" },
               |   (attr = 1), 
               |]
               |
               |// A list's closing bracket is enough to delimit it from the next assignment.
               |yetAnotherList=[1,2]; yetAnotherThing=true
               |
               |""".trimMargin()
        )
        val expected = listOf(
            "booleanFlag = true",
            "someChar = 'x'",
            "Int_Value = -42",
            "longer = 0L",
            "weight = 0.001f",
            "epsilon = 1.0E-9",
            "a0123456789 = \"That's right\"",
            "nothing = null",
            "empty = \"\"",
            "null = 1",
            "true = false",
            "false = true",
            "Char = 1",
            "Int = 2",
            "Long = 3",
            "Float = 4",
            "Double = 5",
            "fun = 6",
            "f00 = 0.0f",
            "f01 = -0.0f",
            "f02 = 0.5f",
            "f03 = -0.5f",
            "f04 = 40000.0f",
            "f05 = -40000.0f",
            "f06 = 40000.0f",
            "f07 = 4.0E-4f",
            "f08 = 12345.678f",
            "f09 = -1234.5677f",
            "d00 = -0.0",
            "d01 = 0.5",
            "d02 = -0.5",
            "d03 = 40000.0",
            "d04 = -40000.0",
            "d05 = 40000.0",
            "d06 = 4.0E-4",
            "d07 = 12345.678",
            "d08 = -1234.5678",
            "s1 = \"\\n\"",
            "s2 = \"\\r\"",
            "s3 = \"\\t\"",
            "s4 = \"\\\\\"",
            "s5 = \"\\\"\"",
            "s6 = \"'\"",
            "s7 = \"`\"",
            "s8 = \"'\"",
            "s9 = \"`\"",
            "s10 = \"Escaping \\$ bills\"",
            "s11 = \"\\$\"",
            "s12 = \"\\$\\n\"",
            "s13 = \"\\$ ws\"",
            "`This key contains spaces and 漢字` = true",
            "`=` = \"=\"",
            "`a.b.c` = true",
            "`\\\\` = true",
            "`\\n` = true",
            "`\\r` = true",
            "`\\t` = true",
            "wrapper { `$dollar` = true }",
            "duplicate { `$dollar` = true }",
            "noEscapingNeeded = [",
            "   '$',",
            "   \"\\$\",",
            "   \"\\$\",",
            "   { `$` = \"dollar\" },",
            "   \"\\${dollar}0\",",
            "   \"\\$dollar%\",",
            "   \"\\$dollar&\",",
            "   \"\\$dollar'\",",
            "   \"\\$dollar(\",",
            "   \"\\$dollar)\",",
            "   \"\\$dollar*\",",
            "   \"\\$dollar+\",",
            "   \"\\$dollar,\",",
            "   \"\\$dollar-\",",
            "   \"\\$dollar.\",",
            "   \"\\$dollar/\",",
            "   \"\\$dollar:\",",
            "   \"\\$dollar;\",",
            "   \"\\$dollar<\",",
            "   \"\\$dollar=\",",
            "   \"\\$dollar>\",",
            "   \"\\$dollar[\",",
            "   \"\\$dollar]\",",
            "   \"\\$dollar^\",",
            "   \"\\$dollar`\",",
            "   \"\\$dollar}\",",
            "   \"\\$dollar~\",",
            "   \"\\$dollar§\",",
            "   \"\\$dollar°\",",
            "   \"\\$dollar$bslash\"\",",
            "   \"\\$dollar\\$dollar\"",
            "]",
            "single = 'X'",
            "double = \"Mr X\"",
            "concatenated = \"Once and for all! Sometimes, I even amaze myself!\"",
            "spansMultipleLines = \"This text can span multiple lines.\\n" +
                "Escaping of \\\"double quotes\\\", 'single quotes' and `backticks` is unnecessary.\\n" +
                "Because of implicit trimIndent(), common leading whitespace and the first newline is removed.\\n" +
                "Because of implicit trimEnd(), all trailing whitespace is removed.\"",
            "willBeInt = 3",
            "willBeLong = 7L",
            "willBeFloat = 12.1f",
            "willBeDouble = 15.0",
            "oneThe = \"same\"",
            "line = true",
            "map {",
            "   one = true",
            "   two = false",
            "}",
            "alsoMap {",
            "   one = true",
            "   two = false",
            "}",
            "anotherMap { yes = true }",
            "anotherKey = 1",
            "thisIsntAComment = 1",
            "doubleSemicolonAre { fine = true }",
            "keyOfAMap(keyOfAttribute = 42) { keyOfChild = true }",
            "keyOfAnotherMap(keyOfAttribute = \"andValue\")",
            "keyOfAList = [1, \"two\", 3L]",
            "keyOfList2 = [\"four\", \"five\"]",
            "nestedList = [",
            "   [1],",
            "   [2, 3],",
            "   4",
            "]",
            "listWithNestedMaps = [",
            "   { a = true },",
            "   { a = false }",
            "]",
            "listWithNestedMapsWithAttr = [",
            "   (attr = 1) { child = \"\" },",
            "   (attr = 1)",
            "]",
            "yetAnotherList = [1, 2]",
            "yetAnotherThing = true",
        )
        s.serialise(result).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }
}
