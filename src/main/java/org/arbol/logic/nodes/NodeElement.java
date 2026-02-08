package org.arbol.logic.nodes;

public record NodeElement<K, V>(
        K key,
        V value
) {
}
