package org.arbol.logic.tree.operation.treeBPlusDisk;

import org.arbol.logic.storage.StorageManager;
import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DiskOperationContext<K extends Comparable<K> & Serializable, V extends Serializable> {

    private final StorageManager storageManager;
    private final int maxSize;
    private final Map<Long, Node<K, V>> buffer;
    private long nextFreePageId;

    public DiskOperationContext(String dataFileName, int maxSize) {
        this.storageManager = new StorageManager(dataFileName);
        this.maxSize = maxSize;
        this.buffer = new HashMap<>();
        this.nextFreePageId = 1L;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void initializeForNewTree() {
        this.nextFreePageId = 1L;
    }

    public void initializeFromStorage() {
        this.nextFreePageId = Math.max(1L, storageManager.getTotalPages());
    }

    public byte[] readPage(long pageId) {
        return storageManager.readPage(pageId);
    }

    public long allocatePageId() {
        return nextFreePageId++;
    }

    public Node<K, V> deserializeNode(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            int type = ois.readInt();
            Node<K, V> node;
            if (type == 0) {
                node = new BPlusLeafNode<>(maxSize);
            } else if (type == 1) {
                node = new BPlusInternalNode<>(maxSize);
            } else {
                throw new IllegalStateException("Tipo de nodo desconocido: " + type);
            }
            node.deserialize(data);
            if (node.getPageId() >= 0) {
                buffer.put(node.getPageId(), node);
            }
            return node;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo deserializar nodo desde pagina", e);
        }
    }

    public void saveNode(Node<K, V> node) {
        if (node.getPageId() < 0) {
            node.setPageId(allocatePageId());
        }

        if (node instanceof BPlusInternalNode<K, V> internal) {
            internal.syncChildPageIdsFromChildren();
        }

        storageManager.writePage(node.getPageId(), node.serialize());
        buffer.put(node.getPageId(), node);
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

        Node<K, V> buffered = buffer.get(childPageId);
        if (buffered != null) {
            parent.setChild(index, buffered);
            return buffered;
        }

        byte[] data = storageManager.readPage(childPageId);
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

        Node<K, V> buffered = buffer.get(pageId);
        if (buffered != null) {
            return buffered;
        }

        byte[] data = storageManager.readPage(pageId);
        if (data == null) {
            return null;
        }

        return deserializeNode(data);
    }

    public void close() {
        storageManager.close();
    }
}

