package org.arbol.logic.nodes;

import java.util.Map;

public final class LeafNode<K extends Comparable, V> extends Node<K, V> {
    private Map<K, V> elements;

    public LeafNode(int maxSize) {
        super(maxSize);
    }
}
