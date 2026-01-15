package io.github.digorydoo.kstruct

import io.github.digorydoo.kstruct.KstructSerialiser.Style
import kotlin.test.Test
import kotlin.test.assertEquals

// This is also part of kutils, but we don't have a dependency to that library at the moment.
infix fun <A, B> A.into(lambda: (A) -> B): B = lambda(this)

internal class KstructSerializerTest {
    @Test
    fun `should correctly serialize a NullValue`() {
        val node = KstructNull()
        val s = KstructSerialiser(indent = 3)
        assertEquals("value = null", s.serialise(node))
    }

    @Test
    fun `should correctly serialize a BooleanValue`() {
        val s = KstructSerialiser(indent = 3)

        var node = KstructBoolean(false)
        assertEquals("value = false", s.serialise(node))

        node = KstructBoolean(true)
        assertEquals("value = true", s.serialise(node))
    }

    @Test
    fun `should correctly serialize a CharValue`() {
        val node = KstructChar('Y')
        val s = KstructSerialiser(indent = 3)
        assertEquals("value = 'Y'", s.serialise(node))
    }

    @Test
    fun `should correctly serialize an IntValue`() {
        val node = KstructInt(0)
        val s = KstructSerialiser(indent = 3)
        assertEquals("value = 0", s.serialise(node))
    }

    @Test
    fun `should correctly serialize a finite FloatValue`() {
        val s = KstructSerialiser(indent = 3)
        KstructFloat(4.2f).let { assertEquals("value = 4.2f", s.serialise(it)) }
        KstructFloat(Float.MAX_VALUE).let { assertEquals("value = 3.4028235E38f", s.serialise(it)) }
        KstructFloat(Float.MIN_VALUE).let { assertEquals("value = 1.4E-45f", s.serialise(it)) }
    }

    @Test
    fun `should serialize a FloatValue as null if it is not finite`() {
        // This is consistent with what JSON.stringify does in JavaScript.
        val s = KstructSerialiser(indent = 3)
        KstructFloat(Float.POSITIVE_INFINITY).let { assertEquals("value = null", s.serialise(it)) }
        KstructFloat(Float.NEGATIVE_INFINITY).let { assertEquals("value = null", s.serialise(it)) }
        KstructFloat(Float.NaN).let { assertEquals("value = null", s.serialise(it)) }
    }

    @Test
    fun `should correctly serialize a finite DoubleValue`() {
        val s = KstructSerialiser(indent = 3)
        KstructDouble(4.2).let { assertEquals("value = 4.2", s.serialise(it)) }
        KstructDouble(Double.MAX_VALUE).let {
            assertEquals("value = 1.7976931348623157E308", s.serialise(it))
        }
        KstructDouble(Double.MIN_VALUE).let { assertEquals("value = 4.9E-324", s.serialise(it)) }
    }

    @Test
    fun `should serialize a DoubleValue as null if it is not finite`() {
        // This is consistent with what JSON.stringify does in JavaScript.
        val s = KstructSerialiser(indent = 3)
        KstructDouble(Double.POSITIVE_INFINITY).let { assertEquals("value = null", s.serialise(it)) }
        KstructDouble(Double.NEGATIVE_INFINITY).let { assertEquals("value = null", s.serialise(it)) }
        KstructDouble(Double.NaN).let { assertEquals("value = null", s.serialise(it)) }
    }

    @Test
    fun `should correctly serialize a StringValue`() {
        val s = KstructSerialiser(indent = 3)

        var node = KstructString("hello")
        assertEquals(
            """
            value = "hello"
            """.trimIndent(),
            s.serialise(node)
        )

        node = KstructString("I'm Batman")
        assertEquals(
            """
            value = "I'm Batman"
            """.trimIndent(),
            s.serialise(node)
        )

        node = KstructString("So-called \"Batman\"")
        assertEquals(
            """
            value = "So-called \"Batman\""
            """.trimIndent(),
            s.serialise(node)
        )

        node = KstructString("With\nNewlines\rCarriage-Returns\tTabs and 漢字")
        assertEquals(
            """
            value = "With\nNewlines\rCarriage-Returns\tTabs and 漢字"
            """.trimIndent(),
            s.serialise(node)
        )
    }

    @Test
    fun `should correctly serialize an empty MapValue`() {
        val node = KstructMap(mutableMapOf(), mutableMapOf())
        val s = KstructSerialiser(indent = 3)
        assertEquals("", s.serialise(node))
    }

