# Kstruct

## Overview

Kstruct is a human-readable, structured data forma that tries to emulate Kotlin DSL as much as possible. It supports
values of type Boolean, Char, Int, Long, Float, Double, Map, List.

Kstruct has similarities with JSON. Key differences:

* Kstruct allows line comments as well as block comments
* Kstruct allows trailing commas in lists
* Key-value pairs of a map are separated by semicolon rather than comma, and they are optional if there's a newline,
  which is consistent with Kotlin DSL
* Keys of a map do not need to be enclosed in quotes if they follow the allowed pattern
* If a key does not follow the allowed pattern, it needs to be enclosed in backticks rather than double quotes, which is
  consistent with how you wrap a variable name in Kotlin
* The root of a map can have attributes (similar to XML)

Kstruct has also similarities to HOCON. Key differences:

* Kstruct is general purpose. HOCON is centred around configuration files, and both the API and features reflect this.
* Kstruct does not allow leaking environment variables into the data
* Kstruct never merges objects with identical keys (in Kstruct, identical keys in a map are disallowed)
* Kstruct does not allow graphs by linking subtrees

## Creating Kstructs from code

While it is possible to create instances of `KstructNode` directly, it is usually more convenient to use
`KstructBuilder.build`. Example:

```
val node = KstructBuilder.build {
    set("nothing", null as Int?)
    set("count", 10)
    set("weight", 0.42f)
    set("tolerance", 0.00110011)
    set("name", "Manny")

    setMap("nameOfMap") {
        set("x", 101.11f)
        set("y", 155.02f)
    }

    setList("theList") {
        add(99)
        add("Luftballons")
        addMap { set("inner", -42.11f) }
        addList { add("one") }
    }
}
```

## Serialising Kstructs to String

Kstructs are always serialised as UTF-8 (similar to JSON). You use `KstructSerialiser` like this:

```
    val node: KstructNode = KstructBuilder.build { }
    val serialiser = KstructSerialiser(indent = 3, style = Style.INDENTED)
    val serialised: String = serialiser.serialise(node)
```

`KstructSerialiser` supports different styles:

* `Style.INDENTED`: pretty-print
* `Style.FLAT`: denser that `INDENTED`, but the root map and lists are still wrapped, which gives better diffs than
  fully dense minified output
* `Style.MINIFIED`: fully dense, TODO: NOT YET IMPLEMENTED

## Examples

The root of a Kstruct is always a map after serialisation. Therefore, a simple key-value file looks like this:

```
someKey = 1
anotherKey = false
and = "a String"
```

Maps and Lists look like this:

```
exampleMap { one = 1; two = "ni" }
exampleList = [ 42, null ]
```

Maps can have attributes:

```
example(x = 5.5f, y = 2.5f) {
   one = 1L
   two = 2.0
}
```

A List containing Maps looks like this:

```
example = [
   { one = "ichi" },
   { two = "ni" }
]
```

## Reference

The following is an extract from the unit test KstructParserTest.

```
// Kstructs supports line comments
/*
   Block comments are also supported
   /* Nested comments are allowed, just like in Kotlin */
*/

// Keys which are conform to common variable rules do not need to be quoted.
booleanFlag=true
someChar='x'
Int_Value=-42
longer=0L
weight=0.001f
epsilon=0.000000001
a0123456789="That's right"

// null is supported, but does not come with a type. Similar to JSON.stringify, the
// KstructSerialiser will silently emit Floats and Doubles as null if they are not
// finite. The empty string and null are not treated as the same.
nothing=null
empty=""

// Unlike Kotlin, keywords do not clash with keys. You do not need to enclose the
// following in backticks.
null = 1
true = false
false = true
Char = 1
Int = 2
Long = 3
Float = 4
Double = 5
fun = 6 // isn't it?

// For Floats, Kotlin's various notations are supported.
f00=0f
f01=-0f
f02=.5f
f03=-.5f
f04=4e4f
f05=-4e4f
f06=4e+4f
f07=4e-4f
f08=1.2345678E4f
f09=-1.2345678e3f

// The same goes for Double.
d00=-0.0
d01=.5
d02=-.5
d03=4e4 // Note that this is a Double in Kotlin, not an Int
d04=-4e4
d05=4e+4
d06=4e-4
d07=1.2345678E4
d08=-1.2345678e3

// Some esacpe sequences are supported inside Strings.
s1 = "\n"
s2 = "\r"
s3 = "\t"
s4 = "\\"
s5 = "\""
s6 = "'" // does not need to be escaped
s7 = "`" // dito
s8 = "\'" // but it is allowed
s9 = "\`" // dito

// Escaping of dollar signs is required even though Kstruct does not allow template strings at present.
s10 = "Escaping \$ bills"

// Just like in Kotlin, escaping is not required in certain edge cases.
s11 = "$" // at end of String
s12 = "$\n" // when an escape sequence follows
s13 = "$ ws" // when whitespace follows

// Similar to Kotlin, keys which are not conform to those rules can be enclosed in
// backticks.
`This key contains spaces and 漢字` = true
`=`="="

// Note that a key escaped in backticks can even contain dots and other characters that Kotlin would
// not allow as a variable name even with backticks. Even though backslash character is illegal in
// Kotlin backticks, Kstruct allows it to support arbitrary keys.
`a.b.c`=true; `\\`=true; `\n`=true; `\r`=true; `\t`=true
wrapper { `\$`=true } // escaping of dollar is optional here
duplicate { `$`=true } // will result in the same key

