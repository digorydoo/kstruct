package ch.digorydoo.kstruct

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class KstructAttributeTest {
    @Test
    fun `should return correct values from getters of node around KstructNull`() {
        val attr = KstructAttribute(KstructNull())
        assertEquals(true, attr.isNull())
        assertEquals(false, attr.isBoolean())
        assertEquals(false, attr.isChar())
        assertEquals(false, attr.isInt())
        assertEquals(false, attr.isFloat())
        assertEquals(false, attr.isDouble())
        assertEquals(false, attr.isString())
        // node.isMap does not exist
        // node.isList does not exist

        assertEquals(null, attr.booleanOrNull())
        assertEquals(null, attr.charOrNull())
        assertEquals(null, attr.intOrNull())
        assertEquals(null, attr.floatOrNull())
        assertEquals(null, attr.doubleOrNull())
        assertEquals(null, attr.stringOrNull())
        // node.mapOrNull() does not exist
        // node.listOrNull() does not exist

        assertFalse(attr.valueToBoolean())
        assertEquals(Char(0), attr.valueToChar())
        assertEquals(0, attr.valueToInt())
        assertEquals(0.0f, attr.valueToFloat())
        assertEquals(0.0, attr.valueToDouble())
        assertEquals("null", attr.valueToString())
    }
}
