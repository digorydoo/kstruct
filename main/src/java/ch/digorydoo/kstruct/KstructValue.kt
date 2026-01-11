package ch.digorydoo.kstruct

sealed class KstructValue {
    open fun booleanOrNull(): Boolean? = null
    open fun charOrNull(): Char? = null
    open fun intOrNull(): Int? = null
    open fun longOrNull(): Long? = null
    open fun floatOrNull(): Float? = null
    open fun doubleOrNull(): Double? = null
    open fun stringOrNull(): String? = null
    open fun mapOrNull(): Map<String, KstructNode>? = null
    open fun listOrNull(): List<KstructNode>? = null

    open fun toBoolean() = false
    open fun toChar() = Char(0)
    open fun toInt(): Int = 0
    open fun toLong(): Long = 0L
    open fun toFloat(): Float = 0.0f
    open fun toDouble(): Double = 0.0
    override fun toString() = "KstructValue()"
}

class KstructNull: KstructValue() {
    override fun toString() = "null"
}

class KstructBoolean(val value: Boolean): KstructValue() {
    override fun booleanOrNull() = value

    override fun toBoolean() = value
    override fun toChar() = if (value) 'y' else 'n'
    override fun toInt() = if (value) 1 else 0
    override fun toLong() = if (value) 1L else 0L
    override fun toFloat() = Float.NaN
    override fun toDouble() = Double.NaN
    override fun toString() = "$value"
}

class KstructChar(val value: Char): KstructValue() {
    override fun charOrNull() = value

    override fun toBoolean() = value.code != 0
    override fun toChar() = value
    override fun toInt() = value.code
    override fun toLong() = value.code.toLong()
    override fun toFloat() = value.code.toFloat()
    override fun toDouble() = value.code.toDouble()
    override fun toString() = value.toString()
}

class KstructInt(val value: Int): KstructValue() {
    override fun intOrNull() = value

    override fun toBoolean() = value != 0
    override fun toChar() = value.toChar()
    override fun toInt() = value
    override fun toLong() = value.toLong()
    override fun toFloat() = value.toFloat()
    override fun toDouble() = value.toDouble()
    override fun toString() = value.toString()
}

class KstructLong(val value: Long): KstructValue() {
    override fun longOrNull() = value

    override fun toBoolean() = value != 0L
    override fun toChar() = value.toInt().toChar()
    override fun toInt() = value.toInt()
    override fun toLong() = value
    override fun toFloat() = value.toFloat()
    override fun toDouble() = value.toDouble()
    override fun toString() = value.toString()
}

class KstructFloat(val value: Float): KstructValue() {
    override fun floatOrNull() = value

    override fun toBoolean() = value != 0.0f
    override fun toChar() = value.toInt().toChar()
    override fun toInt() = value.toInt()
    override fun toLong() = value.toLong()
    override fun toFloat() = value
    override fun toDouble() = value.toDouble()
    override fun toString() = value.toString()
}

class KstructDouble(val value: Double): KstructValue() {
    override fun doubleOrNull() = value

    override fun toBoolean() = value != 0.0
    override fun toChar() = value.toInt().toChar()
    override fun toInt() = value.toInt()
    override fun toLong() = value.toLong()
    override fun toFloat() = value.toFloat()
    override fun toDouble() = value
    override fun toString() = value.toString()
}

class KstructString(val value: String): KstructValue() {
    override fun stringOrNull() = value

    override fun toBoolean() = value.isNotEmpty()
    override fun toChar() = if (value.length == 1) value.first() else Char(0)
    override fun toInt() = value.toInt()
    override fun toLong() = value.toLong()
    override fun toFloat() = value.toFloat()
    override fun toDouble() = value.toDouble()
    override fun toString() = value
}

class KstructMap(
    val children: MutableMap<String, KstructNode>,
    val attributes: MutableMap<String, KstructAttribute>,
): KstructValue() {
    override fun mapOrNull() = children
    override fun toBoolean() = children.isNotEmpty()
    override fun toString() = "KstructMap()"
}

class KstructList(val children: MutableList<KstructNode>): KstructValue() {
    override fun listOrNull() = children
    override fun toBoolean() = children.isNotEmpty()
    override fun toString() = "KstructList()"
}
