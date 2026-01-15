package io.github.digorydoo.kstruct

import kotlin.test.Test
import kotlin.test.assertEquals
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
}
