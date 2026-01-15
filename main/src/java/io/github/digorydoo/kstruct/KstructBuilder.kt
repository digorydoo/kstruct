package io.github.digorydoo.kstruct

// This annotation prevents accidental calls of a member in the DSL's outer scope
@DslMarker
annotation class KstructDsl

@KstructDsl
class KstructBuilder private constructor() {
    private val children = mutableMapOf<String, KstructNode>()
    private val attributes = mutableMapOf<String, KstructAttribute>()

    @KstructDsl
    class ListBuilder internal constructor() {
        val list = mutableListOf<KstructNode>()

        fun add(value: Boolean?) {
            val wrapper = value?.let { KstructBoolean(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun add(value: Char?) {
            val wrapper = value?.let { KstructChar(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun add(value: Int?) {
            val wrapper = value?.let { KstructInt(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun add(value: Float?) {
            val wrapper = value?.let { KstructFloat(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun add(value: Double?) {
            val wrapper = value?.let { KstructDouble(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun add(value: String?) {
            val wrapper = value?.let { KstructString(value) } ?: KstructNull()
            list.add(wrapper)
        }

        fun addMap(lambda: KstructBuilder.() -> Unit) {
            val builder = KstructBuilder()
            builder.lambda()
            list.add(KstructMap(builder.children, builder.attributes))
        }

        fun addList(lambda: ListBuilder.() -> Unit) {
            val builder = ListBuilder()
            builder.lambda()
            list.add(KstructList(builder.list))
        }
    }

    fun set(key: String, value: Boolean?) {
        val wrapper = value?.let { KstructBoolean(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun set(key: String, value: Char?) {
        val wrapper = value?.let { KstructChar(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun set(key: String, value: Int?) {
        val wrapper = value?.let { KstructInt(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun set(key: String, value: Float?) {
        val wrapper = value?.let { KstructFloat(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun set(key: String, value: Double?) {
        val wrapper = value?.let { KstructDouble(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun set(key: String, value: String?) {
        val wrapper = value?.let { KstructString(value) } ?: KstructNull()
        children[key] = wrapper
    }

    fun setMap(key: String, lambda: KstructBuilder.() -> Unit) {
        val builder = KstructBuilder()
        builder.lambda()
        children[key] = KstructMap(builder.children, builder.attributes)
    }

    fun setList(key: String, lambda: ListBuilder.() -> Unit) {
        val builder = ListBuilder()
        builder.lambda()
        children[key] = KstructList(builder.list)
    }

    fun attr(key: String, value: Int?) {
        val wrapper = value?.let { KstructInt(value) } ?: KstructNull()
        attributes[key] = KstructAttribute(wrapper)
    }

    fun attr(key: String, value: Float?) {
        val wrapper = value?.let { KstructFloat(value) } ?: KstructNull()
        attributes[key] = KstructAttribute(wrapper)
    }

    fun attr(key: String, value: Double?) {
        val wrapper = value?.let { KstructDouble(value) } ?: KstructNull()
        attributes[key] = KstructAttribute(wrapper)
    }

    fun attr(key: String, value: String?) {
        val wrapper = value?.let { KstructString(value) } ?: KstructNull()
        attributes[key] = KstructAttribute(wrapper)
    }

    companion object {
        fun build(lambda: KstructBuilder.() -> Unit): KstructMap {
            val builder = KstructBuilder()
            builder.lambda()
            return KstructMap(builder.children, builder.attributes)
        }
    }
}
