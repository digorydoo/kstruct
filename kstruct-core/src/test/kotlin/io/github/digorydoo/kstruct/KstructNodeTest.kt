package io.github.digorydoo.kstruct

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KstructValueTest {
    @Test
    fun `should return correct values from getters of KstructNull`() {
        val value = KstructNull()

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertFalse(value.toBoolean())
        assertEquals(Char(0), value.toChar())
        assertEquals(0, value.toInt())
        assertEquals(0.0f, value.toFloat())
        assertEquals(0.0, value.toDouble())
        assertEquals("null", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructBoolean`() {
        val value = KstructBoolean(true)

        assertEquals(true, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals('y', value.toChar())
        assertEquals(1, value.toInt())
        assertTrue(value.toFloat().isNaN())
        assertTrue(value.toDouble().isNaN())
        assertEquals("true", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructChar`() {
        val value = KstructChar('y')

        assertEquals(null, value.booleanOrNull())
        assertEquals('y', value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals(Char(121), value.toChar())
        assertEquals(121, value.toInt())
        assertEquals(121.0f, value.toFloat())
        assertEquals(121.0, value.toDouble())
        assertEquals("y", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructInt`() {
        val value = KstructInt(42)

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(42, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals(Char(42), value.toChar())
        assertEquals(42, value.toInt())
        assertEquals(42.0f, value.toFloat())
        assertEquals(42.0, value.toDouble())
        assertEquals("42", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructFloat`() {
        val value = KstructFloat(42.42f)

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(42.42f, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals(Char(42), value.toChar())
        assertEquals(42, value.toInt())
        assertEquals(42.42f, value.toFloat())
        assertEquals(42.41999816894531, value.toDouble()) // strangely, we lose precision
        assertEquals("42.42", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructDouble`() {
        val value = KstructDouble(65.65)

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(65.65, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals(Char(65), value.toChar())
        assertEquals(65, value.toInt())
        assertEquals(65.65f, value.toFloat())
        assertEquals(65.65, value.toDouble())
        assertEquals("65.65", value.toString())
    }

    @Test
    fun `should return correct values from getters of KstructString`() {
        val value = KstructString("92")

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals("92", value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertTrue(value.toBoolean())
        assertEquals(Char(0), value.toChar())
        assertEquals(92, value.toInt())
        assertEquals(92.0f, value.toFloat())
        assertEquals(92.0, value.toDouble())
        assertEquals("92", value.toString())
    }

    @Test
    fun `should return correct values from getters of empty KstructMap`() {
        val map = mutableMapOf<String, KstructNode>()
        val attrs = mutableMapOf<String, KstructAttribute>()
        val value = KstructMap(map, attrs)

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(map, value.mapOrNull())
        assertEquals(null, value.listOrNull())

        assertFalse(value.toBoolean())
        assertEquals(Char(0), value.toChar())
        assertEquals(0, value.toInt())
        assertEquals(0.0f, value.toFloat())
        assertEquals(0.0, value.toDouble())
        assertEquals("KstructMap()", value.toString())
    }

    @Test
    fun `should be able to set and retrieve values in KstructMap via getter and setter`() {
        val map = KstructMap(mutableMapOf(), mutableMapOf())
        map["test"] = KstructFloat(7.0f)
        map["fourty-two"] = KstructInt(42)
        assertEquals(2, map.children.size, "size")
        assertEquals(7.0f, (map.children["test"] as? KstructFloat)?.value, "test value")
        assertEquals(42, (map.children["fourty-two"] as? KstructInt)?.value, "fourty-two value")

        map["test"] = KstructFloat(7.5f)
        assertEquals(7.5f, (map.children["test"] as? KstructFloat)?.value, "test NEW value")

        val result = mutableListOf<String>()
        map.forEachChild { key, value -> result.add("(key=$key, value=$value)") }
        assertEquals("(key=test, value=7.5)(key=fourty-two, value=42)", result.joinToString(""))
    }

    @Test
    fun `should return correct values from getters of empty KstructList`() {
        val list = mutableListOf<KstructNode>()
        val value = KstructList(list)

        assertEquals(null, value.booleanOrNull())
        assertEquals(null, value.charOrNull())
        assertEquals(null, value.intOrNull())
        assertEquals(null, value.floatOrNull())
        assertEquals(null, value.doubleOrNull())
        assertEquals(null, value.stringOrNull())
        assertEquals(null, value.mapOrNull())
        assertEquals(list, value.listOrNull())

        assertFalse(value.toBoolean())
        assertEquals(Char(0), value.toChar())
        assertEquals(0, value.toInt())
        assertEquals(0.0f, value.toFloat())
        assertEquals(0.0, value.toDouble())
        assertEquals("KstructList()", value.toString())
    }

    @Test
    fun `should be able to set and retrieve values in KstructList via getter and setter`() {
        val list = KstructList(mutableListOf(KstructFloat(7.0f), KstructInt(42)))
        assertEquals(2, list.children.size, "size")
        assertEquals(7.0f, (list.children[0] as? KstructFloat)?.value, "test value")
        assertEquals(42, (list.children[1] as? KstructInt)?.value, "fourty-two value")

        list[1] = KstructString("ninety-nine")
        assertEquals("ninety-nine", (list.children[1] as? KstructString)?.value, "fourty-two NEW value")

        assertFailsWith<IndexOutOfBoundsException> {
            list[2] = KstructString("Ad hoc extending of list not allowed")
        }

        val result = mutableListOf<String>()
        list.forEachChild { idx, value -> result.add("(idx=$idx, value=$value)") }
        assertEquals("(idx=0, value=7.0)(idx=1, value=ninety-nine)", result.joinToString(""))
    }
}
