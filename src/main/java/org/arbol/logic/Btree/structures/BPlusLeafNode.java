package org.arbol.logic.Btree.structures;

import java.util.ArrayList;
import java.util.List;

public final class BPlusLeafNode<K extends Comparable<K>, V> extends Node<K, V> {

    // mandamos al siguiente
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
}