    @Test
    fun `should correctly serialise a shallow MapValue with no attributes`() {
        val node = KstructMap(
            mutableMapOf(
                "some-null" to KstructNull(),
                "the Boolean" to KstructBoolean(true),
                "some\"Char\"" to KstructChar('a'),
                "some'Int'" to KstructInt(7713),
                "some`Float`" to KstructFloat(99.11f),
                "some-double" to KstructDouble(33.987654),
                "some-string" to KstructString("Hello Josephine"),
            ),
            mutableMapOf()
        )
        val s = KstructSerialiser(indent = 3)
        assertEquals(
            """
            some-null = null
            `the Boolean` = true
            `some"Char"` = 'a'
            `some'Int'` = 7713
            `some\`Float\`` = 99.11f
            some-double = 33.987654
            some-string = "Hello Josephine"
            """.trimIndent(),
            s.serialise(node)
        )
    }

    @Test
    fun `should correctly serialise a MapValue with no children, but non-empty attributes`() {
        val node = KstructMap(
            mutableMapOf(),
            mutableMapOf(
                "attr-null" to KstructAttribute(KstructNull()),
                "attr-boolean" to KstructAttribute(KstructBoolean(false)),
                "attr-char" to KstructAttribute(KstructChar('a')),
                "attr-int" to KstructAttribute(KstructInt(7713)),
                "attr-float" to KstructAttribute(KstructFloat(99.11f)),
                "attr-double" to KstructAttribute(KstructDouble(33.987654)),
                "attr-string" to KstructAttribute(KstructString("Hello Josephine")),
            )
        )
        val s = KstructSerialiser(indent = 3)
        assertEquals(
            "value(attr-null = null, attr-boolean = false, attr-char = 'a', attr-int = 7713, attr-float = 99.11f, " +
                "attr-double = 33.987654, attr-string = \"Hello Josephine\")",
            s.serialise(node)
        )
    }

    @Test
    fun `should correctly serialise a MapValue that has shallow children as well as attributes`() {
        val node = KstructMap(
            mutableMapOf(
                "some-null" to KstructNull(),
                "some-boolean" to KstructBoolean(false),
                "some-char" to KstructChar('a'),
                "some-int" to KstructInt(7713),
                "some-float" to KstructFloat(99.11f),
                "some-double" to KstructDouble(33.987654),
                "some-string" to KstructString("Hello Josephine"),
            ),
            mutableMapOf(
                "b" to KstructAttribute(KstructBoolean(true)),
                "n" to KstructAttribute(KstructNull()),
                "c" to KstructAttribute(KstructChar('C')),
                "i" to KstructAttribute(KstructInt(7714)),
                "f" to KstructAttribute(KstructFloat(99.22f)),
                "d" to KstructAttribute(KstructDouble(42.987654)),
                "s" to KstructAttribute(KstructString("Blah")),
            )
        )
        val s = KstructSerialiser(indent = 3)
        assertEquals(
            """
            value(b = true, n = null, c = 'C', i = 7714, f = 99.22f, d = 42.987654, s = "Blah") {
               some-null = null
               some-boolean = false
               some-char = 'a'
               some-int = 7713
               some-float = 99.11f
               some-double = 33.987654
               some-string = "Hello Josephine"
            }
            """.trimIndent(),
            s.serialise(node)
        )
    }

    @Test
    fun `should correctly serialize an empty ListValue`() {
        val node = KstructList(mutableListOf())
        val s = KstructSerialiser(indent = 3)
        assertEquals("value = []", s.serialise(node))
    }

    @Test
    fun `should correctly serialize a ListValue with just one child`() {
        val s = KstructSerialiser(indent = 3)
        val mutableNodesListOf: (KstructNode) -> MutableList<KstructNode> = ::mutableListOf

        assertEquals(
            "value = [null]",
            KstructNull() into
                mutableNodesListOf into
                ::KstructList into
                s::serialise
        )

        assertEquals(
            "value = [[]]",
            KstructList(mutableListOf()) into
                mutableNodesListOf into
                ::KstructList into
                s::serialise
        )

        assertEquals(
            "value = [[null]]",
            KstructNull() into
                mutableNodesListOf into
                ::KstructList into
                mutableNodesListOf into
                ::KstructList into
                s::serialise
        )

        assertEquals(
            "value = [[{}]]",
            KstructMap(mutableMapOf(), mutableMapOf()) into
                mutableNodesListOf into
                ::KstructList into
                mutableNodesListOf into
                ::KstructList into
                s::serialise
        )

        assertEquals(
            "value = [[{ a = true }]]",
            KstructMap(mutableMapOf("a" to KstructBoolean(true)), mutableMapOf()) into
                mutableNodesListOf into
                ::KstructList into
                mutableNodesListOf into
                ::KstructList into
                s::serialise
        )
    }

    @Test
    fun `should correctly serialize a ListValue with shallow children`() {
        val node = KstructList(
            mutableListOf(
                KstructNull(),
                KstructBoolean(true),
                KstructChar('a'),
                KstructInt(7713),
                KstructFloat(99.11f),
                KstructDouble(33.987654),
                KstructString("Hello Josephine"),
            )
        )
        val s = KstructSerialiser(indent = 3)
        assertEquals("value = [null, true, 'a', 7713, 99.11f, 33.987654, \"Hello Josephine\"]", s.serialise(node))
    }

