package org.arbol.logic.tree.operation.treeBPlusDisk.cache;

import org.arbol.logic.structures.node.Node;

import java.io.Serializable;
import java.util.Optional;

public interface NodeCache<K extends Comparable<K> & Serializable, V extends Serializable> {
    Optional<Node<K, V>> get(long pageId);
    void put(long pageId, Node<K, V> node);
    void invalidate(long pageId);
}

