package org.arbol.logic.tree;

import org.arbol.logic.error.NodeError;
import org.arbol.logic.structures.*;
import org.arbol.logic.structures.Tree;
import org.arbol.logic.tree.operation.interace.TreeDelete;
import org.arbol.logic.tree.operation.interace.TreeInsertion;
import org.arbol.logic.tree.operation.interace.TreeSearch;
import org.arbol.logic.tree.operation.treeBPlus.TreeDeleteTreeBPlus;
import org.arbol.logic.tree.operation.treeBPlus.TreeInsertionTreeBPlus;
import org.arbol.logic.tree.operation.treeBPlus.TreeSearchTreeBPlus;
import org.arbol.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeBPlus<K extends Comparable<K>, V> extends Tree<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(TreeBPlus.class);
    private final int maxSize;

    private TreeSearch<K, V> treeSearch;
    private TreeInsertion<K, V> treeInsertion;
    private TreeDelete<K, V> treeDelete;

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
        this.treeSearch = new TreeSearchTreeBPlus<>();
        this.treeInsertion = new TreeInsertionTreeBPlus<>();
        this.treeDelete = new TreeDeleteTreeBPlus<>();
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