package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.BPlusInternalNode;
import org.arbol.logic.structures.node.BPlusLeafNode;
import org.arbol.logic.structures.node.Node;
import org.arbol.logic.structures.node.NodeElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultNodeSerializer<K extends Comparable<K> & Serializable, V extends Serializable>
        implements NodeSerializer<K, V> {

    private static final int LEGACY_OBJECT_STREAM_HEADER = 0xACED0005;

    private final Map<Integer, NodeCodec<K, V, ? extends Node<K, V>>> codecsByType = new HashMap<>();
    private final Map<Class<?>, NodeCodec<K, V, ? extends Node<K, V>>> codecsByClass = new HashMap<>();

    public DefaultNodeSerializer() {
        register(new BPlusLeafNodeCodec<>());
        register(new BPlusInternalNodeCodec<>());
    }

    public <T extends Node<K, V>> void register(NodeCodec<K, V, T> codec) {
        codecsByType.put(codec.typeId(), codec);
        codecsByClass.put(codec.nodeClass(), codec);
    }

    @Override
    public byte[] serialize(Node<K, V> node) {
        NodeCodec<K, V, ?> codec = resolveCodecForNode(node);

        try {
            byte[] body = serializeBody(codec, node);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream out = new DataOutputStream(baos)) {
                out.writeInt(codec.typeId());
                out.write(body);
                out.flush();
                return baos.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo serializar nodo", e);
        }
    }

    @Override
    public Node<K, V> deserialize(byte[] data, int maxSize) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(data))) {
            int typeId = in.readInt();

            if (typeId == LEGACY_OBJECT_STREAM_HEADER) {
                return deserializeLegacyObjectStream(data);
            }

            if (typeId != 0 && typeId != 1) {
                throw new IllegalStateException("Tipo de nodo desconocido: " + typeId);
            }

            byte[] body = extractBodyForModernFormat(data);

            NodeCodec<K, V, ? extends Node<K, V>> codec = codecsByType.get(typeId);
            if (codec == null) {
                throw new IllegalStateException("Tipo de nodo desconocido: " + typeId);
            }

            return deserializeBody(codec, body, maxSize);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo deserializar nodo", e);
        }
    }

    private NodeCodec<K, V, ?> resolveCodecForNode(Node<K, V> node) {
        NodeCodec<K, V, ? extends Node<K, V>> codec = codecsByClass.get(node.getClass());
        if (codec == null) {
            throw new IllegalStateException("No hay codec registrado para: " + node.getClass().getName());
        }
        return codec;
    }

    @SuppressWarnings("unchecked")
    private <T extends Node<K, V>> byte[] serializeBody(NodeCodec<K, V, ?> codec, Node<K, V> node) {
        return ((NodeCodec<K, V, T>) codec).serializeBody((T) node);
    }

    @SuppressWarnings("unchecked")
    private <T extends Node<K, V>> Node<K, V> deserializeBody(
            NodeCodec<K, V, ? extends Node<K, V>> codec,
            byte[] body,
            int maxSize
    ) {
        return ((NodeCodec<K, V, T>) codec).deserializeBody(body, maxSize);
    }

    private byte[] extractBodyForModernFormat(byte[] data) {
        if (data.length <= Integer.BYTES) {
            throw new IllegalStateException("Payload de nodo invalido: tamano insuficiente");
        }

        if (data.length >= Integer.BYTES * 2) {
            int secondInt = readIntAt(data, Integer.BYTES);
            if (secondInt == LEGACY_OBJECT_STREAM_HEADER) {
                return Arrays.copyOfRange(data, Integer.BYTES, data.length);
            }

            // Tolerancia a formato transitorio: [typeId][bodyLen][body...]
            int bodyLen = secondInt;
            if (bodyLen > 0 && data.length >= Integer.BYTES * 2 + bodyLen) {
                int possibleHeader = readIntAt(data, Integer.BYTES * 2);
                if (possibleHeader == LEGACY_OBJECT_STREAM_HEADER) {
                    return Arrays.copyOfRange(data, Integer.BYTES * 2, Integer.BYTES * 2 + bodyLen);
                }
            }
        }

        return Arrays.copyOfRange(data, Integer.BYTES, data.length);
    }

    private int readIntAt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    @SuppressWarnings("unchecked")
    private Node<K, V> deserializeLegacyObjectStream(byte[] data) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            int legacyType = ois.readInt();

            if (legacyType == 0) {
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
                node.setNextLeafPageId(nextPageId);
                return node;
            }

            if (legacyType == 1) {
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
            }

            throw new IllegalStateException("Tipo de nodo legacy desconocido: " + legacyType);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("No se pudo deserializar nodo legacy", e);
        }
    }
}

