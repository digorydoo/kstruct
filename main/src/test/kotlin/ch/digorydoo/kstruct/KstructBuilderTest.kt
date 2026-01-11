package ch.digorydoo.kstruct

import kotlin.test.Test
import kotlin.test.assertEquals

internal class KstructBuilderTest {
    @Test
    fun `should correctly build a deeply nested object`() {
        val obj = KstructBuilder.build {
            val intNull: Int? = null
            val stringNull: String? = null
            set("intNull", intNull)
            set("stringNull", stringNull)
            set("count", 10)
            set("count", 11) // silently overrides same key
            set("weight", 0.42f)
            set("tolerance", 0.00110011)
            set("name", "Saladdim Gurkovic")

            setMap("theMap") {
                set("x", 101.11f)
                set("y", 155.02f)
            }

            setList("theList") {
                add(intNull)
                add(99)
                add("Luftballons")
                add(3.14f)
                add(3.141592653589793)

                addMap {
                    set("x", -42.11f)
                    set("y", -42.02f)
                }

                addList {
                    add("one")
                    add("two")
                    add("three")
                }
            }
        }
        val serialised = KstructSerialiser().serialise(obj)
        assertEquals(
            """
            intNull = null
            stringNull = null
            count = 11
            weight = 0.42f
            tolerance = 0.00110011
            name = "Saladdim Gurkovic"
            theMap {
               x = 101.11f
               y = 155.02f
            }
            theList = [
               null,
               99,
               "Luftballons",
               3.14f,
               3.141592653589793,
               {
                  x = -42.11f
                  y = -42.02f
               },
               ["one", "two", "three"]
            ]
            """.trimIndent(),
            serialised
        )
    }
}
