package org.arbol.logic.tree;

import org.arbol.logic.nodes.Node;

public interface TreeOperations {
    <K extends Comparable, V> void insert(Node<K, V> node);

    <K extends Comparable, V> Node<K, V> search(K key);

    <K extends Comparable> void delete(K key);
}
