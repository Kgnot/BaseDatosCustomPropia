package org.arbol.logic.tree;

import org.arbol.logic.nodes.Node;

public abstract class Tree<K extends Comparable> {
    protected Node<K, ?> root; // Cómo root no debe tener valor

    public Tree(Node<K, ?> root) {
        this.root = root;
    }
}
