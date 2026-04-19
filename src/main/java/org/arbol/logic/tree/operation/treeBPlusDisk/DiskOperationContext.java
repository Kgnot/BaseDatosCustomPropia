package org.arbol.logic.tree.operation.treeBPlusDisk;

import org.arbol.logic.tree.operation.treeBPlusDisk.allocator.PageAllocator;
import org.arbol.logic.tree.operation.treeBPlusDisk.allocator.SequentialPageAllocator;
import org.arbol.logic.tree.operation.treeBPlusDisk.cache.LruNodeCache;
import org.arbol.logic.tree.operation.treeBPlusDisk.cache.NodeCache;
import org.arbol.logic.tree.operation.treeBPlusDisk.repository.PageStore;
import org.arbol.logic.tree.operation.treeBPlusDisk.repository.StoragePageStore;
import org.arbol.logic.tree.operation.treeBPlusDisk.serializer.DefaultNodeSerializer;
import org.arbol.logic.tree.operation.treeBPlusDisk.serializer.NodeSerializer;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.Node;

import java.io.Serializable;

public class DiskOperationContext<K extends Comparable<K> & Serializable, V extends Serializable> {

    private final int maxSize;
    private final PageStore pageStore;
    private final NodeSerializer<K, V> nodeSerializer;
    private final NodeCache<K, V> nodeCache;
    private final PageAllocator pageAllocator;

    public DiskOperationContext(String dataFileName, int maxSize) {
        this(
                new StoragePageStore(dataFileName),
                new DefaultNodeSerializer<>(),
                new LruNodeCache<>(512),
                new SequentialPageAllocator(),
                maxSize
        );
    }

    public DiskOperationContext(
            PageStore pageStore,
            NodeSerializer<K, V> nodeSerializer,
            NodeCache<K, V> nodeCache,
            PageAllocator pageAllocator,
            int maxSize
    ) {
        this.pageStore = pageStore;
        this.nodeSerializer = nodeSerializer;
        this.nodeCache = nodeCache;
        this.pageAllocator = pageAllocator;
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void initializeForNewTree() {
        this.pageAllocator.initialize(1L);
    }

    public void initializeFromStorage() {
        this.pageAllocator.initialize(pageStore.totalPages());
    }

    public byte[] readPage(long pageId) {
        return pageStore.read(pageId);
    }

    public long allocatePageId() {
        return pageAllocator.allocate();
    }

    public Node<K, V> deserializeNode(byte[] data) {
        Node<K, V> node = nodeSerializer.deserialize(data, maxSize);
        if (node.getPageId() >= 0) {
            nodeCache.put(node.getPageId(), node);
        }
        return node;
    }

    public void saveNode(Node<K, V> node) {
        if (node.getPageId() < 0) {
            node.setPageId(allocatePageId());
        }

        if (node instanceof BPlusInternalNode<K, V> internal) {
            internal.syncChildPageIdsFromChildren();
        }

        pageStore.write(node.getPageId(), nodeSerializer.serialize(node));
        nodeCache.put(node.getPageId(), node);
    }

    public Node<K, V> getChild(BPlusInternalNode<K, V> parent, int index) {
        if (index < 0 || index >= parent.getChildPageIds().size()) {
            return null;
        }

        if (index < parent.getChildren().size()) {
            Node<K, V> inMemoryChild = parent.getChild(index);
            if (inMemoryChild != null) {
                return inMemoryChild;
            }
        }

        long childPageId = parent.getChildPageIdAt(index);
        if (childPageId < 0) {
            return null;
        }

        var buffered = nodeCache.get(childPageId);
        if (buffered.isPresent()) {
            parent.setChild(index, buffered.get());
            return buffered.get();
        }

        byte[] data = pageStore.read(childPageId);
        if (data == null) {
            return null;
        }

        Node<K, V> child = deserializeNode(data);
        parent.setChild(index, child);
        return child;
    }

    public Node<K, V> getNodeByPageId(long pageId) {
        if (pageId < 0) {
            return null;
        }

        var buffered = nodeCache.get(pageId);
        if (buffered.isPresent()) {
            return buffered.get();
        }

        byte[] data = pageStore.read(pageId);
        if (data == null) {
            return null;
        }

        return deserializeNode(data);
    }

    public void close() {
        pageStore.close();
    }
}

