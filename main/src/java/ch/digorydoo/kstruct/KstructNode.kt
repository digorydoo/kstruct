package ch.digorydoo.kstruct

class KstructNode(var value: KstructValue) {
    fun isNull() = value is KstructNull
    fun isBoolean() = value is KstructBoolean
    fun isChar() = value is KstructChar
    fun isInt() = value is KstructInt
    fun isFloat() = value is KstructFloat
    fun isDouble() = value is KstructDouble
    fun isString() = value is KstructString
    fun isMap() = value is KstructMap
    fun isList() = value is KstructList

    fun booleanOrNull() = value.booleanOrNull()
    fun charOrNull() = value.charOrNull()
    fun intOrNull() = value.intOrNull()
    fun floatOrNull() = value.floatOrNull()
    fun doubleOrNull() = value.doubleOrNull()
    fun stringOrNull() = value.stringOrNull()
    fun mapOrNull() = value.mapOrNull()
    fun listOrNull() = value.listOrNull()

    fun valueToBoolean() = value.toBoolean()
    fun valueToChar() = value.toChar()
    fun valueToInt() = value.toInt()
    fun valueToFloat() = value.toFloat()
    fun valueToDouble() = value.toDouble()
    fun valueToString() = value.toString()

    fun keys(): Collection<String>? {
        return when (val value = value) {
            is KstructMap -> value.children.keys
            is KstructList -> value.children.indices.map { "$it" }
            else -> null
        }
    }

    fun children(): Collection<KstructNode>? {
        return when (val value = value) {
            is KstructMap -> value.children.values
            is KstructList -> value.children
            else -> null
        }
    }

    fun forEachChild(lambda: (KstructNode) -> Unit) {
        val collection = children() ?: return
        for (child in collection) lambda(child)
    }

    val attributes get() = (value as? KstructMap)?.attributes
}
