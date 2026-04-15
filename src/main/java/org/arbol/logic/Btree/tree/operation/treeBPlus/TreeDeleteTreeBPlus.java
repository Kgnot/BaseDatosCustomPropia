package org.arbol.logic.Btree.tree.operation.treeBPlus;

import org.arbol.logic.Btree.error.NodeError;
import org.arbol.logic.Btree.structures.InternalNode;
import org.arbol.logic.Btree.structures.Node;
import org.arbol.logic.Btree.structures.NodeElement;
import org.arbol.logic.Btree.tree.operation.interace.TreeDelete;
import org.arbol.logic.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeDeleteTreeBPlus<K extends Comparable<K>, V> implements TreeDelete<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(TreeDeleteTreeBPlus.class);
    private int invariante;

    public Result<Void, NodeError.NodeNotFoundError> execute
            (Node<K, V> root, K key, int maxSize) {

        return null;
//        return deleteRecursive(root, key, maxSize);
    }

}

