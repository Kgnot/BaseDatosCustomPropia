package org.arbol.logic.structures.table;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.node.NodeElement;
import org.arbol.logic.tree.TreeBPlusDisk;
import org.arbol.utils.Result;

import java.io.Serializable;
import java.util.List;

public abstract class Table<K extends Comparable<K> & Serializable, V extends Serializable> {

    protected TreeBPlusDisk<K, V> tree;
    protected String name;

    public Table(String name, int order) {
        this.name = name;
        this.tree = new TreeBPlusDisk<>(name, order);
    }

    public Result<Void, NodeError.DuplicateKeyError> insert(K key, V value) {
        return tree.insert(new NodeElement<>(key, value));
    }

    public Result<V, NodeError.NodeNotFoundError> select(K key) {
        var res = tree.search(key);
        return res.isSuccess()
                ? new Result.Success<>(res.unwrap().value())
                : new Result.Failure<>(new NodeError.NodeNotFoundError(key));
    }

    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return tree.delete(key);
    }

    public List<V> findAll() {
        return tree.findAll().stream()
                .map(NodeElement::value)
                .toList();
    }

    public List<V> findAll(int offset, int limit) {
        return tree.findAll(offset, limit).stream()
                .map(NodeElement::value)
                .toList();
    }

    public String getName() {
        return name;
    }

    public void close() {
        tree.close();
    }

}
