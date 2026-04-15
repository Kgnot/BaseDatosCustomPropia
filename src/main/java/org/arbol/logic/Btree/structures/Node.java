package org.arbol.logic.Btree.structures;

import java.util.ArrayList;
import java.util.List;

public sealed abstract class Node<K extends Comparable<K>, V>
        permits InternalNode, BPlusLeafNode, BPlusInternalNode {

    // tamaño maximo
    protected int maxSize;
    protected List<NodeElement<K, V>> nodeElements;


    public Node(int maxSize) {
        this.maxSize = maxSize;
        this.nodeElements = new ArrayList<>();
    }

    public Node(int maxSize, List<NodeElement<K, V>> nodeElements) {
        this.maxSize = maxSize;
        this.nodeElements = new ArrayList<>();
        this.nodeElements.addAll(nodeElements);
    }

    // Algunas funciones estaticas factory
    public static <K extends Comparable<K>, V> Node<K, V>
    createInternalNode(int maxSize) {
        return new InternalNode<>(maxSize);
    }

    public static <K extends Comparable<K>, V> Node<K, V>
    createInternalNodeWithElements(int maxSize,
                                   List<NodeElement<K, V>> elements) {
        return new InternalNode<>(maxSize, elements);
    }

    // añadimos un elemento
    public void addElement(NodeElement<K, V> nodeElement) {
        this.nodeElements.add(nodeElement);
    }

    //insertar en una posición:
    public void addElementAt(int index, NodeElement<K, V> element) {
        this.nodeElements.add(index, element);
    }

    // Para insertar en el orden que es
    public void insertInOrder(NodeElement<K, V> element) {
        int index = 0;
        // hacemos el while y comparamos con cada uno
        while (index < nodeElements.size()
                && element.key()
                .compareTo(nodeElements.get(index).key()) > 0) {
            index++;
        }
        // apenas aparezca lo insertamos
        nodeElements.add(index, element);
    }

    public NodeElement<K, V> getElementAt(int index) {
        return nodeElements.get(index);
    }

    public void setElementAt(int index, NodeElement<K, V> element) {
        nodeElements.set(index, element);
    }

    public void removeElementAt(int index) {
        nodeElements.remove(index);
    }

    // Para buscar:
    public int findPosition(K key) {
        int index = 0;
        while (index < nodeElements.size() &&
                key.compareTo(nodeElements.get(index).key()) > 0) {
            index++;
        }
        return index;
    }

    // Si este contiene alguna clave
    public boolean containsKey(K key) {
        return nodeElements.stream()
                .anyMatch(e -> e.key().equals(key));
    }

    public boolean isFull() {
        return nodeElements.size() >= maxSize;
    }
    public boolean isEmpty() {
        return nodeElements.isEmpty();
    }

    public int size() {
        return nodeElements.size();
    }

    // Ya los abstractos
    public abstract boolean hasChildren();

    public abstract SplitResult<K, V> split();

    public List<NodeElement<K, V>> getNodeElements() {
        return new ArrayList<>(nodeElements);
    }




    public int getMaxSize() {
        return maxSize;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append("{");
        sb.append("keys=[");

        for (int i = 0; i < nodeElements.size(); i++) {
            sb.append(nodeElements.get(i).key());
            if (i < nodeElements.size() - 1) sb.append(", ");
        }

        sb.append("], ");
        sb.append("size=").append(nodeElements.size());
        sb.append("/").append(maxSize);
        sb.append("}");

        return sb.toString();
    }
}