    @Test
    fun `should correctly apply indentation`() {
        val node = KstructBuilder.build {
            setMap("emptyMap") {}
            setMap("mapWithOneInt") {
                set("i", 42)
            }
            setMap("mapWithTwoInts") {
                set("i", 36)
                set("j", 6)
            }
            setMap("mapWithOneInnerMap") {
                setMap("emptyMap") {}
            }
            setMap("mapWithOneAttrAndOneBoolChild") {
                attr("x", 9)
                set("child", true)
            }
            setMap("mapWithTwoAttrsAndOneBoolChild") {
                attr("x", 5)
                attr("y", 4)
                set("child", true)
            }
            setList("emptyList") {}
            setList("listWithOneFloat") {
                add(42.2f)
            }
            setList("listWithTwoFloats") {
                add(32.0f)
                add(10.2f)
            }
            setList("listWithOneEmptyMap") {
                addMap {}
            }
            setList("listWithOneMap") {
                addMap {
                    set("which", "is not empty")
                }
            }
            setList("anotherListWithOneMap") {
                addMap {
                    setMap("nested") {
                        set("and", "not empty")
                    }
                }
            }
            setList("listWithTwoEmptyMaps") {
                addMap {}
                addMap {}
            }
        }
        val s = KstructSerialiser(indent = 8, style = Style.INDENTED)
        val expected = listOf(
            "emptyMap {}",
            "mapWithOneInt { i = 42 }",
            "mapWithTwoInts {",
            "        i = 36",
            "        j = 6",
            "}",
            "mapWithOneInnerMap {",
            "        emptyMap {}",
            "}",
            "mapWithOneAttrAndOneBoolChild(x = 9) { child = true }",
            "mapWithTwoAttrsAndOneBoolChild(x = 5, y = 4) {",
            "        child = true",
            "}",
            "emptyList = []",
            "listWithOneFloat = [42.2f]",
            "listWithTwoFloats = [32.0f, 10.2f]",
            "listWithOneEmptyMap = [{}]",
            "listWithOneMap = [{ which = \"is not empty\" }]",
            "anotherListWithOneMap = [{",
            "        nested { and = \"not empty\" }",
            "}]",
            "listWithTwoEmptyMaps = [",
            "        {},",
            "        {}",
            "]",
        )
        s.serialise(node).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }

    @Test
    fun `should correctly handle zero indentation`() {
        val node = KstructBuilder.build {
            setMap("emptyMap") {}
            setMap("mapWithOneInt") {
                set("i", 42)
            }
            setMap("mapWithTwoInts") {
                set("i", 36)
                set("j", 6)
            }
            setMap("mapWithOneInnerMap") {
                setMap("emptyMap") {}
            }
            setMap("mapWithOneAttrAndOneBoolChild") {
                attr("x", 9)
                set("child", true)
            }
            setMap("mapWithTwoAttrsAndOneBoolChild") {
                attr("x", 5)
                attr("y", 4)
                set("child", true)
            }
            setList("emptyList") {}
            setList("listWithOneFloat") {
                add(42.2f)
            }
            setList("listWithTwoFloats") {
                add(32.0f)
                add(10.2f)
            }
            setList("listWithOneEmptyMap") {
                addMap {}
            }
            setList("listWithOneMap") {
                addMap {
                    set("which", "is not empty")
                }
            }
            setList("anotherListWithOneMap") {
                addMap {
                    setMap("nested") {
                        set("and", "not empty")
                    }
                }
            }
            setList("listWithTwoEmptyMaps") {
                addMap {}
                addMap {}
            }
        }
        val s = KstructSerialiser(indent = 0)
        val expected = listOf(
            "emptyMap {}",
            "mapWithOneInt { i = 42 }",
            "mapWithTwoInts {",
            "i = 36",
            "j = 6",
            "}",
            "mapWithOneInnerMap {",
            "emptyMap {}",
            "}",
            "mapWithOneAttrAndOneBoolChild(x = 9) { child = true }",
            "mapWithTwoAttrsAndOneBoolChild(x = 5, y = 4) {",
            "child = true",
            "}",
            "emptyList = []",
            "listWithOneFloat = [42.2f]",
            "listWithTwoFloats = [32.0f, 10.2f]",
            "listWithOneEmptyMap = [{}]",
            "listWithOneMap = [{ which = \"is not empty\" }]",
            "anotherListWithOneMap = [{",
            "nested { and = \"not empty\" }",
            "}]",
            "listWithTwoEmptyMaps = [",
            "{},",
            "{}",
            "]",
        )
        s.serialise(node).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }

