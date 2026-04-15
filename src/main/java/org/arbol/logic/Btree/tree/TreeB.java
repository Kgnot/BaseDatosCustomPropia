package org.arbol.logic.Btree.tree;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.utils.Result;
import org.arbol.logic.Btree.structures.InternalNode;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.structures.SplitResult;
import org.arbol.logic.Btree.tree.operation.treeB.TreeDeleteTreeB;
import org.arbol.logic.Btree.tree.operation.treeB.TreeInsertionTreeB;
import org.arbol.logic.Btree.tree.operation.treeB.TreeSearchTreeB;
import org.arbol.logic.shared.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class TreeB<K extends Comparable<K>, V> extends Tree<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeB.class);
    private final int maxSize;
    // aqui pondremos sus operaciones, insert, search y delete
    private TreeSearchTreeB<K, V> treeSearch;
    private TreeInsertionTreeB<K, V> treeInsertion;
    private TreeDeleteTreeB<K, V> treeDelete;

    public TreeB(Node<K, V> root, int maxSize) {
        super(root);
        this.maxSize = maxSize;
        initializeOperations();
    }

    public TreeB(int maxSize) {
        super();
        this.maxSize = maxSize;
        initializeOperations();
    }

    private void initializeOperations() {
        this.treeSearch = new TreeSearchTreeB<>();
        this.treeInsertion = new TreeInsertionTreeB<>();
        this.treeDelete = new TreeDeleteTreeB<>();
    }

    @Override
    public Result<Void, NodeError.DuplicateKeyError> insert(NodeElement<K, V> node) {
        if (root == null) {
            root = Node.createInternalNode(maxSize);
        }
        // intentamos insertar en la raiz
        Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertResult =
                treeInsertion.execute(root, node, maxSize);
        if (insertResult.isFailure()) {
            logger.error("El elemento con clave {} ya existe en el árbol", node.key());
            return insertResult.map(split -> null);
        }
        SplitResult<K, V> splitResult = insertResult.unwrap();

        if (splitResult.newNode() != null) {
            // esto significa que toca crear una nueva raíz:c
            Node<K, V> newRoot = Node.createInternalNode(maxSize);
            // añadimos el que se fue para arriba
            newRoot.addElement(splitResult.promotedElement());
            ((InternalNode<K, V>) newRoot).addChild(root);
            ((InternalNode<K, V>) newRoot).addChild(splitResult.newNode());

            root = newRoot;
            logger.debug("El nuevo root: {}", root);
        }

        return new Result.Success<>(null);
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key) {
        return treeSearch.execute(root, key);
    }

    @Override
    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return treeDelete.execute(root, key, maxSize);
    }

    @Override
    public String toString() {
        if (root == null) {
            return "TreeB(empty)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TreeB (maxSize=").append(maxSize).append(")\n");

        Queue<Node<K, V>> queue = new ArrayDeque<>();
        queue.add(root);

        int level = 0;

        while (!queue.isEmpty()) {
            int size = queue.size();
            sb.append("Level ").append(level).append(": ");

            for (int i = 0; i < size; i++) {
                Node<K, V> node = queue.poll();

                sb.append(node).append(" ");

                if (node instanceof InternalNode<K, V> internal) {
                    queue.addAll(internal.getChildren());
                }
            }

            sb.append("\n");
            level++;
        }

        return sb.toString();
    }

}