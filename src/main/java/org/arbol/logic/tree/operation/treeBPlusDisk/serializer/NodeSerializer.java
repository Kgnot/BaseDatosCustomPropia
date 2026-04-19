package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.Node;

import java.io.Serializable;

public interface NodeSerializer<K extends Comparable<K> & Serializable, V extends Serializable> {
    byte[] serialize(Node<K, V> node);
    Node<K, V> deserialize(byte[] data, int maxSize);
}

