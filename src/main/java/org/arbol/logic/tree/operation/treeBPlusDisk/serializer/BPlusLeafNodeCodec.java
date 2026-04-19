package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.NodeElement;

import java.io.*;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeInt(node.getMaxSize());
            oos.writeLong(node.getPageId());
            List<NodeElement<K, V>> elements = node.getNodeElements();
            oos.writeInt(elements.size());
            oos.writeLong(node.getNextLeafPageId());
            for (NodeElement<K, V> element : elements) {
                oos.writeObject(element.key());
                oos.writeObject(element.value());
            }
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializando nodo hoja B+", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public BPlusLeafNode<K, V> deserializeBody(byte[] data, int maxSize) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            int nodeMaxSize = ois.readInt();
            long pageId = ois.readLong();
            int size = ois.readInt();
            long nextPageId = ois.readLong();

            List<NodeElement<K, V>> elements = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                K key = (K) ois.readObject();
                V value = (V) ois.readObject();
                elements.add(new NodeElement<>(key, value));
            }

            BPlusLeafNode<K, V> node = new BPlusLeafNode<>(nodeMaxSize, elements);
            node.setPageId(pageId);

            BPlusLeafNode<K, V> nextLeafStub = new BPlusLeafNode<>(nodeMaxSize);
            nextLeafStub.setPageId(nextPageId);
            node.setNextLeaf(nextLeafStub);
            return node;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error deserializando nodo hoja B+", e);
        }
    }
}


