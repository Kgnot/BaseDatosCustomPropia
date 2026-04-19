package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.Node;

import java.io.Serializable;

public interface NodeCodec<K extends Comparable<K> & Serializable, V extends Serializable, T extends Node<K, V>> {
    int typeId();
    Class<T> nodeClass();
    byte[] serializeBody(T node);
    T deserializeBody(byte[] data, int maxSize);
}

