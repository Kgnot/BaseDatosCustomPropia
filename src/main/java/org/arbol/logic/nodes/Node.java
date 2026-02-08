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


}
