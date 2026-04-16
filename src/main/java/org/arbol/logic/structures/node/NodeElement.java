package org.arbol.logic.structures.node;

import java.io.Serializable;

public record NodeElement<K extends Comparable<K> & Serializable, V extends Serializable>(
        K key,
        V value
) {
}
