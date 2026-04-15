package org.arbol.logic.structures;

import java.io.Serializable;

public record NodeElement<K extends Comparable<K> & Serializable, V extends Serializable>(
        K key,
        V value
) {
}
