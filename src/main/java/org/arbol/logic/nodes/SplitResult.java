package org.arbol.logic.nodes;

public record SplitResult<K extends Comparable<K>, V>(
        NodeElement<K, V> promotedElement,
        Node<K, V> newNode
) {
}
