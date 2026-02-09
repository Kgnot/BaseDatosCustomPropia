package org.arbol.logic.nodes;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public final class InternalNode<K extends Comparable<K>, V> extends Node<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(InternalNode.class);

    private List<Node<K, V>> children;


    public InternalNode(int maxSize) {
        super(maxSize);
        children = new ArrayList<>();
    }

    public InternalNode(int maxSize, List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
        children = new ArrayList<>();
    }

    @Override
    public Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insert(NodeElement<K, V> element) {
        logger.debug("Insertaremos : {} , {}", element.key(), element.value());
        if (hasChildren()) {
            // recorremos cada hijo
            for (int i = 0; i < children.size(); i++) {
                //obtenemos el hijo
                var childNode = children.get(i);
                var lastElementOfChildNode = childNode.nodeElements.getLast();
                // comparamos el ultimo elmento del hijo con el elemento a insertar
                var insertResult = childNode.insert(element);

                if (element.key().compareTo(lastElementOfChildNode.key()) < 0) {
                    if (insertResult.isFailure()) {
                        return insertResult; // propagas el error
                    }

                    SplitResult<K, V> childSplit =
                            ((Result.Success<SplitResult<K, V>, ?>) insertResult).value();
                    if (childSplit.newNode() != null) {
                        handleChildSplit(i, childSplit);
                        logger.info("El childSplit se dividio");
                        if (isFull()) {
                            return new Result.Success<>(split()); // lo devolvemos exitoso y millonario
                        }
                    }
                }
                // si el elemento a insertar es mayor que todos inserta a la derecha y ya.
                if (i == children.size() - 1) {
                    if (insertResult.isFailure()) {
                        return insertResult; // propagas el error
                    }

                    SplitResult<K, V> childSplit =
                            ((Result.Success<SplitResult<K, V>, ?>) insertResult).value();
                    if (childSplit.newNode() != null) {
                        handleChildSplit(i, childSplit);
                        if (isFull()) return new Result.Success<>(split());
                    }
                    return new Result.Success<>(new SplitResult<>(null, null));
                }
            }
        }
        for (var existing : nodeElements) {
            if (existing.key().compareTo(element.key()) == 0) {
                return new Result.Failure<>(new NodeError.DuplicateKeyError(element.key()));
            }
        }
        insertInOrder(element);
        if (isFull()) {
            return new Result.Success<>(split());
        }
        return new Result.Success<>(new SplitResult<>(null, null));
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key) {
        return new Result.Failure<>(new NodeError.NodeNotFoundError("AUN NO IMPLEMENTO XD"));
    }

    @Override
    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return new Result.Failure<>(new NodeError.NodeNotFoundError("AUN NO IMPLEMENTO XD"));
    }

    @Override
    protected SplitResult<K, V> split() {
        logger.info("Estoy haciendo split");
        // index del medio
        int indexMedio = maxSize / 2;
        // elemento del medio
        var nodeElement = nodeElements.get(indexMedio);
        // extraigo sublistas | sublist las copia xd, mejor array:
        var subListIzq = new ArrayList<>(nodeElements.subList(0, indexMedio));
        var subListDer = new ArrayList<>(nodeElements.subList(indexMedio + 1, nodeElements.size()));
        // creo los nodos
        Node<K, V> nodoIzq = Node.createInternalNodeWithElements(maxSize, subListIzq);
        Node<K, V> nodoDer = Node.createInternalNodeWithElements(maxSize, subListDer);
        if (hasChildren()) {
            logger.info("Si tiene hijos el split");
            int childSplit = indexMedio + 1; // le sumo uno al resultado de en medio
            ((InternalNode<K, V>) nodoIzq).children =
                    new ArrayList<>(children.subList(0, childSplit));
            ((InternalNode<K, V>) nodoDer).children =
                    new ArrayList<>(children.subList(childSplit, children.size()));

        }
        // añado el elemento al izquierdo
        this.nodeElements = subListIzq;
        this.children = ((InternalNode<K, V>) nodoIzq).children;

        return new SplitResult<>(nodeElement, nodoDer);
    }

    // métodos publicos

    public void addChild(Node<K, V> node) {
        this.children.add(node);
    }

    private void insertInOrder(NodeElement<K, V> element) {
        int i = 0;
        while (i < nodeElements.size() && nodeElements.get(i).key().compareTo(element.key()) < 0) {
            i++;
        }
        nodeElements.add(i, element);
    }

    private void handleChildSplit(int index, SplitResult<K, V> splitResult) {
        // Insertar el elemento promovido en este nodo
        insertInOrder(splitResult.promotedElement());
        // Insertar el nuevo hijo en la posición correcta
        children.add(index + 1, splitResult.newNode());
    }

    @Override
    protected boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public List<Node<K, V>> getChildren() {
        return this.children;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InternalNode{");
        sb.append("keys=[");

        for (int i = 0; i < nodeElements.size(); i++) {
            sb.append(nodeElements.get(i).key());
            if (i < nodeElements.size() - 1) sb.append(", ");
        }

        sb.append("]");

        if (children != null && !children.isEmpty()) {
            sb.append(", children=").append(children.size());
        } else {
            sb.append(", children=0");
        }

        sb.append("}");
        return sb.toString();
    }


}
