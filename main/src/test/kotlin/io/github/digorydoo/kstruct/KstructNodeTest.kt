package io.github.digorydoo.kstruct

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KstructNodeTest {
    @Test
    fun `should return correct values from getters of node around KstructNull`() {
        val node = KstructNode(KstructNull())
        assertEquals(true, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertFalse(node.valueToBoolean())
        assertEquals(Char(0), node.valueToChar())
        assertEquals(0, node.valueToInt())
        assertEquals(0.0f, node.valueToFloat())
        assertEquals(0.0, node.valueToDouble())
        assertEquals("null", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructBoolean`() {
        val node = KstructNode(KstructBoolean(true))
        assertEquals(false, node.isNull())
        assertEquals(true, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(true, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals('y', node.valueToChar())
        assertEquals(1, node.valueToInt())
        assertTrue(node.valueToFloat().isNaN())
        assertTrue(node.valueToDouble().isNaN())
        assertEquals("true", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructChar`() {
        val node = KstructNode(KstructChar('y'))
        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(true, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals('y', node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals(Char(121), node.valueToChar())
        assertEquals(121, node.valueToInt())
        assertEquals(121.0f, node.valueToFloat())
        assertEquals(121.0, node.valueToDouble())
        assertEquals("y", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructInt`() {
        val node = KstructNode(KstructInt(42))
        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(true, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(42, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals(Char(42), node.valueToChar())
        assertEquals(42, node.valueToInt())
        assertEquals(42.0f, node.valueToFloat())
        assertEquals(42.0, node.valueToDouble())
        assertEquals("42", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructFloat`() {
        val node = KstructNode(KstructFloat(42.42f))
        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(true, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(42.42f, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals(Char(42), node.valueToChar())
        assertEquals(42, node.valueToInt())
        assertEquals(42.42f, node.valueToFloat())
        assertEquals(42.41999816894531, node.valueToDouble()) // strangely, we lose precision
        assertEquals("42.42", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructDouble`() {
        val node = KstructNode(KstructDouble(65.65))
        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(true, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(65.65, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals(Char(65), node.valueToChar())
        assertEquals(65, node.valueToInt())
        assertEquals(65.65f, node.valueToFloat())
        assertEquals(65.65, node.valueToDouble())
        assertEquals("65.65", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around KstructString`() {
        val node = KstructNode(KstructString("92"))
        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(true, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals("92", node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertTrue(node.valueToBoolean())
        assertEquals(Char(0), node.valueToChar())
        assertEquals(92, node.valueToInt())
        assertEquals(92.0f, node.valueToFloat())
        assertEquals(92.0, node.valueToDouble())
        assertEquals("92", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around empty KstructMap`() {
        val map = mutableMapOf<String, KstructNode>()
        val attrs = mutableMapOf<String, KstructAttribute>()
        val node = KstructNode(KstructMap(map, attrs))

        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(true, node.isMap())
        assertEquals(false, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(map, node.mapOrNull())
        assertEquals(null, node.listOrNull())

        assertFalse(node.valueToBoolean())
        assertEquals(Char(0), node.valueToChar())
        assertEquals(0, node.valueToInt())
        assertEquals(0.0f, node.valueToFloat())
        assertEquals(0.0, node.valueToDouble())
        assertEquals("KstructMap()", node.valueToString())
    }

    @Test
    fun `should return correct values from getters of node around empty KstructList`() {
        val list = mutableListOf<KstructNode>()
        val node = KstructNode(KstructList(list))

        assertEquals(false, node.isNull())
        assertEquals(false, node.isBoolean())
        assertEquals(false, node.isChar())
        assertEquals(false, node.isInt())
        assertEquals(false, node.isFloat())
        assertEquals(false, node.isDouble())
        assertEquals(false, node.isString())
        assertEquals(false, node.isMap())
        assertEquals(true, node.isList())

        assertEquals(null, node.booleanOrNull())
        assertEquals(null, node.charOrNull())
        assertEquals(null, node.intOrNull())
        assertEquals(null, node.floatOrNull())
        assertEquals(null, node.doubleOrNull())
        assertEquals(null, node.stringOrNull())
        assertEquals(null, node.mapOrNull())
        assertEquals(list, node.listOrNull())

        assertFalse(node.valueToBoolean())
        assertEquals(Char(0), node.valueToChar())
        assertEquals(0, node.valueToInt())
        assertEquals(0.0f, node.valueToFloat())
        assertEquals(0.0, node.valueToDouble())
        assertEquals("KstructList()", node.valueToString())
    }

    private fun getMockMap() = KstructNode(
        KstructMap(
            mutableMapOf(
                "text" to KstructNode(KstructString("ans")),
                "dir" to KstructNode(
                    KstructMap(
                        mutableMapOf(),
                        mutableMapOf(
                            "x" to KstructAttribute(KstructFloat(11.5f)),
                            "y" to KstructAttribute(KstructFloat(9.1f)),
                        )
                    )
                ),
            ),
            mutableMapOf(
                "n" to KstructAttribute(KstructInt(2)),
                "m" to KstructAttribute(KstructString("42kg")),
            )
        )
    )

    @Test
    fun `should get correct keys from KstructMap`() {
        val node = getMockMap()
        val keys = node.keys()
        assertTrue(keys != null, "keys")
        assertEquals("text, dir", keys.joinToString(", "))
    }

    @Test
    fun `should get expected children from KstructMap`() {
        val node = getMockMap()
        val children = node.children()
        assertTrue(children != null, "children")
        assertEquals("ans, KstructMap()", children.joinToString(", ") { it.valueToString() })
    }

    @Test
    fun `should get correct attributes from KstructMap`() {
        val node = getMockMap()
        val attrs = node.attributes
        assertTrue(attrs != null, "attrs")
        assertEquals("n, m", attrs.keys.joinToString(", "))
        assertEquals(2, attrs["n"]?.intOrNull())
        assertEquals("42kg", attrs["m"]?.stringOrNull())
    }
}
