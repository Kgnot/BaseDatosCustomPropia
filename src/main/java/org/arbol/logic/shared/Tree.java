package org.arbol.logic.shared;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.utils.Result;
import org.arbol.logic.Btree.structures.Node;

public abstract class Tree<K extends Comparable<K>, V> {
    protected Node<K, V> root;

    public Tree(Node<K, V> root) {
        this.root = root;
    }

    public Tree() {
    }

    abstract public Result<Void, NodeError.DuplicateKeyError> insert(NodeElement<K, V> node);

    // TODO: Aqui devolvemos NodeElement para probar que se devuelve el nodo completo, aunque solo el valor es necesario, esto es para probar que se devuelve el nodo completo, aunque solo el valor es necesario
    abstract public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key);

    abstract public Result<Void, NodeError.NodeNotFoundError> delete(K key);
}