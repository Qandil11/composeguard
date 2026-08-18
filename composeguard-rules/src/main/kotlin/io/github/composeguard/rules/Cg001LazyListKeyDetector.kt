package io.github.composeguard.rules

data class Cg001Finding(
    val line: Int,
    val startOffset: Int,
    val endOffset: Int,
    val container: String,
    val detected: String
)

object Cg001LazyListKeyDetector {
    private val lazyContainers = setOf("LazyColumn", "LazyRow")
    private val lazyItemCalls = setOf("items", "itemsIndexed")

    fun detect(source: String): List<Cg001Finding> {
        val lineForOffset = source.lineNumberLookup()
        val findings = mutableListOf<Cg001Finding>()
        findCalls(source, lazyContainers).forEach { container ->
            val bodyRange = container.lambdaBodyRange ?: return@forEach
            findCalls(source, lazyItemCalls, bodyRange).forEach { itemCall ->
                if (!itemCall.arguments.containsNamedArgument("key")) {
                    findings += Cg001Finding(
                        line = lineForOffset(itemCall.startOffset),
                        startOffset = itemCall.startOffset,
                        endOffset = itemCall.endOffset,
                        container = container.name,
                        detected = "${itemCall.name}(${itemCall.firstArgumentText()}) { ... }"
                    )
                }
            }
        }
        return findings
    }

    private fun findCalls(source: String, names: Set<String>, searchRange: IntRange = source.indices): List<CallSite> {
        val calls = mutableListOf<CallSite>()
        var index = searchRange.first
        while (index <= searchRange.last) {
            val match = names.firstNotNullOfOrNull { name ->
                if (source.matchesIdentifierAt(index, name)) name else null
            }
            if (match == null) {
                index++
                continue
            }

            val nextToken = source.indexOfNextNonWhitespace(index + match.length, searchRange.last)
            if (nextToken == null) {
                index++
                continue
            }

            val arguments: String
            val lambdaOpen: Int?
            val closeOffset: Int
            if (source[nextToken] == '(') {
                val closeParen = source.findMatching(nextToken, '(', ')')
                if (closeParen == null) {
                    index++
                    continue
                }
                arguments = source.substring(nextToken + 1, closeParen)
                lambdaOpen = source.indexOfNextNonWhitespace(closeParen + 1, searchRange.last)
                    ?.takeIf { source[it] == '{' }
                closeOffset = closeParen
            } else if (source[nextToken] == '{') {
                arguments = ""
                lambdaOpen = nextToken
                closeOffset = nextToken
            } else {
                index++
                continue
            }
            val lambdaClose = lambdaOpen?.let { source.findMatching(it, '{', '}') }

            calls += CallSite(
                name = match,
                startOffset = index,
                endOffset = lambdaClose?.plus(1) ?: closeOffset + 1,
                arguments = arguments,
                lambdaBodyRange = if (lambdaOpen != null && lambdaClose != null) {
                    (lambdaOpen + 1) until lambdaClose
                } else {
                    null
                }
            )
            index = (lambdaClose ?: closeOffset) + 1
        }
        return calls
    }

    private fun String.matchesIdentifierAt(index: Int, name: String): Boolean {
        if (index < 0 || index + name.length > length) return false
        if (!regionMatches(index, name, 0, name.length)) return false
        val before = getOrNull(index - 1)
        val after = getOrNull(index + name.length)
        return before?.isIdentifierPart() != true && after?.isIdentifierPart() != true
    }

    private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_'

    private fun String.indexOfNextNonWhitespace(start: Int, endInclusive: Int): Int? {
        var current = start
        while (current <= endInclusive && current < length) {
            if (!this[current].isWhitespace()) return current
            current++
        }
        return null
    }

    private fun String.findMatching(openIndex: Int, open: Char, close: Char): Int? {
        var depth = 0
        var current = openIndex
        while (current < length) {
            when (this[current]) {
                open -> depth++
                close -> {
                    depth--
                    if (depth == 0) return current
                }
                '"' -> current = skipStringLiteral(current)
                '\'' -> current = skipCharLiteral(current)
            }
            current++
        }
        return null
    }

    private fun String.skipStringLiteral(start: Int): Int {
        if (getOrNull(start + 1) == '"' && getOrNull(start + 2) == '"') {
            val end = indexOf("\"\"\"", start + 3)
            return if (end >= 0) end + 2 else lastIndex
        }
        var current = start + 1
        while (current < length) {
            if (this[current] == '\\') current += 2 else if (this[current] == '"') return current else current++
        }
        return lastIndex
    }

    private fun String.skipCharLiteral(start: Int): Int {
        var current = start + 1
        while (current < length) {
            if (this[current] == '\\') current += 2 else if (this[current] == '\'') return current else current++
        }
        return lastIndex
    }

    private fun String.containsNamedArgument(name: String): Boolean =
        Regex("""(^|,)\s*$name\s=""").containsMatchIn(this)

    private fun CallSite.firstArgumentText(): String {
        val comma = arguments.indexOf(',')
        val first = if (comma >= 0) arguments.substring(0, comma) else arguments
        return first.trim().ifBlank { "..." }
    }

    private data class CallSite(
        val name: String,
        val startOffset: Int,
        val endOffset: Int,
        val arguments: String,
        val lambdaBodyRange: IntRange?
    )
}
