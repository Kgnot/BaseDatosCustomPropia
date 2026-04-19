package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.NodeElement;

import java.nio.ByteBuffer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BPlusInternalNodeCodec<K extends Comparable<K> & Serializable, V extends Serializable>
        implements NodeCodec<K, V, BPlusInternalNode<K, V>> {

    @Override
    public int typeId() {
        return 1;
    }

    @Override
    public Class<BPlusInternalNode<K, V>> nodeClass() {
        @SuppressWarnings("unchecked")
        Class<BPlusInternalNode<K, V>> clazz = (Class<BPlusInternalNode<K, V>>) (Class<?>) BPlusInternalNode.class;
        return clazz;
    }

    @Override
    public byte[] serializeBody(BPlusInternalNode<K, V> node) {
        List<NodeElement<K, V>> elements = node.getNodeElements();
        List<Long> childPageIds = node.getChildPageIds();

        int totalSize = Integer.BYTES + Long.BYTES + Integer.BYTES + Integer.BYTES;
        for (NodeElement<K, V> element : elements) {
            totalSize += BinaryValueCodec.sizeOfObject(element.key());
        }
        totalSize += childPageIds.size() * Long.BYTES;

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(node.getMaxSize());
        buffer.putLong(node.getPageId());

        buffer.putInt(elements.size());
        for (NodeElement<K, V> element : elements) {
            BinaryValueCodec.writeObject(buffer, element.key());
        }

        buffer.putInt(childPageIds.size());
        for (Long childPageId : childPageIds) {
            buffer.putLong(childPageId != null ? childPageId : -1L);
        }

        return buffer.array();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BPlusInternalNode<K, V> deserializeBody(byte[] data, int maxSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int nodeMaxSize = buffer.getInt();
        long pageId = buffer.getLong();

        int size = buffer.getInt();
        List<NodeElement<K, V>> elements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            K key = (K) BinaryValueCodec.readObject(buffer);
            elements.add(new NodeElement<>(key, null));
        }

        int childCount = buffer.getInt();
        List<Long> childPageIds = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            childPageIds.add(buffer.getLong());
        }

        BPlusInternalNode<K, V> node = new BPlusInternalNode<>(nodeMaxSize, elements);
        node.setPageId(pageId);
        node.setChildPageIds(childPageIds);
        node.clearChildrenReferences();
        return node;
    }
}