// In Kotlin, sometimes dollar does not need to be escaped. Kstruct emulates this.
noEscapingNeeded = [
   '$', // when it's a Char
   "$", // when a String ends in dollar
   """$""", // when a verbatim string ends in dollar
   { `$` = "dollar" }, // when a backticks key ends in dollar
   "$0", // when dollar is followed by a digit

   // or when a word delimiting character other than an opening brace follows:
   "$%",
   "$&",
   "$'",
   "$(",
   "$)",
   "$*",
   "$+",
   "$,",
   "$-",
   "$.",
   "$/",
   "$:",
   "$;",
   "$<",
   "$=",
   "$>",
   "$[",
   "$]",
   "$^",
   "$`",
   "$}",
   "$~",
   "$§",
   "$°",
   "$\"",
   "$$",
]

// The following would all mean template strings in Kotlin. The Kstruct parser throws if you try these,
// because Kstruct does not currently allow template strings.
// invalid = ["$a", "$_a", "$_", "$$a"]

// Single quotes vs. double quotes are important, they denote Char or String,
// respectively, just like in Kotlin.
single='X'
double="Mr X"

// Long strings can span multiple lines when concatenated with plus. Note that the plus needs to be on
// the right side and cannot be on the left end of the line just like in Kotlin. (Kotlin would end the
// statement of the preceding line and interprete the plus as an unary operator.)
concatenated
   =
      "Once " +
      "and" +
      " for all! " + "Sometimes, I even amaze myself!"

// NOTE: Kotlin's trimIndent() transforms solo \r's into \n's. Therefore, even if you include explicit
// escape sequences of \r in a Kstruct verbatim string, the result will always be \n! Luckily, solo \r
// is extremely rare nowadays.

// Kotlin's verbatim strings are also supported. However, Kstruct parser implicitly always applies
// a trimIndent().trimEnd(), and trimMargin() is not supported.
spansMultipleLines = """
   This text can span multiple lines.
   Escaping of "double quotes", 'single quotes' and `backticks` is unnecessary.
   Because of implicit trimIndent(), common leading whitespace and the first newline is removed.
   Because of implicit trimEnd(), all trailing whitespace is removed.
"""

// The plus operator also supports adding Ints, Longs, Floats or Doubles, but take care
// not to mix them. This is just for completeness; no other operators are allowed at this
// time.
willBeInt = 1 + 2
willBeLong = 3L + 4L
willBeFloat = 5.5f + 6.6f
willBeDouble = 7.0 + 8.0
// notSupported = "nine" + 10

// Multiple assignments can be kept on the same line by delimiting them with semicolon. This
// is similar to Kotlin, but unlike JSON, which uses commas.
oneThe="same"; line=true

// The things we've defined so far define the keys and values of a map. In fact, the root of a
// serialised Kstruct is always a map. If you construct a Kstruct from code and serialise it,
// it will wrap it in a map with a single key named "value". Of course, we can also define a
// key whose value is a nested map.
map = { one = true; two = false }

// We can drop the equal sign with above assignment. This is semantically identical, but looks
// closer to Kotlin DSL.
alsoMap { one = true; two = false }

// Keys in a map must be unique. If they aren't, the parser will throw. This is unlike JSON,
// which typically silently overwrites the keys (even though that behaviour does not appear to
// be exactly documented). It is also unlike HOCON, which merges the values of the two
// definition.

// A map's closing brace is enough to separate it from the next assignment. Therefore, semicolon
// is not needed if another key follows on the same line.
anotherMap { yes = true } anotherKey = 1

// Superfluous semicolons are ignored
; thisIsntAComment = 1 ;; doubleSemicolonAre { fine = true; };

// Like XML, keys whose values are maps can have attributes. This looks like Kotlin DSL with
// arguments.
keyOfAMap(keyOfAttribute=42) { keyOfChild=true }

// The braces can be omitted if the map has only attributes and no children.
keyOfAnotherMap(keyOfAttribute="andValue")

// Like with Kotlin Map, the order of the keys of a Kstruct map is defined as/ the order how
// they are added, i.e. listed in the serialisation. This is unlike JSON, which leaves the
// order of keys arbitrary (which sometimes leads to unexpected behaviour and bugs). JSON uses
// arrays to list values whose order are important. Kstruct also supports this as lists.
// Note that the equals sign is required here, because without it would look like an indexed
// lookup rather than a definition.
keyOfAList = [ 1, "two", 3L ]

// The members of a list are separated by comma, not semicolon. This is similar to Kotlin if
// you would use arrayOf(1, 2). When spanning multiple lines, the comma may not be dropped,
// but one trailing comma is allowed.
keyOfList2 = [
   "four",
   "five",
]

// A list can contain other lists.
nestedList = [ [ 1 ], [ 2, 3 ], 4 ]

// A list can also contain maps.
listWithNestedMaps = [ { a = true }, { a = false } ]

// Nested maps can still have attributes. If a map with attributes has no children, the
// braces can be omitted.
listWithNestedMapsWithAttr = [
   (attr = 1) { child = "" },
   (attr = 1), 
]

// A list's closing bracket is enough to delimit it from the next assignment.
yetAnotherList=[1,2]; yetAnotherThing=true
```
