package io.github.digorydoo.kstruct

class KstructSerialiser(indent: Int = 3, private val style: Style = Style.INDENTED) {
    class EmptyKeysNotAllowedException: Exception("Empty keys are not allowed")

    enum class Style { INDENTED, FLAT }

    private val indent = Array(indent) { " " }.joinToString("")

    fun serialise(kstruct: KstructNode): String {
        val map = (kstruct.value as? KstructMap)
            ?.takeIf { it.attributes.isEmpty() }
            ?: KstructMap(
                children = mutableMapOf("value" to kstruct),
                attributes = mutableMapOf(),
            )
        return serialiseValue(map, -1, true)
    }

    private fun serialiseValue(value: KstructValue, level: Int, anonymous: Boolean) =
        when (value) {
            is KstructNull -> "null"
            is KstructBoolean -> value.value.toString()
            is KstructChar -> serialiseChar(value.value)
            is KstructInt -> value.value.toString()
            is KstructLong -> "${value.value}L"
            is KstructFloat -> if (value.value.isFinite()) "${value.value}f" else "null"
            is KstructDouble -> if (value.value.isFinite()) "${value.value}" else "null"
            is KstructString -> serialiseString(value.value)
            is KstructMap -> serialiseMap(value.children, value.attributes, level, anonymous)
            is KstructList -> serialiseList(value.children, level)
        }

    private fun serialiseString(value: String) =
        value.replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\"", "\\\"")
            .replace("$", "\\$")
            .let { "\"$it\"" }

    private fun serialiseChar(value: Char) =
        when (value) {
            '\\' -> "'\\\\'"
            '\n' -> "'\\n'"
            '\r' -> "'\\r'"
            '\t' -> "'\\t'"
            '\'' -> "'\\\''"
            else -> "'$value'"
        }

    private fun encodeKey(key: String): String = when {
        key.isEmpty() -> throw EmptyKeysNotAllowedException()
        nonEncodedKeysRegex.matches(key) -> key
        else -> key.replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("`", "\\`")
            .let { "`$it`" }
    }

    private fun serialiseMap(
        map: Map<String, KstructNode>,
        attributes: Map<String, KstructAttribute>,
        level: Int,
        anonymous: Boolean,
    ): String {
        var result = ""

        if (attributes.isNotEmpty()) {
            result += "("
            var firstAttr = true

            attributes.forEach { (attrName, attrValue) ->
                if (firstAttr) firstAttr = false
                else result += ", "

                result += encodeKey(attrName) + " = " + serialiseValue(attrValue.value, level + 1, false)
            }

            result += ")"

            if (map.isEmpty()) {
                return result // braces are optional when there are attributes
            }
        }

        if (!anonymous || attributes.isNotEmpty()) result += " "

        if (map.isEmpty()) {
            return if (level >= 0) "$result{}" else result
        }

        val wrap = (style == Style.INDENTED || level < 0) &&
            (map.size > 1 || attributes.size > 1 || map.hasAnyNestedChildren())

        if (level >= 0) {
            result += if (wrap) "{" else "{ "
        }

        val outerIndent = if (level <= 0) "" else Array(level) { indent }.joinToString("")
        val innerIndent = if (!wrap || level < 0) "" else outerIndent + indent
        var firstChild = true

        map.forEach { (key, child) ->
            if (firstChild) {
                firstChild = false
                if (wrap && level >= 0) result += "\n"
            } else {
                result += if (wrap) "\n" else "; "
            }

            result += innerIndent + encodeKey(key)
            val childValue = child.value

            if (childValue !is KstructMap) {
                result += " = "
            }

            result += serialiseValue(childValue, level + 1, false)
        }

        if (level >= 0) {
            result += if (wrap) "\n$outerIndent}" else " }"
        }

        return result
    }

    private fun serialiseList(list: List<KstructNode>, level: Int): String {
        var result = "["
        val wrap = list.size > 1 && list.hasAnyNestedChildren()
        val outerIndent = if (level == 0) "" else Array(level) { indent }.joinToString("")
        val innerIndent = if (!wrap) "" else outerIndent + indent
        var firstChild = true

        list.forEach { child ->
            if (firstChild) {
                firstChild = false
                if (wrap) result += "\n"
            } else {
                result += if (wrap) ",\n" else ", "
            }

            result += innerIndent + serialiseValue(child.value, level + (if (wrap) 1 else 0), true)
        }

        if (wrap) result += "\n$outerIndent"
        result += "]"
        return result
    }

    companion object {
        private val nonEncodedKeysRegex = Regex("[a-zA-Z][-_a-zA-Z0-9]*")

        private fun Map<String, KstructNode>.hasAnyNestedChildren() =
            any { (_, node) -> node.isMap() || node.isList() }

        private fun List<KstructNode>.hasAnyNestedChildren() =
            any { node -> node.isMap() || node.isList() }
    }
}
