package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.NodeElement;

import java.io.*;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeInt(node.getMaxSize());
            oos.writeLong(node.getPageId());

            List<NodeElement<K, V>> elements = node.getNodeElements();
            oos.writeInt(elements.size());
            for (NodeElement<K, V> element : elements) {
                oos.writeObject(element.key());
            }

            List<Long> childPageIds = node.getChildPageIds();
            oos.writeInt(childPageIds.size());
            for (Long childPageId : childPageIds) {
                oos.writeLong(childPageId != null ? childPageId : -1L);
            }

            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error serializando nodo interno B+", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public BPlusInternalNode<K, V> deserializeBody(byte[] data, int maxSize) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            int nodeMaxSize = ois.readInt();
            long pageId = ois.readLong();

            int size = ois.readInt();
            List<NodeElement<K, V>> elements = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                K key = (K) ois.readObject();
                elements.add(new NodeElement<>(key, null));
            }

            int childCount = ois.readInt();
            List<Long> childPageIds = new ArrayList<>(childCount);
            for (int i = 0; i < childCount; i++) {
                childPageIds.add(ois.readLong());
            }

            BPlusInternalNode<K, V> node = new BPlusInternalNode<>(nodeMaxSize, elements);
            node.setPageId(pageId);
            node.setChildPageIds(childPageIds);
            node.clearChildrenReferences();
            return node;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Error deserializando nodo interno B+", e);
        }
    }
}

