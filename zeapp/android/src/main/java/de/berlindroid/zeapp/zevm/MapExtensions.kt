package de.berlindroid.zeapp.zevm

fun <K, V> Map<K, V>.copy(vararg entries: Pair<K, V>): Map<K, V> {
    val result = toMutableMap()

    entries.forEach { entry ->
        val (replaceKey: K, replaceValue: V) = entry
        result[replaceKey] = replaceValue
    }

    return result.toMap()
}
