package rs.moma.therminator.ui.utils

fun naturalCompare(firstString: String, secondString: String): Int {
    val firstParts = "\\d+|\\D+".toRegex().findAll(firstString).map { it.value }
    val secondParts = "\\d+|\\D+".toRegex().findAll(secondString).map { it.value }
    return firstParts.zip(secondParts).map { (firstPart, secondPart) ->
        if (firstPart[0].isDigit() && secondPart[0].isDigit()) firstPart.toLong().compareTo(secondPart.toLong())
        else firstPart.compareTo(secondPart, ignoreCase = true)
    }.firstOrNull { it != 0 } ?: firstString.length.compareTo(secondString.length)
}

fun Int.pad(length: Int = 2, padChar: Char = '0') = toString().padStart(length, padChar)