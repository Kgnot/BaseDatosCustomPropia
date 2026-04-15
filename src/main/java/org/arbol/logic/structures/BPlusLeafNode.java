package org.arbol.logic.structures;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class BPlusLeafNode<K extends Comparable<K> & Serializable, V extends Serializable> extends Node<K, V> {

    // mandamos al siguiente
    private long pageId; // id de la página del disco
    private BPlusLeafNode<K, V> nextLeaf;

    public BPlusLeafNode(int maxSize) {
        super(maxSize);
        this.nextLeaf = null;
    }

    public BPlusLeafNode(int maxSize, List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
        this.nextLeaf = null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public SplitResult<K, V> split() {
        int splitIndex = (nodeElements.size() + 1) / 2; // División hacia arriba para la derecha

        // Elementos que se quedan en la hoja izquierda (este nodo)
        List<NodeElement<K, V>> leftElements = new ArrayList<>(nodeElements.subList(0, splitIndex));

        // Elementos que van a la nueva hoja derecha
        List<NodeElement<K, V>> rightElements = new ArrayList<>(nodeElements.subList(splitIndex, nodeElements.size()));

        // Crear la nueva hoja derecha
        BPlusLeafNode<K, V> newRightNode = new BPlusLeafNode<>(this.maxSize, rightElements);

        // Actualizar punteros de la lista enlazada
        newRightNode.setNextLeaf(this.nextLeaf); // La nueva apunta a la que apuntaba yo
        this.setNextLeaf(newRightNode);          // Yo ahora apunto a la nueva

        // Actualizar este nodo (ahora es la izquierda)
        this.nodeElements = leftElements;

        // Clave a promover: La primera de la derecha
        NodeElement<K, V> promotedKey = newRightNode.getElementAt(0);

        // Devolvemos la clave promovida. El valor aquí es lo de menos para el padre,
        // pero devolvemos el record completo.
        return new SplitResult<>(promotedKey, newRightNode);
    }


    public void setNextLeaf(BPlusLeafNode<K, V> nextLeaf) {
        this.nextLeaf = nextLeaf;
    }

    public BPlusLeafNode<K, V> getNextLeaf() {
        return nextLeaf;
    }

    @Override
    public String toString() {
        return "LeafNode" + super.toString() + (nextLeaf != null ? " -> Next" : " -> END");
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            // escribimos el tipo de nodo,
            // 0-> hoja
            // 1 -> interno
            dos.writeInt(0);
            // escribimos la cantidad de elementos
            dos.writeInt(nodeElements.size());
            // Escribimos el IUde del seiguiente
            dos.writeLong(nextLeaf != null ? ((BPlusLeafNode<?, ?>) nextLeaf).pageId : -1L);

            // escribimos cada Key y value [toca ver el tema de la serializacion de K y V] -> podemos hacer que extiendan de serializable tambien
            for(NodeElement<K, V> nodeElement : nodeElements) {
                dos.write(K.);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[4096];
        }

        return new byte[0];
    }

    @Override
    public byte[] deserialize(byte[] data) {
        return new byte[0];
    }
}
