package org.arbol.logic.nodes;

import java.util.List;

public final class InternalNode<K extends Comparable, V> extends Node<K, V> {
    private List<K> keys;
    private List<Node<K, V>> children;

    public InternalNode(int maxSize) {
        super(maxSize);
    }


    public void addKey(K key) {
        if (keys == null) {
            keys = new java.util.ArrayList<>();
        }
        if (keys.size() == this.maxSize) {
            // debemos delegar esto a crear otro nodo y añadirlo al arbol con cierta lógica
            return;
        }
        keys.add(key);
    }

    public void addChildren(Node<K, V> child) {
        children.add(child);
    }

}
