package org.arbol.logic.tree;

import org.arbol.logic.nodes.Node;

public class TreeB extends Tree implements TreeOperations{


    public TreeB(Node root) {
        super(root);
    }

    @Override
    public <K extends Comparable, V> void insert(Node<K, V> node) {

    }

    @Override
    public <K extends Comparable, V> Node<K, V> search(K key) {
        return null;
    }

    @Override
    public <K extends Comparable> void delete(K key) {

    }
}
