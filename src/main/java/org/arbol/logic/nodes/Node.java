package org.arbol.logic.nodes;

public sealed abstract class Node<K extends Comparable, V> permits LeafNode, InternalNode {
    int maxSize;

    public Node(int maxSize) {
        this.maxSize = maxSize;
    }

    // Esto no vamos a usarlo hasta el final xd

}
