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
    private boolean isLeaf; // esto no creo usarlo hmm


    public InternalNode(int maxSize) {
        super(maxSize);
        children = new ArrayList<>();
    }

    public InternalNode(int maxSize, List<NodeElement<K, V>> nodeElements) {
        super(maxSize, nodeElements);
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
                if (element.key().compareTo(lastElementOfChildNode.key()) < 0) {
                    SplitResult<K, V> childSplit = childNode.insert(element).unwrap();
                    if (childSplit.newNode() != null) {
                        logger.info("El childSplit se dividio");
                        if (isFull()) {
                            return new Result.Success<>(split()); // lo devolvemos exitoso y millonario
                        }
                    }
                }
                // si el elemento a insertar es mayor que todos inserta a la derecha y ya.
                if (i == children.size() - 1) {
                    SplitResult<K, V> childSplit = childNode.insert(element).unwrap();
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
            split();
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
        int indexMedio = Math.floorDiv(maxSize, 2) + 1;
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
            int childSplit = indexMedio; // dejemoslo asi por si se cambia el index medio o hay algo que no
            ((InternalNode<K, V>) nodoIzq).children = new ArrayList<>(children.subList(0, childSplit));
            ((InternalNode<K, V>) nodoIzq).children = new ArrayList<>(children.subList(childSplit, maxSize));

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
}
