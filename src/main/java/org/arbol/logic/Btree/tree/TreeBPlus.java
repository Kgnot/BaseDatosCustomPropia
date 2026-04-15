package org.arbol.logic.Btree.tree;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.*;
import org.arbol.logic.Btree.tree.operation.treeB.TreeDeleteTreeB;
import org.arbol.logic.Btree.tree.operation.treeB.TreeInsertionTreeB;
import org.arbol.logic.Btree.tree.operation.treeB.TreeSearchTreeB;
import org.arbol.logic.shared.Tree;
import org.arbol.logic.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeBPlus<K extends Comparable<K>, V> extends Tree<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeBPlus.class);
    private final int maxSize;

    private TreeSearchTreeB<K, V> treeSearch;
    private TreeInsertionTreeB<K, V> treeInsertion;
    private TreeDeleteTreeB<K, V> treeDelete;

    public TreeBPlus(Node<K, V> root, int maxSize) {
        super(root);
        this.maxSize = maxSize;
        initializeOperations();
    }

    public TreeBPlus(int maxSize) {
        super();
        this.maxSize = maxSize;
        initializeOperations();
    }

    private void initializeOperations() {
        // NOTA: Necesitarás crear clases BPlusTreeInsertion o adaptar las existentes
        // para que gestionen correctamente la distinción entre Internal y Leaf.
        this.treeSearch = new TreeSearchTreeB<>();
        this.treeInsertion = new TreeInsertionTreeB<>();
        this.treeDelete = new TreeDeleteTreeB<>();
    }

    @Override
    public Result<Void, NodeError.DuplicateKeyError> insert(NodeElement<K, V> node) {
        if (root == null) {
            // DIFERENCIA B+: La raíz inicial es una Hoja
            root = new BPlusLeafNode<>(maxSize);
        }

        Result<SplitResult<K, V>, NodeError.DuplicateKeyError> insertResult =
                treeInsertion.execute(root, node, maxSize);

        if (insertResult.isFailure()) {
            logger.error("El elemento con clave {} ya existe", node.key());
            return insertResult.map(split -> null);
        }

        SplitResult<K, V> splitResult = insertResult.unwrap();

        if (splitResult.newNode() != null) {
            // Crecimiento hacia arriba: La raíz se divide
            BPlusInternalNode<K, V> newRoot = new BPlusInternalNode<>(maxSize);

            // Insertamos la clave promovida
            newRoot.addElement(splitResult.promotedElement());

            // Conectamos hijos
            newRoot.addChild(root);
            newRoot.addChild(splitResult.newNode());

            root = newRoot;
            logger.debug("Nueva raíz B+ creada: {}", root);
        }

        return new Result.Success<>(null);
    }

    @Override
    public Result<NodeElement<K, V>, NodeError.NodeNotFoundError> search(K key) {
        // La búsqueda en B+ siempre termina en una hoja.
        return treeSearch.execute(root, key);
    }

    @Override
    public Result<Void, NodeError.NodeNotFoundError> delete(K key) {
        return treeDelete.execute(root, key, maxSize);
    }

    // Método extra para B+: Recorrido secuencial de todas las hojas
    public void printAllLeaves() {
        Node<K, V> current = root;
        // Bajar hasta la hoja más a la izquierda
        while (current instanceof BPlusInternalNode) {
            current = ((BPlusInternalNode<K, V>) current).getChild(0);
        }

        // Recorrer lista enlazada
        while (current != null) {
            System.out.println(current);
            if (current instanceof BPlusLeafNode) {
                current = ((BPlusLeafNode<K, V>) current).getNextLeaf();
            } else {
                break;
            }
        }
    }
}