package org.arbol.logic.tree.operation.treeBPlusDisk.serializer;

import org.arbol.logic.structures.node.Node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DefaultNodeSerializer<K extends Comparable<K> & Serializable, V extends Serializable>
        implements NodeSerializer<K, V> {

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

            if (typeId != 0 && typeId != 1) {
                throw new IllegalStateException("Tipo de nodo desconocido: " + typeId);
            }

            if (data.length <= Integer.BYTES) {
                throw new IllegalStateException("Payload de nodo invalido: tamano insuficiente");
            }
            byte[] body = Arrays.copyOfRange(data, Integer.BYTES, data.length);

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
}