    @Test
    fun `should correctly apply indentation when style is FLAT`() {
        val node = KstructBuilder.build {
            setMap("emptyMap") {}
            setMap("mapWithOneInt") {
                set("i", 42)
            }
            setMap("mapWithTwoInts") {
                set("i", 36)
                set("j", 6)
            }
            setMap("mapWithOneInnerMap") {
                setMap("emptyMap") {}
            }
            setMap("mapWithOneAttrAndOneBoolChild") {
                attr("x", 9)
                set("child", true)
            }
            setMap("mapWithTwoAttrsAndOneBoolChild") {
                attr("x", 5)
                attr("y", 4)
                set("child", true)
            }
            setList("emptyList") {}
            setList("listWithOneFloat") {
                add(42.2f)
            }
            setList("listWithTwoFloats") {
                add(32.0f)
                add(10.2f)
            }
            setList("listWithOneMap") {
                addMap {
                    set("yes", "flat")
                    setMap("even") {
                        set("if", "there's")
                        set("a", "map")
                    }
                }
            }
            setList("listWithMoreThan1Map") {
                addMap {
                    set("note", "We wrap this")
                    set("when", "Even in flat mode")
                }
                addMap {
                    set("why", "Because it's common to have one list covering the entire file")
                }
                addMap {
                    set("also", "You can set indent to 0 to avoid indentation")
                }
            }
        }
        val s = KstructSerialiser(style = Style.FLAT)
        val expected = listOf(
            "emptyMap {}",
            "mapWithOneInt { i = 42 }",
            "mapWithTwoInts { i = 36; j = 6 }",
            "mapWithOneInnerMap { emptyMap {} }",
            "mapWithOneAttrAndOneBoolChild(x = 9) { child = true }",
            "mapWithTwoAttrsAndOneBoolChild(x = 5, y = 4) { child = true }",
            "emptyList = []",
            "listWithOneFloat = [42.2f]",
            "listWithTwoFloats = [32.0f, 10.2f]",
            "listWithOneMap = [{ yes = \"flat\"; even { if = \"there's\"; a = \"map\" } }]",
            "listWithMoreThan1Map = [",
            "   { note = \"We wrap this\"; when = \"Even in flat mode\" },",
            "   { why = \"Because it's common to have one list covering the entire file\" },",
            "   { also = \"You can set indent to 0 to avoid indentation\" }",
            "]",
        )
        s.serialise(node).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }

    @Test
    fun `should correctly serialize a deeply nested structure`() {
        val node = KstructBuilder.build {
            setMap("nested") {
                setMap("inside") {
                    setMap("deeper") {
                        setList("list") {
                            add(null as Char?)
                            add(true)
                            add('z')
                            add(65536)
                            add(65.36f)
                            add(65.36)
                            add("hello")
                            addMap {
                                set("nothing", null as Int?)
                                set("b", false)
                            }
                            addList {
                                addMap {
                                    set("c", 'ä')
                                    set("i", 32768)
                                    set("f", -100.5f)
                                    set("d", 101.0)
                                    set("fnan", Float.NaN)
                                    set("dnan", Double.NaN)
                                    setMap("hasAttributes") {
                                        attr("int", 1)
                                        attr("float", 2.0f)
                                        attr("double", 3.0)
                                        attr("string", "yon")
                                    }
                                    setList("items") {
                                        addList {
                                            add("just one element")
                                        }
                                        addMap {
                                            attr("someAttr", 42)
                                        }
                                        addMap {
                                            set("someValue", 42)
                                        }
                                        addMap {
                                            attr("n", 2)
                                            set("text", "two")

                                            setMap("dir") {
                                                attr("x", 11.5f)
                                                attr("y", 9.1f)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val s = KstructSerialiser(indent = 3)
        val expected = listOf(
            "nested {",
            "   inside {",
            "      deeper {",
            "         list = [",
            "            null,",
            "            true,",
            "            'z',",
            "            65536,",
            "            65.36f,",
            "            65.36,",
            "            \"hello\",",
            "            {",
            "               nothing = null",
            "               b = false",
            "            },",
            "            [{",
            "               c = 'ä'",
            "               i = 32768",
            "               f = -100.5f",
            "               d = 101.0",
            "               fnan = null",
            "               dnan = null",
            "               hasAttributes(int = 1, float = 2.0f, double = 3.0, string = \"yon\")",
            "               items = [",
            "                  [\"just one element\"],",
            "                  (someAttr = 42),",
            "                  { someValue = 42 },",
            "                  (n = 2) {",
            "                     text = \"two\"",
            "                     dir(x = 11.5f, y = 9.1f)",
            "                  }",
            "               ]",
            "            }]",
            "         ]",
            "      }",
            "   }",
            "}",
        )
        s.serialise(node).split("\n").forEachIndexed { i, line ->
            assertEquals(expected.getOrNull(i), line, "serialised[$i]")
        }
    }
}
