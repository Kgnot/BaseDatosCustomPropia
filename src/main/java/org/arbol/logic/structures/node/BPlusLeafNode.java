package org.arbol.logic.structures.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class BPlusLeafNode<K extends Comparable<K> & Serializable, V extends Serializable> extends Node<K, V> {

    // mandamos al siguiente
    private long nextLeafPageId = -1L;
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

        // Si esta hoja vino de disco, puede tener nextLeaf == null pero nextLeafPageId valido.
        long previousNextLeafPageId = this.nextLeaf != null ? this.nextLeaf.getPageId() : this.nextLeafPageId;

        // Actualizar punteros de la lista enlazada
        newRightNode.setNextLeaf(this.nextLeaf); // Mantener referencia en RAM si existe
        if (newRightNode.nextLeaf == null) {
            newRightNode.nextLeafPageId = previousNextLeafPageId; // Conservar enlace persistente
        }
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
        this.nextLeafPageId = nextLeaf != null ? nextLeaf.getPageId() : -1L;
    }

    public BPlusLeafNode<K, V> getNextLeaf() {
        return nextLeaf;
    }

    public long getNextLeafPageId() {
        return nextLeafPageId;
    }

    public void setNextLeafPageId(long nextLeafPageId) {
        this.nextLeafPageId = nextLeafPageId;
    }

    @Override
    public String toString() {
        return "LeafNode" + super.toString() + (nextLeaf != null ? " -> Next" : " -> END");
    }

}
