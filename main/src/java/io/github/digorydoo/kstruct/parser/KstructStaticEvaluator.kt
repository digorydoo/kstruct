package io.github.digorydoo.kstruct.parser

/**
 * This class takes a list of tokens and produces a new list of tokens where sequences of tokens that can be statically
 * evaluated have been replaced by their evaluated result. We currently only implement the plus operator, which we
 * primarily need to concatenate strings. If we ever wanted to implement more operators, we should also implement
 * parentheses and take care of operator precedence. However, for a simple data format that seems unnecessary. Keep It
 * Simple, Stupid!
 */
internal class KstructStaticEvaluator private constructor(private val tokens: List<KstructToken<*>>) {
    private var nextTokenIdx = 0
    private val newTokens = mutableListOf<KstructToken<*>>()

    private fun nextToken() = when {
        nextTokenIdx !in tokens.indices -> null
        else -> tokens[nextTokenIdx++]
    }

    fun evaluate() {
        while (true) {
            when (val token = nextToken()) {
                is PlusToken,
                -> {
                    val left = newTokens.lastOrNull()

                    val right = run {
                        var i = nextTokenIdx
                        var t = tokens.getOrNull(i++)
                        while (t is NewlineToken) t = tokens.getOrNull(i++)
                        t
                    }

                    val evaluated = evaluate(token, left, right)

                    if (evaluated == null) {
                        newTokens.add(token) // pass operator on to the next stage
                    } else {
                        newTokens.removeLast() // drop left operand
                        newTokens.add(evaluated)

                        while (true) {
                            if (nextToken() == right) break
                        }
                    }
                }
                null -> break
                else -> newTokens.add(token)
            }
        }
    }

    companion object {
        fun evaluate(tokens: List<KstructToken<*>>): List<KstructToken<*>> =
            KstructStaticEvaluator(tokens)
                .apply { evaluate() }
                .newTokens

        private fun evaluate(op: PlusToken, left: KstructToken<*>?, right: KstructToken<*>?) = when {
            left is IntToken && right is IntToken -> evaluate(op, left.content, right.content)
            left is LongToken && right is LongToken -> evaluate(op, left.content, right.content)
            left is FloatToken && right is FloatToken -> evaluate(op, left.content, right.content)
            left is DoubleToken && right is DoubleToken -> evaluate(op, left.content, right.content)
            left is StringToken && right is StringToken -> evaluate(op, left.content, right.content)
            else -> null
        }

        private fun evaluate(op: PlusToken, left: Int, right: Int) = IntToken(op.line, left + right)
        private fun evaluate(op: PlusToken, left: Long, right: Long) = LongToken(op.line, left + right)
        private fun evaluate(op: PlusToken, left: Float, right: Float) = FloatToken(op.line, left + right)
        private fun evaluate(op: PlusToken, left: Double, right: Double) = DoubleToken(op.line, left + right)
        private fun evaluate(op: PlusToken, left: String, right: String) = StringToken(op.line, left + right)
    }
}
