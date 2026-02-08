package org.arbol.logic.tree;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.error.Result;
import org.arbol.logic.nodes.InternalNode;
import org.arbol.logic.nodes.Node;
import org.arbol.logic.nodes.NodeElement;
import org.arbol.logic.nodes.SplitResult;

public class TreeB<K extends Comparable<K>, V> extends Tree<K, V> {

    private final int maxSize;

    public TreeB(Node<K, V> root, int maxSize) {
        super(root);
        this.maxSize = maxSize;
    }

    public TreeB(int maxSize) {
        super();
        this.maxSize = maxSize;

    }

    @Override
    public Result<Void, NodeError.DuplicateKeyError> insert(NodeElement<K, V> node) {
        if (root == null) {
            root = Node.createInternalNode(maxSize);
        }
        // intentamos insertar en la raiz
        Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertResult = root.insert(node);
        if (insertResult.isFailure()) {
            return insertResult.map(split -> null);
        }
        SplitResult<K, V> splitResult = insertResult.unwrap();

        if (splitResult.newNode() != null) {
            // esto significa que toca crear una nueva raíz:c
            Node<K, V> newRoot = Node.createInternalNode(maxSize);
            ((InternalNode<K, V>) newRoot).addChild(root);
            ((InternalNode<K, V>) newRoot).addChild(splitResult.newNode());

            root = newRoot;
        }

        return new Result.Success<>(null);
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key) {
        // TODO: Aqui devolvemos NodeElement para probar que se devuelve el nodo completo, aunque solo el valor es necesario, esto es para probar que se devuelve el nodo completo, aunque solo el valor es necesario
        return root.search(key);
    }

    @Override
    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return root.delete(key);
    }
}