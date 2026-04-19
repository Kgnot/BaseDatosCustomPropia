package org.arbol.logic.tree.operation.treeBPlusDisk.cache;

import org.arbol.logic.structures.node.Node;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LruNodeCache<K extends Comparable<K> & Serializable, V extends Serializable>
        implements NodeCache<K, V> {

    private final Map<Long, Node<K, V>> map;

    public LruNodeCache(int capacity) {
        this.map = new LinkedHashMap<>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Node<K, V>> eldest) {
                return size() > capacity;
            }
        };
    }

    @Override
    public Optional<Node<K, V>> get(long pageId) {
        return Optional.ofNullable(map.get(pageId));
    }

    @Override
    public void put(long pageId, Node<K, V> node) {
        map.put(pageId, node);
    }

    @Override
    public void invalidate(long pageId) {
        map.remove(pageId);
    }
}

