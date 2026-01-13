package io.github.digorydoo.kstruct

class KstructAttribute(val value: KstructValue) {
    init {
        require(value !is KstructMap) { "A KstructMap cannot be used as an attribute" }
        require(value !is KstructList) { "A KstructList cannot be used as an attribute" }
    }

    fun isNull() = value is KstructNull
    fun isBoolean() = value is KstructBoolean
    fun isChar() = value is KstructChar
    fun isInt() = value is KstructInt
    fun isFloat() = value is KstructFloat
    fun isDouble() = value is KstructDouble
    fun isString() = value is KstructString

    fun booleanOrNull() = value.booleanOrNull()
    fun charOrNull() = value.charOrNull()
    fun intOrNull() = value.intOrNull()
    fun floatOrNull() = value.floatOrNull()
    fun doubleOrNull() = value.doubleOrNull()
    fun stringOrNull() = value.stringOrNull()

    fun valueToBoolean() = value.toBoolean()
    fun valueToChar() = value.toChar()
    fun valueToInt() = value.toInt()
    fun valueToFloat() = value.toFloat()
    fun valueToDouble() = value.toDouble()
    fun valueToString() = value.toString()
}
