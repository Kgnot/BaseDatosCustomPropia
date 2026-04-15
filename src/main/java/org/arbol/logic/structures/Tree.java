package org.arbol.logic.structures;

import org.arbol.logic.error.NodeError;
import org.arbol.utils.Result;

import java.io.Serializable;

public abstract class Tree<K extends Comparable<K> & Serializable, V extends Serializable> {
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