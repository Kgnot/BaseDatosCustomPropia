package org.arbol.logic.structures;

public record NodeElement<K, V>(
        K key,
        V value
) {
}
