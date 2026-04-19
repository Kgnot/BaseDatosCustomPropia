package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.NodeElement;

import java.nio.ByteBuffer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BPlusLeafNodeCodec<K extends Comparable<K> & Serializable, V extends Serializable>
        implements NodeCodec<K, V, BPlusLeafNode<K, V>> {

    @Override
    public int typeId() {
        return 0;
    }

    @Override
    public Class<BPlusLeafNode<K, V>> nodeClass() {
        @SuppressWarnings("unchecked")
        Class<BPlusLeafNode<K, V>> clazz = (Class<BPlusLeafNode<K, V>>) (Class<?>) BPlusLeafNode.class;
        return clazz;
    }

    @Override
    public byte[] serializeBody(BPlusLeafNode<K, V> node) {
        List<NodeElement<K, V>> elements = node.getNodeElements();

        int totalSize = Integer.BYTES + Long.BYTES + Integer.BYTES + Long.BYTES;
        for (NodeElement<K, V> element : elements) {
            totalSize += BinaryValueCodec.sizeOfObject(element.key());
            totalSize += BinaryValueCodec.sizeOfObject(element.value());
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(node.getMaxSize());
        buffer.putLong(node.getPageId());
        buffer.putInt(elements.size());
        buffer.putLong(node.getNextLeafPageId());

        for (NodeElement<K, V> element : elements) {
            BinaryValueCodec.writeObject(buffer, element.key());
            BinaryValueCodec.writeObject(buffer, element.value());
        }

        return buffer.array();
    }

    @Override
    @SuppressWarnings("unchecked")
    public BPlusLeafNode<K, V> deserializeBody(byte[] data, int maxSize) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int nodeMaxSize = buffer.getInt();
        long pageId = buffer.getLong();
        int size = buffer.getInt();
        long nextPageId = buffer.getLong();

        List<NodeElement<K, V>> elements = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            K key = (K) BinaryValueCodec.readObject(buffer);
            V value = (V) BinaryValueCodec.readObject(buffer);
            elements.add(new NodeElement<>(key, value));
        }

        BPlusLeafNode<K, V> node = new BPlusLeafNode<>(nodeMaxSize, elements);
        node.setPageId(pageId);
        node.setNextLeafPageId(nextPageId);
        return node;
    }
}


