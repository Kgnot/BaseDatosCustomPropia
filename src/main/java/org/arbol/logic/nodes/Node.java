package org.arbol.logic.nodes;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;

import java.util.ArrayList;
import java.util.List;

public sealed abstract class Node<K extends Comparable<K>, V> permits InternalNode {
    // maxSize es el número máximo de elementos que puede contener un nodo antes de dividirse
    // es decir si el arbol es de grado 5, maxSize sería 4.
    protected int maxSize;
    protected List<NodeElement<K, V>> nodeElements;

    public Node(int maxSize) {
        this.maxSize = maxSize;
        nodeElements = new ArrayList<>();
    }

    public Node(int maxSize, List<NodeElement<K, V>> nodeElements) {
        this.maxSize = maxSize;
        this.nodeElements = nodeElements;
    }

    public static <K extends Comparable<K>, V> Node<K, V> createInternalNodeWithElements(int maxSize, List<NodeElement<K, V>> elements) {
        return new InternalNode<>(maxSize, elements);
    }

    public static <K extends Comparable<K>, V> Node<K, V> createInternalNode(int maxSize) {
        return new InternalNode<>(maxSize);
    }

    // funciones de get
    public void addElement(NodeElement<K, V> nodeElement) {
        this.nodeElements.add(nodeElement);
    }

    // funciones comunes
    public abstract Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insert(NodeElement<K, V> element);

    // TODO: Aqui devolvemos NodeElement para probar que se devuelve el nodo completo, aunque solo el valor es necesario, esto es para probar que se devuelve el nodo completo, aunque solo el valor es necesario
    public abstract Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key);

    public abstract Result<Void, NodeError.NodeNotFoundError> delete(K key);

    protected boolean isFull() {
        return nodeElements.size() >= maxSize;
    }

    protected boolean hasChildren() {
        return !nodeElements.isEmpty();
    }

    protected abstract SplitResult<K, V> split();


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
