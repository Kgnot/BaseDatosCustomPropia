package org.arbol.logic.structures.node;

import java.io.Serializable;

public record   SplitResult<K extends Comparable<K> & Serializable, V extends Serializable>(
        NodeElement<K, V> promotedElement,
        Node<K, V> newNode
) {
}
