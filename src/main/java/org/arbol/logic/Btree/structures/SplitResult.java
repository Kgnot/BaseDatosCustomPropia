package org.arbol.logic.Btree.structures;

public record SplitResult<K extends Comparable<K>, V>(
        NodeElement<K, V> promotedElement,
        Node<K, V> newNode
) {
}
