package org.arbol.logic.nodes;

import java.util.ArrayList;
import java.util.List;

public sealed abstract class Node<K extends Comparable<K>, V>
        permits InternalNode {

    // tamaño maximo
    protected int maxSize;
    protected List<NodeElement<K, V>> nodeElements;


    public Node(int maxSize) {
        this.maxSize = maxSize;
        this.nodeElements = new ArrayList<>();
    }

    public Node(int maxSize, List<NodeElement<K, V>> nodeElements) {
        this.maxSize = maxSize;
        this.nodeElements = nodeElements;
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
    // Sabemos si esta lleno o no
    public boolean isFull() {
        return nodeElements.size() >= maxSize;
    }
    // Si este contiene alguna clave
    public boolean containsKey(K key) {
        return nodeElements.stream()
                .anyMatch(e -> e.key().equals(key));
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

    // si tiene hijos. Podriamos crear una clase "Hoja" y eliminar este metodo
    public boolean hasChildren() {
        return !nodeElements.isEmpty();
    }

    public abstract SplitResult<K, V> split();


    @Override
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
